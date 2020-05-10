package ph.games.scg.server;

import com.badlogic.gdx.Game;

import ph.games.scg.screen.ClientTestScreen;
import ph.games.scg.util.Assets;
import ph.games.scg.util.Debug;
import ph.games.scg.util.Debug.DEBUG_MODE;

public class ClientTest extends Game {
		
	@Override
	public void create() {
		Debug.setMode(DEBUG_MODE.ON_VERBOSE);
		new Assets();
		this.setScreen(new ClientTestScreen());
	}

}
