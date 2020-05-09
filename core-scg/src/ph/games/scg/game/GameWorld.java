package ph.games.scg.game;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
//See <http://javadox.com/com.badlogicgames.gdx/gdx-bullet/1.3.1/overview-summary.html> for API info
import com.badlogic.gdx.physics.bullet.Bullet;
import com.badlogic.gdx.physics.bullet.DebugDrawer;
import com.badlogic.gdx.physics.bullet.linearmath.btIDebugDraw;

import ph.games.scg.component.CharacterComponent;
import ph.games.scg.system.BulletSystem;
import ph.games.scg.system.PlayerSystem;
import ph.games.scg.system.RenderSystem;
import ph.games.scg.ui.GameUI;
import ph.games.scg.util.EntityFactory;
import ph.games.scg.util.Settings;

public class GameWorld {

	private static final boolean debug = true;

	private BulletSystem bulletSystem;
	private DebugDrawer debugDrawer;
	private Engine engine;
	private Entity character;
	private GameUI gameUI;
	private PlayerSystem playerSystem;
	private RenderSystem renderSystem;

	public GameWorld(GameUI gameUI) {
		Bullet.init(); //Load in Bullet.dll

		setDebug();

		initWorld(gameUI);
		addSystems();
		addEntities();
	}

	private void setDebug() {
		if (GameWorld.debug) {
			this.debugDrawer = new DebugDrawer();
			this.debugDrawer.setDebugMode(btIDebugDraw.DebugDrawModes.DBG_MAX_DEBUG_DRAW_MODE);
		}
	}

	private void initWorld(GameUI gameUI) {
		this.engine = new Engine();
		this.gameUI = gameUI;
	}

	private void addSystems() {
		this.engine.addSystem(this.renderSystem = new RenderSystem());
		this.engine.addSystem(this.bulletSystem = new BulletSystem());
		this.engine.addSystem(this.playerSystem = new PlayerSystem(this.renderSystem.getPerspectiveCamera(), this.bulletSystem, this.gameUI));
		if (GameWorld.debug) this.bulletSystem.collisionWorld.setDebugDrawer(this.debugDrawer);
	}

	private void addEntities() {
		loadLevel();
		createPlayer(5f, 3f, 5f);
		createSpaceship(2f, 3f, 1f);
	}

	private void loadLevel() {
		engine.addEntity(EntityFactory.loadScene(0f, 0f, 0f));
	}

	private void createPlayer(float x, float y, float z) {
		this.character = EntityFactory.createPlayer(this.bulletSystem, x, y, z);
		this.engine.addEntity(this.character);
	}
	
	private void createSpaceship(float x, float y, float z) {
		Entity spaceshipEntity = EntityFactory.createSpaceship("spaceship.g3dj", this.bulletSystem, x, y, z);
		this.engine.addEntity(spaceshipEntity);
	}
	
	public void dispose() {
		disposeCharacter();
		disposeSystems();
	}

	private void disposeCharacter() {
		CharacterComponent cc = this.character.getComponent(CharacterComponent.class);
		this.bulletSystem.collisionWorld.removeAction(
				cc.characterController
				);
		this.bulletSystem.collisionWorld.removeCollisionObject(
				cc.ghostObject
				);
		cc.dispose();
	}

	private void disposeSystems() {
		this.bulletSystem.dispose();
		this.bulletSystem = null;
		this.renderSystem.dispose();
		this.renderSystem = null;
	}

	public void remove(Entity entity) {
		this.engine.removeEntity(entity);
		this.bulletSystem.removeBody(entity);
	}

	public void resize(int width, int height) {
		this.renderSystem.resize(width, height);
	}

	public void render(float dt) {
		renderWorld(dt);
		checkPause();
	}

	private void renderWorld(float dt) {
		this.engine.update(dt);
		if (GameWorld.debug) {
			this.debugDrawer.begin(this.renderSystem.getPerspectiveCamera());
			this.bulletSystem.collisionWorld.debugDrawWorld();
			this.debugDrawer.end();
		}
	}

	private void checkPause() {
		if (Settings.Paused) {
			this.bulletSystem.setProcessing(false);
			this.playerSystem.setProcessing(false);
		}
		else {
			this.bulletSystem.setProcessing(true);
			this.playerSystem.setProcessing(true);
		}
	}
}