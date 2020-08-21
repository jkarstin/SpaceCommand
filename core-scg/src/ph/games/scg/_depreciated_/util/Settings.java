package ph.games.scg._depreciated_.util;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;

public class Settings {
   
   public static boolean Paused;
   public static boolean soundEnabled = true;
   public final static String file = ".spacecommand";
   
   public static void load() {
      try {
         FileHandle fh = Gdx.files.external(Settings.file);
         String[] strings = fh.readString().split("\n");
         Settings.soundEnabled = Boolean.parseBoolean(strings[0]);
      }
      catch (Exception e) {
         System.out.println("Settings file '" + file + "' could not be located. Loading default values.");
      }
   }
   
   public static void save() {
      try {
         FileHandle fh = Gdx.files.external(Settings.file);
         fh.writeString(Boolean.toString(Settings.soundEnabled) + "\n", false);
      }
      catch (Exception e) {
         e.printStackTrace();
      }
   }
   
}