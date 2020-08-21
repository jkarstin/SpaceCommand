package ph.games.scg._depreciated_.ui;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;

import ph.games.scg._depreciated_.component.PlayerComponent;
import ph.games.scg.util.Assets;

public class ScoreWidget extends Actor {
   
   private Label label;
   
   public ScoreWidget() {
      this.label = new Label("", Assets.skin);
   }
   
   @Override
   public void act(float dt) {
      this.label.act(dt);
      this.label.setText("Score : " + PlayerComponent.score);
   }
   
   @Override
   public void draw(Batch batch, float parentAlpha) {
      this.label.draw(batch, parentAlpha);
   }
   
   @Override
   public void setPosition(float x, float y) {
      super.setPosition(x, y);
      this.label.setPosition(x, y);
   }
   
   @Override
   public void setSize(float width, float height) {
      super.setSize(width, height);
      this.label.setSize(width, height);
   }
   
}