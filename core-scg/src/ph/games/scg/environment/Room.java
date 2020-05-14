package ph.games.scg.environment;

import java.util.ArrayList;

import com.badlogic.gdx.math.Vector3;

import ph.games.scg.util.Debug;
import ph.games.scg.util.ILoggable;

public class Room implements ILoggable {
	
	private String name;
	private Vector3 position;
	private Vector3 dimensions;
	private String texture;
	private String floor;
	private String ceiling;
	private ArrayList<Quad> decorations;
	
	public Room() {
		this.name = null;
		
		this.position = new Vector3();
		this.dimensions = new Vector3();
		
		this.texture = null;
		this.floor = null;
		this.ceiling = null;
		
		this.decorations = new ArrayList<Quad>();
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public void setLocation(float x, float y, float z) {
		this.position.x = x;
		this.position.y = y;
		this.position.z = z;
	}
	
	public void setDimensions(float width, float height, float length) {
		this.dimensions.x = width;
		this.dimensions.y = height;
		this.dimensions.z = length;
	}
	
	public void setTexture(String texture) {
		this.texture = texture;
	}
	
	public void setFloor(String floor) {
		this.floor = floor;
	}
	
	public void setCeiling(String ceiling) {
		this.ceiling = ceiling;
	}
	
	public void addDecoration(Quad decoration) {
		this.decorations.add(decoration);
	}
	
	public String getName() {
		return this.name;
	}
	
	public Vector3 getPosition() {
		return this.position;
	}
	
	public Vector3 getDimensions() {
		return this.dimensions;
	}
	
	public String getTexture() {
		return this.texture;
	}
	
	public String getFloor() {
		return this.floor;
	}
	
	public String getCeiling() {
		return this.ceiling;
	}
	
	public ArrayList<Quad> getDecorations() {
		return this.decorations;
	}
	
	public static class Quad implements ILoggable {
		
		private String texture;
		private Vector3 cornerBL;
		private Vector3 cornerBR;
		private Vector3 cornerTR;
		private Vector3 cornerTL;
		private Vector3 normal;
		private Vector3 dimensions;
		
		public Quad() {
			this.texture = null;
			this.cornerBL = new Vector3();
			this.cornerBR = new Vector3();
			this.cornerTR = new Vector3();
			this.cornerTL = new Vector3();
			this.normal = new Vector3();
			this.dimensions = new Vector3();
		}
		
		public void setTexture(String texture) {
			this.texture = texture;
		}
		
		public void setCorners(Vector3 bl, Vector3 br, Vector3 tr, Vector3 tl) {
			this.cornerBL = bl.cpy();
			this.cornerBR = br.cpy();
			this.cornerTR = tr.cpy();
			this.cornerTL = tl.cpy();
			this.calculateNormal();
			this.calculateDimensions();
		}
		
		public String getTexture() {
			return this.texture;
		}
		
		public Vector3 getBottomLeft() {
			return this.cornerBL;
		}
		
		public Vector3 getBottomRight() {
			return this.cornerBR;
		}
		
		public Vector3 getTopRight() {
			return this.cornerTR;
		}
		
		public Vector3 getTopLeft() {
			return this.cornerTL;
		}
		
		public Vector3 getNormal() {
			return this.normal;
		}
		
		public Vector3 getDimensions() {
			return this.dimensions;
		}
		
		private void calculateNormal() {
			Vector3 dia0 = this.cornerTR.cpy();
			dia0.sub(this.cornerBL);
			
			Vector3 dia1 = this.cornerTL.cpy();
			dia1.sub(this.cornerBR);
			
			Vector3 nml = dia0.crs(dia1).nor();
			
			this.normal = nml;
		}
		
		private void calculateDimensions() {
			float[] mMx = new float[] {this.cornerBL.x, this.cornerBL.x};
			float[] mMy = new float[] {this.cornerBL.y, this.cornerBL.y};
			float[] mMz = new float[] {this.cornerBL.z, this.cornerBL.z};
			
			if (this.cornerBR.x < mMx[0]) mMx[0] = this.cornerBR.x;
			if (this.cornerTR.x < mMx[0]) mMx[0] = this.cornerTR.x;
			if (this.cornerTL.x < mMx[0]) mMx[0] = this.cornerTL.x;
			if (this.cornerBR.x > mMx[1]) mMx[1] = this.cornerBR.x;
			if (this.cornerTR.x > mMx[1]) mMx[1] = this.cornerTR.x;
			if (this.cornerTL.x > mMx[1]) mMx[1] = this.cornerTL.x;
			
			if (this.cornerBR.y < mMy[0]) mMy[0] = this.cornerBR.y;
			if (this.cornerTR.y < mMy[0]) mMy[0] = this.cornerTR.y;
			if (this.cornerTL.y < mMy[0]) mMy[0] = this.cornerTL.y;
			if (this.cornerBR.y > mMy[1]) mMy[1] = this.cornerBR.y;
			if (this.cornerTR.y > mMy[1]) mMy[1] = this.cornerTR.y;
			if (this.cornerTL.y > mMy[1]) mMy[1] = this.cornerTL.y;
			
			if (this.cornerBR.z < mMz[0]) mMz[0] = this.cornerBR.z;
			if (this.cornerTR.z < mMz[0]) mMz[0] = this.cornerTR.z;
			if (this.cornerTL.z < mMz[0]) mMz[0] = this.cornerTL.z;
			if (this.cornerBR.z > mMz[1]) mMz[1] = this.cornerBR.z;
			if (this.cornerTR.z > mMz[1]) mMz[1] = this.cornerTR.z;
			if (this.cornerTL.z > mMz[1]) mMz[1] = this.cornerTL.z;
			
			this.dimensions = new Vector3(
					mMx[1]-mMx[0],
					mMy[1]-mMy[0],
					mMz[1]-mMz[0]
					);
		}
		
		@Override
		public String toString() {
			String str = "QUAD{texture=" + this.texture;
			str += " cornerBL=" + this.cornerBL;
			str += " cornerBR=" + this.cornerBR;
			str += " cornerTR=" + this.cornerTR;
			str += " cornerTL=" + this.cornerTL;
			str += " normal=" + this.normal;
			str += " dimensions=" + this.dimensions;
			str += "}";
			return str;
		}
		
	}
	
	@Override
	public String toString() {
		String sep = "\n";
		
		String str = "ROOM{name=" + this.name;
		str += sep + "position=" + this.position;
		str += sep + "dimensions=" + this.dimensions;
		str += sep + "texture=" + this.texture;
		str += sep + "floor=" + this.floor;
		str += sep + "ceiling=" + this.ceiling;
		str += sep + "decorations=[";
		if (this.decorations.size() > 0) {
			str += sep + this.decorations.get(0);
			for (int d=1; d < this.decorations.size(); d++) str += "," + sep + this.decorations.get(d); 
		}
		str += sep + "]}";
		return str;
	}

}
