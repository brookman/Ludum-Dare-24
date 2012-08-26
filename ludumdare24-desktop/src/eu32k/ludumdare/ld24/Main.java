package eu32k.ludumdare.ld24;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;

public class Main {
   public static void main(String[] args) {
      LwjglApplicationConfiguration cfg = new LwjglApplicationConfiguration();
      cfg.title = "Gamma Ray Deflector";
      cfg.useGL20 = true;
      cfg.width = 800;
      cfg.height = 600;
      cfg.vSyncEnabled = true;
      cfg.resizable = false;
      cfg.useCPUSynch = true;
      cfg.samples = 8;
      cfg.fullscreen = false;

      new LwjglApplication(new LudumDare24(), cfg);
   }
}
