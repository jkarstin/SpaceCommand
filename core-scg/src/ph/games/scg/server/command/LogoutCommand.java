package ph.games.scg.server.command;

import java.net.Socket;

import ph.games.scg.util.Debug;

public class LogoutCommand extends Command {
	
	private String username;
	
	public LogoutCommand(Socket sock, String username) {
		super(CMD_TYP.LOGOUT, sock);
		
		this.username = username;
		
		Debug.logv("New Command: " + this);
	}
	public LogoutCommand(String username) { this(null, username); }
	public LogoutCommand(Socket sock) { this(sock, null); }
	
	public String getUsername() {
		return this.username;
	}
	
	@Override
	public String toString() {
		return super.toString() + "_LOGOUT{sock=" + this.getSock() + "}";
	}
}
