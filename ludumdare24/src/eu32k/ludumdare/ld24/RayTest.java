package eu32k.ludumdare.ld24;

import java.awt.BorderLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.JFrame;
import javax.swing.JPanel;

import math.geom2d.AffineTransform2D;
import math.geom2d.Box2D;
import math.geom2d.Point2D;
import math.geom2d.line.LineSegment2D;
import math.geom2d.line.Ray2D;

public class RayTest extends JFrame {

   public RayTest() {
      super("test");
      setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      setLayout(new BorderLayout());

      add(new RayPanel(), BorderLayout.CENTER);

      setSize(800, 600);
      setLocationRelativeTo(null);
      setVisible(true);
   }

   private class RayPanel extends JPanel {

      @Override
      public void paint(Graphics g1) {
         Graphics2D g2 = (Graphics2D) g1;

         Box2D box = new Box2D(-50, -50, 0, 600);

         Ray2D ray = new Ray2D(new Point2D(20.0, 20.0), new Point2D(0, 0));
         ray.clip(box).draw(g2);

         System.out.println(ray.clip(box).get(0));

         LineSegment2D segment = new LineSegment2D(new Point2D(300.0, 0.0), new Point2D(0.0, 300.0));
         segment.draw(g2);

         Point2D intersection = segment.intersection(ray);

         if (intersection != null) {
            // System.out.println(segment.normal(1.0));

            AffineTransform2D reflection = AffineTransform2D.createLineReflection(segment);

            Ray2D ray2 = new Ray2D(intersection, reflection.transform(ray.clip(box).lastPoint()));
            ray2.clip(box).draw(g2);
         }

         // math.geom2d.line.Line2D line = new math.geom2d.line.Line2D(new
         // Point2D(100.0, 1.0), new Point2D(20.0, 50.0));
         // line.draw(g);

         // LineSegment2D segment = new LineSegment2D(new Point2D(0.0, 1.0), new
         // Point2D(1.0, 0.0));
         //
         // Point2D intersection = ray.intersection(segment);
         // System.out.println(intersection);
         //
         // AffineTransform2D reflection =
         // AffineTransform2D.createLineReflection(segment);

         // Point2D.
         // reflection.transform(ray.direction().)

      }
   }

   public static void main(String[] args) {
      new RayTest();
      // Vector2 rayStart = new Vector2(1.0f, 1.0f);
      // Vector2 rayDir = new Vector2(-1.0f, -1.0f);
      // Vector2 segmentStart = new Vector2(0.0f, 1.0f);
      // Vector2 segmentEnd = new Vector2(1.0f, 0.0f);
      //
      // rayDir.mul(100.0f);
      // rayDir.add(rayStart);
      //
      // Vector2 intersection2 = new Vector2();
      // System.out.println(Intersector.intersectSegments(rayStart, rayDir,
      // segmentStart, segmentEnd, intersection2));
      // System.out.println(intersection2);

   }
}
