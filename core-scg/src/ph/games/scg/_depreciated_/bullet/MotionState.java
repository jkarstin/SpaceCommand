package ph.games.scg._depreciated_.bullet;

import com.badlogic.gdx.math.Matrix4;
//See <http://javadox.com/com.badlogicgames.gdx/gdx-bullet/1.3.1/overview-summary.html> for API info
import com.badlogic.gdx.physics.bullet.linearmath.btMotionState;

public class MotionState extends btMotionState {
   
   private final Matrix4 transform;
   
   public MotionState(final Matrix4 transform) {
      this.transform = transform;
   }
   
   @Override
   public void getWorldTransform(final Matrix4 worldTrans) {
      worldTrans.set(this.transform);
   }
   
   @Override
   public void setWorldTransform(final Matrix4 worldTrans) {
      this.transform.set(worldTrans);
   }
   
}