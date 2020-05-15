package ph.games.scg.server.command;

import java.net.Socket;

public class KillCommand extends Command {
	
	//Command format: \kill name
	
	private String name;
	
	public KillCommand(Socket sock, String name) {
		super(CMD_TYP.KILL, sock);
		
		this.name = name;
	}
	
	//TODO: Revisit this idea when you have time
//	public KillCommand(String commandString) {
//		super(CMD_TYP.KILL, null);
//		
//		
//	}
	
	public String getName() {
		return this.name;
	}
	
	@Override
	public String toCommandString() {
		return "\\kill " + this.name;
	}
	
	@Override
	public String toString() {
		String str = super.toString() + "{";
		str += "name=" + this.name;
		str += "}";
		return str;
	}

}
