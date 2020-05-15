package ph.games.scg.server.command;

import java.net.Socket;

public class AttackCommand extends Command {
	
	private String name;
	private String target;
	
	public AttackCommand(Socket sock, String name, String target) {
		super(CMD_TYP.ATTACK, sock);
		
		this.name = name;
		this.target = target;
	}
	
	public String getName() {
		return this.name;
	}
	
	public String getTarget() {
		return this.target;
	}
	
	@Override
	public String toCommandString() {
		return "\\attack " + this.name + " " + this.target;
	}
	
	@Override
	public String toString() {
		String str = super.toString() + "{";
		str += "name=" + this.name;
		str += " target=" + this.target;
		str += "}";
		return str;
	}
	
}
