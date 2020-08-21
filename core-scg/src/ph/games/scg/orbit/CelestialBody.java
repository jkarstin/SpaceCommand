package ph.games.scg.orbit;

public class CelestialBody {
	
	private float mass;
	private float radius;
	private Atmosphere atmosphere;
	private float baseTemperature;
	
	public CelestialBody() {
		this.mass = 5000000f;
		this.radius = 1000f;
		this.atmosphere = new Atmosphere();
		this.baseTemperature = 20f;
	}
	
	public float getMass() {
		return this.mass;
	}
	
	public float getRadius() {
		return this.radius;
	}
	
	public Atmosphere getAtmosphere() {
		return this.atmosphere;
	}
	
	public float getBaseTemperature() {
		return this.baseTemperature;
	}
	
}
