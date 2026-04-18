package io.github.Chino.LodeRunner.GameInterface.Interface;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import javax.management.InvalidAttributeValueException;

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
import io.github.Chino.LodeRunner.GameInterface.Entity.*;
import io.github.Chino.LodeRunner.GameInterface.World.WorldCreator;
import io.github.Chino.LodeRunner.GameInterface.World.WorldManager;

/** Manage the screen when the player is playing */
public class GameCoopScreen implements Screen{
    private GDXMain main;
    /** Class that manage the WORLD_FILE
     *  and create the level based on it */
    private WorldCreator worldCreator;
    private WorldManager worldManager;

    //** Resize the window size based on the resolution */
    private StretchViewport stretchViewport;

    private final int SCREEN_WIDTH = 854;//480;
    private final int SCREEN_HEIGH = 480;//320;

    private final int AI_NUMBER = 3;
    public AI[] aiList = new AI[AI_NUMBER];

    private final double GRAVITY_POWER = 0.5;
    
    private ClientSide client;
    private SpriteBatch batch;
    // Used to draw font via the batch
    private BitmapFont font = new BitmapFont();

    private Stage uiStage;

    private boolean isGameOver = false;
    private Player player;
    private boolean orientation = true;

    private int playersLife;

    /** Contains: <PlayerId, Object[Username, LabelOfUsername, AnimationId, xPosition, yPosition]> */
    private HashMap<Integer, Object[]> playersInformations = new HashMap<>();
    private HashMap<Integer, AI> AIInformations = new HashMap<>();
    // private int[] positionsOfPlayers;
    private Label scoreLabel;

    public GameCoopScreen(GDXMain main) {
        this.main = main;

        this.batch = new SpriteBatch();
        initScoreLabel();
        
        this.player = this.main.getClientPlayer();

        this.playersLife = this.playersInformations.size();
        
        this.stretchViewport = new StretchViewport(this.SCREEN_WIDTH, this.SCREEN_HEIGH);
        
        this.worldCreator = new WorldCreator(this.batch);
        
        try {
            this.worldManager = this.worldCreator.initWorld();
            this.player.moveToCoordinate(0, this.worldManager.getBottomYPosition() + 32);
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
            this.AIInformations.put(i,entity);
            this.aiList[i] = entity; 
            entity.setNearestPlayer(this.player);
            handleAIOverlaps(entity);
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

        this.player.spriteChangeToIdle();
                
        // Handle player movement
        if(!player.isInLoading){
            try {
                this.player.isOnALadder = (worldManager.entityOverlapWithALadder(player.getHitbox()) != null);

                // Send to server the movement based on the input and gravity in the current frame
                if(!this.isGameOver && worldManager.entityFellOutTheWorld(this.player)){
                    this.isGameOver = true;
                    ByteBuffer killBuffer = new ByteBuffer(8);
                    killBuffer.writeInt(13);
                    this.client.writeStream.write(killBuffer.getBytesList());
                    this.client.writeStream.flush();
                }
                
                int animationId = handlePlayerInput();
    
                this.client.writeStream.write(TranslateToBytes.toPlayerMovement(player, animationId));
                this.client.writeStream.flush();

                if(!this.player.isOnALadder || !player.wasOnALadder) handleEntityGravity(this.player);
                handlePlayerCollection();
                player.syncAll();
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

              //render IAs
        batch.begin();

        if(this.player.getId()==0){
            for(AI ai : this.aiList){
                try{
                    applyTheNearestPlayerOfAnAI(ai);
                    int animationId = ai.updateMovement(delta);
                    handleEntityGravity(ai);
                    if(!this.isGameOver && ai.killPlayer()){
                        ByteBuffer killBuffer = new ByteBuffer(8);
                        killBuffer.writeInt(13);
                        this.client.writeStream.write(killBuffer.getBytesList());
                        this.client.writeStream.flush();
                    }
                    handleAIOverlaps(ai);
                    ai.syncAll();
                    if(worldManager.entityFellOutTheWorld(ai)){
                        ai.setPosition(0, 0);
                    }
                    this.client.writeStream.write(TranslateToBytes.toAIMovement(ai.getId(), ai.getNearestPlayer().getId(), animationId, ai.getPosX(), ai.getPosY()));
                    this.client.writeStream.flush();
                }catch(IOException e){}
                ai.render(batch);
            }  
        }else{
            for(AI ai : this.aiList){
                ai.render(batch);
            }
        }
        batch.end();
        for( AI ai : this.aiList){
            ai.displayHitboxes();
            ai.drawPath();
            ai.syncAll();
        }

        this.player.camera.update();
        this.player.render(batch);
                
        this.worldManager.displayHitboxes();
        this.player.displayHitboxes();
        
        this.stretchViewport.apply();
        this.batch.setProjectionMatrix(this.player.camera.combined);

        this.uiStage.act(delta);
        this.uiStage.draw();

        this.player.wasOnALadder = this.player.isOnALadder;

    }

    /**
     * 
     * @param ai to check about overlaps
     */
    private void handleAIOverlaps(AI ai){
        if(!worldManager.entityDoesntOverlapWorld(ai.getHitbox())){
            ai.snapToBlock(new Rectangle(ai.getPosX(),ai.getPosY(),0,0));
        }
        
    }

    /**
     * 
     * @param aiId AI id to change inormation about an AI
     * @param nearestPlayerId to check if it correspond to ai's nearest player
     * @param animationId for the switch who decide the orientation of ai in interface
     * @param positionX ai new position x
     * @param positionY ai new position y
     */
    public void setAIInfoForGuest(int aiId,int nearestPlayerId,int animationId,int positionX,int positionY){
        AI currentAIToModifiy = this.AIInformations.get(aiId);
        if(!(nearestPlayerId == currentAIToModifiy.getNearestPlayer().getId())){
            currentAIToModifiy.setNearestPlayer(player);
        }
        switch(animationId){
            case 0: currentAIToModifiy.spriteChangeToIdle(); break;
            case 1: currentAIToModifiy.spriteChangeToMovingRight(); break;
            case 2: currentAIToModifiy.spriteChangeToMovingLeft(); break;
            default: break;
        }

        currentAIToModifiy.setPosition(positionX, positionY);
    }

    /**
     * 
     * @param ai to aplly a new nearest player
     */
    public void applyTheNearestPlayerOfAnAI(AI ai){
        int distMax = 10000;
        for(int i = 0;i<this.playersInformations.size();i++){
            if(Math.abs(ai.getPosX()-((int)this.playersInformations.get(i)[3])) < distMax){
                ai.setNearestPlayer(player);
            }
        }
    }
    /**
     * 
     * @return information about the player sprite orientation
     * @throws IOException to avoid too much try catch section
     */
    private int handlePlayerInput() throws IOException{
        int currentAnimationId = 0;
        if (Gdx.input.isKeyPressed(Input.Keys.A)){
            player.spriteChangeToMovingLeft();
            player.physicalBodyMoveX(-this.player.speed);
            this.orientation = false;

            // -7 is a offset based on the player sprite
            if(player.getPosX() < (this.worldManager.worldResolution.x * -16 - 7) || !this.worldManager.entityDoesntOverlapWorld(this.player.getHitbox())){
                player.physicalBodyMoveX(this.player.speed);
                
            }
            currentAnimationId = 1;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.D)){
            player.spriteChangeToMovingRight();
            player.physicalBodyMoveX(this.player.speed);
            this.orientation = true;
            

            // -13 is a offset based on the player sprite
            if(player.getPosX() > (this.worldManager.worldResolution.x * 16 - 13) || !this.worldManager.entityDoesntOverlapWorld(this.player.getHitbox())){
                player.physicalBodyMoveX(-this.player.speed);
            }
            currentAnimationId = 2;
        }
        player.climbing = (Gdx.input.isKeyPressed(Input.Keys.W) || Gdx.input.isKeyPressed(Input.Keys.S));
        if(this.player.climbing){

            if (Gdx.input.isKeyPressed(Input.Keys.W )){
                Rectangle collidingLadder = this.worldManager.entityOverlapWithALadder(this.player.getHitbox());

                boolean canClimb = collidingLadder != null || this.worldManager.isLadderUnderEntity(player);
                if(canClimb){
                    // Snap player to ladder
                    if(collidingLadder!=null)player.snapToLadder(collidingLadder);
                    
                    //Ignore gravitation bacause the player is on a ladder
                    if(collidingLadder!=null)player.isOnALadder = true;
                    
                    //Move the player up
                    if(player.isOnALadder) player.physicalBodyMoveY(4);

                    currentAnimationId = 0;
                    
                    if(this.worldManager.playerOverlapWithNextLevel(this.player)){
                        ByteBuffer buffer = new ByteBuffer(1024);
                        buffer.writeInt(10);
                        
                        this.client.writeStream.write(buffer.getBytesList());
                        this.client.writeStream.flush();
                    }
                    
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
                    
                    currentAnimationId = 0;
                    // player.syncAll();
                }else{player.isOnALadder = false;}
                }

        }

        if(Gdx.input.isKeyPressed(Input.Keys.E)){
            if(player.getPosX() >0){
                if(this.orientation){
                    if((this.player.getPosX() /32)-32 >= 0){
                        this.client.writeStream.write(TranslateToBytes.toBreakBlock(this.player.getPosX()+32, this.player.getPosY()));
                        this.client.writeStream.flush();
                    }
                    else {
                        this.client.writeStream.write(TranslateToBytes.toBreakBlock(this.player.getPosX()+48,this.player.getPosY()));
                        this.client.writeStream.flush();
                    }
                }else{
                    if((this.player.getPosX() /32)-32 >= 0){
                        this.client.writeStream.write(TranslateToBytes.toBreakBlock(this.player.getPosX()-32,this.player.getPosY()));
                        this.client.writeStream.flush();
                    }
                    else {
                        this.client.writeStream.write(TranslateToBytes.toBreakBlock(this.player.getPosX()-16,this.player.getPosY()));
                        this.client.writeStream.flush();
                    }
                }
            }else{
                if(this.orientation){
                    if((this.player.getPosX() /32)-32 >= 0){
                        this.client.writeStream.write(TranslateToBytes.toBreakBlock(this.player.getPosX(),this.player.getPosY()));
                        this.client.writeStream.flush();
                    }
                    else{
                        this.client.writeStream.write(TranslateToBytes.toBreakBlock(this.player.getPosX()+16,this.player.getPosY()));
                        this.client.writeStream.flush();
                    }
                }else{
                    if((this.player.getPosX() /32)-32 >= 0){
                        this.client.writeStream.write(TranslateToBytes.toBreakBlock(this.player.getPosX()-32,this.player.getPosY()));
                        this.client.writeStream.flush();
                    }
                    else {
                        this.client.writeStream.write(TranslateToBytes.toBreakBlock(this.player.getPosX()-48,this.player.getPosY()));
                        this.client.writeStream.flush();
                    }
                }
            }
        }
        return currentAnimationId;
    }
    /**
     * 
     * @param entity entity to handle gravity
     */
    private void handleEntityGravity(Entity entity){
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
    /**
     * Update score and map if the player pick up a gem
     * @throws IOException to avoid useless trycatch
     */
    private void handlePlayerCollection() throws IOException{
        // Return collectible, y index position on world and x index position on world
        List<Object> possibleCollectibleList = this.worldManager.playerOverlapWithCollectible(this.player);
        if(possibleCollectibleList != null){
            this.client.writeStream.write(TranslateToBytes.toPlayerScoreAdd(possibleCollectibleList,0,0));
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
    /**
     * 
     * @param packet for others player's interface information
     */
    public synchronized void handlePlayersDisplay(List<Object> packet){
        int playerId = (int) packet.get(0);
        
        // Adding the animation id from the packet
        this.playersInformations.get(playerId)[1] = (int) packet.get(1);
        // Adding the x position from the packet
        this.playersInformations.get(playerId)[2] = (int) packet.get(2);
        // Adding the y position from the packet
        this.playersInformations.get(playerId)[3] = (int) packet.get(3);
           
    }

    /**
     * 
     * @param map matrix of char that represent the map
     */
    public void sendToNextLevel(char[][] map){
        if(map.length == 0){
            return;
        }

        try {
            this.player.isInLoading = true;
            
            this.worldManager = this.worldCreator.initWorldFromPacket(map);
            this.player.moveToCoordinate(0, this.worldManager.getBottomYPosition() + 32);
            
            this.player.isInLoading = false;
            this.getAIListEmpty();
            this.initAIinWorld();
        } catch (InvalidAttributeValueException e) {
            System.out.println("\nERROR GameInterface/Interface/GameCoopScreen.java: catched IOException will loading to next level");
        }
    }

    private void getAIListEmpty(){
        for(int i = 0 ; i< this.AI_NUMBER;i++){
            this.aiList[i] = null;
        }
    }

    //TODO: update leaderBoard in these two func
    public void sendToGameOverScreen(){
        this.isGameOver = false;
        main.setNewGameEndScreen(false,Integer.valueOf(this.scoreLabel.getText().substring(7)));
        main.setScreen(main.getGameEndScreen());
    }
    public void sendToGameEndScreen(){
        this.isGameOver = true;
        main.setNewGameEndScreen(true,Integer.valueOf(this.scoreLabel.getText().substring(7)));
        main.setScreen(main.getGameEndScreen());
    }
    /**
     * 
     * @param newScore is the new score to add at the current
     * @param yIndexOfItem position y of a collectible based on world matrix
     * @param xIndexOfItem position x of a collectible based on world matrix
     */
    public void updateScoreLabel(int newScore, int yIndexOfItem, int xIndexOfItem){
        this.worldManager.setBlockAt(xIndexOfItem, yIndexOfItem, null);
        this.worldManager.reduceAmountOfCollectible();
        if(!this.worldManager.isThereCollectibles()){
            try{
                ByteBuffer buffer = new ByteBuffer(1024);
                buffer.writeInt(9);

                this.client.writeStream.write(buffer.getBytesList());
                this.client.writeStream.flush();
            }catch(IOException e){}
            }
        newScore += Integer.valueOf(this.scoreLabel.getText().substring(this.scoreLabel.getText().indexOf(": ")+2));
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
    /**
     * 
     * @param tableOfPlayerList from LobbyScreen
     */
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

    }
    
}
