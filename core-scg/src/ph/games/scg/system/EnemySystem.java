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
import ph.games.scg.component.PlayerComponent;
import ph.games.scg.util.EntityFactory;

public class EnemySystem extends EntitySystem implements EntityListener {
   
   private final ComponentMapper<CharacterComponent> cm = ComponentMapper.getFor(CharacterComponent.class);
   
   private ImmutableArray<Entity> entities;
   private Entity player;
   private Quaternion quat = new Quaternion();
   private Engine engine;
   private BulletSystem bulletSystem;
   
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
         Random random = new Random();
         this.engine.addEntity(
            EntityFactory.createEnemy(
               this.bulletSystem,
               random.nextInt(40) - 20,
               16,
               random.nextInt(40) - 20
            )
         );
      }
      
      for (Entity e : this.entities) {
         ModelComponent mod = e.getComponent(ModelComponent.class);
         ModelComponent playerModel = this.player.getComponent(ModelComponent.class);
         Vector3 playerPosition = new Vector3();
         Vector3 enemyPosition = new Vector3();
         playerModel.instance.transform.getTranslation(playerPosition);
         mod.instance.transform.getTranslation(enemyPosition);
         float dX = playerPosition.x - enemyPosition.x;
         float dZ = playerPosition.z - enemyPosition.z;
         float theta = (float)(Math.atan2(dX, dZ));
         //Calculate the transforms
         Quaternion rot = quat.setFromAxis(0f, 1f, 0f, (float)Math.toDegrees(theta));
         //Walk
         Matrix4 ghost = new Matrix4();
         Vector3 translation = new Vector3();
         cm.get(e).ghostObject.getWorldTransform(ghost);
         ghost.getTranslation(translation);
         mod.instance.transform.set(translation.x, translation.y, translation.z, rot.x, rot.y, rot.z, rot.w);
         cm.get(e).characterDirection.set(0f, 0f, 1f).rot(mod.instance.transform);
         cm.get(e).walkDirection.set(0f, 0f, 0f);
         cm.get(e).walkDirection.add(cm.get(e).characterDirection);
         cm.get(e).walkDirection.scl(3f * dt);
         cm.get(e).characterController.setWalkDirection(cm.get(e).walkDirection);
      }
   }
   
   @Override
   public void entityAdded(Entity entity) {
      this.player = entity;
   }
   
   @Override
   public void entityRemoved(Entity entity) { }
   
}