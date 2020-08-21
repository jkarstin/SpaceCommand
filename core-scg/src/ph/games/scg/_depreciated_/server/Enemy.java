package ph.games.scg._depreciated_.server;

import com.badlogic.gdx.math.Vector3;

public class Enemy extends NetEntity {

	public Enemy(String name) {
		super(NET_TYP.ENEMY, name);
	}
	
	@Override
	public boolean hasName(String name) {
		return this.name.equals(name);
	}
	
	@Override
	public void resetHealth() {
		this.health = 100f;
	}
	
	@Override
	public void applyDamage(float amount) {
		this.health -= amount;
	}
	
	@Override
	public void setStrength(float strength) {
		this.strength = strength;
	}
	
	@Override
	public void setPosition(Vector3 position) {
		this.position.set(position);
	}
	
	@Override
	public void moveBy(Vector3 movement) {
		this.position.add(movement);
	}
	
	@Override
	public String toString() {
		String str = super.toString();
		str += "{name=" + this.name;
		str += " health=" + this.health;
		str += " strength=" + this.strength;
		str += " position=" + this.position;
		str += "}";
		return str;
	}

}
