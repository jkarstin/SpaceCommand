package ph.games.scg.screen;

import ph.games.scg.server.Server;
import ph.games.scg.server.ServerUI;

public class ServerScreen extends BaseScreen {
	
	private Server gameServer;
	private ServerUI serverUI;
	
	@Override
	protected void initialize() {
		this.serverUI = new ServerUI();
		this.gameServer = new Server(21595);
		this.gameServer.open();
	}

	@Override
	protected void update(float dt) {
		this.serverUI.update(dt);
		this.gameServer.update(dt);
		this.serverUI.render();
	}
	
	@Override
	public void dispose() {
		this.gameServer.close();
	}

}
