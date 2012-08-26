package eu32k.ludumdare.ld24;

import java.util.ArrayList;
import java.util.List;

import aurelienribon.bodyeditor.BodyEditorLoader;
import aurelienribon.bodyeditor.BodyEditorLoader.RigidBodyModel;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

import eu32k.libgdx.SimpleGame;
import eu32k.libgdx.geometry.PrimitivesFactory;
import eu32k.libgdx.rendering.Mixer;
import eu32k.libgdx.rendering.MultiPassRenderer;
import eu32k.libgdx.rendering.Renderer;
import eu32k.libgdx.rendering.TextureRenderer;
import eu32k.ludumdare.ld24.actors.Actor;
import eu32k.ludumdare.ld24.actors.Actor.ActorType;
import eu32k.ludumdare.ld24.actors.GammaRay;
import eu32k.ludumdare.ld24.actors.GeomObject;

public class LudumDare24 extends SimpleGame {

   private ShaderProgram defaultShader;
   private TextureRenderer normalRenderer;
   private MultiPassRenderer blurRenderer;
   private Mixer mixer;

   private Vector2 clickPositionLeft = new Vector2();
   private Vector2 clickPositionRight = new Vector2();

   private static enum MouseMode {
      NONE, LEFT, RIGHT
   };

   private MouseMode mouseMode = MouseMode.NONE;

   private GeomObject selectedDeflector = null;
   private Vector2 originalPos = null;
   private float originalRot = 0;

   private ArrayList<Actor> actors = new ArrayList<Actor>();

   public LudumDare24() {
      super(false);
   }

   @Override
   public void init() {
      tag(PrimitivesFactory.QUAD);

      defaultShader = tag(new ShaderProgram(Gdx.files.internal("shaders/light.vsh").readString(), Gdx.files.internal("shaders/light.fsh").readString()));
      ShaderProgram simpleShader = tag(new ShaderProgram(Gdx.files.internal("shaders/simple.vsh").readString(), Gdx.files.internal("shaders/simple.fsh").readString()));

      normalRenderer = tag(new TextureRenderer(1200, 900, simpleShader));

      ShaderProgram verticalBlur = tag(new ShaderProgram(Gdx.files.internal("shaders/simple.vsh").readString(), Gdx.files.internal("shaders/blur_v.fsh").readString()));
      ShaderProgram horizontalBlur = tag(new ShaderProgram(Gdx.files.internal("shaders/simple.vsh").readString(), Gdx.files.internal("shaders/blur_h.fsh").readString()));

      List<Renderer> renderStack = new ArrayList<Renderer>();
      renderStack.add(tag(new TextureRenderer(512, 512, verticalBlur)));
      renderStack.add(tag(new TextureRenderer(512, 512, horizontalBlur)));
      renderStack.add(tag(new TextureRenderer(512, 512, simpleShader)));
      blurRenderer = new MultiPassRenderer(renderStack);

      mixer = tag(new Mixer(normalRenderer, blurRenderer, true));

      for (int i = 0; i < 1; i++) {
         float angle = MathUtils.random() * MathUtils.PI * 2.0f;
         actors.add(new GammaRay(new Vector2(MathUtils.cos(angle) * 10.0f, MathUtils.sin(angle) * 10.0f), new Vector2(0, 0), defaultShader));
      }

      float pos = 0.9f;
      BodyEditorLoader loader = new BodyEditorLoader(Gdx.files.internal("models/models.json"));
      for (String key : loader.getInternalModel().rigidBodies.keySet()) {
         RigidBodyModel model = loader.getInternalModel().rigidBodies.get(key);
         GeomObject ob = new GeomObject(defaultShader, model);
         actors.add(ob);

         if (key.startsWith("deflector")) {
            ob.setType(ActorType.DEFLECTOR);
            ob.setPos(new Vector3(pos, 0.0f, 0.0f));
            pos += 0.7;
         } else if (key.startsWith("cell")) {
            ob.setType(ActorType.CELL);
         } else if (key.startsWith("enemy")) {
            ob.setPos(new Vector3(-1.0f, 0.0f, 0.0f));
            ob.setType(ActorType.ENEMY);
         }
      }
   }

   private void renderScene() {
      Gdx.gl.glClearColor(0.0f, 0.63f, 0.91f, 1.0f);
      Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT | (Gdx.graphics.getBufferFormat().coverageSampling ? GL20.GL_COVERAGE_BUFFER_BIT_NV : 0));
      // Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
      Gdx.gl.glEnable(GL20.GL_BLEND);
      Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
      Gdx.gl.glEnable(GL20.GL_TEXTURE_2D);

      normalRenderer.begin();

      for (Actor a : actors) {
         a.render(camera.combined);
      }

      normalRenderer.endAndRender();
   }

   @Override
   public void draw(float delta) {
      setZoom(2.0f);
      camera = createCamera();
      camera.far = 100.0f;
      camera.position.set(new Vector3(0.0f, 0.0f, -1.0f));
      camera.lookAt(0, 0, 0);
      camera.update();

      for (Actor a : actors) {
         if (a.getType() == ActorType.RAY) {
            ((GammaRay) a).updateRay(actors);
         }
      }

      normalRenderer.begin();
      renderScene();
      normalRenderer.end();

      blurRenderer.begin();
      normalRenderer.render();
      blurRenderer.end();

      float glowValue = 1.1f;
      mixer.setFactor1(glowValue + 0.05f);
      mixer.setFactor2(glowValue);
      mixer.noise = 0.1f;

      mixer.render();
   }

   @Override
   public boolean touchDown(int x, int y, int pointer, int button) {
      Vector3 pos = new Vector3(x, y, 0.0f);
      camera.unproject(pos);

      if (button == 0) {
         clickPositionLeft.set(pos.x, pos.y);
         mouseMode = MouseMode.LEFT;
      } else if (button == 1) {
         clickPositionRight.set(pos.x, pos.y);
         mouseMode = MouseMode.RIGHT;
      }

      for (Actor a : actors) {
         if (a.getType() == ActorType.DEFLECTOR && a.hitTest(pos.x, pos.y)) {
            selectedDeflector = (GeomObject) a;
            originalPos = new Vector2(selectedDeflector.getPos().x, selectedDeflector.getPos().y);
            originalRot = selectedDeflector.getRot().z;
            break;
         }
      }
      return false;
   }

   @Override
   public boolean touchUp(int x, int y, int pointer, int button) {
      mouseMode = MouseMode.NONE;
      selectedDeflector = null;
      return false;
   }

   @Override
   public boolean touchDragged(int x, int y, int pointer) {
      Vector3 pos = new Vector3(x, y, 0.0f);
      camera.unproject(pos);

      if (mouseMode == MouseMode.LEFT) {
         if (selectedDeflector != null) {
            Vector2 diff = new Vector2(pos.x, pos.y).sub(clickPositionLeft);
            selectedDeflector.setPos(new Vector3(originalPos.x + diff.x, originalPos.y + diff.y, 0.0f));
         }
      } else if (mouseMode == MouseMode.RIGHT) {
         if (selectedDeflector != null) {
            float a1 = new Vector2(pos.x, pos.y).sub(new Vector2(selectedDeflector.getPos().x, selectedDeflector.getPos().y)).angle();
            float a2 = new Vector2(clickPositionRight.x, clickPositionRight.y).sub(new Vector2(selectedDeflector.getPos().x, selectedDeflector.getPos().y)).angle();
            float diff = a1 - a2;

            selectedDeflector.setRot(new Vector3(0.0f, 0.0f, originalRot + diff));
         }
      }
      return false;
   }

   @Override
   public boolean keyDown(int keycode) {
      if (keycode == Input.Keys.SPACE) {
         // selectedDeflector = (selectedDeflector + 1) % deflectors.size();
      } else if (keycode == Input.Keys.ESCAPE) {
         dispose();
         System.exit(0);
      }
      return false;
   }
}
