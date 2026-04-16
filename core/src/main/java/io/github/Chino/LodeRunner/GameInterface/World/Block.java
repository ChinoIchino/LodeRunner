package io.github.Chino.LodeRunner.GameInterface.World;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Rectangle;

public class Block{
    private boolean isSolid;
    private boolean isLadder;
    private Rectangle hitbox;
    private Texture texture;

    public Block(Texture texture, Rectangle hitbox, boolean isSolid){
        this.hitbox = hitbox;
        this.texture = texture;

        this.isSolid = isSolid;
        this.isLadder = false;
    }
    Block(Texture texture, Rectangle hitbox, boolean isSolid,boolean isLadder){
        this.hitbox = hitbox;
        this.texture = texture;
        this.isSolid = isSolid;
        this.isLadder = isLadder;
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
    public boolean isLadder() {
        return isLadder;
    }

    public void setTexture(Texture texture) {
        this.texture = texture;
    }

    @Override
    public String toString(){
        return "Block {\n\thitbox : (" + hitbox.x + "," + hitbox.y +")\n\tisSolid :" + isSolid +"\n}";
    }

}