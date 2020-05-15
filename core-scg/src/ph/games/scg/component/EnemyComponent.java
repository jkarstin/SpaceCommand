package ph.games.scg.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.Entity;

public class EnemyComponent implements Component {
   
   public enum STATE {
      IDLE,
      RUNNING
   }
   
   public STATE state;
   public Entity target;
   public float reach = 5f;
   
   public EnemyComponent(STATE state) {
      this.state = state;
   }
   
}