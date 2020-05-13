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
	private Quaternion quat = new Quaternion();

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

			float theta = 0f;
			Vector3 movement = new Vector3();

			this.dtRemaining = dt;
			while (this.dtRemaining > 0f && uec.queuedDeltaTime.size() > 0) {

				float queuedDeltaTime = uec.queuedDeltaTime.remove(0);
				float queuedRotation = uec.queuedRotation.remove(0);
				Vector3 queuedMovement = uec.queuedMovement.remove(0);

				Debug.log("Queued movement data: " + queuedMovement + "," + queuedRotation + "," + queuedDeltaTime);

				cc.characterDirection.set(0f, 0f, 0f);

				//If full movement can be applied, apply
				if (this.dtRemaining >= queuedDeltaTime) {
					movement.add(queuedMovement);
					theta += queuedRotation;
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

					movement.add(queuedMovementPortion);
					theta += queuedRotationPortion;
					this.dtRemaining = 0f;
				}
			}

			//Calculate the rotation
			Quaternion rot = quat.setFromAxis(0f, 1f, 0f, theta);
			//Walk
			Matrix4 ghost = new Matrix4();
			Vector3 translation = new Vector3();
			cc.ghostObject.getWorldTransform(ghost);
			ghost.getTranslation(translation);
			mc.instance.transform.set(translation.x, translation.y, translation.z, rot.x, rot.y, rot.z, rot.w);
			cc.characterDirection.set(-1f, 0f, 0f).rot(mc.instance.transform);
			cc.walkDirection.set(0f, 0f, 0f);
			cc.walkDirection.add(movement);
			cc.characterController.setWalkDirection(cc.walkDirection);
		}
	}

	@Override
	public void entityAdded(Entity entity) { }

	@Override
	public void entityRemoved(Entity entity) { }
	
}
