package ph.games.scg.server.command;

import java.net.Socket;

public class LogoutCommand extends Command {

	public LogoutCommand(Socket sock) {
		super(CMD_TYP.LOGOUT, sock);
	}

}
