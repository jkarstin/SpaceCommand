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
import java.util.StringTokenizer;

import ph.games.scg.server.command.Command;
import ph.games.scg.server.command.LoginCommand;
import ph.games.scg.server.command.LogoutCommand;
import ph.games.scg.server.command.MoveCommand;
import ph.games.scg.server.command.SayCommand;
import ph.games.scg.server.command.TellCommand;
import ph.games.scg.server.command.VersionCommand;
import ph.games.scg.util.Debug;
import ph.games.scg.util.ILoggable;

public class Server implements ILoggable {
	
	public static final String SERVER_IP = "192.168.1.2";
	public static final int SERVER_PORT = 21595;
	private static final int DEFAULT_SO_TIMEOUT = 50;
	
	private static final int BYTE_BUFFER_SIZE = 64;
	private static final int VERSION_LO = 2;
	private static final int VERSION_HI = 0;
	
	//Server socket
	private ServerSocket serverSock;
	//Server metadata
	private int port;
	private String hostname;
	private String hostaddress;
	private boolean opened;
	//Number of seconds since server was opened
	private float uptime;
	//Number of milliseconds the server will attempt to look for clients before stopping
	private int soTimeout;
	
	//Message reading
	private byte[] buff;
	private ArrayList<String> messages;
	private ArrayList<SockSegment> sockSegments;
	private StringTokenizer stoker;
	
	//User ArrayList
	private ArrayList<User> registeredUsers;
	//Client ArrayList
	private ArrayList<Socket> socks;
	//UserSock ArrayList
	private ArrayList<UserSock> usersocks;
	//Command Queue
	private ArrayList<Command> commandQ;
	//Direct Message Queue
	private ArrayList<UserMessage> directMessageQ;
	//Broadcast Queue
	private ArrayList<UserMessage> broadcastQ;
	
	public Server(int port, int timeout) {
		this.serverSock = null;
		this.port = port;
		this.opened = false;
		this.uptime = 0f;
		this.soTimeout = timeout;
		
		this.buff = new byte[BYTE_BUFFER_SIZE];
		this.messages = new ArrayList<String>();
		this.sockSegments = new ArrayList<SockSegment>();
		this.stoker = null;
		
		this.registeredUsers = new ArrayList<User>();
		this.registeredUsers.add(new User("phrongorre", "pancakes99"));
		this.registeredUsers.add(new User("roger", "foneybaloney"));
		
		this.socks = new ArrayList<Socket>();
		this.usersocks = new ArrayList<UserSock>();
		this.commandQ = new ArrayList<Command>();
		this.directMessageQ = new ArrayList<UserMessage>();
		this.broadcastQ = new ArrayList<UserMessage>();
	}
	public Server(int port) { this(port, DEFAULT_SO_TIMEOUT); }
	
	public void open() {
		if (this.serverSock == null) try {
			this.serverSock = new ServerSocket(this.port);
			this.serverSock.setSoTimeout(this.soTimeout);
			this.hostname = InetAddress.getLocalHost().getHostName();
			this.hostaddress = InetAddress.getLocalHost().getHostAddress();
			this.opened = true;
			this.uptime = 0f;
			Debug.log("Successfully opened server: " + this);
		} catch (Exception e) {
			Debug.warn("Failed to open server: " + this);
			e.printStackTrace();
		}
	}
	
	public void update(float dt) {
		if (!this.isOpen()) return;
		
		this.uptime += dt;
		Debug.logv("Server uptime: " + this.uptime + " s");
		//Accept new clients
		this.acceptClients();
		//Look for commands from clients
		this.receiveCommands();
		//Execute on commands received
		this.executeCommands();
		//Broadcast server-wide messages
		this.sendMessages();
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
			e.printStackTrace();
		}

		if (sock != null) {
			this.socks.add(sock);
			this.sockSegments.add(new SockSegment(sock));
			Debug.log("Accepted client: " + sock.toString());
		}
	}
	
	private void receiveCommands() {
		if (!this.isOpen()) return;
		
		//Attempt to read in server messages
		for (Socket sock : this.socks) {
			
			//Attempt to get messages
			//Read in from InputStream and break into messages at '\n' occurences
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
						if (socksegment.getSock() == sock) {
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
			
			//Look for commands if messages were received
			for (String message : this.messages) {
				this.stoker = new StringTokenizer(message);
				String token;
				
				String username = "";
				String password = "";
				message = "";
				while ((token = pullToken()) != null) {
					switch (token) {
					
					case "\\login":
						username = pullToken();
						password = pullToken();
						if (username == null) Debug.warn("Invalid use of \\login command. Requires username value");
						else this.commandQ.add(new LoginCommand(sock, username, password));
						break;
						
					case "\\version":
						this.commandQ.add(new VersionCommand(sock));
						break;
						
					case "\\logout":
						this.commandQ.add(new LogoutCommand(sock));
						break;
						
					case "\\say":
						//build message from remaining tokens
						if ((token = pullToken()) != null) message = token;
						while ((token = pullToken()) != null) message += " " + token;
						this.commandQ.add(new SayCommand(sock, message));
						break;
						
					case "\\tell":
						username = pullToken();
						if (username == null) Debug.warn("Invalid use of \\tell command. Requires username value");
						else {
							//build message from remaining tokens
							if ((token = pullToken()) != null) message = token;
							while ((token = pullToken()) != null) message += " " + token;
							this.commandQ.add(new TellCommand(sock, username, message));
						}
						break;
						
					case "\\move":
						//TODO: Handle command
						username = pullToken();
						if (username == null) {
							Debug.warn("Invalid use of \\move command. Requires name value");
							break;
						}
						message = pullToken();
						if (message == null) {
							Debug.warn("Invalid use of \\move command. Requires movement values");
							break;
						}
						
						String[] moveData = message.split(",");
						
						float x = Float.valueOf(moveData[0]);
						float y = Float.valueOf(moveData[1]);
						float z = Float.valueOf(moveData[2]);
						float theta = Float.valueOf(moveData[3]);
						float delta = Float.valueOf(moveData[4]);
						
						this.commandQ.add(new MoveCommand(sock, username, x, y, z, theta, delta));
						
						break;
						
					default:
						Debug.log("Unsupported or unrecognized command delivered: " + token);
						break;
					}
				}
			}
			this.messages.clear();
		}
	}
	
	private void executeCommands() {
		if (!this.isOpen()) return;
	
		Debug.logv(this.commandQ);
		
		boolean success;
		User user, target;
		
		for (Command command : this.commandQ) {
			switch (command.getType()) {
			
			
			case LOGIN:
				LoginCommand logincmd = (LoginCommand)command;
				
				success = false;
				user = null;
				for (User reguser : this.registeredUsers) {
					if (reguser.getUsername().equals(logincmd.getUsername()) && reguser.hasPassword(logincmd.getPassword())) {
						Debug.log("User logged in: " + reguser + " @ " + logincmd.getSock());
						user = reguser;
						this.usersocks.add(new UserSock(reguser, logincmd.getSock()));
						success = true;
						break;
					}
				}
				
				if (!success) {
					Debug.log("Login attempt failed: Incorrect username or password");
					Debug.logv(this.registeredUsers);
				}
				
				//Broadcast login message to all connected clients to update their world state
				if (user != null) {
					String message = "\\login " + user.getUsername();
					this.broadcastQ.add(new UserMessage(message));
				}
				
				break;
				
				
			case VERSION:
				user = null;
				for (UserSock usersock : this.usersocks) {
					if (usersock.getSock() == command.getSock()) {
						user = usersock.getUser();
						break;
					}
				}
				
				String version = "Valid Version " + VERSION_HI + "." + VERSION_LO;
				this.directMessageQ.add(new UserMessage(null, version, user));
				
				break;
				
				
			case LOGOUT:
				success = false;
				user = null;
				for (UserSock usersock : this.usersocks) {
					if (usersock.getSock() == command.getSock()) {
						user = usersock.getUser();
						this.usersocks.remove(usersock);
						Debug.log("User logout successful: " + user + " @ " + usersock.getSock());
						success = true;
						break;
					}
				}
				
				if (!success) Debug.log("Failed to logout. Requesting Socket has no active User: " + command.getSock());
				
				//Broadcast logout message to all connected clients to update their world state
				if (user != null) {
					String message = "\\logout " + user.getUsername();
					this.broadcastQ.add(new UserMessage(message));
				}
				
				break;
				
				
			case SAY:
				SayCommand saycmd = (SayCommand)command;
				
				user = null;
				for (UserSock usersock : this.usersocks) {
					if (saycmd.getSock() == usersock.getSock()) {
						user = usersock.getUser();
						break;
					}
				}
				
				if (user == null) {
					Debug.log("Failed to broadcast message. Requesting Socket has no active User: " + saycmd.getSock() + " [" + saycmd.getMessage() + "]");
					break;
				}
					
				this.broadcastQ.add(new UserMessage(user, saycmd.getMessage()));
				
				break;
				
				
			case TELL:
				TellCommand tellcmd = (TellCommand)command;
				
				user = null;
				for (UserSock usersock : this.usersocks) {
					if (tellcmd.getSock() == usersock.getSock()) {
						user = usersock.getUser();
						break;
					}
				}
				
				if (user == null) {
					Debug.log("Failed to send message. Requesting Socket has no active User: " + tellcmd.getSock() + " [" + tellcmd.getMessage() + "]");
					break;
				}
				
				target = null;
				for (UserSock usersock : this.usersocks) {
					if (tellcmd.getToUsername().equals(usersock.getUser().getUsername())) {
						target = usersock.getUser();
						break;
					}
				}
				
				if (target == null) {
					Debug.log("Failed to send message. No active User with username: " + tellcmd.getToUsername() + " [" + tellcmd.getMessage() + "]");
					break;
				}
				
				this.directMessageQ.add(new UserMessage(user, tellcmd.getMessage(), target));
				
				break;
				
				
			case MOVE:
				MoveCommand movecmd = (MoveCommand)command;
				
				user = null;
				for (UserSock usersock : this.usersocks) {
					if (movecmd.getSock() == usersock.getSock()) {
						user = usersock.getUser();
						break;
					}
				}
				
				//Broadcast move message to all connected clients to update their world state
				
				String moveMessage = "\\move " + movecmd.getName() + " " + movecmd.getMoveVector().x + "," + movecmd.getMoveVector().y + "," + movecmd.getMoveVector().z + "," + movecmd.getFacing() + "," + movecmd.getDeltaTime();
				
				this.broadcastQ.add(new UserMessage(null, moveMessage));
				
				break;
				
				
			default:
				Debug.warn("Invalid command type: " + command);
				break;
			}
		}
		
		this.commandQ.clear();
	}
	
	private void sendMessages() {
		if (this.isOpen()) {
			//Send Direct Messages
			for (UserMessage usermessage : this.directMessageQ) write(usermessage);
			this.directMessageQ.clear();
			
			//Broadcast Messages
			for (UserMessage usermessage : this.broadcastQ) write(usermessage);
			this.broadcastQ.clear();
		}
	}
	
	public void close() {
		if (this.serverSock != null) try {
			
			for (UserSock usersock : this.usersocks) usersock.getSock().close();
			
			this.serverSock.close();
			this.opened = false;
			Debug.log("Successfully closed server: " + this + " uptime=" + this.uptime + " s");
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
		else sender = usermessage.getUser().getUsername();
		
		//Check for target socket
		Socket sock = null;
		if (usermessage.getTarget() != null) {
			for (UserSock usersock : this.usersocks) {
				if (usersock.getUser().getUsername().equals(usermessage.getTarget().getUsername())) {
					sock = usersock.getSock();
					break;
				}
			}
		}
		
		//If no target socket, broadcast message to server
		if (sock == null) {
			
			for (UserSock usersock : this.usersocks) {
				Debug.log("Broadcasting message to User " + usersock.getUser().getUsername() + "... [" + sender + ": " + usermessage.getMessage() + "]");
				write(usersock.getSock(), "[" + sender + "] " + usermessage.getMessage());
			}
			
		}
		else {
			Debug.log("Sending message to User " + usermessage.getTarget().getUsername() + "... [" + sender + ": " + usermessage.getMessage() + "]");
			write(sock, "[" + sender + "] " + usermessage.getMessage());
		}
	}
	
	//Write ArrayList<String> contents to Socket's OutputStream
	private void write(Socket sock, ArrayList<String> messages) {
		if (sock != null) try {
			OutputStream ostream = sock.getOutputStream();
			for (String message : messages) {
				message += "\n";
				byte[] buff = message.getBytes();
				ostream.write(buff);
			}
		} catch (IOException e) {
			Debug.warn("Unable to write to Socket. Removing UserSock record... " + sock);
			for (UserSock usersock : this.usersocks) if (usersock.getSock() == sock) this.usersocks.remove(usersock);
			e.printStackTrace();
		}
	}
	
	//Write String to Socket's OutputStream
	private void write(Socket sock, String message) {
		Debug.log("Writing to socket... [sock=" + sock + " message=" + message + "]");
		if (sock != null) try {
			OutputStream ostream = sock.getOutputStream();
			message += "\n";
			byte[] buff = message.getBytes();
			ostream.write(buff);
		} catch (IOException e) {
			Debug.warn("Unable to write to Socket. Removing UserSock record... " + sock);
			for (UserSock usersock : this.usersocks) if (usersock.getSock() == sock) this.usersocks.remove(usersock);
			e.printStackTrace();
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
		if (this.isOpen()) str += "{serverSock=" + this.serverSock.toString() + " hostname=" + this.hostname + " hostaddress=" + this.hostaddress + "}";
		else str += "{serverSock=" + this.serverSock.toString() + "}";
		return str;
	}
	
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
	
	private static class UserSock {
		
		private User user;
		private Socket sock;
		
		public UserSock(User user, Socket sock) {
			this.user = user;
			this.sock = sock;
		}
		
		public User getUser() {
			return this.user;
		}
		
		public Socket getSock() {
			return this.sock;
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