package ph.games.scg._depreciated_.screen;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

import ph.games.scg._depreciated_.game.GameCore;
import ph.games.scg.screen.BaseScreen;
import ph.games.scg.util.Assets;

public class MainMenuScreen extends BaseScreen {
	
	private TextField usernameField;
	private TextField passwordField;
	private TextButton loginButton;
	
	private void processLogin() {
		this.stage.setKeyboardFocus(null);
		GameCore.client.login(this.usernameField.getText(), this.passwordField.getText());
		GameCore.setActiveScreen(new GameScreen());
	}
	
	@Override
	protected void initialize() {
		this.usernameField = new TextField("", Assets.skin);
		this.usernameField.setMessageText("Username");
		this.usernameField.addListener(new ClickListener() {
			
			@Override
			public void clicked(InputEvent event, float x, float y) {
				stage.setKeyboardFocus(usernameField);
			}
			
		});
		this.usernameField.addListener(new InputListener() {
			
			@Override
			public boolean keyDown(InputEvent event, int keycode) {
				switch (keycode) {
				case Input.Keys.ENTER:
					stage.setKeyboardFocus(passwordField);
					return true;
				default:
					return false;
				}
			}
			
		});
		this.table.add(this.usernameField);
		
		this.passwordField = new TextField("", Assets.skin);
		this.passwordField.setMessageText("Password");
		this.passwordField.setPasswordMode(true);
		this.passwordField.setPasswordCharacter('*');
		this.passwordField.addListener(new ClickListener() {
			
			@Override
			public void clicked(InputEvent event, float x, float y) {
				stage.setKeyboardFocus(passwordField);
			}
			
		});
		this.passwordField.addListener(new InputListener() {
			
			@Override
			public boolean keyDown(InputEvent event, int keycode) {
				switch (keycode) {
				case Input.Keys.ENTER:
					processLogin();
					return true;
				default:
					return false;
				}
			}
			
		});
		this.table.add(this.passwordField);
		
		this.loginButton = new TextButton("LOGIN", Assets.skin);
		this.loginButton.addListener(new ClickListener() {
			
			@Override
			public void clicked(InputEvent event, float x, float y) {
				processLogin();
			}
			
		});
		this.table.add(this.loginButton);
	}
	
	@Override
	protected void update(float dt) { }

}
