package io.github.Chino.LodeRunner.GameInterface.Interface;

import java.io.IOException;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.StretchViewport;

import io.github.Chino.LodeRunner.GameInterface.GDXMain;
import io.github.Chino.LodeRunner.GameInterface.Entity.*;
import io.github.Chino.LodeRunner.GameInterface.World.Collectible;
import io.github.Chino.LodeRunner.GameInterface.World.WorldCreator;
import io.github.Chino.LodeRunner.GameInterface.World.WorldManager;

/** Manage the screen when the player is playing */
public class GameScreen implements Screen{
    private GDXMain main;
    /** Class that manage the WORLD_FILE
     *  and create the level based on it */
    private WorldCreator worldCreator;
    private WorldManager worldManager;
    
    /** The txt that contain the canvas of the world to draw */
    private final String WORLD_FILE = "WorldFile"; 

    //** Resize the window size based on the resolution */
    private StretchViewport stretchViewport;

    private final int SCREEN_WIDTH = 854;//480;
    private final int SCREEN_HEIGH = 480;//320;

    private final int AI_NUMBER = 3;
    public AI[] aiList = new AI[AI_NUMBER];

    
    private Player player;
    private SpriteBatch batch;
    private boolean orientation = true;

    private Stage uiStage;
    private Label scoreLabel;

    public GameScreen(GDXMain main) {
        this.main = main;

        this.batch = new SpriteBatch();
        initScoreLabel();
        
        this.player = new Player();
        
        this.stretchViewport = new StretchViewport(this.SCREEN_WIDTH, this.SCREEN_HEIGH);

        this.worldCreator = new WorldCreator(this.batch, this.WORLD_FILE);
        
        try {
            this.worldManager = this.worldCreator.initWorld();
        } catch (IOException e) {
            System.out.println("\nERROR GameInterface/GameScreen.java: Constructor catched IOException will initializing the world");
        }
        
        initAIinWorld();
    }

    private void initAIinWorld(){
        int resolutionX = (int)this.worldManager.worldResolution.x*16;
        int resolutionY = (int)this.worldManager.worldResolution.y*16;
        for(int i = 0 ; i< this.AI_NUMBER;i++){
            AI entity = new AI((int)(Math.random()*(resolutionX*2))-resolutionX, (int)(Math.random()*(resolutionY*2))-resolutionY, this.worldManager);
            // handleAIOverlaps(entity);
            handleAIOverlaps(entity);
            entity.setNearestPlayer(this.player);
            entity.startAIThread();
            this.aiList[i] = entity;
        }
    }

    @Override
    public void render(float delta){
        //Delete previous frame
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        
        // Render world
        try{
            this.worldManager.drawWorld();
        }catch(IOException e){
            System.err.println("\nERROR GameInterface/GameScreen.java: Function render catched IOException while rendering the world");
        }

        // TODO Find a better way to manage the sprite of the player
        this.player.spriteChangeToIdle();
                
        // Handle player movement
        handlePlayerInput();
        worldManager.entityFellOutTheWorld(this.player);
        handleEntityGravity(this.player);
        handlePlayerCollection();
        
        // Render player after the movement
        this.player.camera.update();
        this.player.render(batch);
        
        this.worldManager.displayHitboxes();
        this.player.displayHitboxes();
        
        //render IAs
        batch.begin();
        for(int i = 0; i<AI_NUMBER;i++){
            handleEntityGravity(this.aiList[i]);
            // handleAIOverlaps(this.aiList[i]);
            worldManager.entityFellOutTheWorld(this.aiList[i]);
            // this.aiList[i].render(batch);
            batch.draw(this.aiList[i].currentAISprite, this.aiList[i].getPosX(), this.aiList[i].getPosY());
        }  
        for( AI ai : this.aiList){
            ai.displayHitboxes();
            // ai.syncAll();
        }
        batch.end();
        
        // apply the stretched view on the screen
        this.stretchViewport.apply();
        // this.stage.act(delta);
        // this.stage.draw();
        this.batch.setProjectionMatrix(this.player.camera.combined);
        // this.batch.setProjectionMatrix(stretchViewport.getCamera().combined);
        
        this.uiStage.act(delta);
        this.uiStage.draw();
        
    }
    private void handleAIOverlaps(AI ai){
        if(worldManager.aiOverlapsWithBlock(ai) !=null){
            ai.snapToBlock(worldManager.aiOverlapsWithBlock(ai));
        }
        
    }

    private void handlePlayerInput(){
        if (Gdx.input.isKeyPressed(Input.Keys.A)){
            player.spriteChangeToMovingLeft();
            player.physicalBodyMoveX(-this.player.speed);
            this.orientation = false;
            
            player.syncAll();

            // -7 is a offset based on the player sprite
            if(player.getPosX() < (this.worldManager.worldResolution.x * -16 - 7) || !this.worldManager.entityDoesntOverlapWorld(this.player)){
                player.physicalBodyMoveX(this.player.speed);
                
                player.syncAll();
            }
        }
        if (Gdx.input.isKeyPressed(Input.Keys.D)){
            player.spriteChangeToMovingRight();
            player.physicalBodyMoveX(this.player.speed);
            this.orientation = true;
            
            player.syncAll();
            
            // -13 is a offset based on the player sprite
            if(player.getPosX() > (this.worldManager.worldResolution.x * 16 - 13) || !this.worldManager.entityDoesntOverlapWorld(this.player)){
                player.physicalBodyMoveX(-this.player.speed);
                
                player.syncAll();
            }
        }
        if (Gdx.input.isKeyPressed(Input.Keys.W)){
            Rectangle collidingLadder = this.worldManager.entityOverlapWithALadder(this.player.getHitbox());
            
            if(collidingLadder != null){
                // Snap player to ladder
                player.snapToLadder(collidingLadder);

                //Ignore gravitation bacause the player is on a ladder
                player.isOnALadder = true;

                //Move the player up
                player.physicalBodyMoveY(4);
                
                player.syncAll();
            }
        }else{
            player.isOnALadder = false;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.S)){
            Rectangle collidingLadder = this.worldManager.entityOverlapWithALadder(this.player.getHitbox());
            //checking if the player is on the ground
            if(this.worldManager.entityReachGroungWithALadder(player)){
                collidingLadder = null;
            }
            
            if(collidingLadder != null){
                // Snap player to ladder
                player.snapToLadder(collidingLadder);

                //Ignore gravitation bacause the player is on a ladder
                player.isOnALadder = true;

                //Move the player up
                player.physicalBodyMoveY(-4);
                
                player.syncAll();
            }
        }else{
            player.isOnALadder = false;
        }

        if(Gdx.input.isKeyPressed(Input.Keys.E)){
            if(player.getPosX() >0){
                if(this.orientation){
                    if((this.player.getPosX() /32)-32 >= 0) this.worldManager.breakBlockAtPos(this.player.getPosX()+32,this.player.getPosY());
                    else this.worldManager.breakBlockAtPos(this.player.getPosX()+48,this.player.getPosY());
                }else{if((this.player.getPosX() /32)-32 >= 0) this.worldManager.breakBlockAtPos(this.player.getPosX()-32,this.player.getPosY());
                else this.worldManager.breakBlockAtPos(this.player.getPosX()-16,this.player.getPosY());
                }
            }else{
                if(this.orientation){
                    if((this.player.getPosX() /32)-32 >= 0) this.worldManager.breakBlockAtPos(this.player.getPosX(),this.player.getPosY());
                    else this.worldManager.breakBlockAtPos(this.player.getPosX()+16,this.player.getPosY());
                }else{if((this.player.getPosX() /32)-32 >= 0) this.worldManager.breakBlockAtPos(this.player.getPosX()-32,this.player.getPosY());
                else this.worldManager.breakBlockAtPos(this.player.getPosX()-48,this.player.getPosY());
                }
            }
        }
    }
    private void handleEntityGravity(Entity entity){
        if(!this.worldManager.entityIsOnGround(entity) && !entity.isOnALadder){
            entity.physicalBodyMoveY(-8);

            if(this.worldManager.entityOverlapWithFloor(entity)){
                entity.physicalBodyMoveY(4);
            }

            entity.syncAll();
        }
    }
    private void handlePlayerCollection(){
        Collectible possibleCollectible = this.worldManager.playerOverlapWithCollectible(this.player);
        if(possibleCollectible != null){
            this.player.addToScore(possibleCollectible.getScore());
            updateScoreLabel(this.player);
            // System.out.println("Score was modified into: " + player.getScore());
        }
    }

    private void updateScoreLabel(Player player){
        this.scoreLabel.setText("Score: " + player.getScore());
    }

    public void killAll(){
        for(AI ai : this.aiList){
            ai.close();
            System.out.println("Ai is deleting");
        }
    }

    @Override
    public void resize(int height, int width){
        this.stretchViewport.update(height, width);
    }

    @Override
    public void resume(){
        Gdx.graphics.setForegroundFPS(15);
    }

    @Override
    public void pause(){
        //if the game was minimized, set the fps to 10 to save user performance
        Gdx.graphics.setForegroundFPS(10);
    }
    
    @Override
    public void show(){
        Gdx.graphics.setForegroundFPS(15);
        Gdx.input.setInputProcessor(this.uiStage);

        // Create a stage to draw based on the screen
        // this.uiStage = new Stage(new ScreenViewport());
        // initScoreLabel();
    }

    private void initScoreLabel(){
        this.uiStage = new Stage(new ScreenViewport());
        Label label = new Label(
            "Score: 0",
            new Label.LabelStyle(new BitmapFont(), Color.WHITE)
        );
        label.setSize(70, 30);
        label.setPosition(10, Gdx.graphics.getHeight() - 30);
        
        this.scoreLabel = label;

        this.uiStage.addActor(label);
    }
    
    @Override
    public void hide(){
        
    }
    
    @Override
    public void dispose(){
        this.batch.dispose();
    }
    
}
