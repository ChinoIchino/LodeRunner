package io.github.Chino.LodeRunner.GameInterface.World;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import javax.management.InvalidAttributeValueException;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public class WorldCreator{
    private static final String WORLD_FILE_NAME = "WorldFile";

    private BufferedReader readerOfFile;
    // Used to init the next level, it skips all the lines to the next level
    // At start need to be at 1, to skip the first line of the world file
    private int lineOffset = 1;

    private final SpriteBatch batch;

    private Vector2 worldResolution;

    private Block[][] blockMatrix;

    private int ammountOfCollectible;

    // Textures used
    private Texture blockTexture;
    // Changed to public static, to use in WorldManager.java/openExitToNextLevel()
    public static Texture ladderTexture;
    
    private Texture greenGemTexture;
    private Texture blueGemTexture;
    private Texture redGemTexture;

    public WorldCreator(SpriteBatch batch) {
        this.batch = batch;

        this.initTextures();
    }

    /** Initiate the textures used in the world creation */
    private void initTextures(){
        this.blockTexture = new Texture("data/textures/blocks/dirt.png");
        WorldCreator.ladderTexture = new Texture("data/textures/blocks/ladder.png");

        this.greenGemTexture = new Texture("data/textures/collectibles/greenGem.png");
        this.blueGemTexture = new Texture("data/textures/collectibles/blueGem.png");
        this.redGemTexture = new Texture("data/textures/collectibles/redGem.png");
    }

    private void initMatrixFromWorldResolution(){
        this.blockMatrix = new Block[(int) this.worldResolution.y][(int) this.worldResolution.x];
    }

    public WorldManager initWorld() throws IOException{
        this.ammountOfCollectible = 0;

        this.worldResolution = getResolutionOfWorld();
        
        initMatrixFromWorldResolution();
        
        int worldWidth = (int) (this.worldResolution.x * 32);
        int worldHeight = (int) (this.worldResolution.y * 32);
        
        // Skip to line 4 of the WorldFile.txt
        this.readerOfFile.readLine();

        String line;
        
        int currentYPosIndex = worldHeight / 32 - 1;
        // int currentYPos = worldHeight / 2 * -1 + 20;
        int currentYPos = worldHeight / 2 - 32;
        // System.out.println("Got the lines:");
        for (int i = 0; i < (int) this.worldResolution.y; i++) {
            line = readerOfFile.readLine();
            // System.out.println(line);
            initLine(currentYPosIndex, currentYPos, worldWidth, line);
            
            // Goes to the next line index
            currentYPosIndex--;
            // Goes to the next pixel position
            currentYPos -= 32;
            // Get the next line
        }
        
        this.lineOffset += this.worldResolution.y + 3;
        return new WorldManager(this.worldResolution, this.blockMatrix, this.batch, this.ammountOfCollectible);
    }

    public WorldManager initWorldFromPacket(char[][] worldMatrix) throws InvalidAttributeValueException{
        this.ammountOfCollectible = 0;

        this.worldResolution.y = worldMatrix.length;
        this.worldResolution.x = worldMatrix[0].length;

        initMatrixFromWorldResolution();
        
        int worldWidth = worldMatrix[0].length * 32;
        int worldHeight = worldMatrix.length * 32;
        
        int currentYPosIndex = worldHeight / 32 - 1;
        // int currentYPos = worldHeight / 2 * -1 + 20;
        int currentYPos = worldHeight / 2 - 32;
        // System.out.println("Got the lines:");
        for (int i = 0; i < (int) this.worldResolution.y; i++) {
            // System.out.println(line);
            this.initLineFromPacket(currentYPosIndex, currentYPos, worldWidth, worldMatrix[i]);
            
            // Goes to the next line index
            currentYPosIndex--;
            // Goes to the next pixel position
            currentYPos -= 32;
        }
        
        this.lineOffset += this.worldResolution.y + 3;
        return new WorldManager(this.worldResolution, this.blockMatrix, this.batch, this.ammountOfCollectible);
    }

    // Used when the host create a lobby, so the server keep the maps
    public static ArrayList<char[][]> getAllMaps() throws IOException{
        ArrayList<char[][]> maps = new ArrayList<>();

        BufferedReader readerOfFile = getWorldTxt();

        // Start reading after the "Levels="
        int ammountOfLevels = Integer.parseInt(readerOfFile.readLine().substring(7));
        String currentLine;

        for (int i = 0; i < ammountOfLevels; i++) {
            // Read the resolution line
            currentLine = readerOfFile.readLine();
            char[][] currentMap = new char[Integer.parseInt(currentLine.substring(7))][Integer.parseInt(currentLine.substring(2,4))];

            // Skip the "Here put the world to generate:"
            readerOfFile.readLine();

            for (int y = 0; y < currentMap.length; y++) {
                currentLine = readerOfFile.readLine();
                for (int x = 0; x < currentMap[y].length; x++) {
                    currentMap[y][x] = currentLine.charAt(x * 2);
                }
            }

            maps.add(currentMap);

            // Skip the empty space between levels
            readerOfFile.readLine();
        }

        return maps;
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
                    break;
                case "c":
                    this.blockMatrix[yIndexToInit][currentXIndex] = new Block(
                        this.blockTexture,
                        new Rectangle(currentXPos, currentYPos, 32, 32),
                        true,
                        false
                    );
                    break;
                case "l":
                    this.blockMatrix[yIndexToInit][currentXIndex] = new Block(
                        WorldCreator.ladderTexture,
                        new Rectangle(currentXPos, currentYPos, 32, 32),
                        false,
                        true
                    );
                    break;
                case "g":
                    this.blockMatrix[yIndexToInit][currentXIndex] = new Collectible(
                        this.greenGemTexture,
                        new Rectangle(currentXPos, currentYPos, 25, 25),
                        100
                    );
                    this.ammountOfCollectible++;
                    break;
                case "r":
                    this.blockMatrix[yIndexToInit][currentXIndex] = new Collectible(
                        this.redGemTexture,
                        new Rectangle(currentXPos, currentYPos, 25, 25),
                        200
                    );
                    this.ammountOfCollectible++;
                    break;
                case "b":
                    this.blockMatrix[yIndexToInit][currentXIndex] = new Collectible(
                        this.blueGemTexture,
                        new Rectangle(currentXPos, currentYPos, 25, 25),
                        500
                    );
                    this.ammountOfCollectible++;
                    break;
                default:
                    throw new AssertionError();
            }
            
            currentXPos += 32;
            currentXIndex++;
        }
    }
    private void initLineFromPacket(int yIndexToInit, int currentYPos, int worldWidth, char[] line) throws InvalidAttributeValueException{
        int currentXPos = worldWidth / 2 * -1;
        int currentXIndex = 0;

        // System.out.println("coordX = " + currentXPos + " // coordY = " + currentYPos);
        for (int i = 0; i < (worldWidth / 32) * 2; i += 2) {
            // System.out.println("yIndexToinit = " + yIndexToInit + " // currentXIndex = " + currentXIndex + " // currentYPos = " + currentYPos + " // currentXPos = " + currentXPos);
            switch (line[currentXIndex]){
                case 'e':
                    break;
                case 'c':
                    this.blockMatrix[yIndexToInit][currentXIndex] = new Block(
                        this.blockTexture,
                        new Rectangle(currentXPos, currentYPos, 32, 32),
                        true,
                        false
                    );
                    break;
                case 'l':
                    this.blockMatrix[yIndexToInit][currentXIndex] = new Block(
                        WorldCreator.ladderTexture,
                        new Rectangle(currentXPos, currentYPos, 32, 32),
                        false,
                        true
                    );
                    break;
                case 'g':
                    this.blockMatrix[yIndexToInit][currentXIndex] = new Collectible(
                        this.greenGemTexture,
                        new Rectangle(currentXPos, currentYPos, 25, 25),
                        100
                    );
                    this.ammountOfCollectible++;
                    break;
                case 'r':
                    this.blockMatrix[yIndexToInit][currentXIndex] = new Collectible(
                        this.redGemTexture,
                        new Rectangle(currentXPos, currentYPos, 25, 25),
                        200
                    );
                    this.ammountOfCollectible++;
                    break;
                case 'b':
                    this.blockMatrix[yIndexToInit][currentXIndex] = new Collectible(
                        this.blueGemTexture,
                        new Rectangle(currentXPos, currentYPos, 25, 25),
                        500
                    );
                    this.ammountOfCollectible++;
                    break;
                default:
                    throw new InvalidAttributeValueException("Error");
            }
            
            currentXPos += 32;
            currentXIndex++;
        }
    }
    
    private Vector2 getResolutionOfWorld(){
        Vector2 worldResolution = new Vector2();
        
        this.readerOfFile = getWorldTxt();
        try {
            // Goes by the offset ammount
            for (int i = 0; i < this.lineOffset; i++) {
                this.readerOfFile.readLine();
            }

            String line = this.readerOfFile.readLine();
            worldResolution.x = Float.parseFloat(line.substring(2,4));
            worldResolution.y = Float.parseFloat(line.substring(7));

            // System.out.println("Got the vector2 x = " + worldResolution.x + " // y = " + worldResolution.y);

            return worldResolution;
        } catch (IOException e) {
            System.err.println("\nERROR GameInterface/World/WorldCreator.java: Function getResolutionOfWorld catched a IOException");
        }
        return null;
    }

    private static BufferedReader getWorldTxt(){
        try {
            return new BufferedReader(new FileReader(Gdx.files.internal(WORLD_FILE_NAME + ".txt").file()));
        } catch (FileNotFoundException e) {
            System.err.println("\nERROR GameInterface/worldCreator.java: Function getWorldTxt catched FileNotFoundException");
        } 
        return null;
    }
}
