package ph.games.scg.ui;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.FitViewport;

import ph.games.scg.game.Core;

public class GameUI {
   
   public Stage stage;
   public HealthWidget healthWidget;
   private PauseWidget pauseWidget;
   private CrosshairWidget crosshairWidget;
   public GameOverWidget gameOverWidget;
   
   public GameUI() {
      this.stage = new Stage(new FitViewport(Core.VIRTUAL_WIDTH, Core.VIRTUAL_HEIGHT));
      this.setWidgets();
      this.configureWidgets();
   }
   
   public void setWidgets() {
      this.healthWidget = new HealthWidget();
      this.pauseWidget = new PauseWidget(this.stage);
      this.crosshairWidget = new CrosshairWidget();
      this.gameOverWidget = new GameOverWidget(this.stage);
   }
   
   public void configureWidgets() {
      this.healthWidget.setSize(140f, 25f);
      this.healthWidget.setPosition(
         (Core.VIRTUAL_WIDTH-this.healthWidget.getWidth())/2f,
         0f
      );
      this.pauseWidget.setSize(64f, 64f);
      this.pauseWidget.setPosition(
         Core.VIRTUAL_WIDTH-this.pauseWidget.getWidth(),
         Core.VIRTUAL_HEIGHT-this.pauseWidget.getHeight()
      );
      this.gameOverWidget.setSize(280f, 100f);
      this.gameOverWidget.setPosition(
         (Core.VIRTUAL_WIDTH-280f)/2f,
         Core.VIRTUAL_HEIGHT/2f
      );
      this.crosshairWidget.setSize(32f, 32f);
      this.crosshairWidget.setPosition(
         (Core.VIRTUAL_WIDTH-32f)/2f,
         (Core.VIRTUAL_HEIGHT-32f)/2f
      );
      
      this.stage.addActor(this.healthWidget);
      this.stage.addActor(this.crosshairWidget);
      this.stage.setKeyboardFocus(this.pauseWidget);
   }
   
   public void revertKeyboardFocus() {
	   this.stage.setKeyboardFocus(this.pauseWidget);
   }
   
   public void changeKeyboardFocus(Actor actor) {
		this.stage.setKeyboardFocus(actor);
	}
   
   public void update(float dt) {
      this.stage.act(dt);
   }
   
   public void render() {
      this.stage.draw();
   }
   
   public void resize(int width, int height) {
      this.stage.getViewport().update(width, height);
   }
   
   public void dispose() {
      this.stage.dispose();
   }
   
}