package ph.games.scg.system;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntityListener;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;

import ph.games.scg.component.NetEntityComponent;

public class NetEntitySystem extends EntitySystem implements EntityListener {
	
	ImmutableArray<Entity> entities;
	
	@Override
	public void update(float dt) {
		
	}
	
	@Override
	public void addedToEngine(Engine engine) {
		this.entities = engine.getEntitiesFor(Family.all(NetEntityComponent.class).get());
	}
	
	@Override
	public void entityAdded(Entity entity) { }

	@Override
	public void entityRemoved(Entity entity) { }

}
