package ph.games.scg.server.command;

import java.net.Socket;

import ph.games.scg.util.Debug;

public class LogoutCommand extends Command {

	public LogoutCommand(Socket sock) {
		super(CMD_TYP.LOGOUT, sock);
		
		Debug.logv("New Command: " + this);
	}
	
	@Override
	public String toString() {
		return super.toString() + "_LOGOUT{sock=" + this.getSock() + "}";
	}
}
