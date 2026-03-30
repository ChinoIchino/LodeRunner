package io.github.Chino.LodeRunner.GameInterface.Interface;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.StretchViewport;

import io.github.Chino.LodeRunner.GameInterface.GDXMain;
import io.github.Chino.LodeRunner.GameInterface.LanConnection.ClientSide;
import io.github.Chino.LodeRunner.GameInterface.LanConnection.Packets.ByteHandler.ByteBuffer;
import io.github.Chino.LodeRunner.GameInterface.LanConnection.TranslateToBytes;
import io.github.Chino.LodeRunner.GameInterface.Player.Player;
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
    // Used to draw font via the batch
    private BitmapFont font = new BitmapFont();

    private Stage uiStage;
    private Player player;

    /** Contains: <PlayerId, Object[Username, LabelOfUsername, AnimationId, xPosition, yPosition]> */
    private HashMap<Integer, Object[]> playersInformations = new HashMap<>();
    // private int[] positionsOfPlayers;
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
            this.player.moveToCoordinate(0, this.worldManager.getBottomYPosition() + 32);
        } catch (IOException e) {
            System.out.println("\nERROR GameInterface/GameScreen.java: Constructor catched IOException will initializing the world");
        }

    }

    public WorldManager getWorldManager(){
        return this.worldManager;
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
        if(!player.isInLoading){
            try {
                // Send to server the movement based on the input and gravity in the current frame
                handlePlayerGravity();
                
                int animationId = handlePlayerInput();
    
                this.client.writeStream.write(TranslateToBytes.toPlayerMovement(player, animationId));
                this.client.writeStream.flush();
                
                handlePlayerCollection();
            } catch (IOException e) {}
        }

        // Draw all the other players
        this.batch.begin();
        Object[] currentInformations;
        for (int currentKey: this.playersInformations.keySet()) {
            currentInformations = this.playersInformations.get(currentKey);
            if(this.player.getId() != currentKey){
                switch((int) currentInformations[1]){
                    case 0:
                        this.batch.draw(Player.playerSpriteIdle, (int) currentInformations[2], (int) currentInformations[3]);
                        break;
                    case 1:
                        this.batch.draw(Player.playerSpriteMovingLeft, (int) currentInformations[2], (int) currentInformations[3]);
                        break;
                    case 2:
                        this.batch.draw(Player.playerSpriteMovingRight, (int) currentInformations[2], (int) currentInformations[3]);
                        break;
                    default:
                        break;
                }
            }
            // Draw the name above the current player
            String currentUsername = (String) currentInformations[0];
            GlyphLayout layout = new GlyphLayout(font, currentUsername);
            
            this.font.draw(
                this.batch, 
                currentUsername,
                ((int) currentInformations[2] + 15) - layout.width / 2, 
                (int) currentInformations[3] + 50
            );
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

    private int handlePlayerInput() throws IOException{
        int currentAnimationId = 0;
        if (Gdx.input.isKeyPressed(Input.Keys.A)){
            player.spriteChangeToMovingLeft();
            player.physicalBodyMoveX(-this.player.speed);
            
            player.syncAll();

            // -7 is a offset based on the player sprite
            if(player.getPosX() < (this.worldManager.worldResolution.x * -16 - 7) || !this.worldManager.playerDoesntOverlapWorld(this.player)){
                player.physicalBodyMoveX(this.player.speed);
                
                player.syncAll();
            }
            currentAnimationId = 1;
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
            currentAnimationId = 2;
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

                currentAnimationId = 0;

                if(this.worldManager.playerOverlapWithNextLevel(this.player)){
                    // TODO init and send player to next level
                    System.out.println("Player about to send to next level");

                    ByteBuffer buffer = new ByteBuffer(1024);
                    buffer.writeInt(10);

                    this.client.writeStream.write(buffer.getBytesList());
                    this.client.writeStream.flush();
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
        return currentAnimationId;
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
    private void handlePlayerCollection() throws IOException{
        // Return collectible, y index position on world and x index position on world
        List<Object> possibleCollectibleList = this.worldManager.playerOverlapWithCollectible(this.player);
        if(possibleCollectibleList != null){
            this.client.writeStream.write(TranslateToBytes.toPlayerScoreAdd(possibleCollectibleList));
            this.client.writeStream.flush();

            if(!this.worldManager.isThereCollectibles()){
                ByteBuffer buffer = new ByteBuffer(1024);
                buffer.writeInt(9);
                
                this.client.writeStream.write(buffer.getBytesList());
                this.client.writeStream.flush();
            }
        }
    }
    // Used to get packets of all players movements
    public synchronized void handlePlayersDisplay(List<Object> packet){
        int playerId = (int) packet.get(0);
        
        // Adding the animation id from the packet
        this.playersInformations.get(playerId)[1] = (int) packet.get(1);
        // Adding the x position from the packet
        this.playersInformations.get(playerId)[2] = (int) packet.get(2);
        // Adding the y position from the packet
        this.playersInformations.get(playerId)[3] = (int) packet.get(3);
         
        // System.out.println("ID: " + packet.get(0) + " // Position: " + packet.get(2) + " x " + packet.get(3));    
    }

    public void sendToNextLevel(){
        try {
            System.out.println("NextLevel function called!");
            this.player.isInLoading = true;
            
            this.worldManager = this.worldCreator.initWorld();
            this.player.moveToCoordinate(0, this.worldManager.getBottomYPosition() + 32);
            
            this.player.isInLoading = false;
        } catch (IOException e) {
            System.out.println("\nERROR GameInterface/Interface/GameCoopScreen.java: catched IOException will loading to next level");
        }
    }

    public void updateScoreLabel(int newScore, int yIndexOfItem, int xIndexOfItem){
        this.worldManager.setBlockAt(xIndexOfItem, yIndexOfItem, null);

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
    protected void initAmmountOfPlayers(Table tableOfPlayerList){
        int bottomYPosition = this.worldManager.getBottomYPosition() + 32;

        int currentId = 0;
        String currentUsername;
        for(Actor actor: tableOfPlayerList.getChildren()){
            // Just in case verify if the actor is a label
            if(actor instanceof Label && !((Label)actor).getText().toString().equals("Players:")){
                currentUsername = ((Label)actor).getText().toString();

                if(this.player.getUsername().equals(currentUsername)){
                    this.player.setId(currentId);
                }
                Object[] initInformation = new Object[4];

                // List contains: Username, animation id, x position, y position 
                initInformation[0] = currentUsername;
                initInformation[1] = 0;
                initInformation[2] = 0;
                initInformation[3] = bottomYPosition;

                this.playersInformations.put(currentId++, initInformation);
            }
        }
    }
    
    @Override
    public void hide(){
        
    }
    
    @Override
    public void dispose(){
        this.batch.dispose();
        this.uiStage.dispose();
        
        this.font.dispose();

        this.main.getLobbyScreen().forceDispose();
    }
    
}
