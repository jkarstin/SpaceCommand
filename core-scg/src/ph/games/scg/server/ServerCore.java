package ph.games.scg.server;

import com.badlogic.gdx.Game;

import ph.games.scg.screen.ServerScreen;
import ph.games.scg.util.Assets;

public class ServerCore extends Game {
		
	@Override
	public void create() {
		new Assets();
		this.setScreen(new ServerScreen());
	}

}
