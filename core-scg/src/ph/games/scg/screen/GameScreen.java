package ph.games.scg.screen;

import com.badlogic.gdx.Gdx;

import ph.games.scg.game.GameCore;
import ph.games.scg.game.GameWorld;
import ph.games.scg.ui.GameUI;
import ph.games.scg.util.Settings;

public class GameScreen extends BaseScreen {
	
	private GameUI gameUI;
	private GameWorld gameWorld;

	@Override
	public void initialize() {
		this.gameUI = new GameUI();
		this.gameWorld = new GameWorld(this.gameUI);
		GameCore.client.setGameWorld(this.gameWorld);
		Settings.Paused = false;
		Gdx.input.setInputProcessor(this.gameUI.stage);
		Gdx.input.setCursorCatched(true);
	}

	@Override
	public void update(float dt) {
		//Updates
		this.gameUI.update(dt);
		GameCore.client.update(dt);
		//Draw
		this.gameWorld.render(dt);
		this.gameUI.render();
	}

	@Override
	public void resize(int width, int height) {
		super.resize(width, height);
		this.gameUI.resize(width, height);
		this.gameWorld.resize(width, height);
	}

	@Override
	public void dispose() {
		this.gameUI.dispose();
		this.gameWorld.dispose();
		super.dispose();
	}

}