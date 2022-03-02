package nl.arjanfrans.mario.graphics;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;

public abstract class CharacterAnimation {
	protected TextureAtlas atlas = new TextureAtlas("data/characters/characters.atlas");
	public static final float scale = 1/16f;
	
	public void dispose() {
		atlas.dispose();
	}

}
