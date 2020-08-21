package ph.games.scg._depreciated_.server;

import com.badlogic.gdx.math.Vector3;

import ph.games.scg.util.ILoggable;

public abstract class NetEntity implements ILoggable {
	
	private static int ID_TAG = 0;
	
	public static enum NET_TYP implements ILoggable {
		USER,
		ENEMY
	}
	
	public final int NET_ID;
	
	private NET_TYP type;
	//NOTE: Needs to be one continuous token with no spaces
	protected String name;
	
	protected float health;
	protected float strength;
	
	protected boolean spawned;
	
	protected float facing;
	protected Vector3 position;
	
	public NetEntity(NET_TYP type, String name) {
		this.NET_ID = ID_TAG++;
		
		this.type = type;
		this.name = name;
		
		this.resetHealth();
		this.strength = 10f;
		
		this.spawned = false;
		
		this.facing = 0f;
		this.position = new Vector3();
	}
	
	public abstract boolean hasName(String name);
	public abstract void resetHealth();
	public abstract void applyDamage(float amount);
	public abstract void setStrength(float strength);
	public abstract void setPosition(Vector3 position);
	public abstract void moveBy(Vector3 movement);
	
	public boolean hasType(NET_TYP type) {
		if (type == null) return false;
		
		return this.type == type;
	}
	
	public String getName() {
		return this.name;
	}
	
	public float getHealth() {
		return this.health;
	}
	
	public float getStrength() {
		return this.strength;
	}
	
	public void setSpawned(boolean state) {
		this.spawned = state;
	}
	
	public boolean isSpawned() {
		return this.spawned;
	}
	
	public Vector3 getPosition() {
		return this.position;
	}
	
	@Override
	public String toString() {
		return "NET_ENTITY_" + this.type;
	}
	
}
