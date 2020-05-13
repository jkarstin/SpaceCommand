package ph.games.scg.ui;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.FitViewport;

import ph.games.scg.game.Core;
import ph.games.scg.server.Client;

public class GameUI {

	public Stage stage;

	private InputWidget inputWidget;
	private ChatWidget chatWidget;
	public HealthWidget healthWidget;
	private PauseWidget pauseWidget;
	private CrosshairWidget crosshairWidget;
	public GameOverWidget gameOverWidget;

	public GameUI(Client client) {
		this.stage = new Stage(new FitViewport(Core.VIRTUAL_WIDTH, Core.VIRTUAL_HEIGHT));
		this.setWidgets(client);
		this.configureWidgets();
	}

	public void setWidgets(Client client) {
		this.chatWidget = new ChatWidget(this, client);
		this.pauseWidget = new PauseWidget(this.stage);
		this.inputWidget = new InputWidget(this.pauseWidget, this.chatWidget);
		this.gameOverWidget = new GameOverWidget(this.stage);
		this.crosshairWidget = new CrosshairWidget();
		this.healthWidget = new HealthWidget();
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
		this.chatWidget.setSize(300f, 300f);
		this.chatWidget.setPosition(0f, 0f);

		this.stage.addActor(this.healthWidget);
		this.stage.addActor(this.crosshairWidget);
		this.stage.addActor(this.chatWidget);
		this.stage.addActor(this.inputWidget);
		this.stage.setKeyboardFocus(this.inputWidget);
	}

	public void revertKeyboardFocus() {
		this.stage.setKeyboardFocus(this.inputWidget);
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

	/*
	 * package ph.games.mudmen.ui;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.FitViewport;

import ph.games.mudmen.game.Core;

public class GameUI {

   public Stage stage;
   public HealthWidget healthWidget;
   private ScoreWidget scoreWidget;
   private PauseWidget pauseWidget;
   private CrosshairWidget crosshairWidget;
   public GameOverWidget gameOverWidget;
   private ChatWidget chatWidget;
   private InputWidget inputWidget;

   public GameUI() {
      this.stage = new Stage(new FitViewport(Core.VIRTUAL_WIDTH, Core.VIRTUAL_HEIGHT));
      this.setWidgets();
      this.configureWidgets();
   }

   public void setWidgets() {
      this.healthWidget = new HealthWidget();
      this.scoreWidget = new ScoreWidget();
      this.pauseWidget = new PauseWidget(this.stage);
      this.crosshairWidget = new CrosshairWidget();
      this.gameOverWidget = new GameOverWidget(this.stage);
      this.chatWidget = new ChatWidget(this);
      this.inputWidget = new InputWidget(this.pauseWidget, this.chatWidget);
   }

   public void configureWidgets() {
      this.healthWidget.setSize(140f, 25f);
      this.healthWidget.setPosition(
         (Core.VIRTUAL_WIDTH-this.healthWidget.getWidth())/2f,
         0f
      );
      this.scoreWidget.setSize(140f, 25f);
      this.scoreWidget.setPosition(
         0f,
         Core.VIRTUAL_HEIGHT-scoreWidget.getHeight()
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
      this.chatWidget.setSize(300f, 300f);
      this.chatWidget.setPosition(0f, 0f);
      this.stage.addActor(this.healthWidget);
      this.stage.addActor(this.scoreWidget);
      this.stage.addActor(this.crosshairWidget);
      this.stage.addActor(this.chatWidget);
      this.stage.addActor(this.inputWidget);
      this.stage.setKeyboardFocus(this.inputWidget);
   }

   public void revertKeyboardFocus() {
	   this.stage.setKeyboardFocus(this.inputWidget);
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
	  this.chatWidget.closeServer();
      this.stage.dispose();
   }

}
	 */

}