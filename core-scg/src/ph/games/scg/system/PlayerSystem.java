package ph.games.scg.system;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntityListener;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.physics.bullet.collision.ClosestRayResultCallback;

import ph.games.scg.component.CharacterComponent;
import ph.games.scg.component.ModelComponent;
import ph.games.scg.component.PlayerComponent;
import ph.games.scg.ui.GameUI;
import ph.games.scg.util.Settings;

public class PlayerSystem extends EntitySystem implements EntityListener {
   
   private final Camera camera;
   private final Vector3 rayFrom = new Vector3();
   private final Vector3 rayTo = new Vector3();
   private final Vector3 tmp = new Vector3();
   private final float turnSpeed = 3f;
   
   private BulletSystem bulletSystem;
   private CharacterComponent characterComponent;
   private ClosestRayResultCallback rayTestCB;
   private Entity player;
   private GameUI gameUI;
   private ModelComponent modelComponent;
   private PlayerComponent playerComponent;
   
   public Entity gun;
   
   public PlayerSystem(Camera camera, BulletSystem bulletSystem, GameUI gameUI) {
      this.camera = camera;
      this.bulletSystem = bulletSystem;
      this.rayTestCB = new ClosestRayResultCallback(Vector3.Zero, Vector3.Z);
      this.gameUI = gameUI;
   }
   
   @Override
   public void addedToEngine(Engine engine) {
      engine.addEntityListener(Family.all(PlayerComponent.class).get(), this);
   }
   
   @Override
   public void update(float dt) {
      if (this.player == null) return;
      updateMovement(dt);
      updateStatus();
      checkGameOver();
   }
   
   @Override
   public void entityAdded(Entity entity) {
      this.player = entity;
      this.playerComponent    = entity.getComponent(   PlayerComponent.class);
      this.characterComponent = entity.getComponent(CharacterComponent.class);
      this.modelComponent     = entity.getComponent(    ModelComponent.class);
   }
   
   @Override
   public void entityRemoved(Entity entity) { }
   
   private void updateMovement(float dt) {
      //Fire when clicked/touched or Space just pressed
      if (Gdx.input.justTouched() ||
          Gdx.input.isKeyJustPressed(Input.Keys.SPACE))
         fire();
      
      this.characterComponent.characterDirection.set(-1f, 0f, 0f).rot(this.modelComponent.instance.transform).nor();
      this.characterComponent.walkDirection.set(0f, 0f, 0f);
      
      if (Gdx.input.isKeyPressed(Input.Keys.W))
         this.characterComponent.walkDirection.add(this.camera.direction);
      if (Gdx.input.isKeyPressed(Input.Keys.S))
         this.characterComponent.walkDirection.sub(this.camera.direction);
      if (Gdx.input.isKeyPressed(Input.Keys.A))
         this.camera.rotate(this.camera.up, this.turnSpeed);
      if (Gdx.input.isKeyPressed(Input.Keys.D))
         this.camera.rotate(this.camera.up, -this.turnSpeed);
      if (Gdx.input.isKeyPressed(Input.Keys.PAGE_UP)) {
         tmp.set(this.camera.direction).crs(this.camera.up).nor();
         this.camera.direction.rotate(tmp, 2*this.turnSpeed/3f);
      }
      if (Gdx.input.isKeyPressed(Input.Keys.PAGE_DOWN)) {
         tmp.set(this.camera.direction).crs(this.camera.up).nor();
         this.camera.direction.rotate(tmp, -2*this.turnSpeed/3f);
      }
      
      this.characterComponent.walkDirection.scl(10f * dt);
      this.characterComponent.characterController.setWalkDirection(this.characterComponent.walkDirection);
      
      //Move
      Matrix4 ghost = new Matrix4();
      Vector3 translation = new Vector3();
      this.characterComponent.ghostObject.getWorldTransform(ghost);
      ghost.getTranslation(translation);
      this.modelComponent.instance.transform.set(
         translation.x, translation.y, translation.z,
         this.camera.direction.x, this.camera.direction.y, this.camera.direction.z,
         0f
      );
      this.camera.position.set(translation.x, translation.y, translation.z);
      this.camera.update(true);
   }
   
   private void fire() {
      Ray ray = this.camera.getPickRay(Gdx.graphics.getWidth()/2f, Gdx.graphics.getHeight()/2f);
      rayFrom.set(ray.origin);
      rayTo.set(ray.direction).scl(50f).add(rayFrom);
      //Because we reuse the ClosestRayResultCallback, we need to reset its values
      this.rayTestCB.setCollisionObject(null);
      this.rayTestCB.setClosestHitFraction(1f);
      this.rayTestCB.setRayFromWorld(rayFrom);
      this.rayTestCB.setRayToWorld(rayTo);
      this.bulletSystem.collisionWorld.rayTest(rayFrom, rayTo, rayTestCB);
      if (rayTestCB.hasHit()) {
         final Entity e = (Entity)(rayTestCB.getCollisionObject().userData);
         //Do stuff with Entity e
      }
   }
   
   private void updateStatus() {
      this.gameUI.healthWidget.setValue(this.playerComponent.health);
   }
   
   private void checkGameOver() {
      if (this.playerComponent.health <= 0 && !Settings.Paused) {
         Settings.Paused = true;
         this.gameUI.gameOverWidget.gameOver();
      }
   }
   
}