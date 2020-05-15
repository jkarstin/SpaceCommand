package ph.games.scg.server.command;

import java.net.Socket;

import ph.games.scg.util.Debug;

public class LoginCommand extends Command {
	
	private String username;
	private String password;
	
	public LoginCommand(Socket sock, String username, String password) {
		super(CMD_TYP.LOGIN, sock);
		
		this.username = username;
		this.password = password;
		
		Debug.logv("New Command: " + this);
	}
	public LoginCommand(String username, String password) { this(null, username, password); }
	public LoginCommand(String username) {
		this(null, username, null);
	}
	
	public String getUsername() {
		return this.username;
	}
	
	public String getPassword() {
		return this.password;
	}
	
	@Override
	public String toCommandString() {
		String str = "\\login " + this.username;
		if (this.password != null) str += " " + this.password;
		return str;
	}
	
	@Override
	public String toString() {
		return super.toString() + "{sock=" + this.getSock() + " username=" + this.username + " password=" + this.password + "}";
	}
}
