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
	private float uptime;
	//Number of milliseconds the server will attempt to look for clients before stopping
	private int soTimeout;
	
	//Message reading
	private byte[] buff;
	private ArrayList<String> messages;
	private String segment;
	private StringTokenizer stoker;
	
	//Client ArrayList
	private ArrayList<Socket> socks;
	//Command Queue
	private ArrayList<Command> commandQ;
	//Broadcast Queue
	private ArrayList<String> broadcastQ;
	
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
		
		this.socks = new ArrayList<Socket>();
		this.commandQ = new ArrayList<Command>();
		this.broadcastQ = new ArrayList<String>();
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
			this.broadcastMessages();
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
				
				//Attempt to get message
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
				
				//Look for command if message was received
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
			
			for (Command command : this.commandQ) {
				switch (command.getType()) {
				case LOGIN:
					break;
				case VERSION:
					String version = "Valid Version " + VERSION_HI + "." + VERSION_LO;
					write(((VersionCommand)command).getSock(), version);
					break;
				case LOGOUT:
					break;
				case SAY:
					this.broadcastQ.add(((SayCommand)command).getMessage());
					break;
				case TELL:
					break;
				default:
					break;
				}
			}
			
			this.commandQ.clear();
		}
	}
	
	private void broadcastMessages() {
		if (this.isOpen()) {
			for (String message : this.broadcastQ) {
				//Broadcast message
				Debug.log("Broadcasting message to server... [" + message + "]");
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
	
}