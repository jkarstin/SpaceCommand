/**********************************************
 * PlayerComponent.java
 * 
 * Added to an Entity to allow them to move in response to Player input
 * [!!!] Warning! Do not add to more than one Entity per GameCore instance. [!!!]
 * 
 * J Karstin Neill    05.24.2020
 **********************************************/

package ph.games.scg._depreciated_.component;

import com.badlogic.ashley.core.Component;

public class PlayerComponent implements Component {

	public static final float moveSpeed = 10f;
	public static final float turnSpeed = 1f;
	public static final float jumpSpeed = 10f;

	public static int score;

	public PlayerComponent() {
		PlayerComponent.score = 0;
	}

}