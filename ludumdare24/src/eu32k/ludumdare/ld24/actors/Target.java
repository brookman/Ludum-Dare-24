package eu32k.ludumdare.ld24.actors;

import java.util.Collection;

import math.geom2d.Point2D;
import math.geom2d.conic.Circle2D;
import math.geom2d.line.LineSegment2D;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Matrix4;

public class Target implements Actor {

   private ShapeRenderer shapeRenderer;

   public float hp = 100;
   public float dot = 0;

   private Collection<LineSegment2D> segments;

   public Target() {
      shapeRenderer = new ShapeRenderer();

      segments = new Circle2D(new Point2D(0, 0), 0.43).asPolyline(16).edges();
   }

   @Override
   public Collection<LineSegment2D> getShape() {
      return segments;
   }

   @Override
   public void render(Matrix4 matrix) {
      shapeRenderer.setProjectionMatrix(matrix);
      shapeRenderer.begin(ShapeType.Line);
      shapeRenderer.setColor(1, 0, 0, 1);

      for (LineSegment2D segment : segments) {
         shapeRenderer.line((float) segment.firstPoint().getX(), (float) segment.firstPoint().getY(), (float) segment.lastPoint().getX(), (float) segment.lastPoint().getY());
      }
      shapeRenderer.end();
   }

}
