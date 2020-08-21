package ph.games.scg._depreciated_.server.command;

import java.net.Socket;

import com.badlogic.gdx.math.Vector3;

public class RollCallCommand extends Command {
	
	String username;
	Vector3 position;
	
	public RollCallCommand(Socket sock, String username, Vector3 position) {
		super(CMD_TYP.ROLLCALL, sock);
		
		this.username = username;
		this.position = position;
	}
	
	public RollCallCommand(Socket sock, String username, String args) {
		super(CMD_TYP.ROLLCALL, sock);
		
		this.username = username;
		
		String[] positionData = args.split(",");
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
	
	public String getUsername() {
		return this.username;
	}
	
	public Vector3 getPosition() {
		return this.position;
	}

	@Override
	public String toCommandString() {
		String str = "\\rc " + this.username;
		if (this.position != null) str += " " + this.position.x + "," + this.position.y + "," + this.position.z;
		return str;
	}
	
	@Override
	public String toString() {
		String str = super.toString() + "{username=" + this.username;
		str += " position=" + this.position;
		str += "}";
		return str;
	}
	
}
