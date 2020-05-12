package ph.games.scg.environment;

import java.util.ArrayList;

import ph.games.scg.util.Debug;
import ph.games.scg.util.ILoggable;

public class Room implements ILoggable {
	
	private String name;
	private float[] location;
	private float[] dimensions;
	private String texture;
	private String floor;
	private String ceiling;
	private ArrayList<Quad> decorations;
	
	public Room() {
		this.name = null;
		
		this.location = new float[] {0f, 0f, 0f};
		this.dimensions = new float[] {0f, 0f, 0f};
		
		this.texture = null;
		this.floor = null;
		this.ceiling = null;
		
		this.decorations = new ArrayList<Quad>();
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public void setLocation(float x, float y, float z) {
		this.location[0] = x;
		this.location[1] = y;
		this.location[2] = z;
	}
	
	public void setX(float x) {
		this.location[0] = x;
	}
	
	public void setY(float y) {
		this.location[1] = y;
	}
	
	public void setZ(float z) {
		this.location[2] = z;
	}
	
	public void setDimensions(float width, float height, float length) {
		this.dimensions[0] = width;
		this.dimensions[1] = height;
		this.dimensions[2] = length;
	}
	
	public void setWidth(float width) {
		this.dimensions[0] = width;
	}
	
	public void setHeight(float height) {
		this.dimensions[1] = height;
	}
	
	public void setLength(float length) {
		this.dimensions[2] = length;
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
	
	public float getX() {
		return this.location[0];
	}
	
	public float getY() {
		return this.location[1];
	}
	
	public float getZ() {
		return this.location[2];
	}
	
	public float getWidth() {
		return this.dimensions[0];
	}
	
	public float getHeight() {
		return this.dimensions[1];
	}
	
	public float getLength() {
		return this.dimensions[2];
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
		private float[] coords;
		private float[] normal;
		private float[] dimensions;
		
		public Quad() {
			this.texture = null;
			this.coords = new float[] {0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f};
			this.normal = new float[] {0f, 0f, 0f};
			this.dimensions = new float[] {0f, 0f, 0f};
		}
		
		public void setTexture(String texture) {
			this.texture = texture;
		}
		
		public void setCoords(float x0, float y0, float z0, float x1, float y1, float z1, float x2, float y2, float z2, float x3, float y3, float z3) {
			this.coords = new float[] {x0, y0, z0, x1, y1, z1, x2, y2, z2, x3, y3, z3};
			this.calculateNormal();
			this.calculateDimensions();
		}
		
		public String getTexture() {
			return this.texture;
		}
		
		public float[] getCoords() {
			return this.coords;
		}
		
		public float[] getNormal() {
			return this.normal;
		}
		
		public float[] getDimensions() {
			return this.dimensions;
		}
		
		private void calculateNormal() {
			//calculate vector ac
			float[] ac = new float[] {
				this.coords[6]-this.coords[0],
				this.coords[7]-this.coords[1],
				this.coords[8]-this.coords[2]
			};
			//calculate vector bd
			float[] bd = new float[] {
				this.coords[9]-this.coords[3],
				this.coords[10]-this.coords[4],
				this.coords[11]-this.coords[5]
			};
			
			//calculate the cross product ab x ad
			float[] bdxac = new float[] {
				bd[1]*ac[2] - bd[2]*ac[1],
				bd[2]*ac[0] - bd[0]*ac[2],
				bd[0]*ac[1] - bd[1]*ac[0]
			};
			
			//calculate magnitude of cross product result
			float m = (float) Math.sqrt(
					Math.pow(bdxac[0], 2f) +
					Math.pow(bdxac[1], 2f) +
					Math.pow(bdxac[2], 2f)
					);
			
			//update normal value
			this.normal = new float[] {
				bdxac[0]/m,
				bdxac[1]/m,
				bdxac[2]/m
			};
		}
		
		private void calculateDimensions() {
			float[] mMx = new float[] {this.coords[0], this.coords[0]};
			float[] mMy = new float[] {this.coords[1], this.coords[1]};
			float[] mMz = new float[] {this.coords[2], this.coords[2]};
			
			for (int i=3; i < this.coords.length; i++) {
				
				Debug.logv("mMx: " + mMx[0] + "," + mMx[1]);
				Debug.logv("mMy: " + mMy[0] + "," + mMy[1]);
				Debug.logv("mMz: " + mMz[0] + "," + mMz[1]);
				
				if (i%3==0) {
					if (this.coords[i] < mMx[0]) mMx[0] = this.coords[i];
					if (this.coords[i] > mMx[1]) mMx[1] = this.coords[i];
				}
				else if (i%3==1) {
					if (this.coords[i] < mMy[0]) mMy[0] = this.coords[i];
					if (this.coords[i] > mMy[1]) mMy[1] = this.coords[i];
				}
				else {
					if (this.coords[i] < mMz[0]) mMz[0] = this.coords[i];
					if (this.coords[i] > mMz[1]) mMz[1] = this.coords[i];
				}
			}
			
			this.dimensions = new float[] {
				mMx[1]-mMx[0],
				mMy[1]-mMy[0],
				mMz[1]-mMz[0]
			};
		}
		
		@Override
		public String toString() {
			String str = "QUAD{texture=" + this.texture;
			str += " coords=[" + this.coords[0];
			for (int c=1; c < this.coords.length; c++) str += " " + this.coords[c];
			str += "]}";
			return str;
		}
		
	}
	
	@Override
	public String toString() {
		String sep = "\n";
		
		String str = "ROOM{name=" + this.name;
		str += sep + "location=[" + this.location[0] + "," + this.location[1] + "," + this.location[2] + "]";
		str += sep + "dimensions=[" + this.dimensions[0] + "," + this.dimensions[1] + "," + this.dimensions[2] + "]";
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
