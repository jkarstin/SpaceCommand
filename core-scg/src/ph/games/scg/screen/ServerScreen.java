package ph.games.scg.screen;

import ph.games.scg.server.Server;
import ph.games.scg.server.ServerUI;

public class ServerScreen extends BaseScreen {
	
	//TODO: General operation structure:
	//		Start Server (Initialize)
	//		Each loop, look for user input (ServerUI)
	//		Each loop, look for new clients (Connection requests)
	//		Each loop, look for messages (commands) from clients (Recieve commands)
	//		Each loop, respond to messages recieved from clients (Execute commands)
	//		Each loop, perform any broadcasts necessary for this loop (Broadcast server messages)
	
	private Server gameServer;
	private ServerUI serverUI;
	
	@Override
	protected void initialize() {
		this.gameServer = new Server(21595);
		this.serverUI = new ServerUI();
	}

	@Override
	protected void update(float dt) {
		this.serverUI.update(dt);
		this.gameServer.update(dt);
		this.serverUI.render();
	}

}
