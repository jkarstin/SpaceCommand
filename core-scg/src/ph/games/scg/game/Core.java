package ph.games.scg.game;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;

import ph.games.scg.screen.StartScreen;
import ph.games.scg.util.Assets;

public class Core extends Game {
	
	public static final float VIRTUAL_WIDTH = 800f;
	public static final float VIRTUAL_HEIGHT = 600f;
	
	public static Core core;
	
	public static void setActiveScreen(Screen screen) {
	      Core.core.screen = screen;
	}
	
	public Core() {
		Core.core = this;
	}
	
	@Override
	public void create () {
		new Assets();
		Core.setActiveScreen(new StartScreen());
	}

	@Override
	public void render () {
		Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
		if (this.screen != null) this.screen.render(Gdx.graphics.getDeltaTime());
	}
	
	@Override
	public void dispose () { }
}
