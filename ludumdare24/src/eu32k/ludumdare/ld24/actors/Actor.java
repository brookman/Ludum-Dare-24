package eu32k.ludumdare.ld24.actors;

import java.util.Collection;

import math.geom2d.line.LineSegment2D;

import com.badlogic.gdx.math.Matrix4;

public abstract class Actor {

   public static enum ActorType {
      RAY, CELL, ENEMY, DEFLECTOR
   }

   protected ActorType type;

   public ActorType getType() {
      return type;
   }

   public void setType(ActorType type) {
      this.type = type;
   }

   public abstract Collection<LineSegment2D> getShape();

   public abstract boolean hitTest(float x, float y);

   public abstract void render(Matrix4 matrix);

}
