package ph.games.scg.server.command;

import java.net.Socket;

public class DamageCommand extends Command {
	
	private String name;
	private String target;
	private float amount;
	
	public DamageCommand(Socket sock, String name, String target, float amount) {
		super(CMD_TYP.DAMAGE, sock);
		
		this.name = name;
		this.target = target;
		this.amount = amount;
	}
	
	public String getName() {
		return this.name;
	}
	
	public String getTarget() {
		return this.target;
	}
	
	public float getAmount() {
		return this.amount;
	}

	@Override
	public String toCommandString() {
		return "\\damage " + this.name + " " + this.target + " " + this.amount;
	}
	
	@Override
	public String toString() {
		String str = super.toString() + "{";
		str += "name=" + this.name;
		str += " target=" + this.target;
		str += " amount=" + this.amount;
		str += "}";
		return str;
	}
	
}
