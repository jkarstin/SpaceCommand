/*********************************************
 * Server.java
 * 
 * Manages server side connection and
 * communications for TCP/IP server design.
 * 
 * J Karstin Neill    05.03.2020
 *********************************************/

package ph.games.scg.server;

import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;

import com.badlogic.gdx.utils.Array;

public class Server {
	
	public static final int DEFAULT_BACKLOG = 16;
	public static final int DEFAULT_TIMEOUT = 10;

	private int port;
	private int clientBacklog;
	private int soTimeout;
	private ServerSocket serverSocket;
	private String hostName;
	private String hostAddress;
	//private int hostPort;
	private Array<Socket> clientSockets;
	private byte[] readBytes;

	public Server(int port, int backlog, int timeout) {
		this.port = port;
		this.clientBacklog = backlog;
		this.soTimeout = timeout;

		this.clientSockets = new Array<Socket>(this.clientBacklog);
		this.readBytes = new byte[100];
	}
	public Server(int port) { this(port, Server.DEFAULT_BACKLOG, Server.DEFAULT_TIMEOUT); }

	public void update(float dt) {
		
	}
	
	public Server open() {
		if (this.serverSocket != null && !this.serverSocket.isClosed()) return this;

		try {
			this.serverSocket = new ServerSocket(this.port, this.clientBacklog);
			this.serverSocket.setSoTimeout(this.soTimeout);
			this.hostName = InetAddress.getLocalHost().getHostName();
			this.hostAddress = InetAddress.getLocalHost().getHostAddress();
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		return this;
	}

	public Client accept() {
		if (this.serverSocket == null || this.serverSocket.isClosed() || this.clientSockets.size >= this.clientBacklog) return null;

		Socket clientSocket = null;

		try {
			clientSocket = this.serverSocket.accept();
		}
		catch (SocketTimeoutException e) {
			//System.out.println("Server Timeout");
			return null;
		}
		catch (Exception e) {
			e.printStackTrace();
			return null;
		}

		if (clientSocket != null) {
			this.clientSockets.add(clientSocket);
		}

		System.out.println("Accepted client: " + clientSocket.toString());

		return new Client(clientSocket);
	}

	public Server close() {
		if (this.serverSocket.isClosed()) return this;

		try {
			if (!this.serverSocket.isClosed()) this.serverSocket.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		return this;
	}

	public int read(Client client) {
		try {
			return client.getInStream().read(this.readBytes);
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		return 0;
	}

	public void write(Client client, byte[] bytes, int n) {
		try {
			client.getOutStream().write(bytes, 0, n);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void broadcast(byte[] bytes, int n) {
		for (Socket client : this.clientSockets) {
			try {
				client.getOutputStream().write(bytes, 0, n);
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public byte[] getReadBytes() {
		return this.readBytes;
	}

	public int clientCount() {
		return this.clientSockets.size;
	}

	public String getHostName() {
		return this.hostName;
	}

	public String getHostAddress() {
		return this.hostAddress;
	}

	public String toString() {
		String str = serverSocket.toString() + "\n" +
				"Host Name: " + this.hostName + "\n" +
				"Host Address: " + this.hostAddress;
		return str;
	}

	public String getShortTag() {
		String tag = "S" + this.port;
		return tag;
	}

}