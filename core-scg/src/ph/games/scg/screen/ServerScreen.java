package ph.games.scg.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;

import ph.games.scg.server.Server;
import ph.games.scg.server.ServerUI;

public class ServerScreen extends BaseScreen {
	
	private Server gameServer;
	private ServerUI serverUI;
	
	@Override
	protected void initialize() {
		this.serverUI = new ServerUI();
		this.gameServer = new Server(21595, this.serverUI);
		this.gameServer.open();
	}

	@Override
	protected void update(float dt) {
		this.gameServer.update(dt);
		this.serverUI.update(dt);
		
		Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		
		this.serverUI.render();
	}
	
	@Override
	public void dispose() {
		this.gameServer.close();
	}

}
