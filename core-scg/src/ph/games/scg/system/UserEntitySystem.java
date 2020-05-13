package ph.games.scg.system;

import java.util.ArrayList;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntityListener;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.gdx.math.Vector3;

import ph.games.scg.component.CharacterComponent;
import ph.games.scg.component.UserEntityComponent;
import ph.games.scg.util.Debug;

public class UserEntitySystem extends EntitySystem implements EntityListener {
	
	private ArrayList<Entity> entities;
	private float dtRemaining;
	
	public UserEntitySystem() {
		this.entities = new ArrayList<Entity>();
		this.dtRemaining = 0f;
	}
	
	@Override
	public void update(float dt) {
		//Update location and rotation state of all entities
		CharacterComponent cc;
		UserEntityComponent uec;
		for (Entity entity : this.entities) {
			cc = entity.getComponent(CharacterComponent.class);
			uec = entity.getComponent(UserEntityComponent.class);
			
			this.dtRemaining = dt;
			while (this.dtRemaining > 0f && uec.queuedDeltaTime.size() > 0) {
								
				float queuedDeltaTime = uec.queuedDeltaTime.remove(0);
				float queuedRotation = uec.queuedRotation.remove(0);
				Vector3 queuedMovement = uec.queuedMovement.remove(0);
				
				Debug.log("Queued movement data: " + queuedMovement + "," + queuedRotation + "," + queuedDeltaTime);
				
				//If full movement can be applied, apply
				if (this.dtRemaining >= queuedDeltaTime) {
					cc.walkDirection.add(queuedMovement);
					cc.characterDirection.add(queuedRotation);
					this.dtRemaining -= queuedDeltaTime;
				}
				//If full movement cannot be applied, calculate percentage and move by that much; re-queue remaining amount
				else {
					float percentage = this.dtRemaining/queuedDeltaTime;
					float queuedRotationPortion = queuedRotation*percentage;
					Vector3 queuedMovementPortion = queuedMovement.cpy();
					queuedMovementPortion.scl(percentage);
					
					uec.queuedDeltaTime.add(0, queuedDeltaTime*(1f-percentage));
					uec.queuedRotation.add(0, queuedRotation*(1f-percentage));
					uec.queuedMovement.add(0, queuedMovement.scl(1f-percentage));
					
					cc.walkDirection.add(queuedMovementPortion);
					cc.characterDirection.add(queuedRotationPortion);
					this.dtRemaining = 0f;
				}
			}
		}
	}
	
	@Override
	public void addedToEngine(Engine engine) {
		engine.addEntityListener(Family.all(UserEntityComponent.class).get(), this);
	}
	
	@Override
	public void entityAdded(Entity entity) {
		this.entities.add(entity);
	}

	@Override
	public void entityRemoved(Entity entity) {
		this.entities.remove(entity);
	}

}
