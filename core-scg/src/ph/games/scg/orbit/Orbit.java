package ph.games.scg.orbit;

public class Orbit {
	
	private CelestialBody orbitedBody;
	
	private float semiMajorAxis;
	private float eccentricity;
	private float inclination;
	private float argumentOfPeriapsis;
	private float timeOfPeriapsisPassage;
	private float longitudeOfAscendingNode;
	
	public Orbit() {
		this.orbitedBody = null;
		
		this.semiMajorAxis = 0f;
		this.eccentricity = 0f;
		this.inclination = 0f;
		this.argumentOfPeriapsis = 0f;
		this.timeOfPeriapsisPassage = 0f;
		this.longitudeOfAscendingNode = 0f;
	}
	
	public void setOrbitedBody(CelestialBody orbitedBody) {
		this.orbitedBody = orbitedBody;
	}
	
	public CelestialBody getOrbitedBody() {
		return this.orbitedBody;
	}
	
	public float getSemiMajorAxis() {
		return this.semiMajorAxis;
	}
	
	public float getEccentricity() {
		return this.eccentricity;
	}
	
	public float getInclination() {
		return this.inclination;
	}
	
	public float getArgumentOfPeriapsis() {
		return this.argumentOfPeriapsis;
	}
	
	public float getTimeOfPariapsisPasage() {
		return this.timeOfPeriapsisPassage;
	}
	
	public float getLongitudeOfAscendingNode() {
		return this.longitudeOfAscendingNode;
	}
	
}
