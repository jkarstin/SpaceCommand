package ph.games.scg.system;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntityListener;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.gdx.math.Vector3;
//See <http://javadox.com/com.badlogicgames.gdx/gdx-bullet/1.3.1/overview-summary.html> for API info
import com.badlogic.gdx.physics.bullet.collision.btAxisSweep3;
import com.badlogic.gdx.physics.bullet.collision.btBroadphaseInterface;
import com.badlogic.gdx.physics.bullet.collision.btCollisionConfiguration;
import com.badlogic.gdx.physics.bullet.collision.btCollisionDispatcher;
import com.badlogic.gdx.physics.bullet.collision.btDefaultCollisionConfiguration;
import com.badlogic.gdx.physics.bullet.collision.btGhostPairCallback;
import com.badlogic.gdx.physics.bullet.dynamics.btConstraintSolver;
import com.badlogic.gdx.physics.bullet.dynamics.btDiscreteDynamicsWorld;
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody;
import com.badlogic.gdx.physics.bullet.dynamics.btSequentialImpulseConstraintSolver;

import ph.games.scg.component.BulletComponent;
import ph.games.scg.component.CharacterComponent;

public class BulletSystem extends EntitySystem implements EntityListener {

	public final btCollisionConfiguration collisionConfiguration;
	public final btCollisionDispatcher dispatcher;
	public final btBroadphaseInterface broadphase;
	public final btConstraintSolver solver;
	public final btDiscreteDynamicsWorld collisionWorld;
	public final int maxSubSteps = 5;
	public final float fixedTimeStep = 1f/60f;

	private btGhostPairCallback ghostPairCallback;

	public BulletSystem() {
		//TODO: THIS CURRENTLY BREAKS THE GAME WHEN MULTIPLE USERS PLAY AT ONCE...
//		PlayerEnemyContactListener peContactListener = new PlayerEnemyContactListener();
//		peContactListener.enable();
		
		this.collisionConfiguration = new btDefaultCollisionConfiguration();
		this.dispatcher = new btCollisionDispatcher(this.collisionConfiguration);
		this.broadphase = new btAxisSweep3(
				new Vector3(-1000f, -1000f, -1000f),
				new Vector3( 1000f,  1000f,  1000f)
				);
		this.solver = new btSequentialImpulseConstraintSolver();
		this.collisionWorld = new btDiscreteDynamicsWorld(
				this.dispatcher,
				this.broadphase,
				this.solver,
				this.collisionConfiguration
				);
		this.ghostPairCallback = new btGhostPairCallback();
		this.broadphase.getOverlappingPairCache().setInternalGhostPairCallback(this.ghostPairCallback);
		this.collisionWorld.setGravity(new Vector3(0f, -0.5f, 0f));
	}

	public void removeBody(Entity entity) {
		BulletComponent comp = entity.getComponent(BulletComponent.class);
		if (comp != null) {
			this.collisionWorld.removeCollisionObject(comp.body);
		}

		CharacterComponent character = entity.getComponent(CharacterComponent.class);
		if (character != null) {
			this.collisionWorld.removeAction(character.characterController);
			this.collisionWorld.removeCollisionObject(character.ghostObject);
		}
	}
	
	public void dispose() {
		this.collisionWorld.dispose();
		if (this.solver != null)                 this.solver.dispose();
		if (this.broadphase != null)             this.broadphase.dispose();
		if (this.dispatcher != null)             this.dispatcher.dispose();
		if (this.collisionConfiguration != null) this.collisionConfiguration.dispose();
		ghostPairCallback.dispose();
	}

	@Override
	public void addedToEngine(Engine engine) {
		engine.addEntityListener(Family.all(BulletComponent.class).get(), this);
	}

	@Override
	public void update(float dt) {
		this.collisionWorld.stepSimulation(dt, this.maxSubSteps, this.fixedTimeStep);
	}

	@Override
	public void entityAdded(Entity entity) {
		BulletComponent bulletComponent = entity.getComponent(BulletComponent.class);
		if (bulletComponent.body != null) {
			this.collisionWorld.addRigidBody((btRigidBody)bulletComponent.body);
		}
	}

	@Override
	public void entityRemoved(Entity entity) { }

}