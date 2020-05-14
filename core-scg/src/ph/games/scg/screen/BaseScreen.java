package ph.games.scg.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.viewport.FitViewport;

import ph.games.scg.game.Core;

public abstract class BaseScreen implements Screen {
	
	protected Stage stage;
	protected Table table;
	
	protected abstract void initialize();
	protected abstract void update(float dt);
	
	public BaseScreen() {
		this.stage = new Stage(new FitViewport(Core.VIRTUAL_WIDTH, Core.VIRTUAL_HEIGHT));
		this.table = new Table();
		this.table.setFillParent(true);
		this.stage.addActor(this.table);
		
		Gdx.input.setInputProcessor(this.stage);
		
		this.initialize();
	}

	@Override
	public void render(float dt) {
		this.stage.act();
		this.update(dt);
		this.stage.draw();
	}

	@Override
	public void resize(int width, int height) {
		this.stage.getViewport().update(width, height);
	}

	@Override
	public void dispose() {
		this.stage.dispose();
	}
	
	@Override
	public void show() { }
	
	@Override
	public void hide() { }
	
	@Override
	public void pause() { }
	
	@Override
	public void resume() { }
	
}
