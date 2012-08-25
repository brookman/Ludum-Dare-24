package eu32k.ludumdare.ld24;

import java.util.ArrayList;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

import eu32k.libgdx.SimpleGame;
import eu32k.libgdx.geometry.PrimitivesFactory;
import eu32k.libgdx.rendering.TextureRenderer;
import eu32k.ludumdare.ld24.actors.Actor;
import eu32k.ludumdare.ld24.actors.Deflector;
import eu32k.ludumdare.ld24.actors.GammaRay;
import eu32k.ludumdare.ld24.actors.Target;

public class LudumDare24_new extends SimpleGame {

   private ShaderProgram defaultShader;
   private TextureRenderer normalRenderer;

   private Vector2 clickPosition = new Vector2();

   private boolean left = false;
   private boolean right = false;

   private ArrayList<Deflector> deflectors = new ArrayList<Deflector>();
   private int selectedDeflector = 0;

   private ArrayList<Actor> actors = new ArrayList<Actor>();

   public LudumDare24_new() {
      super(false);
   }

   @Override
   public void init() {
      tag(PrimitivesFactory.QUAD);

      defaultShader = tag(new ShaderProgram(Gdx.files.internal("shaders/light.vsh").readString(), Gdx.files.internal("shaders/light.fsh").readString()));
      ShaderProgram simpleShader = tag(new ShaderProgram(Gdx.files.internal("shaders/simple.vsh").readString(), Gdx.files.internal("shaders/simple.fsh").readString()));

      normalRenderer = tag(new TextureRenderer(1200, 900, simpleShader));

      Texture cellTexture = tag(new Texture(Gdx.files.internal("textures/cell.png"), true));
      Texture deflectorTexture = tag(new Texture(Gdx.files.internal("textures/deflector.png"), true));
      Texture rayTexture = tag(new Texture(Gdx.files.internal("textures/ray.png"), true));

      for (int i = 0; i < 1; i++) {
         float angle = MathUtils.random() * MathUtils.PI * 2.0f;
         actors.add(new GammaRay(new Vector2(MathUtils.cos(angle) * 10.0f, MathUtils.sin(angle) * 10.0f), new Vector2(0, 0)));
      }

      for (int i = 0; i < 1; i++) {
         Deflector deflector = new Deflector();
         actors.add(deflector);
         deflectors.add(deflector);
      }

      actors.add(new Target());
   }

   private void renderScene() {
      Gdx.gl.glClearColor(0.0f, 0.63f, 0.91f, 1.0f);
      Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT | (Gdx.graphics.getBufferFormat().coverageSampling ? GL20.GL_COVERAGE_BUFFER_BIT_NV : 0));
      // Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
      Gdx.gl.glEnable(GL20.GL_BLEND);
      Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
      Gdx.gl.glEnable(GL20.GL_TEXTURE_2D);

      defaultShader.begin();

      defaultShader.setUniformf("uAmbientColor", 0.2f, 0.2f, 0.2f);
      defaultShader.setUniformf("uPointLightingColor", 1.0f, 1.0f, 1.0f);

      defaultShader.setUniformf("uPointLightingLocation", new Vector3(0.0f, 0.0f, -10.0f));
      defaultShader.setUniformMatrix("uPMatrix", camera.combined);

      defaultShader.end();

      for (Actor a : actors) {
         a.render(camera.combined);
      }

   }

   @Override
   public void draw(float delta) {
      if (Gdx.input.isKeyPressed(Input.Keys.ESCAPE)) {
         dispose();
         System.exit(0);
      }

      setZoom(2.0f);
      camera = createCamera();
      camera.far = 100.0f;
      camera.position.set(new Vector3(0.0f, 0.0f, -1.0f));
      camera.lookAt(0, 0, 0);
      camera.update();

      float size = 0.5f;
      float radius = 0.7f;

      for (Actor a : actors) {
         if (a instanceof Target) {
            ((Target) a).dot = 0.0f;
         }
      }

      for (Actor a : actors) {
         if (a instanceof GammaRay) {
            ((GammaRay) a).updateRay(actors);
         }
      }

      for (Actor a : actors) {
         if (a instanceof Target) {
            ((Target) a).hp -= ((Target) a).dot * delta;
         }
      }

      normalRenderer.begin();
      renderScene();
      normalRenderer.endAndRender();
   }

   @Override
   public boolean touchDown(int x, int y, int pointer, int button) {

      if (button == 0) {
         left = true;
      } else if (button == 1) {
         Vector3 pos = new Vector3(x, y, 0.0f);
         camera.unproject(pos);
         clickPosition.set(pos.x, pos.y);
         right = true;
      }
      return false;
   }

   @Override
   public boolean touchUp(int x, int y, int pointer, int button) {
      if (button == 0) {
         left = false;
      } else if (button == 1) {
         right = false;
      }
      return false;
   }

   @Override
   public boolean touchDragged(int x, int y, int pointer) {
      Vector3 pos = new Vector3(x, y, 0.0f);
      camera.unproject(pos);

      if (left) {
         deflectors.get(0).setPosition(pos.x, pos.y);
      } else if (right) {
         deflectors.get(0).setRotation((pos.x - clickPosition.x) * 5.0f);
      }
      return false;
   }

   @Override
   public boolean keyDown(int keycode) {
      if (keycode == Input.Keys.SPACE) {
         selectedDeflector = (selectedDeflector + 1) % deflectors.size();
      }
      return false;
   }
}
