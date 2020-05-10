package ph.games.scg.server.command;

import java.net.Socket;

public class SayCommand extends Command {
	
	private String message;
	
	public SayCommand(Socket sock, String message) {
		super(CMD_TYP.SAY, sock);
		this.message = message;
	}
	
	public String getMessage() {
		return this.message;
	}
	
}
