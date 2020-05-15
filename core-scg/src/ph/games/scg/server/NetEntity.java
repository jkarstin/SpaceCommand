package ph.games.scg.server;

import com.badlogic.gdx.math.Vector3;

import ph.games.scg.util.ILoggable;

public abstract class NetEntity implements ILoggable {
	
	private static int ID_TAG = 0;
	
	public static enum NET_TYP implements ILoggable {
		USER,
		ENEMY
	}
	
	private NET_TYP type;
	public final int NET_ID;
	protected String name;
	protected float health;
	protected float strength;
	protected Vector3 position;
	
	public NetEntity(NET_TYP type, String name) {
		this.type = type;
		this.name = name;
		
		this.NET_ID = ID_TAG++;
		
		this.resetHealth();
		this.strength = 10f;
		this.position = new Vector3();
	}
	
	public abstract void resetHealth();
	public abstract void applyDamage(float amount);
	public abstract void setStrength(float strength);
	public abstract void setPosition(Vector3 position);
	public abstract void moveBy(Vector3 movement);
	
	public String getName() {
		return this.name;
	}
	
	public float getHealth() {
		return this.health;
	}
	
	public float getStrength() {
		return this.strength;
	}
	
	public Vector3 getPosition() {
		return this.position;
	}
	
	@Override
	public String toString() {
		return "NET_ENTITY_" + this.type;
	}

}
