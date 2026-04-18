package io.github.Chino.LodeRunner.GameInterface.Interface;

import java.io.IOException;
import java.util.List;

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
    

    //** Resize the window size based on the resolution */
    private StretchViewport stretchViewport;

    private final int SCREEN_WIDTH = 854;//480;
    private final int SCREEN_HEIGH = 480;//320;

    private final int AI_NUMBER = 03;
    public AI[] aiList = new AI[AI_NUMBER];

    private final double GRAVITY_POWER = 0.5;

    private boolean isGameOver = false;
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

        this.worldCreator = new WorldCreator(this.batch);
        
        try {
            // Take the player as a attribut so it play him immediately at the bottom
            this.player.isInLoading = true;

            this.worldManager = this.worldCreator.initWorld();
            player.moveToCoordinate(0, this.worldManager.getBottomYPosition() + 32);
            // player.moveToCoordinate(0, 0);
            
            this.player.isInLoading = false;
        } catch (IOException e) {
            System.out.println("\nERROR GameInterface/GameScreen.java: Constructor catched IOException will initializing the world");
        }
        
    }

    public void initAIinWorld(){
        int resolutionX = (int)this.worldManager.worldResolution.x*16;
        int resolutionY = (int)this.worldManager.worldResolution.y*16;
        for(int i = 0 ; i< this.AI_NUMBER;i++){
            // AI entity = new AI((int)(Math.random()*(resolutionX*2))-resolutionX, (int)(Math.random()*(resolutionY*2))-resolutionY, this.worldManager);
            AI entity = new AI((int)(Math.random()*(resolutionX*2))-resolutionX, (int)(this.worldManager.getBottomYPosition() + 32),i,this.worldManager);
            
            this.aiList[i] = entity; 
            entity.setNearestPlayer(this.player);
            handleAIOverlaps(entity);
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
        this.player.isOnALadder = (worldManager.entityOverlapWithALadder(player.getHitbox()) != null);
        handlePlayerInput();
        if (!player.isOnALadder || !player.wasOnALadder) player.climbing = false;
        if(!this.isGameOver && worldManager.entityFellOutTheWorld(this.player)){
            System.out.println("Player hs been killed");
            this.isGameOver = true;
        }
        if(!this.player.isOnALadder || !player.wasOnALadder) handleEntityGravity(this.player);
        handlePlayerCollection();
        player.syncAll();
        
        // Render player after the movement
        this.player.camera.update();
        this.player.render(batch);
        
        this.worldManager.displayHitboxes();
        this.player.displayHitboxes();
        //render IAs
        batch.begin();
        for(AI ai : this.aiList){
            ai.updateMovement(delta);
            handleEntityGravity(ai);
            if(!this.isGameOver && ai.killPlayer()){
                System.out.println("Player has been killed");
                this.isGameOver = true;
            }
            handleAIOverlaps(ai);
            if(worldManager.entityFellOutTheWorld(ai)){
                ai.setPosition(0, 0);
            }
            ai.render(batch);
        }  
        batch.end();
        for( AI ai : this.aiList){
            ai.displayHitboxes();
            ai.drawPath();
            ai.syncAll();
        }

        
        // apply the stretched view on the screen
        this.stretchViewport.apply();
        // this.stage.act(delta);
        // this.stage.draw();
        this.batch.setProjectionMatrix(this.player.camera.combined);
        // this.batch.setProjectionMatrix(stretchViewport.getCamera().combined);
        
        this.uiStage.act(delta);
        this.uiStage.draw();
        
        this.player.wasOnALadder = this.player.isOnALadder;

        if(isGameOver){
            System.out.println("switching screen");
            main.setNewGameEndScreen(isGameOver);
            this.isGameOver = false;
            main.setScreen(main.getGameEndScreen());
            this.dispose();
        }
    }
    private void handleAIOverlaps(AI ai){
        if(!worldManager.entityDoesntOverlapWorld(ai.getHitbox())){
            ai.snapToBlock(new Rectangle(ai.getPosX(),ai.getPosY(),0,0));
        }
        
    }

    private void handlePlayerInput(){
        if (Gdx.input.isKeyPressed(Input.Keys.A)){
            player.spriteChangeToMovingLeft();
            player.physicalBodyMoveX(-this.player.speed);
            this.orientation = false;

            // -7 is a offset based on the player sprite
            if(player.getPosX() < (this.worldManager.worldResolution.x * -16 - 7) || !this.worldManager.entityDoesntOverlapWorld(this.player.getHitbox())){
                player.physicalBodyMoveX(this.player.speed);
                
            }
        }
        if (Gdx.input.isKeyPressed(Input.Keys.D)){
            player.spriteChangeToMovingRight();
            player.physicalBodyMoveX(this.player.speed);
            this.orientation = true;
              
            // -13 is a offset based on the player sprite
            if(player.getPosX() > (this.worldManager.worldResolution.x * 16 - 13) || !this.worldManager.entityDoesntOverlapWorld(this.player.getHitbox())){
                player.physicalBodyMoveX(-this.player.speed);
                
            }
        }
        
        
        player.climbing = (Gdx.input.isKeyPressed(Input.Keys.W) || Gdx.input.isKeyPressed(Input.Keys.S));
        if(this.player.climbing){

        if (Gdx.input.isKeyPressed(Input.Keys.W )){
            Rectangle collidingLadder = this.worldManager.entityOverlapWithALadder(this.player.getHitbox());

            boolean canClimb = collidingLadder != null || this.worldManager.isLadderUnderEntity(player);
            System.out.println(canClimb);
            if(canClimb){
                // Snap player to ladder
                if(collidingLadder!=null)player.snapToLadder(collidingLadder);
                
                //Ignore gravitation bacause the player is on a ladder
                if(collidingLadder!=null)player.isOnALadder = true;
                
                //Move the player up
                if(player.isOnALadder) player.physicalBodyMoveY(4);
                
            }else{player.isOnALadder = false;player.wasOnALadder = true;}
        }
        if (Gdx.input.isKeyPressed(Input.Keys.S)){
            Rectangle collidingLadder = this.worldManager.entityOverlapWithALadder(this.player.getHitbox());
            //checking if the player is on the ground
            if(this.worldManager.entityReachGroungWithALadder(player)){
                collidingLadder = null;
            }
            boolean canClimb = collidingLadder != null || this.worldManager.isLadderUnderEntity(player);
            
            if(canClimb){
                // Snap player to ladder
                if(collidingLadder!=null)player.snapToLadder(collidingLadder);

                //Ignore gravitation bacause the player is on a ladder
                if(collidingLadder!=null)player.isOnALadder = true;

                //Move the player up
                player.physicalBodyMoveY(-4);
                
                // player.syncAll();
            }else{player.isOnALadder = false;}
            }
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
        // System.out.println("Applying gravity");
        if(entity.isOnALadder){
            entity.fallSpeed = 0;
            return;
        } 
        entity.fallSpeed-= this.GRAVITY_POWER;
        if(entity.fallSpeed < -8 ) entity.fallSpeed = -8;
        Rectangle next = new Rectangle(entity.getHitbox());
        next.y += entity.fallSpeed;

        if(this.worldManager.entityDoesntOverlapWorld(next)) {
            entity.physicalBodyMoveY((int)entity.fallSpeed);
        } else {
            entity.fallSpeed = 0;
        }
    }
    private void handlePlayerCollection(){
        List<Object> possibleCollectible = this.worldManager.playerOverlapWithCollectible(this.player);
        if(possibleCollectible != null){
            this.player.addToScore(((Collectible)possibleCollectible.get(0)).getScore());
            updateScoreLabel(this.player);

            if(!this.worldManager.isThereCollectibles()){
                this.worldManager.openExitToNextLevel();
            }
            // System.out.println("Score was modified into: " + player.getScore());
        }
    }

    private void updateScoreLabel(Player player){
        this.scoreLabel.setText("Score: " + player.getScore());
    }

    @Override
    public void resize(int height, int width){
        this.stretchViewport.update(height, width);
    }

    @Override
    public void resume(){
        Gdx.graphics.setForegroundFPS(20);
    }

    @Override
    public void pause(){
        //if the game was minimized, set the fps to 10 to save user performance
        Gdx.graphics.setForegroundFPS(10);
    }
    
    @Override
    public void show(){
        Gdx.graphics.setForegroundFPS(20);
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
