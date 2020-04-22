package ph.games.scg;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;

import ph.games.scg.screen.MainMenuScreen;
import ph.games.scg.util.Assets;
import ph.games.scg.util.Settings;

public class Core extends Game {
	
	public static final float VIRTUAL_WIDTH = 800f;
	public static final float VIRTUAL_HEIGHT = 600f;
	
	public static Core core;
	
	public static void setActiveScreen(Screen screen) {
		//If current screen exists, hide and dispose it
	      if (Core.core.screen != null) {
	         Core.core.screen.hide();
	         Core.core.screen.dispose();
	      }
	      
	      //Set the active screen
	      Core.core.screen = screen;
	      
	      //If the screen provided exists, show and resize it
	      if (Core.core.screen != null) {
	         Core.core.screen.show();
	         Core.core.screen.resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
	      }
	}
	
	public Core() {
		Core.core = this;
	}
	
	@Override
	public void create () {
		new Assets();
		Settings.load();
		Core.setActiveScreen(new MainMenuScreen());
	}

	@Override
	public void render () {
		Gdx.gl.glClearColor(1, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
		if (this.screen != null) this.screen.render(Gdx.graphics.getDeltaTime());
	}
	
	@Override
	public void dispose () {
		Settings.save();
		Assets.dispose();
	}
}
