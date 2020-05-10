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
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;

import com.badlogic.gdx.physics.bullet.linearmath.int4;

import ph.games.scg.server.command.Command;
import ph.games.scg.util.Debug;
import ph.games.scg.util.ILoggable;

public class Server implements ILoggable {
	
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
	
	//Client ArrayList
	private ArrayList<Socket> socks;
	//Command Queue
	private ArrayList<Command> commandQ;
	//Broadcast Queue
	private ArrayList<String> broadcastQ;
	
	//Command definitions?
	
	public Server(int port, int timeout) {
		this.serverSock = null;
		this.port = port;
		this.opened = false;
		this.uptime = 0f;
		this.soTimeout = timeout;
		
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
			Debug.log("Successfully opened server:" + this);
		} catch (Exception e) {
			Debug.warn("Failed to open server: " + this);
			e.printStackTrace();
		}
	}
	
	public void update(float dt) {
		if (this.isOpen()) {
			this.uptime += dt;
			Debug.logv("Server uptime: " + this.uptime + " ms");
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
			boolean halt=false;
			int count=0;
			
			do {
				try {
					sock = this.serverSock.accept();
				}
				catch (SocketTimeoutException e) {
					Debug.logv("Server timeout reached. Accepted " + count + " new clients.");
					halt = true;
				}
				catch (Exception e) {
					Debug.warn("Failed to accept client!");
					e.printStackTrace();
				}
		
				if (sock != null) {
					this.socks.add(sock);
					count++;
					System.out.println("Accepted client: " + sock.toString());
				}
			} while (!halt);
		}
	}
	
	private void receiveCommands() {
		if (this.isOpen()) {
			//Attempt to read in server messages
			for (Socket sock : this.socks) {
				try {
					InputStream istream = sock.getInputStream();
					int len = istream.available();
					byte[] readBytes = new byte[len];
					int num = istream.read(readBytes);
					for (int b=0; b < num; b++) {
						Debug.logv("Read byte [" + b + "]: " + readBytes[b]);
					}
				}
				catch (IOException e) { e.printStackTrace(); }
				
			}
		}
	}
	
	private void executeCommands() {
		if (this.isOpen()) {
			for (Command command : this.commandQ) {
				switch (command.getType()) {
				case LOGIN:
					break;
				case VERSION:
					break;
				case LOGOUT:
					break;
				case SAY:
					break;
				case TELL:
					break;
				default:
					break;
				}
			}
		}
	}
	
	private void broadcastMessages() {
		if (this.isOpen()) {
			for (String message : this.broadcastQ) {
				//Broadcast message
				Debug.log("Broadcasting message to server... [" + message + "]");
			}
		}
	}
	
	public void close() {
		if (this.serverSock != null) try {
			this.serverSock.close();
			this.opened = false;
			Debug.log("Successfully closed server: " + this + " uptime=" + this.uptime + " ms");
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
	
	@Override
	public String toString() {
		String str = serverSock.toString();
		if (this.isOpen()) str += "{hostname=" + this.hostname + " hostaddress=" + this.hostaddress + "}";
		return str;
	}
	
//********* ORIGINAL SERVER DESIGN *********
//	public static final int DEFAULT_BACKLOG = 16;
//	public static final int DEFAULT_TIMEOUT = 10;
//
//	private int port;
//	private int clientBacklog;
//	private int soTimeout;
//	private ServerSocket serverSocket;
//	private String hostName;
//	private String hostAddress;
//	//private int hostPort;
//	private Array<Socket> clientSockets;
//	private byte[] readBytes;

//	public Server(int port, int backlog, int timeout) {
//		this.port = port;
//		this.clientBacklog = backlog;
//		this.soTimeout = timeout;
//
//		this.clientSockets = new Array<Socket>(this.clientBacklog);
//		this.readBytes = new byte[100];
//	}
//	public Server(int port) { this(port, Server.DEFAULT_BACKLOG, Server.DEFAULT_TIMEOUT); }
	
//	public Server open() {
//		if (this.serverSocket != null && !this.serverSocket.isClosed()) return this;
//
//		try {
//			this.serverSocket = new ServerSocket(this.port, this.clientBacklog);
//			this.serverSocket.setSoTimeout(this.soTimeout);
//			this.hostName = InetAddress.getLocalHost().getHostName();
//			this.hostAddress = InetAddress.getLocalHost().getHostAddress();
//		}
//		catch (Exception e) {
//			e.printStackTrace();
//		}
//
//		return this;
//	}

//	public Client accept() {
//		if (this.serverSocket == null || this.serverSocket.isClosed() || this.clientSockets.size >= this.clientBacklog) return null;
//
//		Socket clientSocket = null;
//
//		try {
//			clientSocket = this.serverSocket.accept();
//		}
//		catch (SocketTimeoutException e) {
//			//System.out.println("Server Timeout");
//			return null;
//		}
//		catch (Exception e) {
//			e.printStackTrace();
//			return null;
//		}
//
//		if (clientSocket != null) {
//			this.clientSockets.add(clientSocket);
//		}
//
//		System.out.println("Accepted client: " + clientSocket.toString());
//
//		return new Client(clientSocket);
//	}

//	public Server close() {
//		if (this.serverSocket.isClosed()) return this;
//
//		try {
//			if (!this.serverSocket.isClosed()) this.serverSocket.close();
//		}
//		catch (Exception e) {
//			e.printStackTrace();
//		}
//
//		return this;
//	}

//	public int read(Client client) {
//		try {
//			return client.getInStream().read(this.readBytes);
//		}
//		catch (Exception e) {
//			e.printStackTrace();
//		}
//
//		return 0;
//	}

//	public void write(Client client, byte[] bytes, int n) {
//		try {
//			client.getOutStream().write(bytes, 0, n);
//		}
//		catch (Exception e) {
//			e.printStackTrace();
//		}
//	}

//	public void broadcast(byte[] bytes, int n) {
//		for (Socket client : this.clientSockets) {
//			try {
//				client.getOutputStream().write(bytes, 0, n);
//			}
//			catch (Exception e) {
//				e.printStackTrace();
//			}
//		}
//	}

//	public byte[] getReadBytes() {
//		return this.readBytes;
//	}
//
//	public int clientCount() {
//		return this.clientSockets.size;
//	}
//
//	public String getHostName() {
//		return this.hostName;
//	}
//
//	public String getHostAddress() {
//		return this.hostAddress;
//	}
//
//	public String toString() {
//		String str = serverSocket.toString() + "\n" +
//				"Host Name: " + this.hostName + "\n" +
//				"Host Address: " + this.hostAddress;
//		return str;
//	}
//
//	public String getShortTag() {
//		String tag = "S" + this.port;
//		return tag;
//	}

}