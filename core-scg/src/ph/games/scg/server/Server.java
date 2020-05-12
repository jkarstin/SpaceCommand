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
import ph.games.scg.server.command.SayCommand;
import ph.games.scg.server.command.TellCommand;
import ph.games.scg.server.command.VersionCommand;
import ph.games.scg.util.Debug;
import ph.games.scg.util.ILoggable;

public class Server implements ILoggable {
	
	private static final int BYTE_BUFFER_SIZE = 64;
	private static final int VERSION_LO = 1;
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
	private String segment;
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
		this.segment = "";
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
	public Server(int port) { this(port, 50); }
	
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
		if (this.isOpen()) {
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
	}
	
	private void acceptClients() {
		if (this.isOpen()) {
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
				Debug.log("Accepted client: " + sock.toString());
			}
		}
	}
	
	private void receiveCommands() {
		if (this.isOpen()) {
			//Attempt to read in server messages
			for (Socket sock : this.socks) {
				
				//Attempt to get messages
				try {
					InputStream istream = sock.getInputStream();
					
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
								this.segment = "";
							}
							else this.segment += (char)(this.buff[b]);
						}
						
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
							else {
								if (password == null) password = "";
								this.commandQ.add(new LoginCommand(sock, username, password));
							}
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
	}
	
	private void executeCommands() {
		if (this.isOpen()) {
			Debug.logv(this.commandQ);
			
			boolean success;
			User user, target;
			
			for (Command command : this.commandQ) {
				switch (command.getType()) {
				case LOGIN:
					LoginCommand logincmd = (LoginCommand)command;
					
					success = false;
					for (User reguser : this.registeredUsers) {
						if (reguser.getUsername().equals(logincmd.getUsername()) && reguser.hasPassword(logincmd.getPassword())) {
							Debug.log("User logged in: " + reguser + " @ " + logincmd.getSock());
							this.usersocks.add(new UserSock(reguser, logincmd.getSock()));
							success = true;
							break;
						}
					}
					
					if (!success) {
						Debug.log("Login attempt failed: Incorrect username or password");
						Debug.logv(this.registeredUsers);
					}
					
					break;
				case VERSION:
					String version = "Valid Version " + VERSION_HI + "." + VERSION_LO;
					write(((VersionCommand)command).getSock(), version);
					break;
				case LOGOUT:
					success = false;
					for (UserSock usersock : this.usersocks) {
						if (usersock.getSock() == command.getSock()) {
							this.usersocks.remove(usersock);
							Debug.log("User logout successful: " + usersock.getUser() + " @ " + usersock.getSock());
							success = true;
							break;
						}
					}
					
					if (!success) Debug.log("Failed to logout. Requesting Socket has no active User: " + command.getSock());
					
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
				default:
					Debug.warn("Invalid command type: " + command);
					break;
				}
			}
			
			this.commandQ.clear();
		}
	}
	
	private void sendMessages() {
		if (this.isOpen()) {
			
			//Direct Message Queue
			
			for (UserMessage usermessage : this.directMessageQ) {
				//Broadcast message
				Debug.log("Sending message to User " + usermessage.getTarget().getUsername() + "... [" + usermessage.getUser().getUsername() + ": " + usermessage.getMessage() + "]");
				write(usermessage);
			}
			
			this.directMessageQ.clear();
			
			//Broadcast Queue
			
			for (UserMessage usermessage : this.broadcastQ) {
				//Broadcast message
				Debug.log("Broadcasting message to server... [" + usermessage.getUser().getUsername() + ": " + usermessage.getMessage() + "]");

				for (UserSock usersock : this.usersocks) {
					write(usersock.getSock(), "[" + usermessage.getUser().getUsername() + "]: " + usermessage.getMessage());
				}
			}
			
			this.broadcastQ.clear();
		}
	}
	
	public void close() {
		if (this.serverSock != null) try {
			this.serverSock.close();
			this.opened = false;
			Debug.log("Successfully closed server: " + this + " uptime=" + this.uptime + " s");
		} catch (Exception e) {
			e.printStackTrace();
			Debug.warn("Failed to close server: " + this);
		}
	}
	
	public boolean isOpen() {
		return this.opened;
	}
	
	public float getUptime() {
		return this.uptime;
	}
	
	private void write(UserMessage usermessage) {
		Socket sock = null;
		for (UserSock usersock : this.usersocks) {
			if (usersock.getUser().getUsername().equals(usermessage.getTarget().getUsername())) {
				sock = usersock.getSock();
				break;
			}
		}
		
		if (sock == null) {
			Debug.log("Failed to write to User " + usermessage.getTarget().getUsername() + ": No active User found.");
			return;
		}
		
		write(sock, "[" + usermessage.getUser().getUsername() + "]: " + usermessage.getMessage());
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
		} catch (IOException e) { e.printStackTrace(); }
	}
	
	//Write String to Socket's OutputStream
	private void write(Socket sock, String message) {
		Debug.log("Writing to socket... [sock=" + sock + " message=" + message + "]");
		if (sock != null) try {
			OutputStream ostream = sock.getOutputStream();
			message += "\n";
			byte[] buff = message.getBytes();
			ostream.write(buff);
		} catch (IOException e) { e.printStackTrace(); }
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
		String str = serverSock.toString();
		if (this.isOpen()) str += "{hostname=" + this.hostname + " hostaddress=" + this.hostaddress + "}";
		return str;
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