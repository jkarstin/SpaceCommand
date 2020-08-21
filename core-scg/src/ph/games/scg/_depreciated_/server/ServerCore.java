package ph.games.scg._depreciated_.server;

import com.badlogic.gdx.Game;

import ph.games.scg.util.Assets;
import ph.games.scg.util.Debug;
import ph.games.scg.util.Debug.DEBUG_MODE;

public class ServerCore extends Game {
	
	public static final Server server = new Server(Server.SERVER_PORT);
	
	@Override
	public void create() {
		Debug.setMode(DEBUG_MODE.OFF);
		new Assets();
		this.setScreen(new ServerScreen());
	}

}
