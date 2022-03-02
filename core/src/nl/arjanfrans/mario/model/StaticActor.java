package nl.arjanfrans.mario.model;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.utils.Pool;

public abstract class StaticActor extends Actor {
	protected World world;
	protected boolean destroyed;

	// Constructor
	public StaticActor(World world) {
		this.world = world;
		this.setTouchable(Touchable.disabled);
	}

	public Rectangle rectangle() {
		Rectangle r = rectPool.obtain();
		r.set(this.getX(), this.getY(), this.getWidth(), this.getHeight());
		rectPool.free(r);
		return r;
	}

	// Can maybe move this out of this file
	protected Pool<Rectangle> rectPool = new Pool<Rectangle>()
	{
		@Override
		protected Rectangle newObject() {
			return new Rectangle();
		}
	};

	public boolean isDestroyed() {
		return destroyed;
	}

	protected abstract void hit(int mario_level);
}
