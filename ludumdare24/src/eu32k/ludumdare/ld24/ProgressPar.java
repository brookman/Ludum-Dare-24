package eu32k.ludumdare.ld24;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;

import eu32k.libgdx.geometry.PrimitivesFactory;

public class ProgressPar {

   private float progess = 0.0f;

   private ShaderProgram shader;

   public ProgressPar(ShaderProgram shader) {
      this.shader = shader;

   }

   public float getProgess() {
      return progess;
   }

   public void setProgess(float progess) {
      this.progess = progess;
   }

   public void render(Matrix4 camMatrix) {
      shader.begin();

      shader.setUniformf("uProgress", progess);
      shader.setUniformf("uLeftColor", 1.0f, 0.0f, 0.0f, 0.8f);
      shader.setUniformf("uRightColor", 0.0f, 0.0f, 0.0f, 0.2f);

      Matrix4 matrix = new Matrix4().mul(new Matrix4().translate(new Vector3(0.0f, -0.7f, 0.0f)).scale(1.42f, 0.4f, 1.0f));
      shader.setUniformMatrix("uMVMatrix", matrix);
      shader.setUniformMatrix("uPMatrix", matrix);

      PrimitivesFactory.QUAD.render(shader, GL20.GL_TRIANGLES);

      shader.end();

   }
}
