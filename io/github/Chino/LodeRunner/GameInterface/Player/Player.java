package io.github.Chino.LodeRunner.GameInterface.Player;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.InputListener;

public class Player extends InputListener{
    // Sprite position on the screen
    private int posX = 20;
    private int posY = 20;

    public int speed = 4;

    // Player sprites
    private Texture playerSpriteIdle;
    private Texture playerSpriteMovingLeft;
    private Texture playerSpriteMovingRight;

    private Texture currentPlayerSprite;

    private Rectangle hitbox;

    public Player() {
        this.playerSpriteIdle = new Texture("data/textures/character/characterIdle.png");
        this.playerSpriteMovingLeft = new Texture("data/textures/character/characterMovingLeft.png");
        this.playerSpriteMovingRight = new Texture("data/textures/character/characterMovingRight.png");
    
        this.currentPlayerSprite = this.playerSpriteIdle;

        this.hitbox = new Rectangle(this.posX, this.posY, 13, 20);
    }

    public void spriteChangeToMovingLeft(){
        this.currentPlayerSprite = this.playerSpriteMovingLeft;
    }
    public void spriteChangeToMovingRight(){
        this.currentPlayerSprite = this.playerSpriteMovingRight;
    }
    public void spriteChangeToIdle(){
        this.currentPlayerSprite = this.playerSpriteIdle;
    }

    public int getPosX(){
        return this.posX;
    }
    public Rectangle getHitbox(){
        return this.hitbox;
    }

    public void syncSpriteToPhysicalBody(){
        this.posX = (int) this.hitbox.x;
    }
    public void physicalBodyMoveX(int xMovement){
        this.hitbox.x += xMovement;
    }

    public void render(SpriteBatch batch){
        batch.begin();
        batch.draw(this.currentPlayerSprite, this.posX, this.posY);
        batch.end();
    }

    


}