package ph.games.scg.system;

import java.util.Random;

import com.badlogic.ashley.core.ComponentMapper;
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
import ph.games.scg.component.EnemyComponent;
import ph.games.scg.component.ModelComponent;
import ph.games.scg.component.NetEntityComponent;
import ph.games.scg.component.PlayerComponent;
import ph.games.scg.game.GameCore;
import ph.games.scg.util.EntityFactory;

public class EnemySystem extends EntitySystem implements EntityListener {

	private final ComponentMapper<CharacterComponent> cm = ComponentMapper.getFor(CharacterComponent.class);

	private ImmutableArray<Entity> entities;
	private Entity player;
	private Quaternion quat = new Quaternion();
	private Engine engine;
	private BulletSystem bulletSystem;
	
	private static int ENEMY_COUNTER = 0;

	public EnemySystem(BulletSystem bs) {
		this.bulletSystem = bs;
	}

	@Override
	public void addedToEngine(Engine engine) {
		this.entities = engine.getEntitiesFor(Family.all(EnemyComponent.class, CharacterComponent.class).get());
		engine.addEntityListener(Family.one(PlayerComponent.class).get(), this);
		this.engine = engine;
	}

	@Override
	public void update(float dt) {
		if (this.entities.size() < 1) {
			//Request new enemy spawn
			GameCore.client.spawnEnemy("enemy_" + ENEMY_COUNTER++);
			return;
			
			
//			Random random = new Random();
//			this.engine.addEntity(
//					EntityFactory.createEnemy(
//							this.bulletSystem,
//							"enemy_" + ENEMY_COUNTER++,
//							random.nextInt(40) - 20,
//							16,
//							random.nextInt(40) - 20
//							)
//					);
		}

		EnemyComponent ecomp=null;
		for (Entity e : this.entities) {
			ecomp = e.getComponent(EnemyComponent.class);
			if (ecomp.target == null) continue;
			
			ModelComponent mcomp = e.getComponent(ModelComponent.class);
			ModelComponent tgtmcomp = ecomp.target.getComponent(ModelComponent.class);
			NetEntityComponent necomp = e.getComponent(NetEntityComponent.class);
			Vector3 targetPosition = new Vector3();
			Vector3 enemyPosition = new Vector3();
			tgtmcomp.instance.transform.getTranslation(targetPosition);
			mcomp.instance.transform.getTranslation(enemyPosition);
			float dX = targetPosition.x - enemyPosition.x;
			float dZ = targetPosition.z - enemyPosition.z;
			float theta = (float)Math.toDegrees(Math.atan2(dX, dZ));
			Vector3 movement = new Vector3(0f, 0f, 1f);
			movement.rotate(Vector3.Y, theta).scl(3f * dt);
			
			GameCore.client.move(necomp.netEntity.getName(), movement, theta, dt);
		}
	}

	@Override
	public void entityAdded(Entity entity) {
		this.player = entity;
	}

	@Override
	public void entityRemoved(Entity entity) { }

}