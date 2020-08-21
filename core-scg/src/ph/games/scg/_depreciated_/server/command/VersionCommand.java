package ph.games.scg._depreciated_.server.command;

import java.net.Socket;

import ph.games.scg.util.Debug;

public class VersionCommand extends Command {

	public VersionCommand(Socket sock) {
		super(CMD_TYP.VERSION, sock);
		
		Debug.logv("New Command: " + this);
	}
	
	@Override
	public String toCommandString() {
		return "\\version";
	}
	
	@Override
	public String toString() {
		return super.toString() + "{sock=" + this.getSock() + "}";
	}
	
}
