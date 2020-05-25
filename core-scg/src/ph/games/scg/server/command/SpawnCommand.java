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
	public SpawnCommand(String name, String positionString) {
		super(CMD_TYP.SPAWN, null);
		
		this.name = name;
		
		String[] positionData = positionString.split(",");
		if (positionData.length == 3) {
			this.position = new Vector3(
					Float.valueOf(positionData[0]),
					Float.valueOf(positionData[1]),
					Float.valueOf(positionData[2])
					);
		}
		else {
			this.position = null;
		}
	}
	
	public String getName() {
		return this.name;
	}
	
	public Vector3 getPosition() {
		return this.position;
	}
	
	@Override
	public String toCommandString() {
		String str = "\\spawn " + this.name;
		if (this.position != null) str += " " + this.position.x + "," + this.position.y + "," + this.position.z;
		return str;
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
