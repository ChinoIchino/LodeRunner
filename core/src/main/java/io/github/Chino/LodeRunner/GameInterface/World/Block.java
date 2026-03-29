package io.github.Chino.LodeRunner.GameInterface.World;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Rectangle;

public class Block{
    private boolean isSolid;
    private boolean isClimbable;
    private Rectangle hitbox;
    private Texture texture;

    public Block(Texture texture, Rectangle hitbox, boolean isSolid, boolean isClimbable){
        this.hitbox = hitbox;
        this.texture = texture;

        this.isSolid = isSolid;
        this.isClimbable = isClimbable;
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
    public boolean isClimbable(){
        return this.isClimbable;
    }

    public void setTexture(Texture texture) {
        this.texture = texture;
    }

    @Override
    public String toString(){
        return "Block:\n   -Texture: " + this.texture.toString() + "\n   -Hitbox: " + this.hitbox.toString() + "\n   -isSolid: " + this.isSolid + "\n   -isClimbable: " + this.isClimbable;
    }
}