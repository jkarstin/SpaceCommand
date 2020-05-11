package ph.games.scg.screen;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;

import ph.games.scg.util.Debug;

public class ClientTestScreen extends BaseScreen {
	
	private static final int BYTE_BUFFER_SIZE = 64;
	
	private Socket sock;
	private byte[] buff;
	private ArrayList<String> messages;
	private String segment;
	
	@Override
	protected void initialize() {
		this.messages = new ArrayList<String>();
		
		this.messages.add("\\login phrongorre pancakes99");
		this.messages.add("\\version");
		this.messages.add("\\login roger foneybaloney");
		this.messages.add("\\tell roger who can it be now?");
		this.messages.add("\\say Hello world!");
		this.messages.add("\\logout");
		this.messages.add("\\logout");
				
		this.sock = open("192.168.1.2", 21595);
		this.buff = new byte[BYTE_BUFFER_SIZE];
		
		write(this.sock, this.messages);
		
		this.messages.clear();
		this.segment = "";
	}

	@Override
	protected void update(float dt) {
		this.read(sock);
		for (String message : this.messages) Debug.log("Server: " + message);
		this.messages.clear();
	}
	
	//Attempt to read from the socket and count number of messages
	private int read(Socket sock) {
		int count = 0;
		
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
						count++;
						this.segment = "";
					}
					else this.segment += (char)(this.buff[b]);
				}
			}
		}
		catch (IOException e) { e.printStackTrace(); }
		
		return count;
	}
	
	//Open a new Socket at specified address and port
	private Socket open(String hostaddress, int port) {
		Socket sock = null;
		try {
			sock = new Socket(hostaddress, port);
		} catch (IOException e) { e.printStackTrace(); }
		return sock;
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
	
	//Close given Socket
	private void close(Socket sock) {
		if (sock != null) try {
			sock.close();
		} catch (IOException e) { e.printStackTrace(); }
	}
	
	@Override
	public void dispose() {
		close(sock);
	}

}
