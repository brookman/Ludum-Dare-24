package eu32k.ludumdare.ld24;

import java.util.ArrayList;
import java.util.List;

import aurelienribon.bodyeditor.BodyEditorLoader;

import com.badlogic.gdx.Files.FileType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

import eu32k.libgdx.SimpleGame;
import eu32k.libgdx.common.RunText;
import eu32k.libgdx.geometry.PrimitivesFactory;
import eu32k.libgdx.rendering.Mixer;
import eu32k.libgdx.rendering.MultiPassRenderer;
import eu32k.libgdx.rendering.Renderer;
import eu32k.libgdx.rendering.TextureRenderer;
import eu32k.ludumdare.ld24.actors.Actor;
import eu32k.ludumdare.ld24.actors.Actor.ActorType;
import eu32k.ludumdare.ld24.actors.Enemy;
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
   private ArrayList<GammaRay> rays = new ArrayList<GammaRay>();

   private ArrayList<Enemy> enemies = new ArrayList<Enemy>();
   private ArrayList<GeomObject> deflectors = new ArrayList<GeomObject>();

   private ProgressPar progressPar;

   private SpriteBatch hudBatch;
   private RunText barText;
   private RunText gameOverText;
   private RunText score;

   private Gact gact;

   private Music music;

   private Instructions instructions;
   private boolean showInstuctions = true;

   public LudumDare24() {
      super(false);
   }

   @Override
   public void init() {
      GlobalValues.update();
      tag(PrimitivesFactory.QUAD);

      music = Gdx.audio.newMusic(Gdx.files.getFileHandle("sound/beat1.mp3", FileType.Internal));
      music.setLooping(true);
      music.play();

      defaultShader = tag(new ShaderProgram(Gdx.files.internal("shaders/light.vsh").readString(), Gdx.files.internal("shaders/light.fsh").readString()));
      ShaderProgram simpleShader = tag(new ShaderProgram(Gdx.files.internal("shaders/simple.vsh").readString(), Gdx.files.internal("shaders/simple.fsh").readString()));

      ShaderProgram progressShader = tag(new ShaderProgram(Gdx.files.internal("shaders/light.vsh").readString(), Gdx.files.internal("shaders/progress.fsh").readString()));

      progressPar = new ProgressPar(progressShader);

      normalRenderer = tag(new TextureRenderer(1200, 900, simpleShader));

      ShaderProgram verticalBlur = tag(new ShaderProgram(Gdx.files.internal("shaders/simple.vsh").readString(), Gdx.files.internal("shaders/blur_v.fsh").readString()));
      ShaderProgram horizontalBlur = tag(new ShaderProgram(Gdx.files.internal("shaders/simple.vsh").readString(), Gdx.files.internal("shaders/blur_h.fsh").readString()));

      List<Renderer> renderStack = new ArrayList<Renderer>();
      renderStack.add(tag(new TextureRenderer(512, 512, verticalBlur)));
      renderStack.add(tag(new TextureRenderer(512, 512, horizontalBlur)));
      renderStack.add(tag(new TextureRenderer(512, 512, simpleShader)));
      blurRenderer = new MultiPassRenderer(renderStack);

      mixer = tag(new Mixer(normalRenderer, blurRenderer, true));

      hudBatch = new SpriteBatch();
      barText = new RunText("DNA Mutation", 1.0f);
      gameOverText = new RunText("Your DNA mutated too much... Press ESC to reset.", 2.0f);
      score = new RunText("BLAH", 0.0f);

      gact = new Gact(defaultShader);

      instructions = new Instructions(defaultShader);

      for (int i = 0; i < 20; i++) {
         GammaRay ray = new GammaRay(defaultShader);
         rays.add(ray);
         actors.add(ray);
      }

      float pos = 0.9f;
      BodyEditorLoader loader = new BodyEditorLoader(Gdx.files.internal("models/models.json"));

      makeObject(loader, ActorType.CELL, "cell", 0.0f, 0.0f);

      makeObject(loader, ActorType.DEFLECTOR, "deflector1", 0.0f, 1.0f);
      makeObject(loader, ActorType.DEFLECTOR, "deflector2", 1.0f, 0.0f);
      makeObject(loader, ActorType.DEFLECTOR, "deflector1", 0.0f, -1.0f);
      makeObject(loader, ActorType.DEFLECTOR, "deflector4", -1.0f, 0.0f);

      // for (int i = 0; i < 20; i++) {
      // enemies.add((Enemy) makeObject(loader, ActorType.ENEMY, "enemy1",
      // -2.0f, 0.0f));
      // }

   }

   private GeomObject makeObject(BodyEditorLoader loader, ActorType type, String name, float x, float y) {
      GeomObject ob = null;
      if (type == ActorType.ENEMY) {
         ob = new Enemy(defaultShader, loader.getInternalModel().rigidBodies.get(name));
      } else {
         ob = new GeomObject(defaultShader, loader.getInternalModel().rigidBodies.get(name));
      }
      if (type == ActorType.DEFLECTOR) {
         deflectors.add(ob);
      }
      ob.setType(type);
      ob.setPos(new Vector3(x, y, 0));
      actors.add(ob);
      return ob;
   }

   private void reset() {
      GlobalValues.reset();
      for (GammaRay ray : rays) {
         ray.randomReset();
      }

      deflectors.get(0).setPos(new Vector3(0.0f, 1.0f, 0.0f));
      deflectors.get(0).setRot(new Vector3(0.0f, 0.0f, 0.0f));

      deflectors.get(1).setPos(new Vector3(1.0f, 0.0f, 0.0f));
      deflectors.get(1).setRot(new Vector3(0.0f, 0.0f, 0.0f));

      deflectors.get(2).setPos(new Vector3(0.0f, -1.0f, 0.0f));
      deflectors.get(2).setRot(new Vector3(0.0f, 0.0f, 0.0f));

      deflectors.get(3).setPos(new Vector3(-1.0f, 0.0f, 0.0f));
      deflectors.get(3).setRot(new Vector3(0.0f, 0.0f, 0.0f));

      clickPositionLeft = new Vector2();
      clickPositionRight = new Vector2();
      mouseMode = MouseMode.NONE;

      GlobalValues.reset();
   }

   private void renderScene() {

      Gdx.gl.glClearColor(0.0f, 0.63f, 0.91f, 1.0f);
      Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT | (Gdx.graphics.getBufferFormat().coverageSampling ? GL20.GL_COVERAGE_BUFFER_BIT_NV : 0));

      String scoreString = "" + (GlobalValues.score + GlobalValues.timeScore);
      while (scoreString.length() < 9) {
         scoreString = "0" + scoreString;
      }
      hudBatch.begin();
      score.drawManual("Score: " + scoreString, hudBatch, 20, 580);
      hudBatch.end();

      // Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
      Gdx.gl.glEnable(GL20.GL_BLEND);
      Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
      Gdx.gl.glEnable(GL20.GL_TEXTURE_2D);

      if (GlobalValues.GAME_OVER) {
         hudBatch.begin();
         gameOverText.draw(hudBatch, 50, 300);
         hudBatch.end();
         return;
      }
      gameOverText.reset();

      for (Actor a : actors) {
         a.render(camera.combined);
      }
      gact.render(camera.combined);

      progressPar.setProgess(GlobalValues.DNA_MUTATION);
      progressPar.render(camera.combined);

      hudBatch.begin();
      barText.draw(hudBatch, 2, 28);
      hudBatch.end();
   }

   @Override
   public void draw(float delta) {
      setZoom(2.0f);
      camera = createCamera();
      camera.far = 100.0f;
      camera.position.set(new Vector3(0.0f, 0.0f, -1.0f));
      camera.lookAt(0, 0, 0);
      camera.update();

      if (showInstuctions) {
         instructions.render(camera.combined);
         return;
      }

      GlobalValues.update();

      Mixer.noise = 0.02f;
      if (!GlobalValues.GAME_OVER) {
         for (int i = 0; i < GlobalValues.RAYS; i++) {
            rays.get(i).updateRay(actors, delta);
         }
      }
      gact.update();

      normalRenderer.begin();
      renderScene();
      normalRenderer.end();

      blurRenderer.begin();
      normalRenderer.render();
      blurRenderer.end();

      float glowValue = 1.1f;
      mixer.setFactor1(glowValue + 0.05f);
      mixer.setFactor2(glowValue);

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
         showInstuctions = false;
      } else if (keycode == Input.Keys.ESCAPE) {
         reset();
      } else if (keycode == Input.Keys.A) {
         gact.a();
      } else if (keycode == Input.Keys.C) {
         gact.c();
      } else if (keycode == Input.Keys.G) {
         gact.g();
      } else if (keycode == Input.Keys.T) {
         gact.t();
      }
      return false;
   }
}
