package ph.games.scg.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.math.Vector3;
//See <http://javadox.com/com.badlogicgames.gdx/gdx-bullet/1.3.1/overview-summary.html> for API info
import com.badlogic.gdx.physics.bullet.collision.*;
import com.badlogic.gdx.physics.bullet.dynamics.*;

public class CharacterComponent implements Component {
   
   public btConvexShape ghostShape;
   public btPairCachingGhostObject ghostObject;
   public btKinematicCharacterController characterController;
   
   public Vector3 characterDirection = new Vector3();
   public Vector3 walkDirection = new Vector3();
   
   public void dispose() {
      this.characterController.dispose();
      this.ghostObject.dispose();
      this.ghostShape.dispose();
   }
   
}