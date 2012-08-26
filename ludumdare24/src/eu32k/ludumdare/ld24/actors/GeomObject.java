package eu32k.ludumdare.ld24.actors;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import math.geom2d.AffineTransform2D;
import math.geom2d.Point2D;
import math.geom2d.line.LineSegment2D;
import math.geom2d.polygon.SimplePolygon2D;
import aurelienribon.bodyeditor.BodyEditorLoader.PolygonModel;
import aurelienribon.bodyeditor.BodyEditorLoader.RigidBodyModel;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

import eu32k.libgdx.rendering.Textures;

public class GeomObject extends Actor {

   private ShaderProgram shader;
   private RigidBodyModel model;
   private List<Mesh> meshes;
   private Texture texture;

   private List<SimplePolygon2D> rings;

   private Vector3 pos = new Vector3(0.0f, 0.0f, 0.0f);
   private Vector3 rot = new Vector3(0.0f, 0.0f, 0.0f);
   private Vector3 scale = new Vector3(1.0f, 1.0f, 1.0f);

   private ShapeRenderer shapeRenderer;

   public GeomObject(ShaderProgram shader, RigidBodyModel model) {
      this.shader = shader;
      this.model = model;
      build();

      shapeRenderer = new ShapeRenderer();
   }

   private void build() {
      meshes = new ArrayList<Mesh>();
      rings = new ArrayList<SimplePolygon2D>();

      for (PolygonModel pm : model.polygons) {
         SimplePolygon2D poly = new SimplePolygon2D();
         float[] blob = new float[pm.vertices.size() * 8];
         int c = 0;
         for (Vector2 vertex : pm.vertices) {
            // vertex.sub(0.5f, 0.5f);
            poly.addVertex(new Point2D(-vertex.x + 0.5f, vertex.y - 0.5f));
            blob[c++] = -vertex.x + 0.5f;
            blob[c++] = vertex.y - 0.5f;
            blob[c++] = 0.0f;

            blob[c++] = 0.0f;
            blob[c++] = 0.0f;
            blob[c++] = 1.0f;

            blob[c++] = vertex.x;
            blob[c++] = 1.0f - vertex.y;
         }
         Mesh mesh = new Mesh(true, pm.vertices.size(), 0,//
               new VertexAttribute(Usage.Position, 3, ShaderProgram.POSITION_ATTRIBUTE),//
               new VertexAttribute(Usage.Normal, 3, ShaderProgram.NORMAL_ATTRIBUTE),//
               new VertexAttribute(Usage.TextureCoordinates, 2, ShaderProgram.TEXCOORD_ATTRIBUTE + "0"));//
         mesh.setVertices(blob);
         meshes.add(mesh);
         rings.add(poly);
      }
      texture = Textures.get("textures/" + model.name + ".png");
   }

   @Override
   public void render(Matrix4 camMatrix) {

      texture.bind();
      shader.begin();
      Matrix4 matrix = getMatrix();
      shader.setUniformMatrix("uMVMatrix", matrix);
      shader.setUniformMatrix("uPMatrix", camMatrix);

      for (Mesh mesh : meshes) {
         mesh.render(shader, GL20.GL_TRIANGLE_FAN);
      }

      shader.end();

      // shapeRenderer.setProjectionMatrix(camMatrix);
      // shapeRenderer.begin(ShapeType.Line);
      // shapeRenderer.setColor(1, 0, 0, 1);
      //
      // Collection<LineSegment2D> edges = getShape();
      // for (LineSegment2D segment : edges) {
      // shapeRenderer.line((float) segment.firstPoint().getX(), (float)
      // segment.firstPoint().getY(), (float) segment.lastPoint().getX(),
      // (float) segment.lastPoint().getY());
      // }
      // shapeRenderer.end();
   }

   private Matrix4 getMatrix() {
      return new Matrix4().mul(new Matrix4().translate(pos).rotate(1, 0, 0, rot.x).rotate(0, 1, 0, rot.y).rotate(0, 0, 1, rot.z).scale(scale.x, scale.y, scale.z));
   }

   public Vector3 getPos() {
      return pos;
   }

   public void setPos(Vector3 pos) {
      this.pos = pos;
   }

   public Vector3 getRot() {
      return rot;
   }

   public void setRot(Vector3 rot) {
      this.rot = rot;
   }

   public Vector3 getScale() {
      return scale;
   }

   public void setScale(Vector3 scale) {
      this.scale = scale;
   }

   @Override
   public Collection<LineSegment2D> getShape() {

      AffineTransform2D tPos = AffineTransform2D.createTranslation(pos.x, pos.y);
      AffineTransform2D tRot = AffineTransform2D.createRotation(rot.z * MathUtils.degreesToRadians);

      Collection<LineSegment2D> collection = new ArrayList<LineSegment2D>();
      for (SimplePolygon2D ring : rings) {
         SimplePolygon2D transformed = ring.transform(tRot).transform(tPos);
         collection.addAll(transformed.edges());
      }

      return collection;
   }

   @Override
   public boolean hitTest(float x, float y) {
      AffineTransform2D tPos = AffineTransform2D.createTranslation(pos.x, pos.y);
      AffineTransform2D tRot = AffineTransform2D.createRotation(rot.z * MathUtils.degreesToRadians);

      for (SimplePolygon2D ring : rings) {
         SimplePolygon2D transformed = ring.transform(tRot).transform(tPos);
         if (transformed.boundingBox().contains(x, y)) {
            return true;
         }
      }
      return false;
   }
}
