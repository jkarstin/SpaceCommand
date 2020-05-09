package ph.games.scg.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

import ph.games.scg.game.Core;
import ph.games.scg.screen.GameScreen;
import ph.games.scg.util.Assets;
import ph.games.scg.util.Settings;

public class PauseWidget extends Actor {
   
	private Window window;
	private TextButton closeDialogue, restartButton, quitButton;
	private Stage stage;
   
   public PauseWidget(Stage s) {
	   this.stage = s;
		setWidgets();
		configureWidgets();
		setListeners();
   }
   
	private void setWidgets() {
		this.window = new Window("Pause", Assets.skin);
		this.closeDialogue = new TextButton("X", Assets.skin);
		this.restartButton = new TextButton("Restart", Assets.skin);
		this.quitButton = new TextButton("Quit", Assets.skin);
	}
	
	private void configureWidgets() {
		this.window.getTitleTable().add(this.closeDialogue).height(this.window.getPadTop());
		this.window.add(this.restartButton);
		this.window.add(this.quitButton);
	}
	
	public void activate() {
		handleUpdates();
	}
	
	private void setListeners() {
		this.addListener(new InputListener() {
			
			@Override
			public boolean keyDown(InputEvent event, int keycode) {
				if (keycode == Input.Keys.ESCAPE) {
					handleUpdates();
					return true;
				}
				return false;
			}
			
		});
		
		this.closeDialogue.addListener(new ClickListener() {
			
			@Override
			public void clicked(InputEvent event, float x, float y) {
				handleUpdates();
			}
			
		});
		
		this.restartButton.addListener(new ClickListener() {
			
			@Override
			public void clicked(InputEvent event, float x, float y) {
				Core.setActiveScreen(new GameScreen());
			}
			
		});
		
		this.quitButton.addListener(new ClickListener() {
			
			@Override
			public void clicked(InputEvent event, float x, float y) {
				Gdx.app.exit();
			}
			
		});
	}
	
	private void handleUpdates() {
		if (this.window.getStage() == null) {
			this.stage.addActor(this.window);
			Gdx.input.setCursorCatched(false);
			Settings.Paused = true;
		}
		else {
			this.window.remove();
			Gdx.input.setCursorCatched(true);
			Settings.Paused = false;
		}
	}
	
	@Override
	public void setPosition(float x, float y) {
		super.setPosition(x, y);
		this.window.setPosition(
			(Core.VIRTUAL_WIDTH  - this.window.getWidth() )/2f,
			(Core.VIRTUAL_HEIGHT - this.window.getHeight())/2f
		);
	}
	
	@Override
	public void setSize(float width, float height) {
		super.setSize(width, height);
		this.window.setSize(width*2f, height*2f);
	}
   
}