package io.github.Chino.LodeRunner.GameInterface.Entity;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;

public class Player extends Entity{
    public OrthographicCamera camera;

    public boolean isInLoading = false;

    private int score = 0;

    // Sprite position on the screen
    private int posX = 0;
    private int posY = 0; // -70 for the player to be on the ground

    public int speed = 4;

    // Player sprites
    public final static Texture playerSpriteIdle = new Texture("data/textures/character/characterIdle.png");
    public final static Texture playerSpriteMovingLeft = new Texture("data/textures/character/characterMovingLeft.png");
    public final static Texture playerSpriteMovingRight = new Texture("data/textures/character/characterMovingRight.png");

    private Texture currentPlayerSprite;

    private final Rectangle hitbox;
    private final Rectangle isOnGroundHitbox;

    public boolean isOnALadder = false;

    // For Multiplayer
    private String username;
    private int id;

    public Player() {
        // initPlayerTextures();

        this.currentPlayerSprite = Player.playerSpriteIdle;

        this.hitbox = new Rectangle(this.posX, this.posY, 25, 32);
        this.isOnGroundHitbox = new Rectangle(this.posX, this.posY - 1, 25, 1);
    
        this.camera = new OrthographicCamera(480,320);
    }
    public Player(String username, int id){
        this.username = username;
        this.id = id;
        
        // initPlayerTextures();

        this.currentPlayerSprite = Player.playerSpriteIdle;

        this.hitbox = new Rectangle(this.posX, this.posY, 25, 32);
        this.isOnGroundHitbox = new Rectangle(this.posX, this.posY - 1, 25, 1);
    
        this.camera = new OrthographicCamera(480,320);
    }

    public void addToScore(int toAdd){
        this.score += toAdd;
    }

    public void spriteChangeToMovingLeft(){
        this.currentPlayerSprite = Player.playerSpriteMovingLeft;
    }
    public void spriteChangeToMovingRight(){
        this.currentPlayerSprite = Player.playerSpriteMovingRight;
    }
    public void spriteChangeToIdle(){
        this.currentPlayerSprite = Player.playerSpriteIdle;
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

    public int getScore(){
        return this.score;
    }

    public void syncAll(){
        syncSpriteToPhysicalBody();
        syncCameraToPhysicalBody();
        // syncScoreLabelToPhysicalBody();
    }
    public void syncSpriteToPhysicalBody(){
        this.posX = (int) this.hitbox.x;
        this.posY = (int) this.hitbox.y;
    }
    public void syncCameraToPhysicalBody(){
        this.camera.position.x = (int) this.hitbox.x + 10;
        this.camera.position.y = (int) this.hitbox.y + 5;
    }

    public void physicalBodyMoveX(int xMovement){
        this.hitbox.x += xMovement;
        this.isOnGroundHitbox.x += xMovement;
    }
    public void physicalBodyMoveY(int yMovement){
        this.hitbox.y += yMovement;
        this.isOnGroundHitbox.y += yMovement;
    }

    public void moveToCoordinate(int xCoord, int yCoord){
        this.hitbox.x = xCoord;
        this.hitbox.y = yCoord;

        this.isOnGroundHitbox.x = xCoord;
        this.isOnGroundHitbox.y = yCoord - 1;

        syncAll();
    }

    public void snapToLadder(Rectangle ladderHitbox){
        this.hitbox.x = (int) ladderHitbox.x;
        this.isOnGroundHitbox.x = this.hitbox.x;
    }
    public void setPosition(int x,int y){
        this.hitbox.x = x;
        this.hitbox.y = y;
        this.posX = x;
        this.posY = y;
        this.isOnGroundHitbox.x = posX;
        this.isOnGroundHitbox.y = posY-4;
    }

    public void render(SpriteBatch batch){
        batch.begin();
        batch.draw(this.currentPlayerSprite, this.posX, this.posY);
        batch.end();
    }

    /** Player hitbox for debugging.
     * Displayed by blue squares */
    public void displayHitboxes(){
        ShapeRenderer shapeRenderer = new ShapeRenderer();
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(Color.BLUE);

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

    // Mainly Multiplayer Functions
    public void setUsername(String username){
        this.username = username;
    }
    public void setId(int id){
        this.id = id;
    }

    public String getUsername(){
        return this.username;
    }
    public int getId(){
        return this.id;
    }
    @Override
    protected void initEntityTextures() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'initEntityTextures'");
    }


}