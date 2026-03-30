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
import io.github.Chino.LodeRunner.GameInterface.Player.Player;
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
    
    private Player player;
    private SpriteBatch batch;

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
            // Take the player as a attribut so it play him immediately at the bottom
            this.player.isInLoading = true;

            this.worldManager = this.worldCreator.initWorld();
            player.moveToCoordinate(0, this.worldManager.getBottomYPosition() + 32);

            this.player.isInLoading = false;
        } catch (IOException e) {
            System.out.println("\nERROR GameInterface/GameScreen.java: Constructor catched IOException will initializing the world");
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
        
        if(!player.isInLoading){
            // Handle player movement
            handlePlayerInput();
            handlePlayerGravity();
            handlePlayerCollection();
        }

        // Render player after the movement
        this.player.camera.update();
        this.player.render(batch);
                
        this.worldManager.displayHitboxes();
        this.player.displayHitboxes();
        
        // apply the stretched view on the screen
        this.stretchViewport.apply();
        // this.stage.act(delta);
        // this.stage.draw();
        this.batch.setProjectionMatrix(this.player.camera.combined);
        // this.batch.setProjectionMatrix(stretchViewport.getCamera().combined);

        this.uiStage.act(delta);
        this.uiStage.draw();

    }

    private void handlePlayerInput(){
        if (Gdx.input.isKeyPressed(Input.Keys.A)){
            player.spriteChangeToMovingLeft();
            player.physicalBodyMoveX(-this.player.speed);
            
            player.syncAll();

            // -7 is a offset based on the player sprite
            if(player.getPosX() < (this.worldManager.worldResolution.x * -16 - 7) || !this.worldManager.playerDoesntOverlapWorld(this.player)){
                player.physicalBodyMoveX(this.player.speed);
                
                player.syncAll();
            }
        }
        if (Gdx.input.isKeyPressed(Input.Keys.D)){
            player.spriteChangeToMovingRight();
            player.physicalBodyMoveX(this.player.speed);
            
            player.syncAll();
            
            // -13 is a offset based on the player sprite
            if(player.getPosX() > (this.worldManager.worldResolution.x * 16 - 13) || !this.worldManager.playerDoesntOverlapWorld(this.player)){
                player.physicalBodyMoveX(-this.player.speed);
                
                player.syncAll();
            }
        }
        if (Gdx.input.isKeyPressed(Input.Keys.W)){
            Rectangle collidingLadder = this.worldManager.playerOverlapWithALadder(this.player);
            
            if(collidingLadder != null){
                // Snap player to ladder
                player.snapToLadder(collidingLadder);

                //Ignore gravitation bacause the player is on a ladder
                player.isOnALadder = true;

                //Move the player up
                player.physicalBodyMoveY(4);
                
                player.syncAll();

                if(this.worldManager.playerOverlapWithNextLevel(player)){
                    // TODO init and send player to next level
                    System.out.println("Player about to send to next level");
                    try{
                        // Block the collisions verifications while the next map load
                        this.player.isInLoading = true;

                        this.worldManager = this.worldCreator.initWorld();
                        this.player.moveToCoordinate(0, this.worldManager.getBottomYPosition() + 32);
                        
                        this.player.isInLoading = false;
                    }catch(IOException e){
                        System.out.println("\nERROR GameInterface/Interface/GameScreen.java: catched IOException will loading the next level");
                    }
                }
            }else{
                player.isOnALadder = false;
            }
        }else{
            player.isOnALadder = false;
        }

        if(Gdx.input.isKeyPressed(Input.Keys.E)){
            this.worldManager.breakBlockAtPos(this.player.getPosX(),this.player.getPosY());
        }
    }
    private void handlePlayerGravity(){
        if(!this.worldManager.playerIsOnGround(this.player) && !player.isOnALadder){
            this.player.physicalBodyMoveY(-8);

            if(this.worldManager.playerOverlapWithFloor(player)){
                // System.out.println("REPLACING THE PLAYER");
                this.player.physicalBodyMoveY(4);
            }

            player.syncAll();
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
