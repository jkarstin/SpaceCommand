package ph.games.scg.server.command;

import java.net.Socket;

public class VersionCommand extends Command {

	public VersionCommand(Socket sock) {
		super(CMD_TYP.VERSION, sock);
	}

}
