package ph.games.scg.server;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;

import ph.games.scg.screen.ClientTestScreen;
import ph.games.scg.util.Assets;
import ph.games.scg.util.Debug;
import ph.games.scg.util.Debug.DEBUG_MODE;

public class ClientTest extends Game {
	
	private void runTest() {
		ArrayList<String> messages = new ArrayList<String>();
		
		messages.add("\\login phrongorre pancakes99\n");
		messages.add("\\version\n");
		messages.add("\\tell roger who can it be now?\n");
		messages.add("\\say Hello world!\n");
		messages.add("\\logout\n");
				
		Socket sock = open("192.168.1.2", 21595);
		write(sock, messages);
		close(sock);
		
		Gdx.app.exit();
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
				byte[] buff = message.getBytes();
				int len = buff.length;
				
				ostream.write(buff, 0, len);
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
	public void create() {
		Debug.setMode(DEBUG_MODE.ON_VERBOSE);
		new Assets();
		this.setScreen(new ClientTestScreen());
		
		this.runTest();
	}

}
