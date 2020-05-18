package ph.games.scg.bullet;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.physics.bullet.collision.ContactListener;
import com.badlogic.gdx.physics.bullet.collision.btCollisionObject;

import ph.games.scg.component.CharacterComponent;
import ph.games.scg.component.EnemyComponent;
import ph.games.scg.component.NetEntityComponent;
import ph.games.scg.component.StatusComponent;
import ph.games.scg.server.User;

public class PlayerEnemyContactListener extends ContactListener {

	@Override
	public void onContactStarted(btCollisionObject colObj0, btCollisionObject colObj1) {
		if (colObj0.userData instanceof Entity && colObj1.userData instanceof Entity) {
			Entity e0 = (Entity)colObj0.userData;
			Entity e1 = (Entity)colObj1.userData;
			CharacterComponent cc0 = e0.getComponent(CharacterComponent.class);
			CharacterComponent cc1 = e1.getComponent(CharacterComponent.class);

			if (cc0 != null && cc1 != null) {
				Entity ee, pe;
				User user;
				StatusComponent sc;

				if (e0.getComponent(EnemyComponent.class) != null) {
					ee = e0;
					pe = e1;
				}
				else {
					ee = e1;
					pe = e0;
				}

				user = (User)pe.getComponent(NetEntityComponent.class).netEntity;
				sc = ee.getComponent(StatusComponent.class);

				if (sc.alive) {
					user.applyDamage(10f);
					sc.setAlive(false);
				}
			}
		}
	}

}