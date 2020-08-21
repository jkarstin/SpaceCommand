package ph.games.scg.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

import ph.games.scg.util.Assets;

public class StartScreen extends BaseScreen {
	
	private TextButton quitButton;
	
	@Override
	protected void initialize() {
		this.quitButton = new TextButton("Quit", Assets.skin);
		this.quitButton.addListener(new ClickListener() {
			
			@Override
			public void clicked(InputEvent event, float x, float y) {
				Gdx.app.exit();
			}
			
		});
		this.stage.addActor(this.quitButton);
	}

	@Override
	protected void update(float dt) { }
	
}
