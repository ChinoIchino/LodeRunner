package io.github.Chino.LodeRunner.GameInterface.Entity;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;

import io.github.Chino.LodeRunner.GameInterface.World.Block;
import io.github.Chino.LodeRunner.GameInterface.World.WorldManager;

public class AI extends Entity{

    // Sprite position on the screen
    private int posX;
    private int posY;

    public int speed = 3;

    private Player nearestPlayer= null;

    // AI sprites
    private Texture aiSpriteIdle;
    private Texture aiSpriteMovingLeft;
    private Texture aiSpriteMovingRight;

    public Texture currentAISprite;

    private final Rectangle hitbox = new Rectangle(this.posX, this.posY, 25, 32);
    private final Rectangle isOnGroundHitbox;

    private WorldManager worldManager;

    private boolean threadActive =true;

    public boolean isOnALadder = false;

    public AI(int x, int y, WorldManager worldManager) {
        initEntityTextures();
        setStartPosition(x, y);
        this.currentAISprite = this.aiSpriteIdle;

        this.worldManager = worldManager;

        this.hitbox.x = this.posX;
        this.hitbox.y = this.posY;
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
        this.hitbox.y = (int)blockHitbox.y +32;
        this.posY = (int)blockHitbox.y +32;
    }
    public void setPosition(int x,int y){
        this.hitbox.x = x;
        this.hitbox.y = y;
        this.posX = x;
        this.posY = y;
        this.isOnGroundHitbox.x = posX;
        this.isOnGroundHitbox.y = posY-1;
    }

    public int selectTheNearestLadder(){
        int blockX = ((int) getPosX() / 32)+this.worldManager.getBlockMatrix()[0].length/2;
        int blockY = (((int) getPosY()/ 32)+this.worldManager.getBlockMatrix().length/2)-1;
        int dist = 0;
        boolean otherSide = false;
        Block ladder = this.worldManager.getBlockMatrix()[blockY][blockX];
        while(ladder != null && !ladder.isSolid()){
            ladder = this.worldManager.getBlockMatrix()[blockY][blockX];
            if(blockX == this.worldManager.getBlockMatrix()[0].length){blockX = 0; otherSide = true; dist = 0;}
            blockX++;
        }
        return blockX;

    }

    public void setNearestPlayer(Player nearestPlayer){
        this.nearestPlayer = nearestPlayer;
    }

    public double getDistanceToNearestPlayerX(){
        return this.nearestPlayer.getPosX()-this.posX;
    }
    public double getDistanceToNearestPlayerY(){
        return this.nearestPlayer.getPosY()-this.posY;
    }

    public void startAIThread(){
        Thread moveThread = new Thread(movementThread);
        moveThread.start();
    }

    private Runnable movementThread = () ->{
        while(this.threadActive){
            /*if(getPosY() != this.nearestPlayer.getPosY()){

                if(this.worldManager.entityOverlapWithALadder(getHitbox())!= null && posX != selectTheNearestLadder()*32){

                    if((getPosX() - (selectTheNearestLadder())*32 - (this.worldManager.getBlockMatrix()[0].length/2)*32) <= 0){
                        this.spriteChangeToMovingLeft();
                        this.physicalBodyMoveX(-this.speed);
                    }else{
                        this.spriteChangeToMovingRight();
                        this.physicalBodyMoveX(this.speed);
                    }

                }else{

                    this.spriteChangeToIdle();

                    if(this.worldManager.entityOverlapWithALadder(this.hitbox)!=null && this.worldManager.entityOverlapWithALadder(this.nearestPlayer.getHitbox())!=null){

                        snapToLadder(this.worldManager.entityOverlapWithALadder(this.hitbox));
                        if(getDistanceToNearestPlayerY()<0)this.physicalBodyMoveY(-this.speed);
                        else this.physicalBodyMoveY(this.speed);

                    }
                }          
            }
            else{*/
                if(getDistanceToNearestPlayerX()<0){

                        this.spriteChangeToMovingLeft();
                        if(this.speed > getDistanceToNearestPlayerX()) setPosition(nearestPlayer.getPosX(), getPosY());
                        else this.physicalBodyMoveX(-this.speed);

                }else if(getDistanceToNearestPlayerX()>0){

                        this.spriteChangeToMovingRight();
                        if(this.speed > getDistanceToNearestPlayerX()) setPosition(nearestPlayer.getPosX(), getPosY());
                        else this.physicalBodyMoveX(this.speed);
                    }

                else{

                    this.spriteChangeToIdle();

                    if(this.worldManager.entityOverlapWithALadder(this.hitbox)!=null && this.worldManager.entityOverlapWithALadder(this.nearestPlayer.getHitbox())!=null){

                        snapToLadder(this.worldManager.entityOverlapWithALadder(this.hitbox));
                        if(getDistanceToNearestPlayerY()<0)this.physicalBodyMoveY(-this.speed);
                        else this.physicalBodyMoveY(this.speed);

                    }  
                }
                
                System.out.println("Hitbox of ai pos {\n\t x : " + this.getHitbox().x + "\n\t" + "y : " + this.getHitbox().y +"\n}");
                try{
                    Thread.sleep(2*100/3);
                }catch(InterruptedException e){
                    System.err.println("AI Movement Runnable ERROR\n\n");
                    e.printStackTrace();
                }
            }
        
    };

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


    public void close(){
        this.threadActive = false;
    }
}