package ph.games.scg.system;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntityListener;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.physics.bullet.collision.ClosestRayResultCallback;

import ph.games.scg.component.AnimationComponent;
import ph.games.scg.component.CharacterComponent;
import ph.games.scg.component.ModelComponent;
import ph.games.scg.component.PlayerComponent;
import ph.games.scg.component.StatusComponent;
import ph.games.scg.game.GameCore;
import ph.games.scg.ui.GameUI;
import ph.games.scg.util.Debug;
import ph.games.scg.util.Settings;

public class PlayerSystem extends EntitySystem implements EntityListener {

	private final Camera camera;
	private final Vector3 rayFrom = new Vector3();
	private final Vector3 rayTo = new Vector3();
	private final Vector3 tmp = new Vector3();
	private final float turnSpeed = 1f;
	private final float moveSpeed = 10f;
//	private final float jumpSpeed = 10f;

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
		//Fire when clicked/touched
		if (Gdx.input.isTouched()) fire();
		
		float deltaX = -Gdx.input.getDeltaX() * 0.5f;
		float deltaY = -Gdx.input.getDeltaY() * 0.5f;

		this.tmp.set(0f, 0f, 0f);
		this.camera.rotate(this.camera.up, deltaX * this.turnSpeed);
		this.tmp.set(this.camera.direction).crs(this.camera.up).nor();
		this.camera.direction.rotate(this.tmp, deltaY * this.turnSpeed);
		this.tmp.set(0f, 0f, 0f);
		
		this.characterComponent.characterDirection.set(0f, 0f, 1f).rot(this.modelComponent.instance.transform).nor();
		this.characterComponent.walkDirection.set(0f, 0f, 0f);

		Vector3 movement = new Vector3();
		boolean recordMovement = false;

		if (Gdx.input.isKeyPressed(Input.Keys.W)) {
			movement.add(this.camera.direction);
			recordMovement = true;
		//         this.characterComponent.walkDirection.add(this.camera.direction);
		}
		if (Gdx.input.isKeyPressed(Input.Keys.S)) {
			movement.sub(this.camera.direction);
			recordMovement = true;
		//         this.characterComponent.walkDirection.sub(this.camera.direction);
		}
		if (Gdx.input.isKeyPressed(Input.Keys.A)) {
			movement.sub(tmp.set(this.camera.direction).crs(this.camera.up));
//			rotation += this.turnSpeed;
			recordMovement = true;
		//         this.camera.rotate(this.camera.up, this.turnSpeed);
		}
		if (Gdx.input.isKeyPressed(Input.Keys.D)) {
			movement.add(tmp.set(this.camera.direction).crs(this.camera.up));
//			rotation -= this.turnSpeed;
			recordMovement = true;
		//         this.camera.rotate(this.camera.up, -this.turnSpeed);
		}
//		if (Gdx.input.isKeyPressed(Input.Keys.PAGE_UP)) {
//			tmp.set(this.camera.direction).crs(this.camera.up).nor();
//			this.camera.direction.rotate(tmp, 2*this.turnSpeed/3f);
//		}
//		if (Gdx.input.isKeyPressed(Input.Keys.PAGE_DOWN)) {
//			tmp.set(this.camera.direction).crs(this.camera.up).nor();
//			this.camera.direction.rotate(tmp, -2*this.turnSpeed/3f);
//		}
//		if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
//			movement.add(0f, this.jumpSpeed, 0f);
//			recordMovement = true;
//		}
		
//		this.camera.rotate(this.camera.up, rotation);
		movement.nor().scl(this.moveSpeed * dt);
		this.characterComponent.walkDirection.add(movement);
		this.characterComponent.characterController.setWalkDirection(this.characterComponent.walkDirection);

		//Move
		Vector3 translation = new Vector3();
		this.characterComponent.ghostObject.getWorldTransform().getTranslation(translation);
		this.modelComponent.instance.transform.set(
				translation.x, translation.y, translation.z,
				this.camera.direction.x, this.camera.direction.y, this.camera.direction.z, 0f //Not convinced this is accurate, but we'll leave it for now
				);
		this.camera.position.set(translation.x, translation.y, translation.z);
		this.camera.update(true);
		
		if (recordMovement) {
			//TODO: Figure out why this isn't capturing your rotation
			Quaternion quat = new Quaternion();
			this.modelComponent.instance.transform.getRotation(quat);
			float facing = quat.getAngleAround(this.camera.up);
			
			GameCore.client.move(movement, facing, dt);
		}
	}

	private void fire() {
		final float reach = 50f;
		
		Ray ray = this.camera.getPickRay(Gdx.graphics.getWidth()/2f, Gdx.graphics.getHeight()/2f);
		rayFrom.set(ray.origin);
		rayTo.set(ray.direction).scl(reach).add(rayFrom);
		//Because we reuse the ClosestRayResultCallback, we need to reset its values
		this.rayTestCB.setCollisionObject(null);
		this.rayTestCB.setClosestHitFraction(1f);
		this.rayTestCB.setRayFromWorld(rayFrom);
		this.rayTestCB.setRayToWorld(rayTo);
		this.bulletSystem.collisionWorld.rayTest(rayFrom, rayTo, rayTestCB);
		if (rayTestCB.hasHit()) {
			final Entity hitEntity = (Entity)(rayTestCB.getCollisionObject().userData);
			
			StatusComponent scomp = hitEntity.getComponent(StatusComponent.class);
			if (scomp != null) {
				//We hit one, boys!
				Debug.log("Entity hit: " + scomp);
		             scomp.setAlive(false);
		             PlayerComponent.score += 100;
			}
			
		}
		this.gun.getComponent(AnimationComponent.class).animate("GunArmature|ShootAction", 1, 1f);
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