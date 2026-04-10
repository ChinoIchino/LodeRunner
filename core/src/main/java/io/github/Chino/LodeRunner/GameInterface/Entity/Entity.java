package io.github.Chino.LodeRunner.GameInterface.Entity;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;

public abstract class Entity {
    public boolean isOnALadder;
    protected abstract void initEntityTextures();
    public abstract void spriteChangeToMovingLeft();
    public abstract void spriteChangeToMovingRight();
    public abstract void spriteChangeToIdle();
    public abstract int getPosX();
    public abstract int getPosY();
    public abstract Rectangle getHitbox();
    public abstract Rectangle getIsOnGroundHitbox();
    public abstract void syncAll();
    public abstract void syncSpriteToPhysicalBody();
    public abstract void physicalBodyMoveX(int xMovement);
    public abstract void physicalBodyMoveY(int yMovement);
    public abstract void snapToLadder(Rectangle ladderHitbox);
    public abstract void render(SpriteBatch batch);
    public abstract void setPosition(int x, int y);
}
