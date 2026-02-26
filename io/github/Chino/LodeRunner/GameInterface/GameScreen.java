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
            this.worldManager = this.worldCreator.initWorld();
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
            this.worldManager.drawWorld();
        }catch(IOException e){
            System.err.println("\nERROR GameInterface/GameScreen.java: Function render catched IOException while rendering the world");
            e.printStackTrace();
        }

        // TODO Find a better way to manage the sprite of the player
        this.player.spriteChangeToIdle();
                
        // Handle player movement
        handlePlayerGravity();
        handlePlayerInput();

        // Render player after the movement
        this.player.render(batch);
        this.player.camera.update();
                
        this.worldManager.displayHitboxes();
        this.player.displayHitboxes();
        
        // apply the stretched view on the screen
        this.stretchViewport.apply();
        this.batch.setProjectionMatrix(this.player.camera.combined);
        // this.batch.setProjectionMatrix(stretchViewport.getCamera().combined);
    }

    private void handlePlayerInput(){
        if (Gdx.input.isKeyPressed(Input.Keys.A)){
            player.spriteChangeToMovingLeft();
            player.physicalBodyMoveX(-this.player.speed);
            
            player.syncSpriteToPhysicalBody();
            player.syncCameraToPhysicalBody();

            // -7 is a offset based on the player sprite
            if(player.getPosX() < (this.worldManager.worldResolution.x * -16 - 7) || !this.worldManager.playerDoesntOverlapWorld(this.player)){
                player.physicalBodyMoveX(this.player.speed);
                
                player.syncSpriteToPhysicalBody();
                player.syncCameraToPhysicalBody();
            }
        }
        if (Gdx.input.isKeyPressed(Input.Keys.D)){
            player.spriteChangeToMovingRight();
            player.physicalBodyMoveX(this.player.speed);
            
            player.syncSpriteToPhysicalBody();
            player.syncCameraToPhysicalBody();
            
            // -13 is a offset based on the player sprite
            if(player.getPosX() > (this.worldManager.worldResolution.x * 16 - 13) || !this.worldManager.playerDoesntOverlapWorld(this.player)){
                player.physicalBodyMoveX(-this.player.speed);
                
                player.syncSpriteToPhysicalBody();
                player.syncCameraToPhysicalBody();
            }
        }
        if (Gdx.input.isKeyPressed(Input.Keys.W)){
            Rectangle collidingLadder = this.worldManager.playerOverlapWithALadder(this.player.getHitbox());
            
            if(collidingLadder != null){
                // Snap player to ladder
                player.snapToLadder(collidingLadder);

                //Ignore gravitation bacause the player is on a ladder
                player.isOnALadder = true;

                //Move the player up
                player.physicalBodyMoveY(4);
                
                player.syncSpriteToPhysicalBody();
                player.syncCameraToPhysicalBody();
            }
        }else{
            player.isOnALadder = false;
        }

        if(Gdx.input.isKeyPressed(Input.Keys.E)){
            
        }
    }
    private void handlePlayerGravity(){
        if(!this.worldManager.playerIsOnGround(this.player) && !player.isOnALadder){
            this.player.physicalBodyMoveY(-8);

            if(this.worldManager.playerOverlapWithFloor(player)){
                System.out.println("REPLACING THE PLAYER");
                this.player.physicalBodyMoveY(8);
            }

            this.player.syncSpriteToPhysicalBody();
            this.player.syncCameraToPhysicalBody();
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
