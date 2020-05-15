package ph.games.scg.server.command;

import java.net.Socket;

public class RollCallCommand extends Command {
	
	String username;
	
	public RollCallCommand(Socket sock, String username) {
		super(CMD_TYP.ROLLCALL, sock);
		
		this.username = username;
	}
	
	public String getUsername() {
		return this.username;
	}

	@Override
	public String toCommandString() {
		return "\\rc " + this.username;
	}
	
	@Override
	public String toString() {
		String str = super.toString() + "{";
		str += "username=" + this.username;
		str += "}";
		return str;
	}
	
}
