package io.github.Chino.LodeRunner.GameInterface;

import java.io.IOException;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.viewport.StretchViewport;

import io.github.Chino.LodeRunner.GameInterface.Player.Player;

public class GameScreen implements Screen{
    /** Class that manage the WORLD_FILE
     *  and create the level based on it */
    private WorldCreator worldCreator;
    
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

    }

    @Override
    public void render(float delta){
        //Delete previous frame
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Render world
        try{
            this.worldCreator.drawWorld(this.SCREEN_WIDTH, this.SCREEN_HEIGH);
        }catch(IOException e){
            System.err.println("\nERROR GameInterface/GameScreen.java: Function render catched IOException while rendering the world");
            e.printStackTrace();
        }
        
        // Handle player movement
        handlePlayerInput(delta);
        // Render player after the movement
        this.player.render(batch);

        // apply the stretched view on the screen
        this.stretchViewport.apply();
        this.batch.setProjectionMatrix(stretchViewport.getCamera().combined);
    }

    private void handlePlayerInput(float delta){
        if (Gdx.input.isKeyPressed(Input.Keys.A)) {
            player.moveX(-3);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.D)) {
            player.moveX(3);
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
