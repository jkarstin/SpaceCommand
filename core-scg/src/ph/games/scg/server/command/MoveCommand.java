package ph.games.scg.server.command;

import java.net.Socket;

import com.badlogic.gdx.math.Vector3;

public class MoveCommand extends Command {
	
	private String name;
	private Vector3 moveVector;
	private float facing;
	private float dt;
	
	public MoveCommand(Socket sock, String name, float x, float y, float z, float theta, float delta) {
		super(CMD_TYP.MOVE, sock);
		this.name = name;
		this.moveVector = new Vector3(x, y, z);
		this.facing = theta;
		this.dt = delta;
	}
	
	@Override
	public String toString() {
		String str = super.toString() + "_MOVE{name=" + this.name;
		str += " moveVector=" + this.moveVector.toString();
		str += " facing=" + this.facing;
		str += " dt=" + this.dt;
		str += "}";
		return str;
	}

}
