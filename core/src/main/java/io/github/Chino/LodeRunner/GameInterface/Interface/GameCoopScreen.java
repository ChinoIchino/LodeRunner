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
import io.github.Chino.LodeRunner.GameInterface.LanConnection.ClientSide;
import io.github.Chino.LodeRunner.GameInterface.LanConnection.TranslateToBytes;
import io.github.Chino.LodeRunner.GameInterface.Player.Player;
import io.github.Chino.LodeRunner.GameInterface.World.Collectible;
import io.github.Chino.LodeRunner.GameInterface.World.WorldCreator;
import io.github.Chino.LodeRunner.GameInterface.World.WorldManager;

/** Manage the screen when the player is playing */
public class GameCoopScreen implements Screen{
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
    
    private ClientSide client;
    private SpriteBatch batch;

    private Stage uiStage;
    private Player player;
    private int[] positionsOfPlayers;
    private Label scoreLabel;

    public GameCoopScreen(GDXMain main) {
        this.main = main;

        this.batch = new SpriteBatch();
        initScoreLabel();
        
        this.player = this.main.getClientPlayer();
        
        this.stretchViewport = new StretchViewport(this.SCREEN_WIDTH, this.SCREEN_HEIGH);

        this.worldCreator = new WorldCreator(this.batch, this.WORLD_FILE);

        try {
            this.worldManager = this.worldCreator.initWorld();
        } catch (IOException e) {
            System.out.println("\nERROR GameInterface/GameScreen.java: Constructor catched IOException will initializing the world");
        }

    }

    public void setClient(ClientSide client){
        this.client = client;
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
        try {
            // Send to server the movement based on the input and gravity in the current frame
            handlePlayerGravity();
            handlePlayerInput();
            
            // handlePlayerCollection();
        } catch (IOException e) {
        }

        this.batch.begin();
        for (int i = 0; i < this.positionsOfPlayers.length; i += 4) {
            if(this.player.getId() != this.positionsOfPlayers[i]){
                switch(this.positionsOfPlayers[i + 1]){
                    case 0:
                        this.batch.draw(Player.playerSpriteIdle, this.positionsOfPlayers[i + 2], this.positionsOfPlayers[i + 3]);
                        break;
                    case 1:
                        this.batch.draw(Player.playerSpriteMovingLeft, this.positionsOfPlayers[i + 2], this.positionsOfPlayers[i + 3]);
                        break;
                    case 2:
                        this.batch.draw(Player.playerSpriteMovingRight, this.positionsOfPlayers[i + 2], this.positionsOfPlayers[i + 3]);
                        break;
                    default:
                        break;
                }
            }
        }
        this.batch.end();

        this.player.camera.update();
        this.player.render(batch);
                
        this.worldManager.displayHitboxes();
        this.player.displayHitboxes();
        
        this.stretchViewport.apply();
        this.batch.setProjectionMatrix(this.player.camera.combined);

        this.uiStage.act(delta);
        this.uiStage.draw();

    }

    private void handlePlayerInput() throws IOException{
        if(Gdx.input.isKeyPressed(Input.Keys.E)){
            this.worldManager.breakBlockAtPos(this.player.getPosX(),this.player.getPosY());
        }

        if (Gdx.input.isKeyPressed(Input.Keys.A)){
            player.spriteChangeToMovingLeft();
            player.physicalBodyMoveX(-this.player.speed);
            
            player.syncAll();

            // -7 is a offset based on the player sprite
            if(player.getPosX() < (this.worldManager.worldResolution.x * -16 - 7) || !this.worldManager.playerDoesntOverlapWorld(this.player)){
                player.physicalBodyMoveX(this.player.speed);
                
                player.syncAll();

            }

            this.client.writeStream.write(TranslateToBytes.toPlayerMovement(this.player, 1));
            this.client.writeStream.flush();
            return;
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

            this.client.writeStream.write(TranslateToBytes.toPlayerMovement(this.player, 2));
            this.client.writeStream.flush();
            return;
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
                
                //Sync the sprite and every other attributs to the position of the hitbox
                player.syncAll();
            }else{
                player.isOnALadder = false;
            }

        }else{
            player.isOnALadder = false;
        }
        
        this.client.writeStream.write(TranslateToBytes.toPlayerMovement(this.player, 0));
        this.client.writeStream.flush();
    }
    private void handlePlayerGravity(){
        if(!this.worldManager.playerIsOnGround(this.player) && !player.isOnALadder){
            this.player.physicalBodyMoveY(-8);

            if(this.worldManager.playerOverlapWithFloor(player)){
                this.player.physicalBodyMoveY(4);
            }

            player.syncAll();
        }
    }
    // TODO not implemented yet :(
    private void handlePlayerCollection() throws IOException{
        Collectible possibleCollectible = this.worldManager.playerOverlapWithCollectible(this.player);
        if(possibleCollectible != null){
            this.client.writeStream.write(TranslateToBytes.toPlayerScoreAdd(possibleCollectible.getScore()));
            this.client.writeStream.flush();
            // this.player.addToScore(possibleCollectible.getScore());
            // System.out.println("Score was modified into: " + player.getScore());
        }
    }
    // Used to get packets of all players movements // TODO do not work, find a way to fix
    public synchronized void handlePlayersDisplay(List<Object> packet){
        int playerPositionInList = (int) packet.get(0) * 4;
        
        this.positionsOfPlayers[playerPositionInList + 1] = (int) packet.get(1);
        this.positionsOfPlayers[playerPositionInList + 2] = (int) packet.get(2);
        this.positionsOfPlayers[playerPositionInList + 3] = (int) packet.get(3);
         
        // System.out.println("ID: " + packet.get(0) + " // Position: " + packet.get(2) + " x " + packet.get(3));    
    }

    public void updateScoreLabel(int newScore){
        this.scoreLabel.setText("Score: " + newScore);
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
    protected void initAmmountOfPlayers(int ammountOfPlayers){
        // Each player have the variables: Id, animationId, positionX, positionY
        this.positionsOfPlayers = new int[ammountOfPlayers * 4];

        int count = 0;
        for (int i = 0; i < ammountOfPlayers * 4; i += 4) {
            this.positionsOfPlayers[i] = count++;
            this.positionsOfPlayers[i + 1] = 0; // 0 = idleAnimation
            this.positionsOfPlayers[i + 2] = count * 20;
            this.positionsOfPlayers[i + 3] = -70;
        }
    }
    
    @Override
    public void hide(){
        
    }
    
    @Override
    public void dispose(){
        this.main.getLobbyScreen().forceDispose();
    }
    
}
