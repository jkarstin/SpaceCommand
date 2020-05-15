package ph.games.scg.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;

import ph.games.scg.game.GameCore;

public class DesktopLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.width  = (int)GameCore.VIRTUAL_WIDTH;
		config.height = (int)GameCore.VIRTUAL_HEIGHT;
		new LwjglApplication(new GameCore(), config);
	}
}
