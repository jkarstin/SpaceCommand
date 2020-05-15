package ph.games.scg.server.command;

import java.net.Socket;

import com.badlogic.gdx.math.Vector3;

public class SpawnCommand extends Command {
	
	//Command format: \spawn name x,y,z
	
	private String name;
	private Vector3 position;
	
	public SpawnCommand(Socket sock, String name, Vector3 position) {
		super(CMD_TYP.SPAWN, sock);
		
		this.name = name;
		this.position = position;
	}
	
	public String getName() {
		return this.name;
	}
	
	public Vector3 getPosition() {
		return this.position;
	}
	
	@Override
	public String toCommandString() {
		return "\\spawn " + this.name + " " + this.position.x + "," + this.position.y + "," + this.position.z;
	}
	
	@Override
	public String toString() {
		String str = super.toString() + "{";
		str += "name=" + this.name;
		str += " position=" + this.position;
		str += "}";
		return str;
	}

}
