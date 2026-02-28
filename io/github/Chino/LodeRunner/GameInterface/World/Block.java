package io.github.Chino.LodeRunner.GameInterface.World;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Rectangle;

public class Block{
    private boolean isSolid;
    private Rectangle hitbox;
    private Texture texture;

    Block(Texture texture, Rectangle hitbox, boolean isSolid){
        this.hitbox = hitbox;
        this.texture = texture;
        this.isSolid = isSolid;
    }

    public Rectangle getHitbox(){
        return this.hitbox;
    }
    public Texture getTexture(){
        return this.texture;
    }

    public boolean isSolid(){
        return this.isSolid;
    }
}