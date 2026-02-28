package io.github.Chino.LodeRunner.GameInterface.World;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Rectangle;

public class Collectible extends Block{
    private int score;

    public Collectible(Texture texture, Rectangle hitbox, int score) {
        super(texture, hitbox, false);
        this.score = score;
    }

    public int getScore(){
        return this.score;
    }
    
}
