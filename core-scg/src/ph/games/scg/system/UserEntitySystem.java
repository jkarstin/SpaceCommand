package ph.games.scg.system;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntityListener;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.math.Matrix4;
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
			float facing = quat.getAngleAround(Vector3.Y);
			
			Vector3 translationVector = new Vector3();
			
			this.dtRemaining = dt;
			while (this.dtRemaining > 0f && uec.queuedDeltaTime.size() > 0) {

				float queuedDeltaTime = uec.queuedDeltaTime.remove(0);
				float queuedRotation = uec.queuedRotation.remove(0);
				Vector3 queuedMovement = uec.queuedMovement.remove(0);

				Debug.log("Queued movement data: " + queuedMovement + "," + queuedRotation + "," + queuedDeltaTime);

				cc.characterDirection.set(0f, 0f, 0f);
				
				//If full movement can be applied, apply
				if (this.dtRemaining >= queuedDeltaTime) {
					translationVector.add(queuedMovement);
					facing = queuedRotation;
					this.dtRemaining -= queuedDeltaTime;
				}
				//If full movement cannot be applied, calculate percentage and move by that much; re-queue remaining amount
				else {
					float percentage = this.dtRemaining/queuedDeltaTime;
					float queuedRotationPortion = (queuedRotation-facing)*percentage;
					Vector3 queuedMovementPortion = queuedMovement.cpy();
					queuedMovementPortion.scl(percentage);

					uec.queuedDeltaTime.add(0, queuedDeltaTime*(1f-percentage));
					uec.queuedRotation.add(0, queuedRotation*(1f-percentage));
					uec.queuedMovement.add(0, queuedMovement.scl(1f-percentage));

					translationVector.add(queuedMovementPortion);
					facing += queuedRotationPortion;
					this.dtRemaining = 0f;
				}
			}
			
			//Apply translation
			position.add(translationVector);
			mc.instance.transform.setTranslation(position);
			//Apply rotation
			//mc.instance.transform.setToRotation(Vector3.Y, facing);
			
			cc.walkDirection.set(0f, 0f, 0f);
			cc.walkDirection.add(translationVector);
			cc.characterController.setWalkDirection(cc.walkDirection);
			
			
			
			
			
			
			
//			Vector3 movement = new Vector3();
//

//
//			float theta = (float)(Math.atan2(movement.x, movement.z));
//
//			//Calculate the rotation
//			Quaternion rot = quat.setFromAxis(0f, 1f, 0f, (float)Math.toDegrees(theta) + 90f);
//			//Walk
//			Matrix4 ghost = new Matrix4();
//			Vector3 translation = new Vector3();
//			cc.ghostObject.getWorldTransform(ghost);
//			ghost.getTranslation(translation);
//			mc.instance.transform.set(translation.x, translation.y, translation.z, rot.x, rot.y, rot.z, rot.w);
//			cc.characterDirection.set(-1f, 0f, 0f).rotate(Vector3.Y, theta);//.rot(mc.instance.transform);
//			cc.walkDirection.set(0f, 0f, 0f);
//			cc.walkDirection.add(cc.characterDirection);
//			cc.walkDirection.scl(movement.len());
//			cc.characterController.setWalkDirection(cc.walkDirection);
		}
	}

	@Override
	public void entityAdded(Entity entity) { }

	@Override
	public void entityRemoved(Entity entity) { }

}
