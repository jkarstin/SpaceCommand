package ph.games.scg.component;

import java.util.ArrayList;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.math.Vector3;

public class NetEntityComponent implements Component {
	
	public String netName;
	
	public ArrayList<Vector3> queuedMovement;
	public ArrayList<Float> queuedFacing;
	public ArrayList<Float> queuedDeltaTime;
	
	public NetEntityComponent() {
		this.queuedMovement = new ArrayList<Vector3>();
		this.queuedFacing = new ArrayList<Float>();
		this.queuedDeltaTime = new ArrayList<Float>();
	}
	
}
