package ph.games.scg.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;

import com.badlogic.gdx.math.Vector3;
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
	private static final float SEND_FREQUENCY = 0.33f;
	
	private User user;
	private Socket sock;
	private int soTimeout;
	private byte[] buff;
	private ArrayList<String> outboundMessages;
	private ArrayList<String> messages;
	private String segment;
	private boolean opened;
	
	private GameWorld gameWorld;
	private float timer;
	private ArrayList<Command> outgoingCommands;
	private ArrayList<Command> commandsFromServer;
	
	public Client(String serverIP, int serverPort, int timeout) {
		this.gameWorld = null;
		
		this.user = null;
		this.soTimeout = timeout;
		this.outboundMessages = new ArrayList<String>();
		this.messages = new ArrayList<String>();
		this.buff = new byte[BYTE_BUFFER_SIZE];
		this.segment = "";
		this.opened = false;
		
		this.timer = 0f;
		this.outgoingCommands = new ArrayList<Command>();
		this.commandsFromServer = new ArrayList<Command>();
		
		this.open(serverIP, serverPort);
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
	
	public void queueMessage(String message) {
		String[] tokens = message.split(" ");
		if (tokens != null && tokens.length > 0) {
			switch (tokens[0]) {
			case "\\login":
				this.login(tokens[1], tokens[2]);
				break;
			case "\\logout":
				this.logout();
				break;
			default:
				this.outboundMessages.add(message);
				break;
			}
		}
		else this.outboundMessages.add(message);
	}
	
	public void login(String username, String password) {
		if (!this.isOpen()) return;
		
		this.user = new User(username, password);
		this.outgoingCommands.add(new LoginCommand(username, password));
	}
	
	public void logout() {
		if (!this.isOpen()) return;
		
		this.user = null;
		this.outgoingCommands.add(new LogoutCommand());
	}
	
	public void move(Vector3 movement, float facing, float dt) {
		if (!this.isOpen()) return;
		
		if (this.user != null) {
			this.outgoingCommands.add(new MoveCommand(this.user.getUsername(), movement, facing, dt));
		}
	}
	
	public void update(float dt) {
		this.timer += dt;
		if (this.timer >= SEND_FREQUENCY) {
			this.sendCommands();
			this.timer -= SEND_FREQUENCY;
		}
		this.read();
		this.executeServerCommands();
		this.displayMessages();
	}
	
	private boolean isOpen() {
		return this.opened;
	}
	
	private void sendCommands() {
		
		ArrayList<MoveCommand> condensedMoves = new ArrayList<MoveCommand>();
		
		for (Command command : this.outgoingCommands) {
			switch (command.getType()) {
			case MOVE:
				MoveCommand movecmd = (MoveCommand)command;
				String username = movecmd.getName();
				boolean match = false;
				for (MoveCommand mc : condensedMoves) {
					if (mc.getName().equals(username)) {
						mc.addMoveCommand(movecmd);
						match = true;
						break;
					}
				}
				if (!match) condensedMoves.add(movecmd);
				break;
			default:
				this.write(command.toCommandString());
				break;
			}
		}
		
		for (MoveCommand cmd : condensedMoves) {
			this.write(cmd.toCommandString());
		}
		
		this.outgoingCommands.clear();
		
		//Send outbound messages
		this.write(this.outboundMessages);
		this.outboundMessages.clear();
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