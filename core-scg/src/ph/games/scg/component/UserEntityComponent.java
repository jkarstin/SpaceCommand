package ph.games.scg.component;

import java.util.ArrayList;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.math.Vector3;

public class UserEntityComponent implements Component {

	public ArrayList<Vector3> queuedMovement;
	public ArrayList<Float> queuedRotation;
	public ArrayList<Float> queuedDeltaTime;

}
