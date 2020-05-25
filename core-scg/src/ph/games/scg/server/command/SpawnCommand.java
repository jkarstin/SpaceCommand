package ph.games.scg.server.command;

import java.net.Socket;

import com.badlogic.gdx.math.Vector3;

import ph.games.scg.server.NetEntity.NET_TYP;

public class SpawnCommand extends Command {
	
	//Command format: \spawn U|E name x,y,z
	//	where U: User, E: Enemy
	
	private NET_TYP spawnType;
	private String name;
	private Vector3 position;
	
	public SpawnCommand(Socket sock, NET_TYP spawnType, String name, Vector3 position) {
		super(CMD_TYP.SPAWN, sock);
		
		this.spawnType = spawnType;
		this.name = name;
		this.position = position;
	}
	public SpawnCommand(String typeCode, String name, String positionString) {
		super(CMD_TYP.SPAWN, null);
		
		switch (typeCode) {
		case "U":
			this.spawnType = NET_TYP.USER;
			break;
		case "E":
		default:
			this.spawnType = NET_TYP.ENEMY;
			break;
		}
		
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
	
	public NET_TYP getSpawnType() {
		return this.spawnType;
	}
	
	public String getName() {
		return this.name;
	}
	
	public Vector3 getPosition() {
		return this.position;
	}
	
	@Override
	public String toCommandString() {
		String str = "\\spawn ";
		switch (this.spawnType) {
		case USER:
			str += "U";
			break;
		case ENEMY:
			str += "E";
			break;
		}
		str += " " + this.name;
		if (this.position != null) str += " " + this.position.x + "," + this.position.y + "," + this.position.z;
		return str;
	}
	
	@Override
	public String toString() {
		String str = super.toString() + "{spawnType=" + this.spawnType;
		str += " name=" + this.name;
		str += " position=" + this.position;
		str += "}";
		return str;
	}

}
