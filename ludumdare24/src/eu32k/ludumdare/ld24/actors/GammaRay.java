package eu32k.ludumdare.ld24.actors;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import math.geom2d.AffineTransform2D;
import math.geom2d.Box2D;
import math.geom2d.Point2D;
import math.geom2d.line.AbstractLine2D;
import math.geom2d.line.LineSegment2D;
import math.geom2d.line.Ray2D;

import com.badlogic.gdx.Files.FileType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

import eu32k.libgdx.geometry.PrimitivesFactory;
import eu32k.libgdx.math.InterpolatedAnimation;
import eu32k.libgdx.math.Interpolation.Type;
import eu32k.libgdx.rendering.Mixer;
import eu32k.libgdx.rendering.Textures;
import eu32k.ludumdare.ld24.GlobalValues;

public class GammaRay extends Actor {

   private Box2D box = new Box2D(-10, 10, -10, 1140);

   private Ray2D ray;

   private InterpolatedAnimation charger = new InterpolatedAnimation(0.0f, 0.08f, Type.SMOOTH_STEP, 1600);
   private InterpolatedAnimation damageTime = new InterpolatedAnimation(1.0, 1.0f, Type.LINEAR, 500);
   private float mutationRate = 0.1f;

   private ArrayList<LineSegment2D> subRays = new ArrayList<LineSegment2D>();

   private Texture texture1;
   private Texture texture2;
   private ShaderProgram shader;

   private Sound chargeSound;
   private Sound fireSound;

   private boolean running = false;
   private boolean fired = false;

   private long nextRay = 0;

   public GammaRay(ShaderProgram shader) {
      setType(ActorType.RAY);
      texture1 = Textures.get("textures/ray.png");
      texture2 = Textures.get("textures/ray2.png");
      this.shader = shader;

      chargeSound = Gdx.audio.newSound(Gdx.files.getFileHandle("sound/charge.wav", FileType.Internal));
      fireSound = Gdx.audio.newSound(Gdx.files.getFileHandle("sound/fire.wav", FileType.Internal));

      randomReset();
   }

   public void randomReset() {
      float angle = MathUtils.random() * MathUtils.PI * 2.0f;

      float angle2 = MathUtils.random() * MathUtils.PI * 2.0f;
      float dist = MathUtils.random() * 1.4f - 0.7f;

      Vector2 origin = new Vector2(MathUtils.cos(angle) * 10.0f, MathUtils.sin(angle) * 10.0f);
      Vector2 destination = new Vector2(MathUtils.cos(angle2) * dist, MathUtils.sin(angle2) * dist);

      int diff = GlobalValues.RAY_SLEEP_MAX - GlobalValues.RAY_SLEEP_MIN;
      reset(origin, destination, System.currentTimeMillis() + GlobalValues.RAY_SLEEP_MIN + MathUtils.random(diff));
   }

   public void reset(Vector2 start, Vector2 end, long startAt) {
      ray = new Ray2D(new Point2D(start.x, start.y), new Point2D(end.x, end.y));

      damageTime.reset();
      charger.reset();

      running = false;
      fired = false;
      nextRay = startAt;
   }

   public void updateRay(List<Actor> actors, float delta) {

      if (!running && System.currentTimeMillis() > nextRay) {
         running = true;
         charger.start();
         chargeSound.play();
      }

      if (!running) {
         return;
      }

      if (charger.isFinished() && !fired) {
         fireSound.play();
         fired = true;
         damageTime.start();
      }

      if (damageTime.isFinished()) {
         randomReset();
         return;
      }

      if (damageTime.isRunning()) {
         Mixer.noise = 0.4f;
      }

      subRays.clear();
      Ray2D currentRay = ray;
      Point2D lastIntersection = null;

      int iterations = 0;
      while (iterations++ < 6) {

         double minDistance = Double.MAX_VALUE;
         Actor nearestActor = null;

         LineSegment2D intersectionSegment = null;
         Point2D intersectionPoint = null;

         for (Actor a : actors) {
            if (a.getShape() == null) {
               continue;
            }

            for (LineSegment2D segment : a.getShape()) {
               Point2D intersection = currentRay.intersection(segment);
               if (intersection != null) {
                  if (lastIntersection != null && lastIntersection.almostEquals(intersection, 0.0001)) {
                     continue;
                  }
                  double distance = currentRay.origin().distance(intersection);
                  if (distance < minDistance) {
                     minDistance = distance;
                     intersectionPoint = intersection;
                     intersectionSegment = segment;
                     nearestActor = a;
                  }

               }
            }

         }

         if (nearestActor != null) {
            subRays.add(new LineSegment2D(currentRay.origin(), intersectionPoint));
            if (nearestActor.getType() == ActorType.DEFLECTOR) {
               lastIntersection = intersectionPoint;
               AffineTransform2D reflection = AffineTransform2D.createLineReflection(intersectionSegment);
               currentRay = new Ray2D(intersectionPoint, reflection.transform(currentRay.clip(box).lastPoint()));
            } else {
               if (nearestActor.getType() == ActorType.CELL) {
                  if (damageTime.isRunning()) {
                     GlobalValues.DNA_MUTATION += mutationRate * delta;
                  }
               } else if (nearestActor.getType() == ActorType.ENEMY) {
                  Enemy enemy = (Enemy) nearestActor;
                  enemy.mutation += mutationRate * delta;
               }
               break;
            }
         } else {

            AbstractLine2D l = currentRay.clip(box).get(0);
            if (l instanceof LineSegment2D) {
               subRays.add((LineSegment2D) l);
            }

            break;
         }
      }

   }

   @Override
   public void render(Matrix4 camMatrix) {

      if (!running) {
         return;
      }

      if (damageTime.isRunning()) {
         texture2.bind();
      } else {
         texture1.bind();
      }

      shader.begin();

      for (LineSegment2D segment : subRays) {
         Vector3 center = new Vector3((float) ((segment.firstPoint().getX() + segment.lastPoint().getX()) / 2.0), (float) ((segment.firstPoint().getY() + segment.lastPoint().getY()) / 2.0), 0.0f);

         float thickness = (float) charger.getValue();
         if (damageTime.isRunning()) {
            thickness = 0.1f;
         }
         Matrix4 matrix = new Matrix4().mul(new Matrix4().translate(center).rotate(0, 0, 1, (float) segment.direction().angle() * MathUtils.radiansToDegrees)
               .scale((float) segment.length() + 0.05f, thickness, 1.0f));
         shader.setUniformMatrix("uMVMatrix", matrix);
         shader.setUniformMatrix("uPMatrix", camMatrix);

         PrimitivesFactory.QUAD.render(shader, GL20.GL_TRIANGLES);
      }

      shader.end();

      // shapeRenderer.setProjectionMatrix(matrix);
      // shapeRenderer.begin(ShapeType.Line);
      // float color = 1.0f - currentDamage / 10.0f;
      // shapeRenderer.setColor(color, color, color, 1.0f);
      //
      // for (LineSegment2D segment : subRays) {
      // shapeRenderer.line((float) segment.firstPoint().getX(), (float)
      // segment.firstPoint().getY(), (float) segment.lastPoint().getX(),
      // (float) segment.lastPoint().getY());
      // }
      // shapeRenderer.end();
   }

   @Override
   public Collection<LineSegment2D> getShape() {
      return null;
   }

   @Override
   public boolean hitTest(float x, float y) {
      return false;
   }
}
