package ph.games.scg.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

import ph.games.scg.game.GameCore;
import ph.games.scg.util.Assets;

public class GameOverWidget extends Actor {

	private Stage stage;
	private Image image;
	private TextButton /*retryButton,*/ quitButton;

	public GameOverWidget(Stage s) {
		this.stage = s;
		setWidgets();
		setListeners();
	}

	private void setWidgets() {
		this.image = new Image(new Texture(Gdx.files.internal("gameOver.png")));
//		this.retryButton = new TextButton("Retry", Assets.skin);
		this.quitButton = new TextButton("Quit", Assets.skin);
	}

	private void setListeners() {
//		this.retryButton.addListener(
//				new ClickListener() {
//
//					@Override
//					public void clicked(InputEvent event, float x, float y) {
//						GameCore.setActiveScreen(new GameScreen());
//					}
//
//				}
//				);
		this.quitButton.addListener(
				new ClickListener() {

					@Override
					public void clicked(InputEvent event, float x, float y) {
						GameCore.client.logout();
						Gdx.app.exit();
					}

				}
				);
	}

	@Override
	public void setPosition(float x, float y) {
		super.setPosition(0f, 0f);
		this.image.setPosition(x, y + 32f);
//		this.retryButton.setPosition(x - 45f, y - 96f);
		this.quitButton.setPosition(x + 45f, y - 96f);
	}

	@Override
	public void setSize(float width, float height) {
		super.setSize(GameCore.VIRTUAL_WIDTH, GameCore.VIRTUAL_HEIGHT);
		this.image.setSize(width, height);
//		this.retryButton.setSize(width / 2.5f, height / 2f);
		this.quitButton.setSize(width / 2.5f, height / 2f);
	}

	public void gameOver() {
		this.stage.addActor(this.image);
//		this.stage.addActor(this.retryButton);
		this.stage.addActor(this.quitButton);
		this.stage.unfocus(this.stage.getKeyboardFocus());
		Gdx.input.setCursorCatched(false);
	}

}