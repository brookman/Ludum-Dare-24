package eu32k.ludumdare.ld24;

import com.badlogic.gdx.Files.FileType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;

import eu32k.libgdx.geometry.PrimitivesFactory;
import eu32k.libgdx.rendering.Textures;

public class Gact {

   private Texture[] textures = new Texture[4];

   private ShaderProgram shader;

   private boolean shown = false;
   private int currentLetter = MathUtils.random(3);

   private long nextGact = 0;

   private Sound good;
   private Sound bad;

   public Gact(ShaderProgram shader) {
      this.shader = shader;
      textures[0] = Textures.get("textures/a.png");
      textures[1] = Textures.get("textures/c.png");
      textures[2] = Textures.get("textures/g.png");
      textures[3] = Textures.get("textures/t.png");

      good = Gdx.audio.newSound(Gdx.files.getFileHandle("sound/good.wav", FileType.Internal));
      bad = Gdx.audio.newSound(Gdx.files.getFileHandle("sound/bad.wav", FileType.Internal));
   }

   public void update() {
      if (!shown) {
         if (System.currentTimeMillis() > nextGact) {
            shown = true;
            nextGact = System.currentTimeMillis() + GlobalValues.GACT_SHOWN;
            currentLetter = MathUtils.random(3);
         }
      } else {
         if (System.currentTimeMillis() > nextGact) {
            shown = false;
            int diff = GlobalValues.GACT_SLEEP_MIN - GlobalValues.GACT_SLEEP_MAX;
            nextGact = System.currentTimeMillis() + GlobalValues.RAY_SLEEP_MIN + MathUtils.random(diff);
         }
      }
   }

   private void heal() {
      good.play();
      GlobalValues.DNA_MUTATION -= 0.02f;
      shown = false;
      GlobalValues.score += 100;
   }

   private void damage() {
      bad.play();
      GlobalValues.DNA_MUTATION += 0.03f;
   }

   public void a() {
      if (shown && currentLetter == 0) {
         heal();
      } else {
         damage();
      }
   }

   public void c() {
      if (shown && currentLetter == 1) {
         heal();
      } else {
         damage();
      }
   }

   public void g() {
      if (shown && currentLetter == 2) {
         heal();
      } else {
         damage();
      }
   }

   public void t() {
      if (shown && currentLetter == 3) {
         heal();
      } else {
         damage();
      }
   }

   public void render(Matrix4 camMatrix) {

      if (!shown) {
         return;
      }

      textures[currentLetter].bind();

      shader.begin();
      Matrix4 matrix = new Matrix4().mul(new Matrix4().scale(0.5f, 0.5f, 1.0f));
      shader.setUniformMatrix("uMVMatrix", matrix);
      shader.setUniformMatrix("uPMatrix", camMatrix);

      PrimitivesFactory.QUAD.render(shader, GL20.GL_TRIANGLES);

      shader.end();
   }
}
