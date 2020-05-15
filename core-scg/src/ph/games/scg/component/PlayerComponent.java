package ph.games.scg.component;

import com.badlogic.ashley.core.Component;

public class PlayerComponent implements Component {
   
   public float health;
   public static int score;
   
   public PlayerComponent() {
      this.health = 100f;
      PlayerComponent.score = 0;
   }
   
}