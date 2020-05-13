package ph.games.scg.screen;

import com.badlogic.gdx.Gdx;

import ph.games.scg.game.GameWorld;
import ph.games.scg.server.Client;
import ph.games.scg.server.Server;
import ph.games.scg.ui.GameUI;
import ph.games.scg.util.Settings;

public class GameScreen extends BaseScreen {
	
	private GameUI gameUI;
	private GameWorld gameWorld;
	private Client client;

	@Override
	public void initialize() {
		this.client = new Client(Server.SERVER_IP, Server.SERVER_PORT);
		this.gameUI = new GameUI(this.client);
		this.gameWorld = new GameWorld(this.gameUI, this.client);
		Settings.Paused = false;
		Gdx.input.setInputProcessor(this.gameUI.stage);
		Gdx.input.setCursorCatched(true);
	}

	@Override
	public void update(float dt) {
		//Updates
		this.gameUI.update(dt);
		this.client.update(dt);
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
		this.client.dispose();
		super.dispose();
	}

}