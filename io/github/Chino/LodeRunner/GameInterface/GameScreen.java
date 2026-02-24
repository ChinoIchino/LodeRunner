package io.github.Chino.LodeRunner.GameInterface;

import java.io.IOException;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.viewport.StretchViewport;

import io.github.Chino.LodeRunner.GameInterface.Player.Player;
import io.github.Chino.LodeRunner.GameInterface.World.WorldCreator;
import io.github.Chino.LodeRunner.GameInterface.World.WorldManager;

public class GameScreen implements Screen{
    /** Class that manage the WORLD_FILE
     *  and create the level based on it */
    private WorldCreator worldCreator;
    private WorldManager worldManager;
    
    /** The txt that contain the canvas of the world to draw */
    private final String WORLD_FILE = "WorldFile"; 

    //** Resize the window size based on the resolution */
    private StretchViewport stretchViewport;

    private final int SCREEN_WIDTH = 320;
    private final int SCREEN_HEIGH = 180;
    
    private Player player;
    private SpriteBatch batch;

    public GameScreen() {
        this.batch = new SpriteBatch();
        
        this.player = new Player();
        
        this.stretchViewport = new StretchViewport(this.SCREEN_WIDTH, this.SCREEN_HEIGH);
        
        this.worldCreator = new WorldCreator(this.batch, this.WORLD_FILE);

        try {
            this.worldManager = this.worldCreator.initWorld(this.SCREEN_WIDTH, this.SCREEN_HEIGH);
        } catch (IOException e) {
            System.out.println("\nERROR GameInterface/GameScreen.java: Constructor catched IOException will initializing the world");
        }

    }

    @Override
    @SuppressWarnings("CallToPrintStackTrace")
    public void render(float delta){
        //Delete previous frame
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        
        // Render world
        try{
            this.worldManager.drawWorld(this.SCREEN_WIDTH, this.SCREEN_HEIGH);
        }catch(IOException e){
            System.err.println("\nERROR GameInterface/GameScreen.java: Function render catched IOException while rendering the world");
            e.printStackTrace();
        }

        // TODO Find a better way to manage the sprite of the player
        this.player.spriteChangeToIdle();
                
        // Handle player movement
        handlePlayerInput();
        handlePlayerGravity();

        // Render player after the movement
        this.player.render(batch);
                
        this.worldManager.displayHitboxes();
        this.player.displayHitboxes();
        
        // apply the stretched view on the screen
        this.stretchViewport.apply();
        this.batch.setProjectionMatrix(stretchViewport.getCamera().combined);
    }

    private void handlePlayerInput(){
        if (Gdx.input.isKeyPressed(Input.Keys.A)){
            player.spriteChangeToMovingLeft();
            player.physicalBodyMoveX(-this.player.speed);
            player.syncSpriteToPhysicalBody();
            
            // -7 is a offset based on the player sprite
            if(player.getPosX() < (this.SCREEN_WIDTH / 2 * -1 - 7) || !this.worldManager.playerDoesntOverlapWorld(this.player)){
                player.physicalBodyMoveX(this.player.speed);
                player.syncSpriteToPhysicalBody();
            }
        }
        if (Gdx.input.isKeyPressed(Input.Keys.D)){
            player.spriteChangeToMovingRight();
            player.physicalBodyMoveX(this.player.speed);
            player.syncSpriteToPhysicalBody();
            
            // -13 is a offset based on the player sprite
            if(player.getPosX() > (this.SCREEN_WIDTH / 2 - 13) || !this.worldManager.playerDoesntOverlapWorld(this.player)){
                player.physicalBodyMoveX(-this.player.speed);
                player.syncSpriteToPhysicalBody();
            }
        }
        if(Gdx.input.isKeyPressed(Input.Keys.W)){
            Rectangle collidingLadder = this.worldManager.playerOverlapWithALadder(player);
            if(collidingLadder != null){
                // Snap player to ladder
                player.snapToLadder(collidingLadder);

                //Ignore gravitation bacause the player is on a ladder
                player.isOnALadder = true;

                //Move the player up
                player.physicalBodyMoveY(5);
                player.syncSpriteToPhysicalBody();
            }
        }else{
            player.isOnALadder = false;
        }
    }
    private void handlePlayerGravity(){
        if(!this.worldManager.playerIsOnGround(this.player) && !player.isOnALadder){
            this.player.physicalBodyMoveY(-10);
            this.player.syncSpriteToPhysicalBody();
        }
    }

    @Override
    public void resize(int height, int width){
        this.stretchViewport.update(height, width);
    }

    @Override
    public void resume(){
        
    }

    @Override
    public void pause(){

    }
    
    @Override
    public void show(){
        
    }
    
    @Override
    public void hide(){
        
    }
    
    @Override
    public void dispose(){
        this.batch.dispose();
    }
    
}
