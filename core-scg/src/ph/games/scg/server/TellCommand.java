package ph.games.scg.server;

import java.net.Socket;

import ph.games.scg.server.command.Command;

public class TellCommand extends Command {
	
	private String message;
	private String toUsername;
	
	public TellCommand(Socket sock, String message, String toUsername) {
		super(CMD_TYP.TELL, sock);
		this.message = message;
		this.toUsername = toUsername;
	}
	
	public String getMessage() {
		return this.message;
	}
	
	public String getToUsername() {
		return this.toUsername;
	}

}
