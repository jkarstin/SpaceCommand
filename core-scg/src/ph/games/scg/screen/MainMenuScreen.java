package ph.games.scg.screen;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

import ph.games.scg.game.Core;
import ph.games.scg.util.Assets;

public class MainMenuScreen extends BaseScreen {
	
	private TextField usernameField;
	private TextField passwordField;
	private TextButton loginButton;
	
	private void processLogin() {
		Core.client.login(this.usernameField.getText(), this.passwordField.getText());
	}
	
	@Override
	protected void initialize() {
		this.usernameField = new TextField("", Assets.skin);
		this.usernameField.setMessageText("Username");
		this.passwordField = new TextField("", Assets.skin);
		this.passwordField.setMessageText("Password");
		this.passwordField.setPasswordMode(true);
		this.passwordField.setPasswordCharacter('*');
		this.loginButton = new TextButton("LOGIN", Assets.skin);
		this.loginButton.addListener(new ClickListener() {
			
			@Override
			public void clicked(InputEvent event, float x, float y) {
				processLogin();
			}
			
		});
	}
	
	@Override
	protected void update(float dt) { }

}
