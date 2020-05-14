package ph.games.scg.component;

import com.badlogic.ashley.core.Component;

import ph.games.scg.util.EnemyAnimations;

public class StatusComponent implements Component {

	public boolean alive, running;
	public float aliveStateTime;

	private AnimationComponent animationComponent;

	public StatusComponent(AnimationComponent animationComponent) {
		this.alive = true;
		this.animationComponent = animationComponent;
		this.setIdle(false);
	}

	public void update(float dt) {
		if (!this.alive) this.aliveStateTime += dt;
	}

	public void setAlive(boolean alive) {
		this.alive = alive;
	}
	
	public void setIdle(boolean idle) {
		this.running = !idle;
		if (idle) this.animationComponent.animate(EnemyAnimations.IdleID, -1, 1f);
		else this.animationComponent.animate(EnemyAnimations.BasicRunID, -1, 1f);
	}

}