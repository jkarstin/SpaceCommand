package ph.games.scg.orbit;

public class Atmosphere {
	
	private float height;
	private float density;
	
	public Atmosphere() {
		this.height = 100000f;
		this.density = 0.5f;
	}
	
	public float getHeight() {
		return this.height;
	}
	
	public float getDensity() {
		return this.density;
	}
	
}
