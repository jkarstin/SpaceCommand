package ph.games.scg.server.command;

import java.net.Socket;

import com.badlogic.gdx.math.Vector3;

public class MoveCommand extends Command {
	
	private String name;
	private Vector3 moveVector;
	private float facing;
	private float dt;
	
	public MoveCommand(Socket sock, String name, Vector3 movement, float theta, float delta) {
		super(CMD_TYP.MOVE, sock);
		this.name = name;
		this.moveVector = movement;
		this.facing = theta;
		this.dt = delta;
	}
	public MoveCommand(Socket sock, String name, float x, float y, float z, float theta, float delta) { this(sock, name, new Vector3(x, y, z), theta, delta); }
	public MoveCommand(String name, Vector3 movement, float theta, float delta) { this(null, name, movement, theta, delta); }
	public MoveCommand(Socket sock, String name, String args) {
		super(CMD_TYP.MOVE, sock);
		this.name = name;
		
		String[] argsSplit = args.split(",");
		if (argsSplit != null && argsSplit.length == 5) {
			this.moveVector = new Vector3(Float.valueOf(argsSplit[0]), Float.valueOf(argsSplit[1]), Float.valueOf(argsSplit[2]));
			this.facing = Float.valueOf(argsSplit[3]);
			this.dt = Float.valueOf(argsSplit[4]);
		}
		else {
			this.moveVector = new Vector3();
			this.facing = 0f;
			this.dt = 0f;
		}
	}
	public MoveCommand(String name, String args) { this(null, name, args); }
	
	public void addMoveCommand(MoveCommand other) {
		this.moveVector.add(other.moveVector);
		this.facing = other.facing;
		this.dt += other.dt;
	}
	
	public String getName() {
		return this.name;
	}
	
	public Vector3 getMoveVector() {
		return this.moveVector;
	}
	
	public float getFacing() {
		return this.facing;
	}
	
	public float getDeltaTime() {
		return this.dt;
	}
	
	@Override
	public String toCommandString() {
		return "\\move " + this.name + " " + this.moveVector.x + "," + this.moveVector.y + "," + this.moveVector.z + "," + this.facing + "," + this.dt;
	}
	
	@Override
	public String toString() {
		String str = super.toString() + "{name=" + this.name;
		str += " moveVector=" + this.moveVector.toString();
		str += " facing=" + this.facing;
		str += " dt=" + this.dt;
		str += "}";
		return str;
	}

}
