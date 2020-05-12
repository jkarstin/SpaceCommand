package ph.games.scg.screen;

import ph.games.scg.server.Client;
import ph.games.scg.server.Server;

public class ClientTestScreen extends BaseScreen {
	
	private Client client;
	
	@Override
	protected void initialize() {
		this.client = new Client(Server.SERVER_IP, Server.SERVER_PORT);
	}

	@Override
	protected void update(float dt) {
		this.client.update(dt);
	}
	
	@Override
	public void dispose() {
		this.client.dispose();
	}

}
