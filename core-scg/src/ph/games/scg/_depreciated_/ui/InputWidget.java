package ph.games.scg._depreciated_.ui;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;

public class InputWidget extends Actor {
	
	private PauseWidget pauseWidget;
	private ChatWidget chatWidget;
	
	public InputWidget(PauseWidget pw, ChatWidget cw) {
		this.pauseWidget = pw;
		this.chatWidget = cw;
		setListeners();
	}
	
	private void setListeners() {
		super.addListener(new InputListener() {
			
			@Override
			public boolean keyDown(InputEvent event, int keycode) {
				switch (keycode) {
				case Input.Keys.ESCAPE:
					pauseWidget.activate();
					return true;
				case Input.Keys.SLASH:
					chatWidget.activate();
					return true;
				default:
					return false;
				}
			}
			
		});
	}
	
}
