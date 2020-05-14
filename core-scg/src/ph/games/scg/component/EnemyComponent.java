package ph.games.scg.component;

import com.badlogic.ashley.core.Component;

public class EnemyComponent implements Component {
   
   public enum STATE {
      IDLE,
      RUNNING
   }
   
   public STATE state = STATE.IDLE;
   
   public EnemyComponent(STATE state) {
      this.state = state;
   }
   
}