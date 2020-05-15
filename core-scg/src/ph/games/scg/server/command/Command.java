package ph.games.scg.server.command;

import java.net.Socket;

import ph.games.scg.util.ILoggable;

//TODO: Command should be the class handling translation of command strings to command objects internally

public abstract class Command implements ILoggable {

	public static enum CMD_TYP implements ILoggable {
		ROLLCALL,
		LOGIN,
		VERSION,
		LOGOUT,
		SAY,
		TELL,
		MOVE,
		SPAWN,
		KILL,
		ATTACK,
		DAMAGE
	}
	
	private CMD_TYP type;
	private Socket sock;

	public Command(CMD_TYP type, Socket sock) {
		this.type = type;
		this.sock = sock;
	}
	
	public abstract String toCommandString();
	
	public CMD_TYP getType() {
		return this.type;
	}
	
	public Socket getSock() {
		return this.sock;
	}
	
	@Override
	public String toString() {
		return "COMMAND_" + this.type;
	}
	
	
	//TODO: Revisit this when you have time
//	//Takes in String version of a command and attempts to generate the relevant command
//	public static Command parseCommand(Socket sock, String commandString) {
//		Command cmd = null;
//		
//		byte[] bytes = commandString.getBytes();
//		
//		if (bytes == null || bytes.length == 0 || (char)(bytes[0]) != '\\') return null;
//		
//		String call = "";
//		boolean record = false;
//		
//		for(int b = 0; b < bytes.length; b++) {
//			char c = (char)(bytes[b]);
//			
//			switch (c) {
//			case '\\':
//				if (b > 0) return null;
//				else record = true;
//				break;
//			case '\n':
//			case '\t':
//			case ' ':
//				record = false;
//				break;
//			default:
//				if (record) call += c;
//				break;
//			}
//			
//			if (!record) break;
//		}
//		
//		switch (call) {
//		case "rc":
//			cmd = new RollCallCommand(commandString);
//			break;
//		case "login":
//			break;
//		case "version":
//			break;
//		case "logout":
//			break;
//		case "say":
//			break;
//		case "tell":
//			break;
//		case "move":
//			break;
//		case "spawn":
//			break;
//		case "kill":
//			break;
//		default:
//			return null;
//		}
//		
//		return cmd;
//	}

}
