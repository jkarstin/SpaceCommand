package ph.games.scg.server;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;

import ph.games.scg.game.GameCore;
import ph.games.scg.util.Assets;
import ph.games.scg.util.ILoggable;

public class ServerUI {
	
	private Stage stage;
	private Label logLabel;
	private TextField commandLine;
	
	public ServerUI() {
		ServerCore.server.setServerUI(this);
		
		this.stage = new Stage();
		Gdx.input.setInputProcessor(this.stage);
		
		this.logLabel = new Label("", Assets.skin);
		this.logLabel.setSize(GameCore.VIRTUAL_WIDTH, GameCore.VIRTUAL_HEIGHT-80f);
		this.logLabel.setPosition(0f, 40f, Align.bottomLeft);
		this.logLabel.setWrap(true);
		this.logLabel.setAlignment(Align.bottomLeft);
		this.stage.addActor(this.logLabel);
		
		this.commandLine = new TextField("", Assets.skin);
		this.commandLine.setMessageText("Enter admin command...");
		this.commandLine.setSize(GameCore.VIRTUAL_WIDTH, 40f);
		this.commandLine.setPosition(0f, 0f);
		this.commandLine.addListener(new ClickListener() {
			
			@Override
			public void clicked(InputEvent event, float x, float y) {
				stage.setKeyboardFocus(commandLine);
			}
			
		});
		this.commandLine.addListener(new InputListener() {
			
			@Override
			public boolean keyDown(InputEvent event, int keycode) {
				switch (keycode) {
				case Input.Keys.ENTER:
					ServerCore.server.queueAdminMessage(commandLine.getText());
					commandLine.setText("");
					return true;
				default:
					return false;
				}
			}
			
		});
		this.stage.addActor(this.commandLine);
		
		this.stage.setKeyboardFocus(this.commandLine);
	}
	
	public void log(String s) {
		this.logLabel.setText(this.logLabel.getText() + "\n" + s);
	}
	
	public void log(ILoggable l) {
		this.log(l.toString());
	}
	
	public void update(float dt) {
		this.stage.act(dt);
	}
	
	public void render() {
		this.stage.draw();
	}

}
