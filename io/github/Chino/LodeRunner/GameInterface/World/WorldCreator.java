package io.github.Chino.LodeRunner.GameInterface.World;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;

public class WorldCreator{
    private final String nameOfTxtFile;

    private final SpriteBatch batch;

    private final Texture[][] worldTextures = new Texture[9][16];
    //Rectangle[y][x]
    /** Hitboxes of the world*/
    private final Rectangle[][] worldBlockHitboxes = new Rectangle[9][16];
    private final Rectangle[][] worldLadderHitboxes = new Rectangle[9][16];

    // Textures used
    private Texture blockTexture;
    private Texture ladderTexture;

    public WorldCreator(SpriteBatch batch, String nameOfFile) {
        this.nameOfTxtFile = nameOfFile;
        
        this.batch = batch;

        this.initTextures();
    }

    /** Initiate the textures used in the world creation */
    private void initTextures(){
        this.blockTexture = new Texture("data/textures/blocks/brick.png");
        this.ladderTexture = new Texture("data/textures/blocks/ladder.png");
    }

    public WorldManager initWorld(int windowWidth, int windowHeight) throws IOException{
        BufferedReader bufferedReader = this.getWorldTxt();
        // Skip to line 21 of the WorldFile.txt
        for (int i = 0; i < 21; i++) {
            bufferedReader.readLine();
        }

        String line = bufferedReader.readLine();
    
        int currentYPosIndex = windowHeight / 20 - 1;
        // int currentYPos = windowHeight / 2 * -1 + 20;
        int currentYPos = windowHeight / 2 - 20;
        
        while(line != null){
            initLine(currentYPosIndex, currentYPos, windowWidth, line);

            // Goes to the next line index
            currentYPosIndex--;
            // Goes to the next pixel position
            // currentYPos += 20;
            currentYPos -= 20;
            // Get the next line
            line = bufferedReader.readLine();
        }

        return new WorldManager(this.worldTextures, this.worldBlockHitboxes, this.worldLadderHitboxes, this.batch);
    }

    private void initLine(int yIndexToInit, int currentYPos, int windowWidth, String currentLine){
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
