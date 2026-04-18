package io.github.Chino.LodeRunner.GameInterface.Entity;

import java.util.ArrayList;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import io.github.Chino.LodeRunner.GameInterface.World.WorldManager;

public class AI extends Entity{

    // Sprite position on the screen
    private int posX;
    private int posY;

    private final int id;
    public int speed = 3;

    //For AI deplacement
    private Player nearestPlayer= null;
    private ArrayList<int[]> currentPath = new ArrayList<>();
    private int pathIndex = 0;
    private double pathTimer = 0;
    private float stuckTimer = 0;

    // AI sprites
    private Texture aiSpriteIdle;
    private Texture aiSpriteMovingLeft;
    private Texture aiSpriteMovingRight;

    public Texture currentAISprite;

    private final Rectangle hitbox = new Rectangle(this.posX, this.posY, 25, 32);
    private final Rectangle isOnGroundHitbox;

    private WorldManager worldManager;

    /**
     * 
     * @param x initial position x
     * @param y initial position y
     * @param id ai id to recognize instances
     * @param worldManager need it to interact with world
     */
    public AI(int x, int y,int id, WorldManager worldManager) {
        initEntityTextures();
        setStartPosition(x, y);
        this.currentAISprite = this.aiSpriteIdle;

        this.worldManager = worldManager;

        this.id = id;

        this.hitbox.x = this.posX;
        this.hitbox.y = this.posY;
        this.isOnGroundHitbox = new Rectangle(this.posX, this.posY - 1, 25, 1);
    }


    protected void initEntityTextures(){
        this.aiSpriteIdle = new Texture("data/textures/character/aiIdle.png");
        this.aiSpriteMovingLeft = new Texture("data/textures/character/aiMovingLeft.png");
        this.aiSpriteMovingRight = new Texture("data/textures/character/aiMovingRight.png");
    }

    /**
     * To place AI in coords in param
     * @param x to place in x axis
     * @param y to place in y axis
     */
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
    public Player getNearestPlayer() {
        return nearestPlayer;
    }
    public int getId() {
        return id;
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

    /**
     * @param xMovement distance x to moove hitbox
     */
    public void physicalBodyMoveX(int xMovement){
        this.hitbox.x += xMovement;
        this.isOnGroundHitbox.x += xMovement;
    }
    /**
     * @param xMovement distance y to moove hitbox
     */
    public void physicalBodyMoveY(int yMovement){
        this.hitbox.y += yMovement;
        this.isOnGroundHitbox.y += yMovement;
    }
    /**
     * @param ladderHitbox Hitbox where Entity will snap
     */
    public void snapToLadder(Rectangle ladderHitbox){
        this.hitbox.x = (int) ladderHitbox.x;
        this.isOnGroundHitbox.x = this.hitbox.x;
    }
    /**
     * 
     * @param blockHitbox has to be under AI
     */
    public void snapToBlock(Rectangle blockHitbox){
        this.hitbox.y = (int)blockHitbox.y +32;
        this.posY = (int)blockHitbox.y +32;
        this.isOnGroundHitbox.y+=32;
    }

    /**
     * @param x coordinate to place : sprite, hitbox and ground hitbox
     * @param y coordinate to place : sprite, hitbox and ground hitbox
     */
    public void setPosition(int x,int y){
        this.hitbox.x = x;
        this.hitbox.y = y;
        this.posX = x;
        this.posY = y;
        this.isOnGroundHitbox.x = x;
        this.isOnGroundHitbox.y = y-1;
    }


    /**
     * 
     * @return true if AI overlap with her nearest player ( kill him )
     */
    public boolean killPlayer(){
        Rectangle killableHitbox = new Rectangle(this.nearestPlayer.getHitbox());
        killableHitbox.width/=2;
        return this.getHitbox().overlaps(killableHitbox);
    }

    /**
     * 
     * @param nearestPlayer apply a player like the nearest
     */
    public void setNearestPlayer(Player nearestPlayer){
        this.nearestPlayer = nearestPlayer;
    }

    /**
     * 
     * @param dx distance to travel
     * @return  to see if the movement is possible
     */
    public boolean canMove(int dx){
        Rectangle nextHitbox = new Rectangle(hitbox);
        nextHitbox.x += dx;
        return this.worldManager.entityDoesntOverlapWorld(nextHitbox);
    }
    /**
     * 
     * @param delta GDX render fram per sec
     * @return type of movement for coop (0 is idle, 1 is right and 2 is left)
     */
    public int updateMovement(float delta){
        this.isOnALadder = (worldManager.entityOverlapWithALadder(this.getHitbox()) != null);

        int offsetX = worldManager.getBlockMatrix()[0].length / 2;
        int offsetY = worldManager.getBlockMatrix().length / 2;
        
        pathTimer += delta;
        stuckTimer += delta;
        
        if(currentPath.isEmpty() || pathIndex >= currentPath.size()){
            pathTimer = 0;
            
            int gridX = (int)(this.getHitbox().x / 32) + offsetX;
            int gridY = (int)(this.getHitbox().y / 32) + offsetY;
            
            int nearestPlayerGridX = (int)(nearestPlayer.getPosX() / 32) +offsetX;
            int nearestPlayerGridY = (int)(nearestPlayer.getPosY() / 32) + offsetY;
            
            currentPath = worldManager.findPath(gridX, gridY, nearestPlayerGridX, nearestPlayerGridY);
            pathIndex = 0;
        }
        if (currentPath.size() <= 1) {
            pathTimer = 0;
            return-1;
        }
        if(currentPath.size() > 1 && pathIndex < currentPath.size()){
            int[] next = currentPath.get(pathIndex);
            
            int targetX = (next[0]- offsetX) * 32 ;
            int targetY = (next[1]- offsetY) * 32 ;
            
            int dx = targetX - (int)this.getHitbox().x;
            int dy = targetY - (int)this.getHitbox().y;

            if (currentPath == null || currentPath.size() == 0) {
                return-1;
            }
            
            if( this.isOnALadder&&Math.abs(dy) > 1){
                Rectangle ladder = worldManager.entityOverlapWithALadder(this.getHitbox());
                if(ladder != null){
                    snapToLadder(ladder);
                    spriteChangeToIdle();
                }

                if(dy > 0){
                    physicalBodyMoveY(speed);
                    return 0;
                } else {
                    physicalBodyMoveY(-speed);
                    return 0;
                }
            }else{
                if(dx > 0 && canMove(speed)){
                    spriteChangeToMovingRight();
                    if(this.speed > Math.abs(dx)) this.physicalBodyMoveX((int)(Math.abs(dx%this.speed )));
                    else this.physicalBodyMoveX(this.speed);
                    return 1;
                }
                else if(dx < 0 && canMove(-speed)){
                    spriteChangeToMovingLeft();
                    if(this.speed > Math.abs(dx)) this.physicalBodyMoveX((int)-(Math.abs(dx%this.speed )));
                    else this.physicalBodyMoveX(-(this.speed));
                    return 2;
                }
                else spriteChangeToIdle();
            }
            

            if (Math.abs(hitbox.x - targetX) <= speed &&  Math.abs(hitbox.y - targetY) <= speed) pathIndex++;
            if (stuckTimer > 1.0f) {
                pathIndex++;
                stuckTimer = 0;
            }

            return 0;
        }     
        return 0;
        
    }
    public void render(SpriteBatch batch){
        batch.draw(this.currentAISprite, this.posX, this.posY);
    }

    public void displayHitboxes(){
        ShapeRenderer shapeRenderer = new ShapeRenderer();
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(Color.BLACK);

        shapeRenderer.rect(
            this.hitbox.x + 900,
            this.hitbox.y + 200,
            this.hitbox.width,
            this.hitbox.height
        );
        
        shapeRenderer.setColor(Color.CYAN);
        shapeRenderer.rect(
            this.isOnGroundHitbox.x + 900,
            this.isOnGroundHitbox.y + 200,
            this.isOnGroundHitbox.width,
            this.isOnGroundHitbox.height
        );
        shapeRenderer.end();
    }
    public void drawPath( ){
        ShapeRenderer shapeRenderer = new ShapeRenderer();
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(Color.WHITE);

        for(int i = 0; i < currentPath.size(); i++){
            int[] node = currentPath.get(i);

            shapeRenderer.rect(
                node[0] * 32 ,
                node[1] * 32 + 500,
                32,
                32
            );
        }

        shapeRenderer.end();
    }
}