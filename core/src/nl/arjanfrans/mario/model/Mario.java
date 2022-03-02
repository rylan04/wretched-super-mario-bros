package nl.arjanfrans.mario.model;

// Local imports
import nl.arjanfrans.mario.actions.MarioActions;
import nl.arjanfrans.mario.actions.MoveableActions;
import nl.arjanfrans.mario.audio.Audio;
import nl.arjanfrans.mario.graphics.MarioAnimation;

// Library imports
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.utils.Array;

public class Mario extends Creature {
	protected MarioAnimation gfx = new MarioAnimation();
	protected Rectangle rect = new Rectangle();
	private float jump_boost = 40f, width, height;
	private boolean immume;
	private boolean controlsEnabled = true;

	// Constructor
	public Mario(World world, float positionX, float positionY) {
		super(world, positionX, positionY, 8f);
		immume = false;
		moving = true;
		level = 1; // Health
		updateSize();
	}

	protected void updateSize() {
		this.setSize(gfx.getDimensions(state, level).x, gfx.getDimensions(state, level).y);
	}

	// Mario gets hit by enemy
	private void hitByEnemy() {
		if(!immume) level--;

		boolean dead = level < 1 && !immume;

		// Mario dies
		if(dead) {
			state = State.Dying;
			velocity.set(0, 0);
			// Mario's death animation
			this.setWidth(gfx.getDimensions(state, level).x);
			this.setHeight(gfx.getDimensions(state, level).y);
			this.addAction(Actions.sequence(Actions.moveBy(0, 1, 0.2f, Interpolation.linear),
					Actions.delay(0.6f),
					Actions.moveBy(0, -10, 0.6f, Interpolation.linear),
					Actions.delay(1.6f),
					MoveableActions.DieAction(this)));

			Audio.stopSong();
			Audio.playSong("lifelost", false); // Play death music
		} else {
			// Mario takes damage hit
			if(!immume) Audio.powerDown.play();
			immume = true; // Mario cannot take another damage hit for a second
			// Mario damage animation
			this.addAction(Actions.sequence(Actions.parallel(Actions.alpha(0f, 2f, Interpolation.linear),
					Actions.fadeIn(0.4f, Interpolation.linear),
					Actions.fadeOut(0.4f, Interpolation.linear),
					Actions.fadeIn(0.4f, Interpolation.linear),
					Actions.fadeOut(0.4f, Interpolation.linear),
					Actions.fadeIn(0.4f, Interpolation.linear)),
					Actions.alpha(1f),
					MarioActions.stopImmumeAction(this)));
		}
	}

	// Mario interacts with end flag
	public void captureFlag(Flag flag, float endX, float endY) {
		Rectangle flagRect = flag.rect();
		state = State.FlagSlide;

		// TODO Flip mario sprite in sliding pose when at bottom
		this.addAction(Actions.sequence(
				Actions.delay(0.2f),
				Actions.parallel(
						Actions.moveTo(this.getX(), flagRect.y, 0.5f, Interpolation.linear),
						MarioActions.flagTakeDownAction(flag)),
				MarioActions.setStateAction(this, State.Walking),
				MarioActions.walkToAction(this, endX, endY),
				MarioActions.setStateAction(this, State.Pose),
				MarioActions.finishLevelAction())
		);

		Audio.stopSong();
		Audio.flag.play();
	}

	// Mario dies from falling
	protected void dieByFalling() {
		if(this.getY() < -3f) {
			state = State.Dying;
			velocity.set(0, 0);
			this.addAction(Actions.sequence(Actions.delay(3f),
					MoveableActions.DieAction(this)));
			Audio.stopSong();
			Audio.playSong("lifelost", false);
		}
	}

	// Controls Mario's movement
	@Override
	public void act(float delta) {
		super.act(delta);
		// If Mario is in a controllable state
		if (state != State.Dying && state != State.FlagSlide && controlsEnabled) {
			// Up
			if ((Gdx.input.isKeyPressed(Keys.SPACE)) && grounded)
				jump();

			// Left
			if (Gdx.input.isKeyPressed(Keys.LEFT) || Gdx.input.isKeyPressed(Keys.A))
				move(Direction.LEFT);

			// Right
			if (Gdx.input.isKeyPressed(Keys.RIGHT) || Gdx.input.isKeyPressed(Keys.D))
				move(Direction.RIGHT);

			// Update display
			width = gfx.getFrameWidth(level, width);
			height = gfx.getFrameHeight(level, height);
			rect.set(this.getX(), this.getY(), width, height);

			// Check for collision
			collisionWithEnemy();
			collisionWithMushroom();

			if(state != State.Dying) applyPhysics(rect);
		}
	}

	// Draw Mario on screen
	@Override
	public void draw(Batch batch, float parentAlpha) {
		TextureRegion frame = gfx.getAnimation(state, level).getKeyFrame(stateTime);
		updateSize();
		Color oldColor = batch.getColor();
		batch.setColor(this.getColor());

		if(state == State.Dying) {
			batch.draw(frame, getX(), getY(),
					getX()+this.getWidth()/2, getY() + this.getHeight()/2,
					this.getWidth(), this.getHeight(), getScaleX(), getScaleY(), getRotation());
		} else {
			if(facesRight) {
				batch.draw(frame, this.getX(), this.getY(),
						this.getX()+this.getWidth()/2, this.getY() + this.getHeight()/2,
						this.getWidth(), this.getHeight(), getScaleX(), getScaleY(), getRotation());
			}
			else {
				batch.draw(frame, this.getX() + this.getWidth(), this.getY(),
						this.getX()+this.getWidth()/2, this.getY() + this.getHeight()/2,
						-this.getWidth(), this.getHeight(), getScaleX(), getScaleY(), getRotation());
			}
		}
		batch.setColor(oldColor);
	}

	// Return whether Mario is touching an asset
	private boolean isTouched(float startX, float endX) {
		for (int i = 0; i < 2; i++) {
			float x = Gdx.input.getX() / (float) Gdx.graphics.getWidth();
			if (Gdx.input.isTouched(i) && (x >= startX && x <= endX)) {
				return true;
			}
		}
		return false;
	}

	// Jump
	private void jump() {
		if(!grounded) return;

		velocity.y += jump_velocity; // Apply velocity in Y direction
		state = MovingActor.State.Jumping; // Update state
		grounded = false; // Not grounded
		Audio.jump.play(); // Play jumping sound
	}

	// If Mario collides in the X direction
	@Override
	protected void collisionXAction() {
		velocity.x = 0;
	}

	// Collision with Goomba
	protected void collisionWithEnemy() {
		Array<Goomba> goombas = world.getEnemies();
		// Is this creating a copy of Mario's rectangle?
		// Why can't we just use a get function to grab Mario's rectangle
		Rectangle marioRect = rectangle();
		marioRect.set(this.getX(), this.getY(), this.getWidth(), this.getHeight());

		for(Goomba goomba : goombas) {
			Rectangle gRect = goomba.rectangle();

			if(gRect.overlaps(marioRect) && goomba.state != State.Dying) {
				boolean marioTrampledGoomba = velocity.y < 0 && this.getY() > goomba.getY();

				if(marioTrampledGoomba) {
					goomba.deadByTrample(); // Goomba dies
					Audio.stomp.play(); // Play stomp sound
					velocity.y += jump_boost; // Mario gains jump boost
					grounded = false;

				} else {
					hitByEnemy(); // Mario takes damage
				}
			}
		}
	}

	// Gravity and collision behaviour
	@Override
	protected void applyPhysics(Rectangle rect) {
		float deltaTime = Gdx.graphics.getDeltaTime();
		if (deltaTime == 0) return;

		stateTime += deltaTime;
		velocity.add(0, World.GRAVITY * deltaTime); // apply gravity if we are falling

		// Round velocity to 0 if small enough
		if (Math.abs(velocity.x) < 1) {
			velocity.x = 0;
			if (grounded && controlsEnabled) state = State.Standing;
		}

		// Scale by deltaTime
		velocity.scl(deltaTime);

		// Collision in the X direction
		if(collisionX(rect)) collisionXAction();
		rect.x = this.getX();

		// Collision in Y direction
		collisionY(rect);

		// Move
		this.setPosition(this.getX() + velocity.x, this.getY() +velocity.y);
		// Unscale velocity
		velocity.scl(1 / deltaTime);

		// Apply damping to velocity
		velocity.x *= damping;

		// Check if falling out of map
		dieByFalling();
	}

	// Upgrade Mario to Big Mario
	private void big_mario(Mushroom mushroom) {
		level = 2; // Give extra health point
		World.objectsToRemove.add(mushroom); // Remove mushroom
		Audio.powerUp.play(); // Play power up sound
	}

	//Check for collision with a mushroom/powerup
	protected void collisionWithMushroom() {
		Array<Mushroom> mushrooms = world.getMushrooms();
		Rectangle marioRect = rectangle();
		marioRect.set(this.getX(), this.getY(), this.getWidth(), this.getHeight());

		// Check for collision with a mushroom by their rectangles
		for(Mushroom mushroom : mushrooms) {
			Rectangle mRect = mushroom.rectangle();
			if(mushroom.isVisible() && mRect.overlaps(marioRect) && mushroom.state != State.Dying) {
				if(level == 1) big_mario(mushroom);
			}
		}
	}

	@Override
	public Animation getAnimation() {
		return gfx.getAnimation(state, level);
	}

	public void dispose() {
		gfx.dispose();
	}

	public void setImmume(boolean immume) {
		this.immume = immume;
	}

	// Move Mario
	@Override
	public void move(Direction dir) {
		if(state != State.Dying && moving) {
			if(dir == Direction.LEFT) {
				velocity.x = -max_velocity;
				facesRight = false;
			}
			else {
				velocity.x = max_velocity;
				facesRight = true;
			}
			direction = dir;
			if (grounded) state = MovingActor.State.Walking;
		}
	}

	public boolean isControlsEnabled() {
		return controlsEnabled;
	}

	public void setControlsEnabled(boolean controlsEnabled) {
		this.controlsEnabled = controlsEnabled;
	}

}

