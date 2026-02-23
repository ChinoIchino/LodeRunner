package io.github.Chino.LodeRunner.GameInterface;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class WorldCreator{
    private SpriteBatch batch;
    private String nameOfTxtFile;

    //Textures used
    private Texture blockTexture;
    private Texture ladderTexture;

    public WorldCreator(SpriteBatch batch, String nameOfFile) {
        this.nameOfTxtFile = nameOfFile;
        this.batch = batch;

        this.initTextures();
    }

    /** Initiate the textures used in the world creation */
    private void initTextures(){
        AssetManager assetManager = new AssetManager();

        this.blockTexture = new Texture("data/textures/blocks/brick.png");
        this.ladderTexture = new Texture("data/textures/blocks/ladder.png");
    }


    // Screen Res: X = [-180, 180] Y = [-90, 90]
    public void drawWorld(int windowWidth, int windowHeight) throws IOException{
        BufferedReader bufferedReader = this.getWorldTxt();
        // Skip to line 21 of the WorldFile.txt
        for (int i = 0; i < 21; i++) {
            bufferedReader.readLine();
        }
        
        StringBuilder stringBuilder = new StringBuilder();

        String line = bufferedReader.readLine();
        char currentBlock;
        
        // To get the word that represent the block/ladder/gold/etc...
        int currentWorldYPosition = (windowHeight / 2) - 20;
        // Draw the world
        this.batch.begin();
        while(line != null){
            this.drawLine(line, currentWorldYPosition, windowWidth);
            
            // Get the next Y position for block placement (blocks are 20x20)
            currentWorldYPosition -= 20;
            // Get the next line in the txt
            line = bufferedReader.readLine();
        }
        this.batch.end();
    }

    /**
     * @param :
     *  currentYPos: To track the ammount of the world that was already built
     *  ammountOfSlots: Get the ammount of blocks in a single line
     */
    private void drawLine(String currentLine, int currentYPos, int windowWidth){
        // i += 2 to skip the commas
        String currentBlock;

        // Window width is 180 and we want to start at -90 because de widow is in range of [-90, 90] 
        int currentXPos = windowWidth / 2 * -1;
        for (int i = 0; i < (windowWidth / 20 * 2); i += 2) {
            currentBlock = currentLine.substring(i, i + 1);

            // batch.draw(this.logoImage, -500, -500, 1000, 1000);
            switch (currentBlock) {
                // Empty block does nothing
                case "e":
                    currentXPos += 20;
                    break;
                case "b":
                    // Param: texture, positionX, positionY, sizeXTexture, sizeYOfTexture
                    this.batch.draw(this.blockTexture, currentXPos, currentYPos, 20, 20);
                    currentXPos += 20;
                    break;
                case "l":
                    // Param: texture, positionX, positionY, sizeXTexture, sizeYOfTexture
                    this.batch.draw(this.ladderTexture, currentXPos, currentYPos, 20, 20);
                    currentXPos += 20;
                    break;
                default:
                    throw new AssertionError();
            }

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
}
