package io.github.Chino.LodeRunner.GameInterface;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;

import io.github.Chino.LodeRunner.GameInterface.Player.Player;

public class WorldCreator{
    private SpriteBatch batch;
    private String nameOfTxtFile;

    private Texture[][] worldTextures = new Texture[9][16];
    //Rectangle[y][x]
    /** Hitboxes of the world*/
    private Rectangle[][] worldBlockHitboxes = new Rectangle[9][16];
    private Rectangle[][] worldLadderHitboxes = new Rectangle[9][16];

    // Textures used
    private Texture blockTexture;
    private Texture ladderTexture;

    public WorldCreator(SpriteBatch batch, String nameOfFile) {
        this.nameOfTxtFile = nameOfFile;
        this.batch = batch;

        this.initTextures();

        // this.initWorld(windowWidth, windowHeight);
    }

    /** Initiate the textures used in the world creation */
    private void initTextures(){
        AssetManager assetManager = new AssetManager();

        this.blockTexture = new Texture("data/textures/blocks/brick.png");
        this.ladderTexture = new Texture("data/textures/blocks/ladder.png");
    }

    public void initWorld(int windowWidth, int windowHeight) throws IOException{
        BufferedReader bufferedReader = this.getWorldTxt();
        // Skip to line 21 of the WorldFile.txt
        for (int i = 0; i < 21; i++) {
            bufferedReader.readLine();
        }

        StringBuilder stringBuilder = new StringBuilder();
        String line = bufferedReader.readLine();
    
        int currentYPosIndex = windowHeight / 20 - 1;
        // int currentYPos = windowHeight / 2 * -1 + 20;
        int currentYPos = windowHeight / 2 - 20;
        
        while(line != null){
            initLine(currentYPosIndex, currentYPos, windowHeight, windowWidth, line);

            // Goes to the next line index
            currentYPosIndex--;
            // Goes to the next pixel position
            // currentYPos += 20;
            currentYPos -= 20;
            // Get the next line
            line = bufferedReader.readLine();
        }
    }

    private void initLine(int yIndexToInit, int currentYPos, int windowHeight, int windowWidth, String currentLine){
        String sliceString;

        int currentXPos = windowWidth / 2 * -1;
        int currentXIndex = 0;

        // System.out.println("coordX = " + currentXPos + " // coordY = " + currentYPos);
        for (int i = 0; i < (windowWidth / 20) * 2; i += 2) {
            sliceString = currentLine.substring(i, i + 1);
            
            // System.out.println("yIndexToinit = " + yIndexToInit + " // currentXIndex = " + currentXIndex + " // currentYPos = " + currentYPos + " // currentXPos = " + currentXPos);
            switch (sliceString){
                case "e":
                    // this.worldTextures[yIndexToInit][currentXIndex] = null;
                    // this.worldHitboxes[yIndexToInit][currentXIndex] = null;
                    break;
                case "b":
                    this.worldTextures[yIndexToInit][currentXIndex] = this.blockTexture;
                    this.worldBlockHitboxes[yIndexToInit][currentXIndex] = new Rectangle(currentXPos, currentYPos, 13, 20);
                    break;
                case "l":
                    this.worldTextures[yIndexToInit][currentXIndex] = this.ladderTexture;
                    this.worldLadderHitboxes[yIndexToInit][currentXIndex] = new Rectangle(currentXPos, currentYPos, 13, 20);
                    break;
                default:
                    throw new AssertionError();
            }
            
            currentXPos += 20;
            currentXIndex++;
        }
    }

    // Screen Res: X = [-180, 180] Y = [-90, 90]
    public void drawWorld(int windowWidth, int windowHeight) throws IOException{
        // To get the word that represent the block/ladder/gold/etc...
        int currentWorldYPosition = (windowHeight / 2) - 20;
        
        // Draw the world
        this.batch.begin();
        for (int y = (windowHeight / 20) - 1; y >= 0; y--) {
            for (int x = 0; x < (windowWidth / 20); x++) {
                this.drawLine(y, currentWorldYPosition, windowWidth);
            }
            // Get the next Y position for block placement (blocks are 20x20)
            currentWorldYPosition -= 20;
        }
        this.batch.end();
    }

    /**
     * @param :
     *  currentYPos: To track the ammount of the world that was already built
     *  ammountOfSlots: Get the ammount of blocks in a single line
     */
    private void drawLine(int currentYIndex, int currentYPosition, int windowWidth){
        int currentXPosition = windowWidth / 2 * -1;
        
        for (int i = 0; i < windowWidth / 20; i++) {
            if(this.worldTextures[currentYIndex][i] != null){
                this.batch.draw(this.worldTextures[currentYIndex][i], currentXPosition, currentYPosition);
            }

            currentXPosition += 20;
        }
    }

    private BufferedReader getWorldTxt(){
        try {
            return new BufferedReader(new FileReader(Gdx.files.internal(this.nameOfTxtFile + ".txt").file()));
        } catch (FileNotFoundException e) {
            System.err.println("\nERROR GameInterface/worldCreator.java: Function getWorldTxt catched FileNotFoundException");
            e.printStackTrace();
        } 
        return null;
    }


    public boolean playerDoesntOverlapWorld(Player player){
        for (int i = 0; i < 9; i++) {
            for(int j = 0; j < 16; j++){
                if((this.worldBlockHitboxes[i][j] != null) && (player.getHitbox().overlaps(this.worldBlockHitboxes[i][j]))){
                    System.out.println("Player is colliding with a wall");
                    return false;
                }
            }
        }
        return true;
    }
    public Rectangle playerOverlapWithALadder(Player player){
        for (int i = 0; i < 9; i++) {
            for(int j = 0; j < 16; j++){
                if((this.worldLadderHitboxes[i][j] != null) && (player.getHitbox().overlaps(this.worldLadderHitboxes[i][j]))){
                    System.out.println("Player is colliding with a ladder");
                    return this.worldLadderHitboxes[i][j];
                }
            }
        }
        return null;
    }

    //TODO make this func
    public boolean playerIsOnGround(Player player){
        for (int i = 0; i < 9; i++) {
            for(int j = 0; j < 16; j++){
                if((this.worldBlockHitboxes[i][j] != null) && (player.getIsOnGroundHitbox().overlaps(this.worldBlockHitboxes[i][j]))){
                    System.out.println("Player is colliding with a floor");
                    return true;
                }
            }
        }
        return false;
    }

    /** Used in debugging of hitboxes */
    public void printHitbox(){
        for (int y = 0; y < 9; y++) {
            for(int x = 0; x < 16; x++){
                System.out.print(this.worldBlockHitboxes[y][x] + " ");
            }
            System.out.println("");
        }
    }

    /** Used in debugging to see hitboxes
     * Red for walls
     * Green for ladders
     */
    public void displayHitboxes(){
        ShapeRenderer shapeRenderer = new ShapeRenderer();
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(Color.RED);

        Rectangle currentHitbox;
        for (int y = 8; y >= 0; y--) {
            for(int x = 0; x < 16; x++){
                currentHitbox = this.worldBlockHitboxes[y][x];
                if(currentHitbox != null){
                    shapeRenderer.rect(
                        currentHitbox.x + 200,
                        currentHitbox.y + 200,
                        currentHitbox.width,
                        currentHitbox.height
                    );
                }
            }
        }
        shapeRenderer.setColor(Color.GREEN);
        for (int y = 8; y >= 0; y--) {
            for(int x = 0; x < 16; x++){
                currentHitbox = this.worldLadderHitboxes[y][x];
                if(currentHitbox != null){
                    shapeRenderer.rect(
                        currentHitbox.x + 200,
                        currentHitbox.y + 200,
                        currentHitbox.width,
                        currentHitbox.height
                    );
                }
            }
        }
        shapeRenderer.end();
    }
}
