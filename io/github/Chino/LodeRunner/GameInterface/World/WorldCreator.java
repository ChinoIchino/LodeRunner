package io.github.Chino.LodeRunner.GameInterface.World;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public class WorldCreator{
    private final String nameOfTxtFile;

    private BufferedReader readerOfFile;

    private final SpriteBatch batch;

    private Vector2 worldResolution;

    private Block[][] blockMatrix;

    // Textures used
    private Texture blockTexture;
    private Texture ladderTexture;
    
    private Texture greenGemTexture;
    private Texture blueGemTexture;
    private Texture redGemTexture;

    public WorldCreator(SpriteBatch batch, String nameOfFile) {
        this.nameOfTxtFile = nameOfFile;
        
        this.batch = batch;

        this.initTextures();
    }

    /** Initiate the textures used in the world creation */
    private void initTextures(){
        this.blockTexture = new Texture("data/textures/blocks/dirt.png");
        this.ladderTexture = new Texture("data/textures/blocks/ladder.png");

        this.greenGemTexture = new Texture("data/textures/collectibles/greenGem.png");
        this.blueGemTexture = new Texture("data/textures/collectibles/blueGem.png");
        this.redGemTexture = new Texture("data/textures/collectibles/redGem.png");
    }

    private void initMatrixFromWorldResolution(){
        this.blockMatrix = new Block[(int) this.worldResolution.y][(int) this.worldResolution.x];
    }

    public WorldManager initWorld() throws IOException{
        this.worldResolution = getResolutionOfWorld();

        initMatrixFromWorldResolution();

        int worldWidth = (int) (this.worldResolution.x * 32);
        int worldHeight = (int) (this.worldResolution.y * 32);

        // Skip to line 3 of the WorldFile.txt
        this.readerOfFile.readLine();

        String line = readerOfFile.readLine();
    
        int currentYPosIndex = worldHeight / 32 - 1;
        // int currentYPos = worldHeight / 2 * -1 + 20;
        int currentYPos = worldHeight / 2 - 32;
        System.out.println("Got the line:");
        for (int i = 0; i < (int) this.worldResolution.y - 1; i++) {
            System.out.println(line);
            initLine(currentYPosIndex, currentYPos, worldWidth, line);

            // Goes to the next line index
            currentYPosIndex--;
            // Goes to the next pixel position
            // currentYPos += 20;
            currentYPos -= 32;
            // Get the next line
            line = readerOfFile.readLine();
        }
        return new WorldManager(this.worldResolution, this.blockMatrix, this.batch);
    }

    private void initLine(int yIndexToInit, int currentYPos, int worldWidth, String currentLine){
        String sliceString;

        int currentXPos = worldWidth / 2 * -1;
        int currentXIndex = 0;

        // System.out.println("coordX = " + currentXPos + " // coordY = " + currentYPos);
        for (int i = 0; i < (worldWidth / 32) * 2; i += 2) {
            sliceString = currentLine.substring(i, i + 1);
            
            // System.out.println("yIndexToinit = " + yIndexToInit + " // currentXIndex = " + currentXIndex + " // currentYPos = " + currentYPos + " // currentXPos = " + currentXPos);
            switch (sliceString){
                case "e":
                    // this.worldTextures[yIndexToInit][currentXIndex] = null;
                    // this.worldHitboxes[yIndexToInit][currentXIndex] = null;
                    break;
                case "c":
                    this.blockMatrix[yIndexToInit][currentXIndex] = new Block(
                        this.blockTexture,
                        new Rectangle(currentXPos, currentYPos, 32, 32),
                        true
                    );
                    break;
                case "l":
                    this.blockMatrix[yIndexToInit][currentXIndex] = new Block(
                        this.ladderTexture,
                        new Rectangle(currentXPos, currentYPos, 32, 32),
                        false
                    );
                    break;

                case "g":
                    this.blockMatrix[yIndexToInit][currentXIndex] = new Collectible(
                        this.greenGemTexture,
                        new Rectangle(currentXPos, currentYPos, 25, 25),
                        100
                    );
                    break;
                case "r":
                    this.blockMatrix[yIndexToInit][currentXIndex] = new Collectible(
                        this.redGemTexture,
                        new Rectangle(currentXPos, currentYPos, 25, 25),
                        200
                    );
                    break;
                case "b":
                    this.blockMatrix[yIndexToInit][currentXIndex] = new Collectible(
                        this.blueGemTexture,
                        new Rectangle(currentXPos, currentYPos, 25, 25),
                        500
                    );
                    break;
                default:
                    throw new AssertionError();
            }
            
            currentXPos += 32;
            currentXIndex++;
        }
    }
    
    private Vector2 getResolutionOfWorld(){
        Vector2 worldResolution = new Vector2();
        
        this.readerOfFile = getWorldTxt();
        try {

            String line = this.readerOfFile.readLine();
            worldResolution.x = Float.parseFloat(line.substring(2,4));
            worldResolution.y = Float.parseFloat(line.substring(7));

            System.out.println("Got the vector2 x = " + worldResolution.x + " // y = " + worldResolution.y);

            return worldResolution;
        } catch (IOException e) {
            System.err.println("\nERROR GameInterface/World/WorldCreator.java: Function getResolutionOfWorld catched a IOException");
            e.printStackTrace();
        }
        return null;
    }


    @SuppressWarnings("CallToPrintStackTrace")
    private BufferedReader getWorldTxt(){
        try {
            return new BufferedReader(new FileReader(Gdx.files.internal(this.nameOfTxtFile + ".txt").file()));
        } catch (FileNotFoundException e) {
            System.err.println("\nERROR GameInterface/worldCreator.java: Function getWorldTxt catched FileNotFoundException");
            e.printStackTrace();
        } 
        return null;
    }
}
