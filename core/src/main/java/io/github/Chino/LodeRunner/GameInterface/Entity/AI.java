package io.github.Chino.LodeRunner.GameInterface.Entity;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;

import io.github.Chino.LodeRunner.GameInterface.World.Block;

public class AI extends Entity{

    // Sprite position on the screen
    private int posX;
    private int posY;

    public int speed = 3;

    // AI sprites
    private Texture aiSpriteIdle;
    private Texture aiSpriteMovingLeft;
    private Texture aiSpriteMovingRight;

    private Texture currentAISprite;

    private final Rectangle hitbox;
    private final Rectangle isOnGroundHitbox;

    public boolean isOnALadder = false;

    public AI(int x, int y) {
        initEntityTextures();
        setStartPosition(x, y);
        this.currentAISprite = this.aiSpriteIdle;

        this.hitbox = new Rectangle(this.posX, this.posY, 25, 32);
        this.isOnGroundHitbox = new Rectangle(this.posX, this.posY - 1, 25, 1);
    }

    protected void initEntityTextures(){
        this.aiSpriteIdle = new Texture("data/textures/character/aiIdle.png");
        this.aiSpriteMovingLeft = new Texture("data/textures/character/aiMovingLeft.png");
        this.aiSpriteMovingRight = new Texture("data/textures/character/aiMovingRight.png");
    }

    private void setStartPosition(int x, int y){
        this.posX= x;
        this.posY= y;
    }
    public void spriteChangeToMovingLeft(){
        this.currentAISprite = this.aiSpriteMovingLeft;
    }
    public void spriteChangeToMovingRight(){
        this.currentAISprite = this.aiSpriteMovingRight;
    }
    public void spriteChangeToIdle(){
        this.currentAISprite = this.aiSpriteIdle;
    }

    public int getPosX(){
        return this.posX;
    }
    public int getPosY(){
        return this.posY;
    }
    
    public Rectangle getHitbox(){
        return this.hitbox;
    }
    public Rectangle getIsOnGroundHitbox(){
        return this.isOnGroundHitbox;
    }

    public void syncAll(){
        syncSpriteToPhysicalBody();
    }
    public void syncSpriteToPhysicalBody(){
        this.posX = (int) this.hitbox.x;
        this.posY = (int) this.hitbox.y;
    }

    public void physicalBodyMoveX(int xMovement){
        this.hitbox.x += xMovement;
        this.isOnGroundHitbox.x += xMovement;
    }
    public void physicalBodyMoveY(int yMovement){
        this.hitbox.y += yMovement;
        this.isOnGroundHitbox.y += yMovement;
    }

    public void snapToLadder(Rectangle ladderHitbox){
        this.hitbox.x = (int) ladderHitbox.x;
        this.isOnGroundHitbox.x = this.hitbox.x;
    }
    public void snapToBlock(Rectangle blockHitbox){
        this.hitbox.y = (int)blockHitbox.y+100;
    }

    public void render(SpriteBatch batch){
        
        batch.draw(this.currentAISprite, this.posX, this.posY);
        
    }
}