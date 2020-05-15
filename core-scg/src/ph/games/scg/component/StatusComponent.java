package ph.games.scg.component;

import com.badlogic.ashley.core.Component;

import ph.games.scg.util.EnemyAnimations;

public class StatusComponent implements Component {
	
	public static final float DYING_STATE_TIME = 1f;
	
	public boolean alive, running;
	public float dyingStateTimer;

	private AnimationComponent animationComponent;

	public StatusComponent(AnimationComponent animationComponent) {
		this.alive = true;
		this.running = false;
		this.animationComponent = animationComponent;
	}

	public void update(float dt) {
		if (!this.alive) this.dyingStateTimer += dt;
	}

	public void setAlive(boolean alive) {
		this.alive = alive;
		if (!this.alive) this.setIdle(true);
	}
	
	public void setIdle(boolean idle) {
		this.running = !idle;
		if (idle) this.animationComponent.animate(EnemyAnimations.IdleID, 0f, -1f, -1, 1f);
		else this.animationComponent.animate(EnemyAnimations.BasicRunID, 0f, -1f, -1, 1f);
	}

}