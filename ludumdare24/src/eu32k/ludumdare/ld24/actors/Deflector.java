package eu32k.ludumdare.ld24.actors;

import java.util.Collection;

import math.geom2d.AffineTransform2D;
import math.geom2d.Point2D;
import math.geom2d.line.LineSegment2D;
import math.geom2d.polygon.LinearRing2D;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Matrix4;

public class Deflector implements Actor {

   private ShapeRenderer shapeRenderer;

   private LinearRing2D original = new LinearRing2D(new Point2D(-0.2, -0.2), new Point2D(0.2, -0.2), new Point2D(0.2, 0.2));
   private LinearRing2D current = original.clone();

   private float x = 0;
   private float y = 0;
   private float rot = 0;

   public Deflector() {
      shapeRenderer = new ShapeRenderer();
   }

   public void setPosition(float x, float y) {
      this.x = x;
      this.y = y;
      update();
   }

   public void setRotation(float rot) {
      this.rot = rot;
      update();
   }

   private void update() {
      AffineTransform2D tPos = AffineTransform2D.createTranslation(x, y);
      AffineTransform2D tRot = AffineTransform2D.createRotation(rot);
      current = original.transform(tRot).transform(tPos);
   }

   private boolean hitTest(float x, float y) {
      return current.isInside(x, y);
   }

   @Override
   public Collection<LineSegment2D> getShape() {
      return current.edges();
   }

   @Override
   public void render(Matrix4 matrix) {
      shapeRenderer.setProjectionMatrix(matrix);
      shapeRenderer.begin(ShapeType.Line);
      shapeRenderer.setColor(1, 0, 0, 1);

      for (LineSegment2D segment : current.edges()) {
         shapeRenderer.line((float) segment.firstPoint().getX(), (float) segment.firstPoint().getY(), (float) segment.lastPoint().getX(), (float) segment.lastPoint().getY());
      }
      shapeRenderer.end();
   }

}
