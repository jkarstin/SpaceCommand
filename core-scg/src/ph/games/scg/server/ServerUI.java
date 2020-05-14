package ph.games.scg.server;

import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.Align;

import ph.games.scg.game.Core;
import ph.games.scg.util.Assets;
import ph.games.scg.util.ILoggable;

public class ServerUI {
	
	private Stage stage;
	private Label logLabel;
	
	public ServerUI() {
		this.stage = new Stage();
		
		this.logLabel = new Label("", Assets.skin);
		this.logLabel.setWidth(Core.VIRTUAL_WIDTH);
		this.logLabel.setWrap(true);
		this.logLabel.setAlignment(Align.bottomLeft);
		
		this.stage.addActor(this.logLabel);
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
