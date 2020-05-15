package ph.games.scg.game;

import java.util.ArrayList;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector3;
//See <http://javadox.com/com.badlogicgames.gdx/gdx-bullet/1.3.1/overview-summary.html> for API info
import com.badlogic.gdx.physics.bullet.Bullet;
import com.badlogic.gdx.physics.bullet.DebugDrawer;
import com.badlogic.gdx.physics.bullet.linearmath.btIDebugDraw;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;

import ph.games.scg.component.CharacterComponent;
import ph.games.scg.component.UserEntityComponent;
import ph.games.scg.environment.Room;
import ph.games.scg.environment.Room.Quad;
import ph.games.scg.server.User;
import ph.games.scg.system.BulletSystem;
import ph.games.scg.system.EnemySystem;
import ph.games.scg.system.PlayerSystem;
import ph.games.scg.system.RenderSystem;
import ph.games.scg.system.StatusSystem;
import ph.games.scg.system.UserEntitySystem;
import ph.games.scg.ui.GameUI;
import ph.games.scg.util.Debug;
import ph.games.scg.util.EntityFactory;
import ph.games.scg.util.Settings;

public class GameWorld {

	private BulletSystem bulletSystem;
	private DebugDrawer debugDrawer;
	private Engine engine;
	private Entity character;
	private GameUI gameUI;
//	private UserEntitySystem userEntitySystem;
	private PlayerSystem playerSystem;
//	private EnemySystem enemySystem;
//	private StatusSystem statusSystem;
	private RenderSystem renderSystem;
	
	private ArrayList<UserEntity> userEntities;

	public GameWorld(GameUI gameUI) {
		Bullet.init(); //Load in Bullet.dll

		setDebug();

		initWorld(gameUI);
		
		this.userEntities = new ArrayList<UserEntity>();
		
		addSystems();
		
		loadRooms();
		
		addEntities();
	}
	
	public ArrayList<UserEntity> getUserEntities() {
		return this.userEntities;
	}
	
	private void setDebug() {
		if (Debug.isOn()) {
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
		
		this.engine.addSystem(/* this.userEntitySystem = */ new UserEntitySystem());
		this.engine.addSystem(/* this.enemySystem = */ new EnemySystem(this.bulletSystem));
		this.engine.addSystem(/* this.statusSystem = */ new StatusSystem(this));
		
		if (Debug.isOn()) this.bulletSystem.collisionWorld.setDebugDrawer(this.debugDrawer);
	}
	
	private void loadRooms() {
//		engine.addEntity(EntityFactory.loadScene(0f, 0f, 0f));
		
		JsonReader jsonReader = new JsonReader();
		JsonValue jsonValue = jsonReader.parse(Gdx.files.internal("./rooms/jebcsac.json"));
		
		/********** JSON FORMAT **********
		 * [
		 * {
		 * "class": classname,
		 * "name": name_of_object,
		 * "x": x_coordinate_of_object,
		 * "y": y_coordinate_of_object,
		 * "z": z_coordinate_of_object,
		 * "w": width_of_object,
		 * "h": height_of_object,
		 * "l": length_of_object,
		 * "texture": wall_texture_image_file,
		 * "floor": floor_texture_image_file,
		 * "ceiling": ceiling_texture_image_file,
		 * "decorations": [
		 * 		{"class": classname0, "texture": decor0_texture_image_file,
		 * 		 "coords": [x00, y00, z00,
		 * 					x01, y01, z01,
		 * 					x02, y02, z02,
		 * 					x03, y03, z03]
		 * 			},
		 * 		{"class": classname1, "texture": decor1_texture_image_file,
		 * 		 "coords": [x10, y10, z10,
		 * 					x11, y11, z11,
		 * 					x12, y12, z12,
		 * 					x13, y13, z13]
		 * 			},
		 * 
		 * 						...
		 * 
		 * 		{"class": classnamen, "texture": decorn_texture_image_file,
		 * 		 "coords": [xn0, yn0, zn0,
		 * 					xn1, yn1, zn1,
		 * 					xn2, yn2, zn2,
		 * 					xn3, yn3, zn3]
		 * 			}
		 * ]
		 * }
		 * ]
		 */
		
		//Debug.log(jsonValue.toString());
		
		for (JsonValue jv=jsonValue.child(); jv != null; jv = jv.next()) {
//			Debug.log(jv.toString());
			
			//Build new Room object from JsonValue
			Room room = new Room();
			room.setName(jv.getString("name"));
			room.setLocation(jv.getFloat("x"), jv.getFloat("y"), jv.getFloat("z"));
			room.setDimensions(jv.getFloat("w"), jv.getFloat("h"), jv.getFloat("l"));
			room.setTexture(jv.getString("texture"));
			room.setFloor(jv.getString("floor"));
			room.setCeiling(jv.getString("ceiling"));
			
			for (JsonValue decorjv=jv.get("decorations").child(); decorjv != null; decorjv = decorjv.next()) {
				Debug.log(decorjv.toString());
				
				Quad q = new Quad();
				q.setTexture(decorjv.getString("texture"));
				
				float[] coords = decorjv.get("coords").asFloatArray();
				
				q.setCorners(
						new Vector3(coords[0], coords[1], coords[2]),
						new Vector3(coords[3], coords[4], coords[5]),
						new Vector3(coords[6], coords[7], coords[8]),
						new Vector3(coords[9], coords[10], coords[11])
						);
				
				room.addDecoration(q);
			}
			
			Debug.log(room);
			
			//TODO: Generate room boundary Quads and add to engine
			Vector3 pos = room.getPosition();
			Vector3 dim = room.getDimensions();
			//Floor
			Quad floor = new Quad();
			floor.setTexture(room.getFloor());
			floor.setCorners(
					new Vector3(pos.x-(dim.x/2f), pos.y-(dim.y/2f), pos.z-(dim.z/2f)),
					new Vector3(pos.x+(dim.x/2f), pos.y-(dim.y/2f), pos.z-(dim.z/2f)),
					new Vector3(pos.x+(dim.x/2f), pos.y-(dim.y/2f), pos.z+(dim.z/2f)),
					new Vector3(pos.x-(dim.x/2f), pos.y-(dim.y/2f), pos.z+(dim.z/2f))
					);
			this.engine.addEntity(EntityFactory.createQuad(floor));
			//Wall0
			Quad wall0 = new Quad();
			wall0.setTexture(room.getTexture());
			wall0.setCorners(
					new Vector3(pos.x-(dim.x/2f), pos.y-(dim.y/2f), pos.z-(dim.z/2f)),
					new Vector3(pos.x-(dim.x/2f), pos.y+(dim.y/2f), pos.z-(dim.z/2f)),
					new Vector3(pos.x-(dim.x/2f), pos.y+(dim.y/2f), pos.z+(dim.z/2f)),
					new Vector3(pos.x-(dim.x/2f), pos.y-(dim.y/2f), pos.z+(dim.z/2f))
					);
			this.engine.addEntity(EntityFactory.createQuad(wall0));
			//Wall1
			
			//Wall2
			
			//Wall3
			
			//Ceiling
			
			
			//Generate and add decoration Quads to engine
			for (Quad quad : room.getDecorations()) {
				this.engine.addEntity(EntityFactory.createQuad(quad));	
			}
						
		}
		
	}
	
	private void addEntities() {
		this.engine.addEntity(EntityFactory.loadScene(10f, 11f, 18f));
		createPlayer(10f, 16f, 18f);
//		createSpaceship(2f, 3f, 1f);
	}

	private void createPlayer(float x, float y, float z) {
		this.character = EntityFactory.createPlayer(this.bulletSystem, x, y, z);
		this.engine.addEntity(this.character);
	}
	
//	private void createSpaceship(float x, float y, float z) {
//		Entity spaceshipEntity = EntityFactory.createSpaceship("spaceship.g3dj", this.bulletSystem, x, y, z);
//		this.engine.addEntity(spaceshipEntity);
//	}
	
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
		if (Debug.isOn()) {
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
	
	public void addUserEntity(String username) {
		UserEntity userEntity = new UserEntity(new User(username), EntityFactory.createUserEntity(this.bulletSystem, 10f, 16f, 18f));
		this.engine.addEntity(userEntity.getEntity());
		this.userEntities.add(userEntity);
	}
	
	public void updateUserEntity(String username, Vector3 moveVector, float facing, float deltaTime) {
		Entity entity = null;
		for (UserEntity userEntity : this.userEntities) {
			if (userEntity.getUser().getUsername().equals(username)) {
				entity = userEntity.getEntity();
				break;
			}
		}
		
		if (entity != null) {
			UserEntityComponent uec = entity.getComponent(UserEntityComponent.class);
			if (uec == null) Debug.warn("Entity does not have a UserEntityComponent");
			else {
				Debug.log("Queueing movement data... " + moveVector + "," + facing + "," + deltaTime);
				uec.queuedMovement.add(moveVector);
				uec.queuedFacing.add(facing);
				uec.queuedDeltaTime.add(deltaTime);
			}
		}
	}
	
	public void spawnUserEntity(String username, Vector3 position) {
		for (UserEntity userEntity : this.userEntities) {
			if (userEntity.getUser().getUsername().equals(username)) {
				Entity entity = userEntity.getEntity();
				
				
				
				break;
			}
		}
	}

	public void killUserEntity(String username) {
		for (UserEntity userEntity : this.userEntities) {
			if (userEntity.getUser().getUsername().equals(username)) {
				Entity entity = userEntity.getEntity();
				
				
				
				break;
			}
		}
	}
	
	public void removeUserEntity(String username) {
		for (UserEntity userEntity : this.userEntities) {
			if (userEntity.getUser().getUsername().equals(username)) {
				this.userEntities.remove(userEntity);
				this.engine.removeEntity(userEntity.getEntity());
				break;
			}
		}
	}
	
	//Link between a User instance and their GameWorld representation
	public static class UserEntity {
		
		private User user;
		private Entity entity;
		
		public UserEntity(User user, Entity entity) {
			this.user = user;
			this.entity = entity;
		}
		
		public User getUser() {
			return this.user;
		}
		
		public Entity getEntity() {
			return this.entity;
		}
		
	}
	
}