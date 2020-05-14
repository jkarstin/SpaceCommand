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
import ph.games.scg.component.ModelComponent;
import ph.games.scg.component.UserEntityComponent;
import ph.games.scg.util.Debug;

public class UserEntitySystem extends EntitySystem implements EntityListener {

	private ImmutableArray<Entity> entities;
	private float dtRemaining;

	@Override
	public void addedToEngine(Engine engine) {
		this.entities = engine.getEntitiesFor(Family.all(UserEntityComponent.class, CharacterComponent.class).get());
		engine.addEntityListener(Family.all(UserEntityComponent.class).get(), this);
	}

	@Override
	public void update(float dt) {
		ModelComponent mc;
		CharacterComponent cc;
		UserEntityComponent uec;

		for (Entity entity : this.entities) {
			mc = entity.getComponent(ModelComponent.class);
			cc = entity.getComponent(CharacterComponent.class);
			uec = entity.getComponent(UserEntityComponent.class);

			//Get position
			Vector3 position = new Vector3();
			cc.ghostObject.getWorldTransform().getTranslation(position);
			
			//Get rotation
			Quaternion quat = new Quaternion();
			mc.instance.transform.getRotation(quat);
			float currentFacing = quat.getAngleAround(Vector3.Y);
			
			Vector3 translationVector = new Vector3();
			
			this.dtRemaining = dt;
			while (this.dtRemaining > 0f && uec.queuedDeltaTime.size() > 0) {

				float queuedDeltaTime = uec.queuedDeltaTime.remove(0);
				float queuedFacing = uec.queuedFacing.remove(0);
				Vector3 queuedMovement = uec.queuedMovement.remove(0);

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

					uec.queuedDeltaTime.add(0, queuedDeltaTime*(1f-percentage));
					uec.queuedFacing.add(0, queuedFacing);
					uec.queuedMovement.add(0, queuedMovement.scl(1f-percentage));

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
	public void entityAdded(Entity entity) { }

	@Override
	public void entityRemoved(Entity entity) { }

}
