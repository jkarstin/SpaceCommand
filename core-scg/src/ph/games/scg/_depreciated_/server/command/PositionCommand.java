package ph.games.scg._depreciated_.server.command;

import java.net.Socket;

import com.badlogic.gdx.math.Vector3;

public class PositionCommand extends Command {
	
	private String name;
	private Vector3 position;
	
	public PositionCommand(Socket sock, String name, Vector3 position) {
		super(CMD_TYP.POSITION, sock);
		
		this.name = name;
		this.position = position;
	}
	public PositionCommand(Socket sock, String name, String args) {
		super(CMD_TYP.POSITION, sock);
		
		this.name = name;
		
		String[] positionData = args.split(",");
		if (positionData != null && positionData.length == 3) {
			this.position = new Vector3(
					Float.valueOf(positionData[0]),
					Float.valueOf(positionData[1]),
					Float.valueOf(positionData[2])
					);
		}
		else {
			this.position = new Vector3();
		}
	}
	public PositionCommand(String name, String args) { this(null, name, args); }

	public String getName() {
		return this.name;
	}
	
	public Vector3 getPosition() {
		return this.position;
	}
	
	@Override
	public String toCommandString() {
		return "\\pos " + this.name + " " + this.position.x + "," + this.position.y + "," + this.position.z;
	}
	
	@Override
	public String toString() {
		String str = super.toString();
		str += "{name=" + this.name;
		str += " position=" + this.position;
		str += "}";
		return str;
	}
	
}
