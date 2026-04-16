package io.github.Chino.LodeRunner.GameInterface.Entity;

import java.util.ArrayList;

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

    //For AI deplacement
    private Player nearestPlayer= null;
    private int targetLadderX = -1;
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

    private boolean threadActive =true;


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
        this.isOnGroundHitbox.y+=32;
    }
    public void setPosition(int x,int y){
        this.hitbox.x = x;
        this.hitbox.y = y;
        this.posX = x;
        this.posY = y;
        this.isOnGroundHitbox.x = x;
        this.isOnGroundHitbox.y = y-1;
    }

    public boolean killPlayer(){
        Rectangle killableHitbox = new Rectangle(this.nearestPlayer.getHitbox());
        killableHitbox.width/=2;
        return this.getHitbox().overlaps(killableHitbox);
    }

    
    public void setNearestPlayer(Player nearestPlayer){
        this.nearestPlayer = nearestPlayer;
    }
    
    public double getDistanceToNearestPlayerX(){
        if (nearestPlayer == null) return 0;
        return this.nearestPlayer.getPosX()-this.posX;
    }
    public double getDistanceToNearestPlayerY(){
        if (nearestPlayer == null) return 0;
        return this.nearestPlayer.getPosY()-this.posY;
    }
    
    public int selectTheNearestLadder(boolean needToCheckUnder){
        int distLeft = 0;
        int distRight = 0;
        int ladderLeftX=0;
        int ladderRightX=0;
        Rectangle mooveingHitbox = new Rectangle(this.getHitbox());
        for(int i = 0; i< (worldManager.getBlockMatrix()[0].length/2)*32;i+=32){
            mooveingHitbox.x += 32;
            if(this.worldManager.entityOverlapWithALadder(mooveingHitbox) != null){ladderRightX = (int)mooveingHitbox.x;break;}
            else distRight++;
        }
        mooveingHitbox.x = this.getHitbox().x;
        for(int i = 0; i< (worldManager.getBlockMatrix()[0].length/2)*32;i+=32){
            mooveingHitbox.x -= 32;
            if(this.worldManager.entityOverlapWithALadder(mooveingHitbox) != null){ladderLeftX = (int)mooveingHitbox.x;break;}
            else distLeft++;
        }

        if(distLeft < distRight){this.targetLadderX = ladderLeftX; return 1;}
        else {this.targetLadderX = ladderRightX; return 2;}

    }
    public boolean canMove(int dx){
        Rectangle nextHitbox = new Rectangle(hitbox);
        nextHitbox.x += dx;
        return this.worldManager.entityDoesntOverlapWorld(nextHitbox);
    }
    
    public void updateMovement(float delta){
        this.isOnALadder = (worldManager.entityOverlapWithALadder(this.getHitbox()) != null);

        int offsetX = worldManager.getBlockMatrix()[0].length / 2;
        int offsetY = worldManager.getBlockMatrix().length / 2;

        // System.out.println("ia stuck at node : " + pathIndex);
        // System.out.println("current path size : " + currentPath.size());
        
        pathTimer += delta;
        stuckTimer += delta;
        
        if(currentPath.isEmpty() || pathIndex >= currentPath.size()){
            pathTimer = 0;
            
            int gridX = (int)(this.getHitbox().x / 32) + offsetX;
            int gridY = (int)(this.getHitbox().y / 32) + offsetY;
            
            int nearestPlayerGridX = (int)(nearestPlayer.getPosX() / 32) +offsetX;
            int nearestPlayerGridY = (int)(nearestPlayer.getPosY() / 32) + offsetY;
            
            // System.out.println("grid in ai : " + gridX + "," + gridY);
            currentPath = worldManager.findPath(gridX, gridY, nearestPlayerGridX, nearestPlayerGridY);
            pathIndex = 0;
        }
        if (currentPath.size() <= 1) {
            pathTimer = 0;
            return;
        }
        if(currentPath.size() > 1 && pathIndex < currentPath.size()){
            int[] next = currentPath.get(pathIndex);
            
            int targetX = (next[0]- offsetX) * 32 ;
            int targetY = (next[1]- offsetY) * 32 ;
            // System.out.println("Next: " + next[0] + "," + next[1]);
            
            int dx = targetX - (int)this.getHitbox().x;
            int dy = targetY - (int)this.getHitbox().y;

            if (currentPath == null || currentPath.size() == 0) {
                return;
            }
            
            if( this.isOnALadder&&Math.abs(dy) > 1){
                Rectangle ladder = worldManager.entityOverlapWithALadder(this.getHitbox());
                if(ladder != null){
                    snapToLadder(ladder);
                }

                if(dy > 0){
                    physicalBodyMoveY(speed);
                } else {
                    physicalBodyMoveY(-speed);
                }
            }else /*if(Math.abs(dx) > 2)*/{
                if(dx > 0 && canMove(speed)){
                    spriteChangeToMovingRight();
                    if(this.speed > Math.abs(dx)) this.physicalBodyMoveX((int)(Math.abs(dx%this.speed )));
                    else this.physicalBodyMoveX(this.speed);
                }
                else if(dx < 0 && canMove(-speed)){
                    spriteChangeToMovingLeft();
                    if(this.speed > Math.abs(dx)) this.physicalBodyMoveX((int)-(Math.abs(dx%this.speed )));
                    else this.physicalBodyMoveX(-(this.speed));
                }
                else spriteChangeToIdle();
            }
            

            if (Math.abs(hitbox.x - targetX) <= speed &&  Math.abs(hitbox.y - targetY) <= speed) pathIndex++;
            if (stuckTimer > 1.0f) {
                pathIndex++;
                stuckTimer = 0;
            }

            
        }     
        
        
        // Local pathfinding : if playerY != aiY : go ti nearest ladder else follow player in x

        /*int ladderChoice = selectTheNearestLadder(getDistanceToNearestPlayerY()<0);  
        t("Choice: " + ladderChoice);
        System.out.println("On ladder: " + isOnALadder);
        System.out.println("AI X: " + this.getHitbox().x);
        System.out.println("Target ladder: " + targetLadderX);
        if(this.isOnALadder && Math.abs(getDistanceToNearestPlayerY()) != 0){
            snapToLadder(this.worldManager.entityOverlapWithALadder(this.getHitbox()));
            if(getDistanceToNearestPlayerY()<0){
                if(this.speed > Math.abs(getDistanceToNearestPlayerY()))this.physicalBodyMoveY((int)-(Math.abs(getDistanceToNearestPlayerY()%this.speed )));
                else this.physicalBodyMoveY(-this.speed);
            }
             else{
                if(this.speed > Math.abs(getDistanceToNearestPlayerY()))this.physicalBodyMoveY((int)(Math.abs(getDistanceToNearestPlayerY()%this.speed )));
                else this.physicalBodyMoveY(this.speed);
            }
        }
        else if(Math.abs(getDistanceToNearestPlayerY())>16){
            if(this.worldManager.entityOverlapWithALadder(this.getHitbox())!=null){
                
                snapToLadder(this.worldManager.entityOverlapWithALadder(this.getHitbox()));
                if(getDistanceToNearestPlayerY()<0){
                    if(this.speed > Math.abs(getDistanceToNearestPlayerY()))this.physicalBodyMoveY((int)-(Math.abs(getDistanceToNearestPlayerY()%this.speed )));
                    else this.physicalBodyMoveY(-this.speed);
                }
                else{
                    if(this.speed > Math.abs(getDistanceToNearestPlayerY()))this.physicalBodyMoveY((int)(Math.abs(getDistanceToNearestPlayerY()%this.speed )));
                    else this.physicalBodyMoveY(this.speed);
                }
            }else{
                switch(ladderChoice){
                
                    case 1 : {
    
                        if(canMove(-this.speed)){
                            this.spriteChangeToMovingLeft();
                            this.physicalBodyMoveX(-(this.speed));
                        }
    
                        break;
    
                    }
    
                    case 2 : {
    
                        if(canMove(this.speed)){
                            this.spriteChangeToMovingRight();
                            this.physicalBodyMoveX(this.speed);
                        }
    
                        break;
    
                    }
    
                    default : break;
                }
            }
            
        }else{
            if(getDistanceToNearestPlayerX()<0 && canMove(-this.speed)){

                    this.spriteChangeToMovingLeft();
                    if(this.speed > Math.abs(getDistanceToNearestPlayerX())) this.physicalBodyMoveX((int)-(Math.abs(getDistanceToNearestPlayerX()%this.speed )));
                    else this.physicalBodyMoveX(-(this.speed));

            }else if(getDistanceToNearestPlayerX()>0 && canMove(this.speed)){

                    this.spriteChangeToMovingRight();
                    if(this.speed > Math.abs(getDistanceToNearestPlayerX())) this.physicalBodyMoveX((int)(Math.abs(getDistanceToNearestPlayerX()%this.speed )));
                    else this.physicalBodyMoveX(this.speed);
                }

            else{

                this.spriteChangeToIdle();
    
            }
    
        }*/
                
        // System.out.println("Hitbox of ai pos {\n\t x : " + this.getHitbox().x + "\n\t" + "y : " + this.getHitbox().y +"\n}");
    }


    /*   Old method for the movement
    

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
            else{

                try{
                    Thread.sleep(2*100/3);
                }catch(InterruptedException e){
                    System.err.println("AI Movement Runnable ERROR\n\n");
                    e.printStackTrace();
                }
            }
        System.out.println("AI Thread is finish");
        
    };*/

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
    
    
    public void close(){
        this.threadActive = false;
    }
}