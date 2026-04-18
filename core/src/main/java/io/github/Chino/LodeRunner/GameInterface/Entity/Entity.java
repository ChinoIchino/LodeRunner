package io.github.Chino.LodeRunner.GameInterface.Entity;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;

public abstract class Entity {

    public double fallSpeed = 0;
    public boolean climbing = false;
    public boolean isOnALadder = false;
    public boolean wasOnALadder = false;

    protected abstract void initEntityTextures();
    public abstract void spriteChangeToMovingLeft();
    public abstract void spriteChangeToMovingRight();
    public abstract void spriteChangeToIdle();

    /**
     * 
     * @return x position of entity
     */
    public abstract int getPosX();
    /**
     * 
     * @return y position of entity
     */
    public abstract int getPosY();
    /**
     * 
     * @return hitbox of entity
     */
    public abstract Rectangle getHitbox();
    /**
     * 
     * @return the ground detectable hitbox
     */
    public abstract Rectangle getIsOnGroundHitbox();
    public abstract void syncAll();
    public abstract void syncSpriteToPhysicalBody();

    /**
     * 
     * @param xMovement is the distance to moove the player in x axis
     */
    public abstract void physicalBodyMoveX(int xMovement);
    /**
     * 
     * @param yMovement is the distance to moove the player in y axis
     */
    public abstract void physicalBodyMoveY(int yMovement);
    /**
     * 
     * @param ladderHitbox hitbox to snap the entity
     */
    public abstract void snapToLadder(Rectangle ladderHitbox);
    public abstract void render(SpriteBatch batch);
    /**
     * @param x coordinate to place : sprite, hitbox and ground hitbox
     * @param y coordinate to place : sprite, hitbox and ground hitbox
     */
    public abstract void setPosition(int x, int y);
}
