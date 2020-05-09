package ph.games.scg.server;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;

import ph.games.scg.game.Core;

public class ServerLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.width  = (int)Core.VIRTUAL_WIDTH;
		config.height = (int)Core.VIRTUAL_HEIGHT;
		new LwjglApplication(new ServerCore(), config);
	}
}
