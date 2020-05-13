package ph.games.scg.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;

import com.badlogic.gdx.utils.Disposable;

import ph.games.scg.game.GameWorld;
import ph.games.scg.server.command.Command;
import ph.games.scg.server.command.LoginCommand;
import ph.games.scg.server.command.LogoutCommand;
import ph.games.scg.server.command.MoveCommand;
import ph.games.scg.util.Debug;

public class Client implements Disposable {

	private static final int BYTE_BUFFER_SIZE = 64;
	private static final int DEFAULT_SO_TIMEOUT = 50;
	
	private User user;
	private Socket sock;
	private int soTimeout;
	private byte[] buff;
	private ArrayList<String> messages;
	private String segment;
	private boolean opened;
	
	private GameWorld gameWorld;
	private ArrayList<Command> commandsFromServer;
	
	public Client(String serverIP, int serverPort, int timeout) {
		this.gameWorld = null;
		
		this.user = null;
		this.soTimeout = timeout;
		this.messages = new ArrayList<String>();
		this.buff = new byte[BYTE_BUFFER_SIZE];
		this.segment = "";
		this.opened = false;
		
		this.commandsFromServer = new ArrayList<Command>();
		
		this.open(serverIP, serverPort);
		
		//TODO: test login capabilities
		this.login("phrongorre", "pancakes99");
		
		this.messages.add("\\version");
		this.messages.add("\\tell roger who can it be now?");
		this.messages.add("\\say Hello world!");
		this.messages.add("\\move phrongorre 10.50,0.0,8.01,0.0,1");
		this.write(this.messages);
		this.messages.clear();
	}
	public Client(String serverIP, int serverPort) { this(serverIP, serverPort, DEFAULT_SO_TIMEOUT); }
	
	public void setGameWorld(GameWorld gameWorld) {
		this.gameWorld = gameWorld;
	}
	
	//Open a new Socket at specified address and port
	private void open(String hostaddress, int port) {
		try {
			this.sock = new Socket(hostaddress, port);
			this.sock.setSoTimeout(this.soTimeout);
			this.opened = true;
			Debug.log("Successfully opened Client: " + this);
		} catch (IOException e) {
			Debug.warn("Failed to open Client: " + this);
			e.printStackTrace();
		}
	}
	
	private void login(String username, String password) {
		if (!this.isOpen()) return;
		
		this.user = new User(username, password);
		this.write("\\login " + username + " " + password);
	}
	
	private void logout() {
		if (!this.isOpen()) return;
		
		this.user = null;
		this.write("\\logout");
	}
	
	public void update(float dt) {
		this.read();
		this.executeServerCommands();
		this.displayMessages();
	}
	
	private boolean isOpen() {
		return this.opened;
	}
	
	//Attempt to read from the socket and count number of messages
	private int read() {
		if (!this.isOpen()) return -1;
		
		//Counter for number of messages received
		int count = 0;
		
		//Read in from InputStream and break into messages at '\n' occurences
		try {
			InputStream istream = this.sock.getInputStream();
			
			int len = Math.min(istream.available(), BYTE_BUFFER_SIZE);
			int num = istream.read(this.buff, 0, len);
			Debug.logv("Bytes read: " + num);
			if (num > 0) {
				char c;
				for (int b=0; b < num; b++) {
					c = (char)(this.buff[b]);
					Debug.logv("[" + b + "]\t" + c);
					if (c == '\n') {
						this.messages.add(this.segment);
						count++;
						this.segment = "";
					}
					else this.segment += (char)(this.buff[b]);
				}
			}
		}
		catch (IOException e) { e.printStackTrace(); }
		
		
		ArrayList<String> deQMessages = new ArrayList<String>();
		for (String message : this.messages) {
			String[] tokens = message.split(" ");
			if (tokens != null && tokens.length > 2) switch (tokens[0]) {
			case "[SERVER]":
				switch (tokens[1]) {
				case "\\login":
					//Login command relayed, add new UserEntity to gameWorld
					this.commandsFromServer.add(new LoginCommand(tokens[2]));
					deQMessages.add(message);
					break;
				
				case "\\logout":
					//Logout command relayed, remove UserEntity from gameWorld
					this.commandsFromServer.add(new LogoutCommand(tokens[2]));
					deQMessages.add(message);
					break;
					
				case "\\move":
					//Move command relayed, apply move to target UserEntity
					this.commandsFromServer.add(new MoveCommand(tokens[2], tokens[3]));
					deQMessages.add(message);
					break;
					
				default:
					//Leave server message alone
					break;
				}
				break;
			default:
				//Leave message alone
				break;
			}
		}
		
		//Remove messages that were flagged for removal
		for (String message : deQMessages) this.messages.remove(message);
		
		return count;
	}
	
	//TODO: Carry out commands delivered by Server to update world state
	private void executeServerCommands() {
		for (Command cmd : this.commandsFromServer) {
			Debug.log("Command From Server: " + cmd);
			
			
			switch (cmd.getType()) {
			case LOGIN:
				LoginCommand logincmd = (LoginCommand)cmd;
				if (this.user != null && !this.user.getUsername().equals(logincmd.getUsername())) {
					//Add new UserEntity to GameWorld
					this.gameWorld.addUserEntity(logincmd.getUsername());
				}
				break;
			case LOGOUT:
				//TODO: Similar to \login, we don't want to cause problems for the user to sent the request
				LogoutCommand logoutcmd = (LogoutCommand)cmd;
				if (this.user != null && !this.user.getUsername().equals(logoutcmd.getUsername())) {
					//Remove UserEntity from GameWorld
					this.gameWorld.removeUserEntity(logoutcmd.getUsername());
				}
				break;
			case MOVE:
				MoveCommand movecmd = (MoveCommand)cmd;
				this.gameWorld.updateUserEntity(movecmd.getName(), movecmd.getMoveVector(), movecmd.getFacing(), movecmd.getDeltaTime());
				break;
			default:
				break;
			}
			
			
		}
		this.commandsFromServer.clear();
	}
	
	private void displayMessages() {
		for (String message : this.messages) Debug.log(message);
		this.messages.clear();
	}
	
	//Write ArrayList<String> contents to Socket's OutputStream
	private void write(ArrayList<String> messages) {
		if (!this.isOpen()) return;
		
		if (this.sock != null) try {
			OutputStream ostream = this.sock.getOutputStream();
			for (String message : messages) {
				message += "\n";
				byte[] buff = message.getBytes();
				ostream.write(buff);
			}		
		} catch (IOException e) { e.printStackTrace(); }
	}
	
	//Write ArrayList<String> contents to Socket's OutputStream
	private void write(String message) {
		if (!this.isOpen()) return;
		
		if (this.sock != null) try {
			OutputStream ostream = this.sock.getOutputStream();
			message += "\n";
			byte[] buff = message.getBytes();
			ostream.write(buff);
		} catch (IOException e) { e.printStackTrace(); }
	}
	
	//Close Socket
	private void close() {
		if (!this.isOpen()) return;
		
		if (this.sock != null) try { this.sock.close(); }
		catch (IOException e) { e.printStackTrace(); }
	}
	
	@Override
	public String toString() {
		String str = "CLIENT{sock=" + this.sock + "}";
		return str;
	}
	
	@Override
	public void dispose() {
		this.close();
	}

}