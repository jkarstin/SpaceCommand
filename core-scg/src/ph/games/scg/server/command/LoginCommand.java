package ph.games.scg.server.command;

import java.net.Socket;

public class LoginCommand extends Command {
	
	private String username;
	private String password;
	
	public LoginCommand(Socket sock, String username, String password) {
		super(CMD_TYP.LOGIN, sock);
		
		this.username = username;
		this.password = password;
	}
	
	public String getUsername() {
		return this.username;
	}
	
	public String getPassword() {
		return this.password;
	}
}
