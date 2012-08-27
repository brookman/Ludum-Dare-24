package eu32k.ludumdare.ld24;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix4;

import eu32k.libgdx.geometry.PrimitivesFactory;
import eu32k.libgdx.rendering.Textures;

public class Instructions {

   private Texture texture;

   private ShaderProgram shader;

   public Instructions(ShaderProgram shader) {
      this.shader = shader;
      texture = Textures.get("textures/instructions.png");
   }

   public void render(Matrix4 camMatrix) {
      texture.bind();

      shader.begin();
      Matrix4 matrix = new Matrix4().mul(new Matrix4().scale(5.4f, 4.0f, 1.0f));
      shader.setUniformMatrix("uMVMatrix", matrix);
      shader.setUniformMatrix("uPMatrix", camMatrix);

      PrimitivesFactory.QUAD.render(shader, GL20.GL_TRIANGLES);

      shader.end();
   }
}
