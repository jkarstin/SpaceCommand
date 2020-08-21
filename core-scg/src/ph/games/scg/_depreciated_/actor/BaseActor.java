package ph.games.scg._depreciated_.actor;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Array;

public class BaseActor extends Group {
   
   private Animation<TextureRegion> animation;
   private float elapsedTime;
   private boolean animationPaused;
   
   public BaseActor(float x, float y, Stage s) {
      super();
      
      this.setPosition(x, y);
      s.addActor(this);
      
      this.animation = null;
      this.elapsedTime = 0f;
      this.animationPaused = false;
   }
   
   public BaseActor(Stage s) { this(0f, 0f, s); }
   
   @Override
   public void act(float dt) {
      super.act(dt);
      
      if (!this.animationPaused) {
         this.elapsedTime += dt;
      }
   }
   
   @Override
   public void draw(Batch batch, float parentAlpha) {
      Color c = this.getColor();
      batch.setColor(c);
      
      if (this.isVisible()) {
         batch.draw(
            this.animation.getKeyFrame(this.elapsedTime),
            this.getX(),
            this.getY(),
            this.getOriginX(),
            this.getOriginY(),
            this.getWidth(),
            this.getHeight(),
            this.getScaleX(),
            this.getScaleY(),
            this.getRotation()
         );
      }
      
      super.draw(batch, parentAlpha);
   }
   
   public void setAnimation(Animation<TextureRegion> animation) {
		this.animation = animation;
		TextureRegion tr = this.animation.getKeyFrame(0);
		float w = tr.getRegionWidth();
		float h = tr.getRegionHeight();
		this.setSize(w, h);
		this.setOrigin(w/2, h/2);
	}
	
	public void setAnimationPaused(boolean pause) {
		this.animationPaused = pause;
	}
	
	public Animation<TextureRegion> loadAnimationFromFiles(String[] fileNames, float frameDuration, boolean loop) {
		int fileCount = fileNames.length;
		Array<TextureRegion> textureArray = new Array<TextureRegion>();
		
		for (int n=0; n < fileCount; n++) {
			String fileName = fileNames[n];
			Texture texture = new Texture(Gdx.files.internal(fileName));
			texture.setFilter(TextureFilter.Linear, TextureFilter.Linear);
			textureArray.add(new TextureRegion(texture));
		}
		
		Animation<TextureRegion> anim = new Animation<TextureRegion>(frameDuration, textureArray);
		
		if (loop) {
			anim.setPlayMode(Animation.PlayMode.LOOP);
		}
		else {
			anim.setPlayMode(Animation.PlayMode.NORMAL);
		}
		
		if (this.animation == null) {
			this.setAnimation(anim);
		}
		
		return anim;
	}
	
	public Animation<TextureRegion> loadAnimationFromSheet(String fileName, int rows, int cols, float frameDuration, boolean loop) {
		Texture texture = new Texture(Gdx.files.internal(fileName));
		texture.setFilter(TextureFilter.Linear, TextureFilter.Linear);
		int frameWidth = texture.getWidth() / cols;
		int frameHeight = texture.getHeight() / rows;
		
		TextureRegion[][] temp = TextureRegion.split(texture, frameWidth, frameHeight);
		Array<TextureRegion> textureArray = new Array<TextureRegion>();
		
		for (int r=0; r < rows; r++) {
			for (int c=0; c < cols; c++) {
				textureArray.add(temp[r][c]);
			}
		}
		
		Animation<TextureRegion> anim = new Animation<TextureRegion>(frameDuration, textureArray);
		
		if (loop) {
			anim.setPlayMode(Animation.PlayMode.LOOP);
		}
		else {
			anim.setPlayMode(Animation.PlayMode.NORMAL);
		}
		
		if (this.animation == null) {
			this.setAnimation(anim);
		}
		
		return anim;
	}
	
	public Animation<TextureRegion> loadTexture(String fileName) {
		String[] fileNames = new String[1];
		fileNames[0] = fileName;
		return loadAnimationFromFiles(fileNames, 1f, true);
	}
	
	public boolean isAnimationFinished() {
		return this.animation.isAnimationFinished(this.elapsedTime);
	}
	
	public void setOpacity(float opacity) {
		this.getColor().a = opacity;
	}
   
   //TODO: Add getList(Class<? extends BaseActor> actorClass, Stage stage)
   
}