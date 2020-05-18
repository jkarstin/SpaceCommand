package ph.games.scg.server;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;

import ph.games.scg.screen.BaseScreen;

public class ServerScreen extends BaseScreen {
	
	private ServerUI serverUI;
	
	@Override
	protected void initialize() {
		this.serverUI = new ServerUI();
		ServerCore.server.open();
	}

	@Override
	protected void update(float dt) {
		ServerCore.server.update(dt);
		this.serverUI.update(dt);
		
		Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		
		this.serverUI.render();
	}
	
	@Override
	public void dispose() {
		ServerCore.server.close();
	}

}
