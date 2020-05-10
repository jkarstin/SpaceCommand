package ph.games.scg.server.command;

import java.net.Socket;

import ph.games.scg.util.Debug;

public class TellCommand extends Command {
	
	private String toUsername;
	private String message;
	
	public TellCommand(Socket sock, String toUsername, String message) {
		super(CMD_TYP.TELL, sock);
		this.toUsername = toUsername;
		this.message = message;
		
		Debug.logv("New Command: " + this);
	}
	
	public String getToUsername() {
		return this.toUsername;
	}
	
	public String getMessage() {
		return this.message;
	}
	
	@Override
	public String toString() {
		return super.toString() + "_TELL{sock=" + this.getSock() + " toUsername=" + this.toUsername + " message=" + this.message + "}";
	}
	
}
