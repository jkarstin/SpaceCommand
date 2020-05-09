package ph.games.scg.server;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.Socket;

public class Client {

	private String hostAddress;
	private int hostPort;
	private String serverAddress;
	private int serverPort;
	private Socket socket;
	private InputStream inStream;
	private OutputStream outStream;
	private byte[] readBytes;

	public Client(Socket socket) {
		this.socket = socket;

		this.hostAddress = socket.getInetAddress().getHostAddress();
		this.hostPort = socket.getPort();
		this.serverAddress = socket.getLocalAddress().getHostAddress();
		this.serverPort = socket.getLocalPort();

		try {
			this.inStream = socket.getInputStream();
			this.outStream = socket.getOutputStream();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	public Client(String serverAddress, int serverPort) {
		this.socket = null;
		this.serverAddress = serverAddress;
		this.serverPort = serverPort;
		this.inStream = null;
		this.outStream = null;
	}
	
	public void setServerIP(String serverAddress) {
		this.serverAddress = serverAddress;
	}
	
	public Client connect() {
		this.socket = null;

		try {
			this.socket = new Socket(this.serverAddress, this.serverPort);
		}
		catch (ConnectException ce) {
			System.out.println("Failed to connect: connection refused");
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		if (this.socket != null) {
			this.hostAddress = socket.getLocalAddress().getHostAddress();
			this.hostPort = socket.getLocalPort();

			try {
				this.inStream = this.socket.getInputStream();
				this.outStream = this.socket.getOutputStream();
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}

		return this;
	}

	public Client close() {
		if (this.socket == null || this.socket.isClosed()) return this;

		try {
			this.socket.close();
			this.inStream = null;
			this.outStream = null;
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		return this;
	}

	public int read() {
		try {
			return this.inStream.read(this.readBytes);
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		return 0;
	}
	
	public InputStream getInStream() {
		if (this.socket != null) return this.inStream;
		else return null;
	}
	
	public OutputStream getOutStream() {
		if (this.socket != null) return this.outStream;
		else return null;
	}
	
	public void write(byte[] writeBytes) {
		try {
			this.outStream.write(writeBytes);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	public String toString() {
		String str = socket.toString() + "\n" +
				"Host Address: " + this.hostAddress + "\n" +
				"Host Port: " + this.hostPort + "\n" +
				"Server Address: " + this.serverAddress + "\n" +
				"Server Port: " + this.serverPort;
		return str;
	}

	public String getShortTag() {
		String tag = "C" + this.hostPort;
		return tag;
	}

	public boolean isClosed() {
		return (this.socket == null || this.socket.isClosed());
	}

}