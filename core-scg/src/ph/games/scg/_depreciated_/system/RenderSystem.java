package ph.games.scg._depreciated_.system;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;

import ph.games.scg._depreciated_.component.AnimationComponent;
import ph.games.scg._depreciated_.component.GunComponent;
import ph.games.scg._depreciated_.component.ModelComponent;
import ph.games.scg._depreciated_.game.GameCore;

public class RenderSystem extends EntitySystem {
   
   private static final float FOV = 67f;
   
   private ImmutableArray<Entity> entities;
   private ModelBatch batch;
   private Environment environment;
   private PerspectiveCamera pCam, gCam;
   
   public Entity gun;
   
   public RenderSystem() {
      this.batch = new ModelBatch();
      this.environment = new Environment();
      this.environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.3f, 0.3f, 0.3f, 1f));
      this.environment.add(new DirectionalLight().set(0.8f, 0.8f, 0.8f, -1f, -0.8f, -0.2f));
      this.pCam = new PerspectiveCamera(FOV, GameCore.VIRTUAL_WIDTH, GameCore.VIRTUAL_HEIGHT);
      this.pCam.far = 10000f;
      this.gCam = new PerspectiveCamera(FOV, GameCore.VIRTUAL_WIDTH, GameCore.VIRTUAL_HEIGHT);
      this.gCam.far = 10000f;
   }
   
   @Override
   public void addedToEngine(Engine e) {
      this.entities = e.getEntitiesFor(Family.all(ModelComponent.class).get());
   }
   
   @Override
   public void update(float dt) {
      //Draw non-this.gun models
      this.batch.begin(this.pCam);
      
      for (int i=0; i < this.entities.size(); i++) {
         Entity e = this.entities.get(i);
         //Skip over this.gun
         if (e.getComponent(GunComponent.class) == null) {
            ModelComponent mc = e.getComponent(ModelComponent.class);
            this.batch.render(mc.instance, this.environment);
            if (e.getComponent(AnimationComponent.class) != null) {
               e.getComponent(AnimationComponent.class).update(dt);
            }
         }
      }
      
      this.batch.end();
      
      if (this.gun != null) {
	      //Draw this.gun model
	      //Clear depth buffer in order to display gun on a different camera (I guess...)
	      Gdx.gl.glClear(GL20.GL_DEPTH_BUFFER_BIT);
	      this.batch.begin(this.gCam);
	      this.batch.render(this.gun.getComponent(ModelComponent.class).instance);
	      this.gun.getComponent(AnimationComponent.class).update(dt);
	      this.batch.end();
      }
   }
   
   public void resize(int width, int height) {
      this.pCam.viewportWidth  = width;
      this.pCam.viewportHeight = height;
      this.gCam.viewportWidth  = width;
      this.gCam.viewportHeight = height;
   }
   
   public PerspectiveCamera getPerspectiveCamera() {
      return this.pCam;
   }
   
   public void dispose() {
      this.batch.dispose();
      this.batch = null;
   }
   
}