/*********************************************
 * Server.java
 * 
 * Manages server side connection and
 * communications for TCP/IP CVE server design.
 * 
 * J Karstin Neill    05.09.2020
 *********************************************/

package ph.games.scg.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.StringTokenizer;

import com.badlogic.ashley.core.Engine;
import com.badlogic.gdx.math.Vector3;

import ph.games.scg.server.command.AttackCommand;
import ph.games.scg.server.command.Command;
import ph.games.scg.server.command.Command.CMD_TYP;
import ph.games.scg.server.command.DamageCommand;
import ph.games.scg.server.command.KillCommand;
import ph.games.scg.server.command.LoginCommand;
import ph.games.scg.server.command.LogoutCommand;
import ph.games.scg.server.command.MoveCommand;
import ph.games.scg.server.command.PositionCommand;
import ph.games.scg.server.command.RollCallCommand;
import ph.games.scg.server.command.SayCommand;
import ph.games.scg.server.command.SpawnCommand;
import ph.games.scg.server.command.TellCommand;
import ph.games.scg.server.command.VersionCommand;
import ph.games.scg.system.BulletSystem;
import ph.games.scg.system.NetEntitySystem;
import ph.games.scg.util.Debug;
import ph.games.scg.util.ILoggable;

public class Server implements ILoggable {
	
	public static /*final*/ String SERVER_IP;// = "192.168.1.2";
	public static final int SERVER_PORT = 21595;
	private static final int SO_TIMEOUT = 50;
	private static final float ROLL_CALL_FREQUENCY = 1f;
	private static final float SEND_FREQUENCY = 2.4f;
	
	private static final int BYTE_BUFFER_SIZE = 64;
	private static final int VERSION_LO = 6;
	private static final int VERSION_HI = 0;
	
	private ServerUI serverUI;
	
	//Server socket
	private ServerSocket serverSock;
	//Server metadata
	private int port;
	private String hostname;
	private String hostaddress;
	private boolean opened;
	//Number of seconds since server was opened
	private float uptime;
	private float sendTimer;
	private float rollCallTimer;
	
	//Message reading
	private byte[] buff;
	private ArrayList<String> adminMessages;
	private ArrayList<String> messages;
	private ArrayList<SockSegment> sockSegments;
	private StringTokenizer stoker;
	
	//Registered Users ArrayList
	private ArrayList<User> registeredUsers;
	//Client ArrayList
	private ArrayList<Socket> socks;
	//UserSock ArrayList
	private ArrayList<UserSock> usersocks;
	//NetworkEntity ArrayList
	private ArrayList<NetEntity> netEntities;
	//Command Queue
	private ArrayList<Command> commandQ;
	//Direct Message Queue
	private ArrayList<UserMessage> directMessageQ;
	//Broadcast Queue
	private ArrayList<UserMessage> broadcastQ;
	//Close Queue
	private ArrayList<UserSock> closeQ;
	
	//Server-side engine for processing NetEntity state simulation
	private Engine servEngine;
	private BulletSystem bulletSystem;
	private NetEntitySystem NES;
	
	public Server(int port) {
		this.serverUI = null;
		
		this.serverSock = null;
		this.port = port;
		this.opened = false;
		this.uptime = 0f;
		this.sendTimer = 0f;
		this.rollCallTimer = 0f;
		
		this.buff = new byte[BYTE_BUFFER_SIZE];
		this.adminMessages = new ArrayList<String>();
		this.messages = new ArrayList<String>();
		this.sockSegments = new ArrayList<SockSegment>();
		this.stoker = null;
		
		//TODO: Make a more secure way of storing usernames and passwords. This is just negligence... :/
		this.registeredUsers = new ArrayList<User>();
		this.registeredUsers.add(new User("phrongorre", "pancakes99"));
		this.registeredUsers.add(new User("roger", "foneybaloney"));
		
		this.socks = new ArrayList<Socket>();
		this.usersocks = new ArrayList<UserSock>();
		this.netEntities = new ArrayList<NetEntity>();
		
		this.commandQ = new ArrayList<Command>();
		this.directMessageQ = new ArrayList<UserMessage>();
		this.broadcastQ = new ArrayList<UserMessage>();
		this.closeQ = new ArrayList<UserSock>();
		
		this.servEngine = new Engine();
		this.servEngine.addSystem(this.bulletSystem = new BulletSystem());
		this.servEngine.addSystem(new NetEntitySystem(this.bulletSystem, this));
	}
	
	public void open() {
		if (this.serverSock == null) try {
			this.serverSock = new ServerSocket(this.port);
			this.serverSock.setSoTimeout(SO_TIMEOUT);
			this.hostname = InetAddress.getLocalHost().getHostName();
			this.hostaddress = InetAddress.getLocalHost().getHostAddress();
			Server.SERVER_IP = this.hostaddress;
			this.opened = true;
			this.uptime = 0f;
			Debug.log("Successfully opened server: " + this);
			this.serverUI.log("Successfully opened server: " + this);
		} catch (Exception e) {
			Debug.warn("Failed to open server: " + this);
			this.serverUI.log("Failed to open server: " + this);
			e.printStackTrace();
		}
	}
	
	public void update(float dt) {
		if (!this.isOpen()) return;
		
		this.uptime += dt;
		this.sendTimer += dt;
		this.rollCallTimer += dt;
		Debug.logv("Server uptime: " + this.uptime + " s");
		
		//Cycle the engine
		this.servEngine.update(dt);
		
		//Poll clients for roll call
		this.rollCall();
		//Close queued UserSocks
		this.closeUserSocks();
		//Accept new clients
		this.acceptClients();
		//Look for commands from clients
		this.receiveCommands();
		//Execute on commands received
		this.executeCommands();
		//Broadcast server-wide messages
		this.sendMessages();
	}
	
	public void setServerUI(ServerUI serverUI) {
		this.serverUI = serverUI;
	}
	
	public void setNetEntitySystem(NetEntitySystem NES) {
		this.NES = NES;
	}
	
	public void queueAdminMessage(String adminMessage) {
		this.adminMessages.add(adminMessage);
	}
	
	public void kill(String name) {
		this.commandQ.add(new KillCommand(name));
	}
	
	private void rollCall() {
		if (this.rollCallTimer < 1f/ROLL_CALL_FREQUENCY) return;
		
		this.rollCallTimer -= 1f/ROLL_CALL_FREQUENCY;
		
		//Logout and close any UserSocks still flagged from last Roll Call
		for (UserSock usersock : this.usersocks) {
			if (usersock.getRC()) {
				Debug.warn("UserSock did not reply to Roll Call. Logging out User and closing Socket: " + usersock);
				this.serverUI.log("UserSock did not reply to Roll Call. Logging out User and closing Socket: " + usersock);
				
				LogoutCommand logoutcmd = new LogoutCommand(usersock.getUser().getName());
				this.commandQ.add(logoutcmd);
				
				//Queue socket for closing
				this.closeQ.add(usersock);
			}
		}
		
		//Flag remaining UserSocks for next Roll Call
		final String rc = "\\rc";
		UserMessage rollCallMessage;
		
		//Check to ensure all UserSock objects are still active
		for (UserSock usersock : this.usersocks) {
			usersock.setRC(true);
			rollCallMessage = new UserMessage(null, rc, usersock.getUser());
			this.write(rollCallMessage);
		}
	}
	
	private void acceptClients() {
		if (!this.isOpen()) return;

		Socket sock=null;
		
		try { sock = this.serverSock.accept(); }
		catch (SocketTimeoutException e) {
			Debug.logv("Server timeout reached");
		}
		catch (Exception e) {
			Debug.warn("Failed to accept client!");
			this.serverUI.log("Failed to accept client!");
			e.printStackTrace();
		}

		if (sock != null) {
			this.socks.add(sock);
			this.sockSegments.add(new SockSegment(sock));
			Debug.log("Accepted client: " + sock.toString());
			this.serverUI.log("Accepted client: " + sock.toString());
		}
	}
	
	private void receiveCommands() {
		if (!this.isOpen()) return;
		
		/*** PHASE 1: PROCESS ADMIN MESSAGES ***/
		
		//Process server admin messages
		for (String adminMessage : this.adminMessages) {
			
			//TODO: Revisit this when you have time: Letting the Command class convert strings to Command objects (outsource)
//			Command cmd = Command.parseCommand(adminMessage);
//			
//			if (cmd != null) this.commandQ.add(cmd);
			
			this.stoker = new StringTokenizer(adminMessage);
			String token;
			
			String name = "";
			String target = "";
			String message = "";
			while ((token = pullToken()) != null) {
				switch (token) {
				case "\\rc":
					//Unsupported
					Debug.warn("Unsupported or unrecognized command delivered: " + token);
					break;
					
				case "\\login":
					//Unsupported
					Debug.warn("Unsupported or unrecognized command delivered: " + token);
					break;
				
				case "\\version":
					this.commandQ.add(new VersionCommand(null));
					break;
					
				case "\\logout":
					//Unsupported
					Debug.warn("Unsupported or unrecognized command delivered: " + token);
					break;
					
				case "\\say":
					//build message from remaining tokens
					if ((token = pullToken()) != null) message = token;
					while ((token = pullToken()) != null) message += " " + token;
					this.commandQ.add(new SayCommand(null, message));
					break;
					
				case "\\tell":
					name = pullToken();
					if (name == null) Debug.warn("Invalid use of \\tell command. Requires username value");
					else {
						//build message from remaining tokens
						if ((token = pullToken()) != null) message = token;
						while ((token = pullToken()) != null) message += " " + token;
						//Queue \tell command to target username
						this.commandQ.add(new TellCommand(null, name, message));
					}
					break;
					
				case "\\spawn":
					name = pullToken();
					
					if (name == null) {
						Debug.warn("Invalid use of \\spawn command. Requires name value");
						break;
					}
					
					message = pullToken();
					if (message == null) {
						Debug.warn("Invalid use of \\spawn command. Requires position values");
						break;
					}
					
					this.commandQ.add(new SpawnCommand(name, message));
					
					break;
					
				case "\\kill":
					name = pullToken();
					
					if (name == null) {
						Debug.warn("Invalid use of \\kill command. Requires name value");
						break;
					}
					
					this.commandQ.add(new KillCommand(name));
					
					break;
					
				case "\\pos":
					name = pullToken();
					if (name == null) {
						Debug.warn("Invalid use of \\pos command. Requires name value");
						break;
					}
					message = pullToken();
					if (message == null) {
						Debug.warn("Invalid use of \\pos command. Requires position coordinates");
						break;
					}
					
					this.commandQ.add(new PositionCommand(name, message));
					
					break;
					
				case "\\move":
					name = pullToken();
					if (name == null) {
						Debug.warn("Invalid use of \\move command. Requires name value");
						break;
					}
					message = pullToken();
					if (message == null) {
						Debug.warn("Invalid use of \\move command. Requires movement values");
						break;
					}
					
					this.commandQ.add(new MoveCommand(name, message));
					
					break;
					
				case "\\attack":
					name = pullToken();
					
					if (name == null) {
						Debug.warn("Invalid use of \\attack command. Requires name value");
						break;
					}
					
					target = pullToken();
					
					if (target == null) {
						Debug.warn("Invalid use of \\attack command. Requires target value");
						break;
					}
					
					this.commandQ.add(new AttackCommand(null, name, target));
					
					break;
				
				case "\\damage":
					name = pullToken();
					
					if (name == null) {
						Debug.warn("Invalid use of \\damage command. Requires name value");
						break;
					}
					
					target = pullToken();
					
					if (target == null) {
						Debug.warn("Invalid use of \\damage command. Requires target value");
						break;
					}
					
					message = pullToken();
					
					if (message == null) {
						Debug.warn("Invalid use of \\damage command. Requires amount value");
						break;
					}
					
					this.commandQ.add(new DamageCommand(name, target, message));
					
					break;
					
				default:
					Debug.warn("Unsupported or unrecognized command delivered: " + token);
					break;
				}
			}
		}
		this.adminMessages.clear();
		
		
		/*** PHASE 2: READ FROM CLIENTS ***/
		
		//Attempt to read in server client messages
		for (Socket sock : this.socks) {
			
			//Attempt to get messages
			//Read in from InputStream and break into messages at '\n' occurrences
			try {
				InputStream istream = sock.getInputStream();
				
				//Try to read BYTE_BUFFER_SIZE bytes
				int len = Math.min(istream.available(), BYTE_BUFFER_SIZE);
				//Store actual number of bytes read
				int num = istream.read(this.buff, 0, len);
				Debug.logv("Bytes read: " + num);
				//If bytes were read, process
				if (num > 0) {
					char c;
					SockSegment socksegment = null;
					for (SockSegment ss : this.sockSegments) {
						if (ss.getSock() == sock) {
							socksegment = ss;
							break;
						}
					}
					
					if (socksegment == null) {
						Debug.warn("No SockSegment found for this Socket: " + sock);
						break;
					}
					
					String segment = socksegment.getSegment();
					
					for (int b=0; b < num; b++) {
						c = (char)(this.buff[b]);
						Debug.logv("[" + b + "]\t" + c);
						//Store segment as new message and reset segment
						if (c == '\n') {
							this.messages.add(segment);
							segment = "";
						}
						else segment += (char)(this.buff[b]);
					}
					
					socksegment.setSegment(segment);
					
					//Log the messages received for debugging purposes
					Debug.log("Received Messages [" + this.messages.size() + "]:");
					for (String message : this.messages) {
						Debug.log(message);
					}
				}
			}
			catch (IOException e) { e.printStackTrace(); }
			
			
			/*** PHASE 3: PROCESS CLIENT MESSAGES ***/
			
			//Process server client message
			for (String message : this.messages) {

				//TODO: Revisit this when you have time: Letting the Command class convert strings to Command objects (outsource)
//				Command cmd = Command.parseCommand(message);
//				
//				if (cmd != null) this.commandQ.add(cmd);
				
				this.stoker = new StringTokenizer(message);
				String token;
				
				String name = "";
				String target = "";
				String password = "";
				String args = "";
				while ((token = pullToken()) != null) {
					switch (token) {
					
					case "\\rc":
						name = pullToken();
						this.commandQ.add(new RollCallCommand(sock, name));
					
					case "\\login":
						name = pullToken();
						password = pullToken();
						if (name == null) Debug.warn("Invalid use of \\login command. Requires username value");
						else this.commandQ.add(new LoginCommand(sock, name, password));
						break;
						
					case "\\version":
						this.commandQ.add(new VersionCommand(sock));
						break;
						
					case "\\logout":
						this.commandQ.add(new LogoutCommand(sock));
						break;
						
					case "\\say":
						//build message from remaining tokens
						if ((token = pullToken()) != null) args = token;
						while ((token = pullToken()) != null) args += " " + token;
						this.commandQ.add(new SayCommand(sock, args));
						break;
						
					case "\\tell":
						name = pullToken();
						if (name == null) Debug.warn("Invalid use of \\tell command. Requires username value");
						else {
							//build message from remaining tokens
							if ((token = pullToken()) != null) args = token;
							while ((token = pullToken()) != null) args += " " + token;
							//Queue \tell command to target username
							this.commandQ.add(new TellCommand(sock, name, args));
						}
						break;
						
					case "\\spawn":
						//Unsupported
						Debug.warn("Unsupported or unrecognized command delivered: " + token);
						break;
						
					case "\\kill":
						//Unsupported
						Debug.log("Unsupported or unrecognized command delivered: " + token);
						break;
						
					case "\\pos":
						name = pullToken();
						if (name == null) {
							Debug.warn("Invalid use of \\pos command. Requires name value");
							break;
						}
						args = pullToken();
						if (args == null) {
							Debug.warn("Invalid use of \\pos command. Requires position coordinates");
							break;
						}
						
						this.commandQ.add(new PositionCommand(sock, name, args));
						
						break;
						
					case "\\move":
						name = pullToken();
						if (name == null) {
							Debug.warn("Invalid use of \\move command. Requires name value");
							break;
						}
						args = pullToken();
						if (args == null) {
							Debug.warn("Invalid use of \\move command. Requires movement values");
							break;
						}
						
						this.commandQ.add(new MoveCommand(sock, name, args));
						
						break;
						
					case "\\attack":
						name = pullToken();
						
						if (name == null) {
							Debug.warn("Invalid use of \\attack command. Requires name value");
							break;
						}
						
						target = pullToken();
						
						if (target == null) {
							Debug.warn("Invalid use of \\attack command. Requires target value");
							break;
						}
						
						this.commandQ.add(new AttackCommand(sock, name, target));
						
						break;
						
					case "\\damage":
						//Unsupported
						Debug.warn("Unsupported or unrecognized command delivered: " + token);
						break;
						
					default:
						Debug.warn("Unsupported or unrecognized command delivered: " + token);
						break;
					}
				}
			}
			this.messages.clear();
		}
	}
	
	//Main command processing loop
	private void executeCommands() {
		if (!this.isOpen()) return;
	
		Debug.logv(this.commandQ);
		
		boolean success;
		User user, other;
		
		for (Command command : this.commandQ) {
			//Mute RollCallCommands (they clutter output)
			if (command.getType() != CMD_TYP.ROLLCALL) this.serverUI.log(command);
			
			switch (command.getType()) {
			case ROLLCALL:
				RollCallCommand rccmd = (RollCallCommand)command;
				
				//Admin command
				if (command.getSock() == null) {
					//Not supported
					break;
				}
				
				//Client command
				else {
					//Roll call reply received, unflag associated UserSock
					for (UserSock usersock : this.usersocks) {
						if (usersock.getSock() == rccmd.getSock()) {
							usersock.setRC(false);
							break;
						}
					}
				}
				
				break;
				
				
			case LOGIN:
				LoginCommand logincmd = (LoginCommand)command;
				
				//Admin command
				if (command.getSock() == null) {
					//Not supported
					break;
				}
				
				//Client command
				else {
					success = false;
					user = null;
					for (User reguser : this.registeredUsers) {
						if (reguser.getName().equals(logincmd.getUsername()) && reguser.hasPassword(logincmd.getPassword())) {
							Debug.log("User logged in: " + reguser + " @ " + logincmd.getSock());
							this.serverUI.log("User logged in: " + reguser.getName());
							user = reguser;
							this.usersocks.add(new UserSock(reguser, logincmd.getSock()));
							this.netEntities.add(user);
							success = true;
							break;
						}
					}
					
					if (!success) {
						Debug.warn("Login attempt failed: Incorrect username or password");
						this.serverUI.log("Login attempt failed: Incorrect username or password");
						Debug.logv(this.registeredUsers);
						
						//TODO: Send login failed command to requesting socket
					}
					
					//Broadcast login message to all connected clients to update their world state
					if (user != null) {
						String message = "\\login " + user.getName();
						this.broadcastQ.add(new UserMessage(message));
					}
					
					//Send direct server message to sender to "login" all currently logged in users
					for (UserSock usersock : this.usersocks) {
						//Skip the sending user
						if (usersock.getSock() == logincmd.getSock()) continue;
						
						this.directMessageQ.add(new UserMessage(new LoginCommand(null, usersock.getUser().getName())));
					}
				}
				
				break;
				
				
			case VERSION:
				VersionCommand versioncmd = (VersionCommand)command;
				
				String version = "Valid Version " + VERSION_HI + "." + VERSION_LO;
				//Admin command
				if (command.getSock() == null) {
					//Log to ServerUI
					this.serverUI.log("[ADMIN] " + version);
				}
				
				//Client command
				else {
					//Verify requesting User is logged in
					user = null;
					for (UserSock usersock : this.usersocks) {
						if (usersock.getSock() == versioncmd.getSock()) {
							user = usersock.getUser();
							break;
						}
					}
					
					if (user == null) {
						Debug.warn("Failed to process command. Requesting Socket has no active User: " + command);
						break;
					}
					
					//Send version info to requesting User
					this.directMessageQ.add(new UserMessage(null, version, user));
				}
				
				break;
				
				
			case LOGOUT:
				LogoutCommand logoutcmd = (LogoutCommand)command;
				
				//Admin command
				if (command.getSock() == null) {
					//Broadcast logout message to all connected clients to update their world state
					this.broadcastQ.add(new UserMessage(logoutcmd.toCommandString()));
				}
				
				//Client command
				else {
					success = false;
					user = null;
					for (UserSock usersock : this.usersocks) {
						if (usersock.getSock() == logoutcmd.getSock()) {
							user = usersock.getUser();
							this.usersocks.remove(usersock);
							Debug.log("User logout successful: " + user + " @ " + usersock.getSock());
							this.serverUI.log("User logout successful: " + user.getName());
							success = true;
							break;
						}
					}
					
					if (!success) Debug.warn("Failed to logout. Requesting Socket has no active User: " + command.getSock());
					
					//Broadcast logout message to all connected clients to update their world state
					if (user != null) {
						this.broadcastQ.add(new UserMessage(logoutcmd));
					}
				}
				
				break;
				
				
			case SAY:
				SayCommand saycmd = (SayCommand)command;
				
				//Admin command
				if (command.getSock() == null) {
					this.broadcastQ.add(new UserMessage(saycmd.getMessage()));
				}
				
				//Client command
				if (command.getSock() != null) {
					//Verify User is logged in
					user = null;
					for (UserSock usersock : this.usersocks) {
						if (saycmd.getSock() == usersock.getSock()) {
							user = usersock.getUser();
							break;
						}
					}
					
					if (user == null) {
						Debug.warn("Failed to broadcast message. Requesting Socket has no active User: " + saycmd.getSock() + " [" + saycmd.getMessage() + "]");
						break;
					}
						
					this.broadcastQ.add(new UserMessage(user, saycmd.getMessage()));
				}
				
				break;
				
				
			case TELL:
				TellCommand tellcmd = (TellCommand)command;
				
				//Admin command
				if (command.getSock() == null) {
					user = null;
					for (UserSock usersock : this.usersocks) {
						if (tellcmd.getToUsername().equals(usersock.getUser().getName())) {
							user = usersock.getUser();
							break;
						}
					}
					
					if (user == null) {
						Debug.warn("Failed to send message. No active User with username: " + tellcmd.getToUsername() + " [" + tellcmd.getMessage() + "]");
						break;
					}
					
					//Queue direct message to target user
					this.directMessageQ.add(new UserMessage(null, tellcmd.getMessage(), user));
				}				
				
				//Client command
				else {
					//Verify User is logged in
					user = null;
					for (UserSock usersock : this.usersocks) {
						if (tellcmd.getSock() == usersock.getSock()) {
							user = usersock.getUser();
							break;
						}
					}
					
					if (user == null) {
						Debug.warn("Failed to send message. Requesting Socket has no active User: " + tellcmd.getSock() + " [" + tellcmd.getMessage() + "]");
						break;
					}
					
					other = null;
					for (UserSock usersock : this.usersocks) {
						if (tellcmd.getToUsername().equals(usersock.getUser().getName())) {
							other = usersock.getUser();
							break;
						}
					}
					
					//Queue direct message to sending user for chat purposes
					if (user != other) this.directMessageQ.add(new UserMessage(user, tellcmd.getMessage(), user));
					
					if (other == null) {
						Debug.warn("Failed to send message. No active User with username: " + tellcmd.getToUsername() + " [" + tellcmd.getMessage() + "]");
						break;
					}
					
					//Queue direct message to target user
					this.directMessageQ.add(new UserMessage(user, tellcmd.getMessage(), other));
				}
				
				break;
				
				
			case SPAWN:
				SpawnCommand spawncmd = (SpawnCommand)command;
				
				//Server command
				if (command.getSock() == null) {
					//Broadcast spawn message to all connected clients to update their world state
					this.broadcastQ.add(new UserMessage(spawncmd.toCommandString()));
				}				
				
				//Client command
				else {
					//Check to see if NetEntity should be an existing User or a new Enemy
					String name = spawncmd.getName();
					
					boolean match = false;
					for (NetEntity ne : this.netEntities) {
						if (ne.getName().equals(name)) {
							match = true;
							break;
						}
					}
					
					//If no active NetEntity was found, create a new enemy
					if (!match) {
						this.netEntities.add(new Enemy(name));
					}
					
					//Pick a random spawn location
					Random random = new Random();
					Vector3 position = new Vector3(random.nextFloat()*5f+10f, 20f, random.nextFloat()*5f+14f);
					
					//Broadcast spawn message to all connected clients to update their world state
					this.broadcastQ.add(new UserMessage((new SpawnCommand(null, name, position)).toCommandString()));
				}
				
				break;
				
				
			//TODO: Verify this command is doing what is expected (is old version of command)
			case KILL:
				KillCommand killcmd = (KillCommand)command;
				
				//Verify target User is logged in
				NetEntity target = null;
				for (UserSock usersock : this.usersocks) {
					if (usersock.getUser().hasName(killcmd.getName())) {
						target = usersock.getUser();
						break;
					}
				}
				
				if (target == null) {
					Debug.warn("Cannot kill target. No active NetEntity with name: " + killcmd.getName());
					break;
				}
				
				//Broadcast spawn message to all connected clients to update their world state
				this.broadcastQ.add(new UserMessage(killcmd));
				
				break;
				
				
			case POSITION:
				PositionCommand poscmd = (PositionCommand)command;
				
				//Client command
				if (command.getSock() != null) {
					//Search for NetEntity with given name
					NetEntity netEntity=null;
					for (NetEntity ne : this.netEntities) {
						if (ne.hasName(poscmd.getName())) {
							netEntity = ne;
							break;
						}
					}
					
					if (netEntity == null) {
						Debug.warn("Failed to set position. No active NetEntity with name: " + poscmd.getName());
						break;
					}
					
					netEntity.setPosition(poscmd.getPosition());
					
					//Broadcast position command to all other clients
					this.broadcastQ.add(new UserMessage(poscmd));
				}
				
				break;
				
				
			case MOVE:
				MoveCommand movecmd = (MoveCommand)command;

				//Client command
				if (command.getSock() != null) {
					//Verify User is logged in
					user = null;
					for (UserSock usersock : this.usersocks) {
						if (movecmd.getSock() == usersock.getSock()) {
							user = usersock.getUser();
							break;
						}
					}
					
					//Broadcast move message to all connected clients to update their world state
					this.broadcastQ.add(new UserMessage(movecmd));
				}
				
				break;
				
				
			case ATTACK:
				AttackCommand attackcmd = (AttackCommand)command;
				
				//Admin command
				if (command.getSock() == null) {
					NetEntity attacker = null;
					for (NetEntity ne : this.netEntities) {
						if (ne.hasName(attackcmd.getName())) {
							attacker = ne;
						}
					}
					
					if (attacker == null) {
						Debug.warn("Failed to attack. Attacker has no active NetEntity: " + attackcmd);
						break;
					}
					
					NetEntity attackee = null;
					for (NetEntity ne : this.netEntities) {
						if (ne.hasName(attackcmd.getTarget())) {
							attackee = ne;
						}
					}
					
					if (attackee == null) {
						Debug.warn("Failed to attack. Attackee has no active NetEntity: " + attackcmd);
						break;
					}
					
					float damage = attacker.getStrength();
					
					//Broadcast new \damage command to clients to update
					this.broadcastQ.add(new UserMessage((new DamageCommand(null, attacker.getName(), attackee.getName(), damage))));
				}
				
				//Client command
				else {
					NetEntity attacker = null;
					for (NetEntity ne : this.netEntities) {
						if (ne.hasName(attackcmd.getName())) {
							attacker = ne;
						}
					}
					
					if (attacker == null) {
						Debug.warn("Failed to attack. Attacker has no active NetworkEntity: " + attackcmd);
						break;
					}
					
					NetEntity attackee = null;
					for (NetEntity ne : this.netEntities) {
						if (ne.hasName(attackcmd.getTarget())) {
							attackee = ne;
						}
					}
					
					if (attackee == null) {
						Debug.warn("Failed to attack. Attackee has no active NetEntity: " + attackcmd);
						break;
					}
					
					float damage = attacker.getStrength();
					
					//Broadcast new \damage command to clients to update
					this.broadcastQ.add(new UserMessage((new DamageCommand(null, attacker.getName(), attackee.getName(), damage))));
				}
				
				break;
				
				
			case DAMAGE:
				DamageCommand damagecmd = (DamageCommand)command;
				
				//Admin command
				if (command.getSock() == null) {
					//Broadcast \damage command to clients
					this.broadcastQ.add(new UserMessage(damagecmd.toCommandString()));
				}
				
				//Client command
				else {
					//Not supported
					break;
				}
				
				break;
				
				
			default:
				Debug.warn("Invalid or unhandled command type: " + command);
				Debug.warn(this);
				break;
			}
		}
		
		this.commandQ.clear();
	}
	
	private void sendMessages() {
		if (!this.isOpen()) return;
		
		if (this.sendTimer < 1f/SEND_FREQUENCY) return;
		
		this.sendTimer -= 1f/SEND_FREQUENCY;
		
		//Send Direct Messages
		for (UserMessage usermessage : this.directMessageQ) write(usermessage);
		this.directMessageQ.clear();
		
		//Broadcast Messages
		for (UserMessage usermessage : this.broadcastQ) write(usermessage);
		this.broadcastQ.clear();
	}
	
	private void closeUserSocks() {
		if (!this.isOpen()) return;
		
		for (UserSock usersock : this.closeQ) {
			if (usersock.getSock() != null) try  {
				usersock.getSock().close();
				this.usersocks.remove(usersock);
				this.socks.remove(usersock.getSock());
			} catch (Exception e) { e.printStackTrace(); }
		}
	}
	
	public void close() {
		if (this.serverSock != null) try {
			
			for (UserSock usersock : this.usersocks) usersock.getSock().close();
			
			this.serverSock.close();
			this.opened = false;
			Debug.log("Successfully closed server: " + this + " uptime=" + this.uptime + " s");
			this.serverUI.log("Successfully closed server: " + this + " uptime=" + this.uptime + " s");
		} catch (Exception e) {
			Debug.warn("Failed to close server: " + this);
			e.printStackTrace();
		}
	}
	
	public boolean isOpen() {
		return this.opened;
	}
	
	public float getUptime() {
		return this.uptime;
	}
	
	private void write(UserMessage usermessage) {
		
		String sender;
		if (usermessage.getUser() == null) sender = "SERVER";
		else sender = usermessage.getUser().getName();
		
		//Check for target socket
		Socket sock = null;
		if (usermessage.getTarget() != null) {
			for (UserSock usersock : this.usersocks) {
				if (usersock.getUser().getName().equals(usermessage.getTarget().getName())) {
					sock = usersock.getSock();
					break;
				}
			}
		}
		
		//If no target socket, broadcast message to server
		if (sock == null) {
			
			for (UserSock usersock : this.usersocks) {
				Debug.log("Broadcasting message to User " + usersock.getUser().getName() + "... [" + sender + ": " + usermessage.getMessage() + "]");
				this.serverUI.log("Broadcasting message to User " + usersock.getUser().getName() + "... [" + sender + ": " + usermessage.getMessage() + "]");
				write(usersock.getSock(), "[" + sender + "] " + usermessage.getMessage());
			}
			
		}
		//Otherwise, send only to specified Socket
		else {
			//Mute RollCall commands (they clutter up output)
			if (!usermessage.getMessage().contains("\\rc")) {
				Debug.log("Sending message to User " + usermessage.getTarget().getName() + "... [" + sender + ": " + usermessage.getMessage() + "]");
				this.serverUI.log("Sending message to User " + usermessage.getTarget().getName() + "... [" + sender + ": " + usermessage.getMessage() + "]");
			}
			write(sock, "[" + sender + "] " + usermessage.getMessage());
		}
	}
	
	//Write ArrayList<String> contents to Socket's OutputStream
//	private void write(Socket sock, ArrayList<String> messages) {
//		if (sock != null) try {
//			OutputStream ostream = sock.getOutputStream();
//			for (String message : messages) {
//				message += "\n";
//				byte[] buff = message.getBytes();
//				ostream.write(buff);
//			}
//		} catch (IOException e) {
//			Debug.warn("Unable to write to Socket. Removing UserSock record... " + sock);
//			for (UserSock usersock : this.usersocks) if (usersock.getSock() == sock) this.usersocks.remove(usersock);
//			e.printStackTrace();
//		}
//	}
	
	//Write String to Socket's OutputStream
	private void write(Socket sock, String message) {
		Debug.log("Writing to socket... [sock=" + sock + " message=" + message + "]");
		if (sock != null) try {
			OutputStream ostream = sock.getOutputStream();
			message += "\n";
			byte[] buff = message.getBytes();
			ostream.write(buff);
		} catch (IOException e) {
			Debug.warn("Unable to write to Socket: " + sock);
			this.serverUI.log("Unable to write to Socket: " + sock);
//			e.printStackTrace();
		}
	}
	
	public String pullToken() {
		if (this.stoker == null) return null;
		try {
			String token = this.stoker.nextToken();
			return token;
		} catch (NoSuchElementException e) {
			Debug.logv("No more tokens to pull");
			return null;
		}
	}
	
	@Override
	public String toString() {
		String str = "SERVER";
		if (this.isOpen()) str += "{version=" + VERSION_HI + "." + VERSION_LO + " serverSock=" + this.serverSock + " hostname=" + this.hostname + " hostaddress=" + this.hostaddress + "}";
		else str += "{serverSock=" + this.serverSock + "}";
		return str;
	}
	
	
	/*** UTILITY CLASSES ***/
	
	private static class SockSegment {
		
		private Socket sock;
		private String segment;
		
		public SockSegment(Socket sock) {
			this.sock = sock;
			this.segment = "";
		}
		
		public Socket getSock() {
			return this.sock;
		}
		
		public void setSegment(String segment) {
			this.segment = segment;
		}
		
		public String getSegment() {
			return this.segment;
		}
		
	}
	
	private static class UserSock implements ILoggable {
		
		private User user;
		private Socket sock;
		//Flag for use during server roll call
		private boolean rcflag;
		
		public UserSock(User user, Socket sock) {
			this.user = user;
			this.sock = sock;
			this.rcflag = false;
		}
		
		public User getUser() {
			return this.user;
		}
		
		public Socket getSock() {
			return this.sock;
		}
		
		public void setRC(boolean rc) {
			this.rcflag = rc;
		}
		
		public boolean getRC() {
			return this.rcflag;
		}
		
		@Override
		public String toString() {
			String str = "USER_SOCK{user=" + this.user;
			str += " sock=" + this.sock + "}";
			return str;
		}
		
	}
	
	private static class UserMessage {
		
		//Left null if this message is from the Server
		private User user;
		private String message;
		//Left null if message is to be broadcast
		private User target;
		
		public UserMessage(User user, String message, User target) {
			this.user = user;
			this.message = message;
			this.target = target;
		}
		public UserMessage(User user, String message) { this(user, message, null); }
		public UserMessage(String message) { this(null, message, null); }
		public UserMessage(Command command) { this(null, command.toCommandString(), null); }
		
		public User getUser() {
			return this.user;
		}
		
		public String getMessage() {
			return this.message;
		}
		
		public User getTarget() {
			return this.target;
		}
		
	}
	
}