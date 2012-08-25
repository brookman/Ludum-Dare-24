package eu32k.ludumdare.ld24.actors;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import math.geom2d.AffineTransform2D;
import math.geom2d.Box2D;
import math.geom2d.Point2D;
import math.geom2d.line.LineSegment2D;
import math.geom2d.line.Ray2D;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;

import eu32k.libgdx.math.InterpolatedAnimation;
import eu32k.libgdx.math.Interpolation.Type;

public class GammaRay implements Actor {

   private Box2D box = new Box2D(-50, 50, -50, 50);

   private Ray2D ray;
   private ShapeRenderer shapeRenderer;

   private InterpolatedAnimation damage = new InterpolatedAnimation(0.1f, 10.0f, Type.SMOOTH_STEP, 2000);
   private InterpolatedAnimation life = new InterpolatedAnimation(0.0, 1.0f, Type.LINEAR, 5000);
   private float currentDamage = 0.0f;

   private ArrayList<LineSegment2D> subRays = new ArrayList<LineSegment2D>();

   public GammaRay() {
      randomReset();
   }

   public GammaRay(Vector2 start, Vector2 end) {
      reset(start, end);
   }

   private void randomReset() {
      float angle = MathUtils.random() * MathUtils.PI * 2.0f;
      reset(new Vector2(MathUtils.cos(angle) * 10.0f, MathUtils.sin(angle) * 10.0f), new Vector2(0, 0));
   }

   private void reset(Vector2 start, Vector2 end) {
      ray = new Ray2D(new Point2D(start.x, start.y), new Point2D(end.x, end.y));
      shapeRenderer = new ShapeRenderer();

      damage.start();
      life.start();
   }

   public void updateRay(List<Actor> actors) {

      if (life.getValue() == 1.0f) {
         randomReset();
      }
      currentDamage = (float) damage.getValue();

      subRays.clear();
      Ray2D currentRay = ray;
      Actor lastHit = null;

      int iterations = 0;
      while (iterations++ < 6) {

         double minDistance = Double.MAX_VALUE;
         Actor nearestActor = null;

         LineSegment2D intersectionSegment = null;
         Point2D intersectionPoint = null;

         for (Actor a : actors) {
            if (a.getShape() == null || a == lastHit) {
               continue;
            }

            for (LineSegment2D segment : a.getShape()) {
               Point2D intersection = currentRay.intersection(segment);
               if (intersection != null) {
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
            if (nearestActor instanceof Deflector) {
               lastHit = nearestActor;
               AffineTransform2D reflection = AffineTransform2D.createLineReflection(intersectionSegment);
               currentRay = new Ray2D(intersectionPoint, reflection.transform(currentRay.clip(box).lastPoint()));
            } else {
               if (nearestActor instanceof Target) {
                  Target target = (Target) nearestActor;
                  target.dot += damage.getValue();
               }
               break;
            }
         } else {
            subRays.add((LineSegment2D) currentRay.clip(box).get(0));
            break;
         }
      }

   }

   @Override
   public void render(Matrix4 matrix) {

      shapeRenderer.setProjectionMatrix(matrix);
      shapeRenderer.begin(ShapeType.Line);
      float color = 1.0f - currentDamage / 10.0f;
      shapeRenderer.setColor(color, color, color, 1.0f);

      for (LineSegment2D segment : subRays) {
         shapeRenderer.line((float) segment.firstPoint().getX(), (float) segment.firstPoint().getY(), (float) segment.lastPoint().getX(), (float) segment.lastPoint().getY());
      }
      shapeRenderer.end();
   }

   @Override
   public Collection<LineSegment2D> getShape() {
      return null;
   }
}
