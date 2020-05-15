/*************************************************************************
 * ChatWidget.java
 * 
 * Provides chat server hosting and connecting through an interactive GUI.
 * 
 * TODO: Look into how ServerSocket and Socket communicate.
 * 		 Research how sizing Cells works in Tables and other UI elements.
 * 		
 * 
 * J Karstin Neill    04.08.2020
 *************************************************************************/

package ph.games.scg.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;

import ph.games.scg.game.Core;
import ph.games.scg.util.Assets;
import ph.games.scg.util.Debug;
import ph.games.scg.util.Settings;

public class ChatWidget extends Actor {
	private static final int LOG_MAX_SIZE = 20;
	
	private GameUI gameUI;
	
	private Window chatWindow;
	private TextField chatField;
	private TextButton closeWindow;
	private Label chatLogLabel;
	
	private String[] chatLog;
	private int logLines;
	
	public ChatWidget(GameUI gameUI) {
		this.chatLog = new String[LOG_MAX_SIZE];
		this.logLines = 0;
		this.gameUI = gameUI;
		Core.client.setChatWidget(this);
		setWidgets();
		configureWidgets();
		setListeners();
	}	
	
	private void setWidgets() {
		this.chatWindow = new Window("Chat Window", Assets.skin);
		this.chatField = new TextField("", Assets.skin);
		this.chatField.setMessageText("Click here to enter text...");
		this.closeWindow = new TextButton("X", Assets.skin);
		this.chatLogLabel = new Label("", Assets.skin);
	}

	private void configureWidgets() {
		this.chatLogLabel.setAlignment(Align.bottomLeft);
		this.chatLogLabel.setWrap(true);
		this.chatWindow.getTitleTable().add(this.closeWindow).height(this.chatWindow.getPadTop());
		this.chatWindow.add(this.chatLogLabel).align(Align.bottomLeft).expand().prefWidth(this.chatWindow.getPrefWidth()).row();
		this.chatWindow.add(this.chatField).align(Align.bottomLeft).fill(true, false);
		this.chatWindow.pack();
	}

	private void setListeners() {
		this.addListener(new InputListener() {
			
			@Override
			public boolean keyDown(InputEvent event, int keycode) {
				if (keycode == Input.Keys.SLASH) {
					handleUpdates();
					return true;
				}
				return false;
			}
			
		});
		
		this.closeWindow.addListener(new ClickListener() {

			@Override
			public void clicked(InputEvent event, float x, float y) {
				handleUpdates();
			}

		});
		
		this.chatField.addListener(new ClickListener() {
			
			@Override
			public void clicked(InputEvent event, float x, float y) {
				chatField.setText("");
				gameUI.changeKeyboardFocus(chatField);
			}
			
		});
		this.chatField.addListener(new InputListener() {
			
			@Override
			public boolean keyDown(InputEvent event, int keycode) {
				switch (keycode) {
				case Input.Keys.ESCAPE:
					chatField.setText("");
					gameUI.revertKeyboardFocus();
					return true;
				case Input.Keys.ENTER:
//					logText(chatField.getText());
					Core.client.queueMessage(chatField.getText());
					chatField.setText("");
					return true;
				default:
					return false;
				}
			}
			
		});
	}
	
	public void logText(String str) {
		Debug.log(str);
		if (this.logLines == LOG_MAX_SIZE) {
			for (int l=0; l < LOG_MAX_SIZE-1; l++) {
				this.chatLog[l] = this.chatLog[l+1];
				this.logLines--;
			}
		}
		this.chatLog[this.logLines++] = str;
		this.updateLog();
	}
	
	private void updateLog() {
		String logText = "";
		for (String line : this.chatLog) if (line != null) logText += line + "\n";
		this.chatLogLabel.setText(logText);
	}
	
	public void activate() {
		handleUpdates();
	}

	private void handleUpdates() {
		if (this.chatWindow.getStage() == null) {
			this.gameUI.stage.addActor(this.chatWindow);
			Gdx.input.setCursorCatched(false);
			Settings.Paused = true;
		}
		else {
			this.gameUI.revertKeyboardFocus();
			this.chatWindow.remove();
			Gdx.input.setCursorCatched(true);
			Settings.Paused = false;
		}
	}
	
	@Override
	public void setPosition(float x, float y) {
		super.setPosition(x, y);
		this.chatWindow.setPosition(x, y);
	}

	@Override
	public void setSize(float width, float height) {
		super.setSize(width, height);
		this.chatWindow.setSize(width, height);
	}

}