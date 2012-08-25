package eu32k.ludumdare.ld24.actors;

import java.util.Collection;

import math.geom2d.line.LineSegment2D;

import com.badlogic.gdx.math.Matrix4;

public interface Actor {

   public Collection<LineSegment2D> getShape();

   public void render(Matrix4 matrix);

}
