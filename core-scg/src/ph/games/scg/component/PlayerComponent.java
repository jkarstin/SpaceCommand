package ph.games.scg.component;

import com.badlogic.ashley.core.Component;

public class PlayerComponent implements Component {
   
   public float energy;
   public float oxygen;
   public float health;
   public static int score;
   
   public PlayerComponent() {
      this.energy = 100f;
      this.oxygen = 100f;
      this.health = 100f;
      PlayerComponent.score = 0;
   }
   
}