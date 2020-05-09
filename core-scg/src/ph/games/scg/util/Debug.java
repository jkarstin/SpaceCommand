package ph.games.scg.util;

import java.util.ArrayList;

import com.badlogic.gdx.Gdx;

public class Debug {
	
	public static enum DEBUG_MODE {
		OFF,
		ON,
		ON_VERBOSE
	}
	
	private static DEBUG_MODE MODE;
	
	static {
		Debug.MODE = DEBUG_MODE.OFF;
	}
	
	public static boolean isOn() {
		switch (Debug.MODE) {
		case ON:
		case ON_VERBOSE:
			return true;
		default:
			return false;
		}
	}
	
	public static void log(String s) {
		switch (Debug.MODE) {
		case ON_VERBOSE:
			System.out.print("[[DEBUG]]:\t");
		default:
			System.out.println(s);
		case OFF:
			break;
		};
	}
	
	public static void log() { Debug.log(""); }
	
	public static void logv(String s) {
		switch (Debug.MODE) {
		default:
			System.out.println("[[VERBOSE]]:\t" + s);
		case ON:
		case OFF:
			break;
		};
	}
	
	public static void err(String s) {
		switch (Debug.MODE) {
		case ON_VERBOSE:
			System.err.print("[[ERROR]]:\t");
		default:
			System.err.println("[!!!]" + s + "[!!!]");
		case OFF:
			break;
		};
		Gdx.app.exit();
	}

	public static void warn(String s) {
		switch (Debug.MODE) {
		case ON_VERBOSE:
			System.err.print("[[WARNING]]:\t");
		default:
			System.err.println("[!!!]" + s + "[!!!]");
		case OFF:
			break;
		};
	}
	
	//Overloaded wrapper versions for object logging
	
	public static void log(ILoggable l) {
		if (l == null) return;
		Debug.log(l.toString());
	}
	
	public static void log(ArrayList<? extends ILoggable> list) {
		if (list == null) return;
		Debug.log("ArrayList[" + list.size() + "] contents:");
		for (ILoggable elem : list) Debug.log(elem);
	}
	
	public static void logv(ILoggable l) {
		if (l == null) return;
		Debug.logv(l.toString());
	}
	
	public static void logv(ArrayList<? extends ILoggable> list) {
		if (list == null) return;
		Debug.logv("ArrayList[" + list.size() + "] contents:");
		for (ILoggable elem : list) Debug.logv(elem);
	}
	
	public static void err(ILoggable l) {
		if (l == null) return;
		Debug.err(l.toString());
	}
	
	public static void err(ArrayList<? extends ILoggable> list) {
		if (list == null) return;
		Debug.err("ArrayList[" + list.size() + "] contents:");
		for (ILoggable elem : list) Debug.err(elem);
	}
	
	public static void warn(ILoggable l) {
		if (l == null) return;
		Debug.warn(l.toString());
	}
	
	public static void warn(ArrayList<? extends ILoggable> list) {
		if (list == null) return;
		Debug.warn("ArrayList[" + list.size() + "] contents:");
		for (ILoggable elem : list) Debug.warn(elem);
	}
	
	public static void setMode(DEBUG_MODE m) {
		Debug.MODE = m;
	}
	
}
