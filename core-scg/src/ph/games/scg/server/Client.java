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
import ph.games.scg.server.command.KillCommand;
import ph.games.scg.server.command.LoginCommand;
import ph.games.scg.server.command.LogoutCommand;
import ph.games.scg.server.command.MoveCommand;
import ph.games.scg.server.command.RollCallCommand;
import ph.games.scg.server.command.SpawnCommand;
import ph.games.scg.ui.ChatWidget;
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
	private ArrayList<String> inboundMessages;
	private String segment;
	private boolean opened;
	
	private GameWorld gameWorld;
	private ChatWidget chatWidget;
	private float timer;
	private ArrayList<Command> outgoingCommands;
	private ArrayList<Command> commandsFromServer;
	
	public Client(String serverIP, int serverPort, int timeout) {
		this.gameWorld = null;
		this.chatWidget = null;
		
		this.user = null;
		this.soTimeout = timeout;
		this.outboundMessages = new ArrayList<String>();
		this.inboundMessages = new ArrayList<String>();
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
	
	public void setChatWidget(ChatWidget chatWidget) {
		this.chatWidget = chatWidget;
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
	
	//Processes chat messages looking for @username references; converts messages to \say or \tell commands based on results
	public void queueMessage(String message) {
		//Make new username ArrayList
		ArrayList<String> toUsers = new ArrayList<String>();
		
		//Get message as array of bytes
		byte[] messageBytes = message.getBytes();
		//If we have message content, look for @username instances
		if (messageBytes.length > 0) {
			//Set container for username candidate to empty
			String tmpToUser = "";
			//Set recording flag to false
			boolean recording = false;
			
			//Loop through message byte characters
			for (byte b : messageBytes) {
				//Cast byte as character
				char c = (char)b;
				
				//Check state of character
				switch (c) {
				//If an @ character is found, following characters make up username
				case '@':
					//If we were already recording, store currently recorded username and start fresh
					if (recording) {
						//Add candidate username to saved usernames
						toUsers.add(tmpToUser);
						//Reset username container
						tmpToUser = "";
					}
					//Make sure to set the recording flag if it isn't already
					if (!recording) recording = true;
					//Done processing this character
					break;
				//If white space is found, end any current recording
				case '\t':
				case '\n':
				case ' ':
					//If we are currently recording, save username
					if (recording) {
						//Add to candidate usernames
						toUsers.add(tmpToUser);
						//Reset username container
						tmpToUser = "";
					}
					//Stop recording
					recording = false;
					//Done processing this character
					break;
				default:
					//If we are recording, add character to current candidate username
					if (recording) {
						tmpToUser += c;
					}
					//Done processing this character
					break;
				}
			}
			//If we reached end of message before recording finished
			if (recording) {
				//Add username to candidates
				toUsers.add(tmpToUser);
				//Reset username container
				tmpToUser = "";
				//Stop recording
				recording = false;
			}
		}
		
		//If any usernames were found
		if (toUsers.size() > 0) {
			//Queue up \tell commands for each username
			for (String toUser : toUsers) {
				this.outboundMessages.add("\\tell " + toUser + " " + message);
			}
			//Clear username candidate ArrayList
			toUsers.clear();
		}
		//Otherwise, queue up a global \say command
		else this.outboundMessages.add("\\say " + message);
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
						this.inboundMessages.add(this.segment);
						count++;
						this.segment = "";
					}
					else this.segment += (char)(this.buff[b]);
				}
			}
		}
		catch (IOException e) { e.printStackTrace(); }
		
		//Catch and process any recognized commands from the Server
		ArrayList<String> deQMessages = new ArrayList<String>();
		for (String message : this.inboundMessages) {
			String[] tokens = message.split(" ");
			if (tokens != null && tokens.length > 1) switch (tokens[0]) {
			case "[SERVER]":
				switch (tokens[1]) {
				case "\\rc":
					this.commandsFromServer.add(new RollCallCommand(this.sock, null));
					deQMessages.add(message);
					break;
				
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
		for (String message : deQMessages) this.inboundMessages.remove(message);
		
		return count;
	}
	
	//TODO: Carry out commands delivered by Server to update world state
	private void executeServerCommands() {
		if (this.gameWorld == null || this.user == null) return;
		
		for (Command command : this.commandsFromServer) {
			Debug.log("Command From Server: " + command);
			
			
			switch (command.getType()) {
			case ROLLCALL:
				//Relay roll call command back to server to confirm
				this.outgoingCommands.add(command);
				
				break;
			
			case LOGIN:
				LoginCommand logincmd = (LoginCommand)command;
				
				if (!this.user.getUsername().equals(logincmd.getUsername())) {
					//Add new UserEntity to GameWorld
					this.gameWorld.addUserEntity(logincmd.getUsername());
				}
				
				break;

			case LOGOUT:
				LogoutCommand logoutcmd = (LogoutCommand)command;
				
				if (!this.user.getUsername().equals(logoutcmd.getUsername())) {
					//Remove UserEntity from GameWorld
					this.gameWorld.removeUserEntity(logoutcmd.getUsername());
				}
				
				break;
			
			case MOVE:
				MoveCommand movecmd = (MoveCommand)command;
				
				this.gameWorld.updateUserEntity(movecmd.getName(), movecmd.getMoveVector(), movecmd.getFacing(), movecmd.getDeltaTime());
				
				break;
			
			case SPAWN:
				SpawnCommand spawncmd = (SpawnCommand)command;
				
				this.gameWorld.spawnUserEntity(spawncmd.getName(), spawncmd.getPosition());
				
				break;
				
			case KILL:
				KillCommand killcmd = (KillCommand)command;
				
				this.gameWorld.killUserEntity(killcmd.getName());
				
				break;
				
			default:
				break;
			}
			
			
		}
		this.commandsFromServer.clear();
	}
	
	private void displayMessages() {
		for (String message : this.inboundMessages) {
			if (this.chatWidget == null) break;
			
			this.chatWidget.logText(message);
		}
		//Clear inboundMessages queue
		this.inboundMessages.clear();
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