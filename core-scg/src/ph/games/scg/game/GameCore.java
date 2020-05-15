package ph.games.scg.game;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;

import ph.games.scg.screen.MainMenuScreen;
import ph.games.scg.server.Client;
import ph.games.scg.server.Server;
import ph.games.scg.util.Assets;
import ph.games.scg.util.Debug;
import ph.games.scg.util.Debug.DEBUG_MODE;
import ph.games.scg.util.Settings;

public class GameCore extends Game {
	
	public static final float VIRTUAL_WIDTH = 800f;
	public static final float VIRTUAL_HEIGHT = 600f;
	
	public static GameCore core;
	public static Client client;
	
	public static void setActiveScreen(Screen screen) {
	      //Set the active screen
	      GameCore.core.screen = screen;
	}
	
	public GameCore() {
		GameCore.core = this;;
	}
	
	@Override
	public void create () {
		Debug.setMode(DEBUG_MODE.ON);
		
		GameCore.client = new Client(Server.SERVER_IP, Server.SERVER_PORT);
		new Assets();
		Settings.load();
//		Core.setActiveScreen(new GameScreen());
		GameCore.setActiveScreen(new MainMenuScreen());
	}

	@Override
	public void render () {
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
		if (this.screen != null) this.screen.render(Gdx.graphics.getDeltaTime());
	}
	
	@Override
	public void dispose () {
		GameCore.client.dispose();
		Settings.save();
		Assets.dispose();
	}
}