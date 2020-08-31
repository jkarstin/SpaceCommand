package ph.games.scg._depreciated_.server;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;

import ph.games.scg._depreciated_.game.GameCore;

public class ServerLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.width  = (int)GameCore.VIRTUAL_WIDTH;
		config.height = (int)GameCore.VIRTUAL_HEIGHT;
		new LwjglApplication(new ServerCore(), config);
	}
}
