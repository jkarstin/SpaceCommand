package ph.games.scg._depreciated_.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Image;

import ph.games.scg._depreciated_.util.Settings;

public class CrosshairWidget extends Actor {
   
   private Image crosshairDot, crosshairInnerRing;
   
   public CrosshairWidget() {
      this.crosshairDot = new Image(
         new Texture(Gdx.files.internal("crosshair/crossHairPoint.png"))
      );
      this.crosshairInnerRing = new Image(
         new Texture(Gdx.files.internal("crosshair/crossHairInnerRing.png"))
      );
   }
   
   @Override
   public void act(float dt) {
      if (Settings.Paused) return;
   }
   
   @Override
   public void draw(Batch batch, float parentAlpha) {
      if (Settings.Paused) return;
      
      this.crosshairDot.draw(batch, parentAlpha);
      this.crosshairInnerRing.draw(batch, parentAlpha);
   }
   
   @Override
   public void setPosition(float x, float y) {
      super.setPosition(x, y);
      this.crosshairDot.setPosition(x-16f, y-16f);
      this.crosshairInnerRing.setPosition(x-16f, y-16f);
      this.crosshairInnerRing.setOrigin(
         this.crosshairInnerRing.getWidth()/2f,
         this.crosshairInnerRing.getHeight()/2f
      );
   }
   
   @Override
   public void setSize(float width, float height) {
      super.setSize(width, height);
      this.crosshairDot.setSize(width*2f, height*2f);
      this.crosshairInnerRing.setSize(width*2f, height*2f);
   }
   
}