package ph.games.scg._depreciated_.component;

import com.badlogic.ashley.core.Component;
//See <http://javadox.com/com.badlogicgames.gdx/gdx-bullet/1.3.1/overview-summary.html> for API info
import com.badlogic.gdx.physics.bullet.collision.btCollisionObject;
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody;

import ph.games.scg._depreciated_.bullet.MotionState;

public class BulletComponent implements Component {
   
   public MotionState motionState;
   public btRigidBody.btRigidBodyConstructionInfo bodyInfo;
   public btCollisionObject body;
   
}