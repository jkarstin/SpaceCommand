package ph.games.scg._depreciated_.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar;
import com.badlogic.gdx.utils.Align;

import ph.games.scg.util.Assets;

public class HealthWidget extends Actor {
   
   private ProgressBar healthBar;
   private ProgressBar.ProgressBarStyle progressBarStyle;
   private Label label;
   
   public HealthWidget() {
      this.progressBarStyle = new ProgressBar.ProgressBarStyle(
         Assets.skin.newDrawable("white", Color.RED),
         Assets.skin.newDrawable("white", Color.GREEN)
      );
      this.progressBarStyle.knobBefore = this.progressBarStyle.knob;
      this.healthBar = new ProgressBar(0f, 100f, 1f, false, this.progressBarStyle);
      this.label = new Label("Health", Assets.skin);
      this.label.setAlignment(Align.center);
   }
   
   @Override
   public void act(float dt) {
      this.healthBar.act(dt);
      this.label.act(dt);
   }
   
   @Override
   public void draw(Batch batch, float parentAlpha) {
      this.healthBar.draw(batch, parentAlpha);
      this.label.draw(batch, parentAlpha);
   }
   
   @Override
   public void setPosition(float x, float y) {
      super.setPosition(x, y);
      this.healthBar.setPosition(x, y);
      this.label.setPosition(x, y);
   }
   
   @Override
   public void setSize(float width, float height) {
      super.setSize(width, height);
      this.healthBar.setSize(width, height);
      this.progressBarStyle.background.setMinWidth(width);
      this.progressBarStyle.background.setMinHeight(height);
      this.progressBarStyle.knob.setMinWidth(this.healthBar.getValue());
      this.progressBarStyle.knob.setMinHeight(height);
      this.label.setSize(width, height);
   }
   
   public void setValue(float value) {
      this.healthBar.setValue(value);
   }
   
}