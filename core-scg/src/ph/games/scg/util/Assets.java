package ph.games.scg.util;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

public class Assets {
   
   public static Skin skin;
   
   private final static String skinFile = "uiskin";
   
   public Assets() {
      Assets.skin = new Skin();
      FileHandle fileHandle = Gdx.files.internal("skins/" + skinFile + ".json");
      FileHandle atlasFile = fileHandle.sibling(skinFile + ".atlas");
      if (atlasFile.exists()) {
         Assets.skin.addRegions(new TextureAtlas(atlasFile));
      }
      Assets.skin.load(fileHandle);
   }
   
   public static void dispose() {
      Assets.skin.dispose();
   }
   
}