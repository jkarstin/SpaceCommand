/**************************************
 * NetEntitySystem.java
 * 
 * Entity System designed to manage and update
 * position data for Entities with NetEntityComponents
 * 
 * Is used by both the Server and the Client to manage their NetEntity instances.
 * 
 * J Karstin Neill    05.17.2020
 **************************************/

package ph.games.scg.system;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntityListener;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;

import ph.games.scg.component.CharacterComponent;
import ph.games.scg.component.EnemyComponent;
import ph.games.scg.component.ModelComponent;
import ph.games.scg.component.NetEntityComponent;
import ph.games.scg.component.PlayerComponent;
import ph.games.scg.server.Client;
import ph.games.scg.server.ServerCore;
import ph.games.scg.util.Debug;
import ph.games.scg.util.EntityFactory;

public class NetEntitySystem extends EntitySystem implements EntityListener {
	
	private ImmutableArray<Entity> nentities;
	
	private BulletSystem bulletSystem;
	private Engine engine;
	private float dtRemaining;
	
	private boolean serverSide;
	
	//Constructor used by Client-side code
	public NetEntitySystem(BulletSystem bulletSystem, Client client) {		
		this.bulletSystem = bulletSystem;
		
		client.setNetEntitySystem(this);
		
		//Helps the NES to know which side it is interacting from
		this.serverSide = false;
	}
	
	//Constructor used by Server-side code
	public NetEntitySystem(BulletSystem bulletSystem) {
		this.bulletSystem = bulletSystem;
		
		//Helps the NES to know which side it is interacting from
		this.serverSide = true;
	}
	
	public boolean isServerSide() {
		return this.serverSide;
	}
	
	public void applyDamage(String attacker, String attackee, float amount) {
		Entity attackerNentity=null, attackeeNentity=null;
		NetEntityComponent necomp=null;
		for (Entity nentity : this.nentities) {
			necomp = nentity.getComponent(NetEntityComponent.class);
			if (necomp.netEntity.getName().equals(attacker)) attackerNentity = nentity;
			if (necomp.netEntity.getName().equals(attackee)) attackeeNentity = nentity;
		}
		
		if (attackerNentity == null || attackeeNentity == null) {
			Debug.warn("Failed to apply damage. Attacker or attackee NetEntity could not be found: [attacker=" + attacker + " attackee=" + attackee + " attackerNentity=" + attackerNentity + " attackeeNentity=" + attackeeNentity + "]");
			return;
		}
		
		//See if attackee has an EnemyComponent
		EnemyComponent ecomp = attackeeNentity.getComponent(EnemyComponent.class);
		if (ecomp != null) {
			//Set the target to the attacker
			ecomp.target = attackerNentity;
		}
		
		//Apply damage
		necomp = attackeeNentity.getComponent(NetEntityComponent.class);
		necomp.netEntity.applyDamage(amount);
		
		//If serverSide and attackee was killed, send \kill command to clients
		if (this.serverSide && necomp.netEntity.getHealth() <= 0f) {
			this.killNetEntity(attackee);
			ServerCore.server.kill(attackee);
		}
	}
	
	public void spawnNetEntity(String name, Vector3 position) {
		Entity nentity=null;
		
		//TODO: Quick fix for now, make more general later
		if (name.contains("enemy_")) nentity = EntityFactory.createEnemy(bulletSystem, name, position.x, position.y, position.z);
		else nentity = EntityFactory.createUserEntity(this.bulletSystem, name, position.x, position.y, position.z);
		
		this.engine.addEntity(nentity);
	}
	
	public void updatePosition(String name, Vector3 position) {
		NetEntityComponent necomp=null;
		
		//Search through entities for a match
		boolean match = false;
		for (Entity nentity : this.nentities) {
			necomp = nentity.getComponent(NetEntityComponent.class);
			if (necomp.netEntity.hasName(name)) {
				match = true;
				break;
			}
		}
		
		if (!match) {
			Debug.warn("Failed to update NetEntity position. No NetEntity with name was found: " + name);
			return;
		}
		
		necomp.netEntity.setPosition(position);
	}
	
	public void queueMovement(String name, Vector3 moveVector, float facing, float deltaTime) {
		NetEntityComponent necomp=null;
		
		//Search through entities for a match
		boolean match = false;
		for (Entity nentity : this.nentities) {
			necomp = nentity.getComponent(NetEntityComponent.class);
			if (necomp.netEntity.getName().equals(name)) {
				match = true;
				break;
			}
		}
		
		//If no match was found, send warning and quit
		if (!match) {
			Debug.warn("Failed to update NetEntity. No NetEntity with name was found: " + name);
			return;
		}
		
		//Otherwise, NetEntity was found, queue movements
		Debug.log("Queueing movement data... " + moveVector + "," + facing + "," + deltaTime);
		necomp.queuedMovement.add(moveVector);
		necomp.queuedFacing.add(facing);
		necomp.queuedDeltaTime.add(deltaTime);
	}
	
	public void killNetEntity(String name) {
		for (Entity nentity : this.nentities) {
			NetEntityComponent necomp = nentity.getComponent(NetEntityComponent.class);
			if (necomp.netEntity.getName().equals(name)) {
				this.engine.removeEntity(nentity);
				this.bulletSystem.removeBody(nentity);
				return;
			}
		}
		//If no NetEntity match was found, send warning
		Debug.warn("Failed to kill NetEntity. No NetEntity with name was found: " + name);
	}
	
	@Override
	public void update(float dt) {
		ModelComponent mc;
		CharacterComponent cc;
		NetEntityComponent nec;

		for (Entity entity : this.nentities) {
			//Skip Player entity
			if (entity.getComponent(PlayerComponent.class) != null) continue;
			
			mc = entity.getComponent(ModelComponent.class);
			cc = entity.getComponent(CharacterComponent.class);
			nec = entity.getComponent(NetEntityComponent.class);

			//Get position
			Vector3 position = new Vector3();
			cc.ghostObject.getWorldTransform().getTranslation(position);
			
			//Get rotation
			Quaternion quat = new Quaternion();
			mc.instance.transform.getRotation(quat);
			float currentFacing = quat.getAngleAround(Vector3.Y);
			
			Vector3 translationVector = new Vector3();
			
			this.dtRemaining = dt;
			while (this.dtRemaining > 0f && nec.queuedDeltaTime.size() > 0) {

				float queuedDeltaTime = nec.queuedDeltaTime.remove(0);
				float queuedFacing = nec.queuedFacing.remove(0);
				Vector3 queuedMovement = nec.queuedMovement.remove(0);

				Debug.logv("Queued movement data: " + queuedMovement + "," + queuedFacing + "," + queuedDeltaTime);

				cc.characterDirection.set(0f, 0f, 0f);
				
				//If full movement can be applied, apply
				if (this.dtRemaining >= queuedDeltaTime) {
					translationVector.add(queuedMovement);
					currentFacing = queuedFacing;
					this.dtRemaining -= queuedDeltaTime;
				}
				//If full movement cannot be applied, calculate percentage and move by that much; re-queue remaining amount
				else {
					float percentage = this.dtRemaining/queuedDeltaTime;
					float queuedFacingPortion = (queuedFacing - currentFacing)*percentage;
					Vector3 queuedMovementPortion = queuedMovement.cpy();
					queuedMovementPortion.scl(percentage);

					nec.queuedDeltaTime.add(0, queuedDeltaTime*(1f-percentage));
					nec.queuedFacing.add(0, queuedFacing);
					nec.queuedMovement.add(0, queuedMovement.scl(1f-percentage));

					translationVector.add(queuedMovementPortion);
					currentFacing += queuedFacingPortion;
					this.dtRemaining = 0f;
				}
			}
			
			//Apply translation and rotation
			position.add(translationVector);
			quat.setFromAxis(Vector3.Y, currentFacing);
			mc.instance.transform.set(position.x, position.y, position.z, quat.x, quat.y, quat.z, quat.w);
			
			cc.walkDirection.set(0f, 0f, 0f);
			cc.walkDirection.add(translationVector);
			cc.characterController.setWalkDirection(cc.walkDirection);
		}
	}
	
	@Override
	public void addedToEngine(Engine engine) {
		this.nentities = engine.getEntitiesFor(Family.all(NetEntityComponent.class).get());
		engine.addEntityListener(Family.all(NetEntityComponent.class).get(), this);
		this.engine = engine;
	}
	
	@Override
	public void entityAdded(Entity entity) {
		this.nentities = engine.getEntitiesFor(Family.all(NetEntityComponent.class).get());
	}

	@Override
	public void entityRemoved(Entity entity) {
		this.nentities = engine.getEntitiesFor(Family.all(NetEntityComponent.class).get());
	}

}
