package ph.games.scg.system;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;

import ph.games.scg.component.StatusComponent;
import ph.games.scg.game.GameWorld;

public class StatusSystem extends EntitySystem {
   
   private ImmutableArray<Entity> entities;
   private GameWorld gameWorld;
   
   public StatusSystem(GameWorld gameWorld) {
      this.gameWorld = gameWorld;
   }
   
   @Override
   public void addedToEngine(Engine engine) {
      entities = engine.getEntitiesFor(Family.all(StatusComponent.class).get());
   }
   
   @Override
   public void update(float dt) {
      for (Entity entity : this.entities) {
         entity.getComponent(StatusComponent.class).update(dt);
         if (entity.getComponent(StatusComponent.class).dyingStateTimer >= StatusComponent.DYING_STATE_TIME) gameWorld.remove(entity);
      }
   }
   
}