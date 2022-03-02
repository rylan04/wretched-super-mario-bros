package nl.arjanfrans.mario.model;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;

public abstract class MovingActor extends Actor {

	public static enum State {
		Standing,
		Walking,
		Jumping,
		Dying,
		Dead,
		FlagSlide,
		NoControl,
		Pose;
	}

	public static enum Direction {
		LEFT,
		RIGHT;
	}

	protected float max_velocity;
	protected float jump_velocity = 40f;
	protected float damping = 0.87f;
	protected Vector2 position;
	protected Vector2 velocity;
	protected World world;
	protected boolean dead;
	protected boolean moving;
	protected State state = State.Standing;
	protected float stateTime = 0;
	protected int level;
	protected boolean facesRight = true;
	protected Direction direction;
	protected boolean grounded = false;

	protected Pool<Rectangle> rectPool = new Pool<Rectangle>() {
		@Override
		protected Rectangle newObject() {
			return new Rectangle();
		}
	};

	protected Rectangle rectangle() {
		return new Rectangle(this.getX(), this.getY(), this.getWidth(), this.getHeight());
	}

	// Constructor
	public MovingActor(World world, float x, float y, float max_velocity) {
		this.world = world;
		this.setPosition(x, y);
		this.max_velocity = max_velocity;
		velocity = new Vector2(0, 0);
		dead = false;
		moving = false;
		this.setTouchable(Touchable.disabled);
	}

	protected void applyPhysics(Rectangle rect) {
		float deltaTime = Gdx.graphics.getDeltaTime();
		if (deltaTime == 0) return;

		stateTime += deltaTime;

		velocity.add(0, World.GRAVITY * deltaTime);

		if (Math.abs(velocity.x) < 1) {
			velocity.x = 0;
			if (grounded) state = State.Standing;
		}

		// Scale velocity by delta time
		velocity.scl(deltaTime);

		if(collisionX(rect)) collisionXAction();

		rect.x = this.getX();

		collisionY(rect);

		// Update x and y positions
		this.setPosition(this.getX() + velocity.x, this.getY() + velocity.y);

		// Un-scale by delta time
		velocity.scl(1 / deltaTime);

		// Apply damping so character eventually stops moving
		velocity.x *= damping;

		// Checks if the actor is dead from falling
		dieByFalling();
	}

	// Checks if anything collides in X direction
	protected boolean collisionX(Rectangle rect) {
		// int[] bounds = checkTiles(true);
		// Array<Rectangle> tiles = world.getTiles(bounds[0], bounds[1], bounds[2], bounds[3]);

		Array<Rectangle> tiles = getTiles(true);
		// What is this for?
		rect.x += velocity.x;

		// Checks if actor is colliding with a tile
		for (Rectangle tile : tiles) {
			if (rect.overlaps(tile)) {
				return true;
			}
		}

		// If actor is colliding with a static actor and is not destroyed
		for(StaticActor a : world.getStaticActors()) {
			if(rect.overlaps(a.rectangle()) && !a.isDestroyed()) {
				return true;
			}
		}

		return false;
	}

	// Don't really understand this
	protected int[] checkTiles(boolean checkX) {
		int startX, startY, endX, endY;

		if(checkX) {
			if (velocity.x > 0)
				startX = endX = (int) (this.getX() + this.getWidth() + velocity.x);
			else
				startX = endX = (int) (this.getX() + velocity.x);

			startY = (int) (this.getY());
			endY = (int) (this.getY() + this.getHeight());
		} else {
			if (velocity.y > 0)
				startY = endY = (int) (this.getY() + this.getHeight() + velocity.y);
			else
				startY = endY = (int) (this.getY() + velocity.y);

			startX = (int) (this.getX());
			endX = (int) (this.getX() + this.getWidth());
		}

		return new int[]{startX, startY, endX, endY};
	}

	// Checks collision in the Y direction
	protected void collisionY(Rectangle rect) {
		// int[] bounds = checkTiles(false);
		// world.getTiles(bounds[0], bounds[1], bounds[2], bounds[3]);
		Array<Rectangle> tiles = getTiles(false);

		rect.y += velocity.y;

		// Can not jump while falling
		if(velocity.y < 0 ) grounded = false;

		for (Rectangle tile : tiles) {
			if (rect.overlaps(tile)) {
				if (velocity.y > 0) this.setY(tile.y - this.getHeight());
				else {
					this.setY(tile.y + tile.height);
					hitGround();
				}

				velocity.y = 0;

				// Break look once overlapped
				break;
			}
		}

		for (StaticActor a : world.getStaticActors()) {
			if(rect.overlaps(a.rectangle()) && !a.isDestroyed()) {
				if (velocity.y > 0) {
					a.hit(level);
					this.setY(a.getOriginY() - this.getHeight());
				} else {
					this.setY(a.getY() + a.getHeight());
					hitGround();
				}
				velocity.y = 0;
				break;
			}
		}
		rectPool.free(rect);
	}

	// Moves character in specified direction
	public void move(Direction dir) {
		if(state != State.Dying && moving) {
			if(dir == Direction.LEFT) {
				velocity.x = -max_velocity;
				facesRight = false;
			}
			else { // Moving right
				velocity.x = max_velocity;
				facesRight = true;
			}

			// Update direction
			direction = dir;

			// If grounded, then update state
			if (grounded) state = MovingActor.State.Walking;
		}
	}

	protected Array<Rectangle> getTiles(boolean isX) {
		int[] bounds = checkTiles(isX);
		return world.getTiles(bounds[0], bounds[1], bounds[2], bounds[3]);
	}

	protected void hitGround() {
		grounded = true;
	}

	public float getMax_velocity() {
		return max_velocity;
	}

	public float getJump_velocity() {
		return jump_velocity;
	}

	public float getDamping() {
		return damping;
	}

	public float getStateTime() {
		return stateTime;
	}

	public boolean isFacesRight() {
		return facesRight;
	}

	public Vector2 getVelocity() {
		return velocity;
	}

	public boolean isMoving() {
		return moving;
	}

	public void setMoving(boolean moving) {
		this.moving = moving;
	}

	public State getState() {
		return state;
	}

	public void setState(State state) {
		this.state = state;
	}

	// Why does this set dead to either true or false, yet it always removes the actor?
	public void setDead(boolean dead) {
		this.dead = dead;
		world.removeActor(this);
	}

	public boolean isDead() {
		return dead;
	}

	// Abstract methods - handled by inherited class
	protected abstract void dieByFalling();

	protected abstract void collisionXAction();

}
