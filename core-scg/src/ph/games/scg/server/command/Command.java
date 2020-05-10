package ph.games.scg.server.command;

import java.net.Socket;

import ph.games.scg.util.ILoggable;

public abstract class Command implements ILoggable {

	public static enum CMD_TYP implements ILoggable {
		LOGIN,
		VERSION,
		LOGOUT,
		SAY,
		TELL
	}
	
	private CMD_TYP type;
	private Socket sock;
	
	public Command(CMD_TYP type, Socket sock) {
		this.type = type;
		this.sock = sock;
	}
	
	public CMD_TYP getType() {
		return this.type;
	}
	
	public Socket getSock() {
		return this.sock;
	}
	
	@Override
	public String toString() {
		return "Command";
	}

}
