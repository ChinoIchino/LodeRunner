package io.github.Chino.LodeRunner.GameInterface.World;

import java.io.IOException;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import io.github.Chino.LodeRunner.GameInterface.Player.Player;

public class WorldManager {
    private SpriteBatch batch;

    public Vector2 worldResolution;

    private Block[][] blockMatrix;

    //TODO to implement
    // private final BreakBlockThreadManager breakBlockManager = new BreakBlockThreadManager(this);

    public WorldManager(Vector2 worldResolution, Block[][] blockMatrix, SpriteBatch batch) {
        this.worldResolution = worldResolution;

        this.blockMatrix = blockMatrix;

        this.batch = batch;
    }

    

    // Screen Res: X = [-180, 180] Y = [-90, 90]
    public void drawWorld() throws IOException{
        // To get the word that represent the block/ladder/gold/etc...
        int currentWorldYPosition = ((int) (this.worldResolution.y) * 32 / 2) - 32;
        
        // Draw the world
        this.batch.begin();
        for (int y = (int) (this.worldResolution.y) - 1; y >= 0; y--) {
            for (int x = 0; x < (int) (this.worldResolution.x) - 1; x++) {
                this.drawLine(y, currentWorldYPosition);
            }
            // Get the next Y position for block placement (blocks are 32x32)
            currentWorldYPosition -= 32;
        }
        this.batch.end();
    }

    /**
     * @param :
     *  currentYPos: To track the ammount of the world that was already built
     *  ammountOfSlots: Get the ammount of blocks in a single line
     */
    private void drawLine(int currentYIndex, int currentYPosition){
        int currentXPosition = (int) (this.worldResolution.x) * 32 / 2 * -1;
        
        for (int i = 0; i < (int) (this.worldResolution.x); i++) {
            if(this.blockMatrix[currentYIndex][i] != null){
                this.batch.draw(this.blockMatrix[currentYIndex][i].getTexture(), currentXPosition, currentYPosition);
            }

            currentXPosition += 32;
        }
    }

    // public playerBreakBlock(Player player){
    //     Rectangle toBreak = isBlockThere(x, y);
    // }
    
    public boolean playerDoesntOverlapWorld(Player player){
        for (int i = 0; i < (int) (this.worldResolution.y); i++) {
            for(int j = 0; j < (int) (this.worldResolution.x); j++){
                if(
                    (this.blockMatrix[i][j] != null)
                    && this.blockMatrix[i][j].isSolid() 
                    && player.getHitbox().overlaps(this.blockMatrix[i][j].getHitbox())
                ){
                    // System.out.println("Player is colliding with a wall");
                    return false;
                }
            }
        }
        return true;
    }
    public Rectangle playerOverlapWithALadder(Rectangle hitboxOfPlayer){
        for (int i = 0; i < (int) (this.worldResolution.y); i++) {
            for(int j = 0; j < (int) (this.worldResolution.x); j++){
                if((this.blockMatrix[i][j] != null) && (hitboxOfPlayer.overlaps(this.blockMatrix[i][j].getHitbox()))){
                    // System.out.println("Player is colliding with a ladder");
                    return this.blockMatrix[i][j].getHitbox();
                }
            }
        }
        return null;
    }
    public boolean playerOverlapWithFloor(Player player){
        int level = 0;
        int yPosition = (int) (this.worldResolution.y) * -10;
        for (int j = (int) this.worldResolution.y; j >= 0; j++) {
            if(player.getPosY() < yPosition){
                break;
            }
            yPosition += 32;
            level++;
        }
        // this.printHitbox();
        System.out.println("Got the level " + level + " // and the yPos =" + yPosition);
        for(int x = 0; x < (int) (this.worldResolution.x); x++){
            if(this.blockMatrix[level][x] != null){
                if(player.getHitbox().overlaps(this.blockMatrix[level][x].getHitbox())){
                    return true;
                }
            }
        }
        return false;
    }

    public boolean playerIsOnGround(Player player){
        for (int i = 0; i < (int) (this.worldResolution.y); i++) {
            for(int j = 0; j < (int) (this.worldResolution.x); j++){
                if((this.blockMatrix[i][j] != null) && (player.getIsOnGroundHitbox().overlaps(this.blockMatrix[i][j].getHitbox()))){
                    // System.out.println("Player is colliding with a floor");
                    return true;
                }
            }
        }
        return false;
    }

    // private Rectangle isBlockThere(int x, int y){
    //     int blockXIndex = (int) x / 32;
    //     int blockYIndex = (int) y / 32;

    //     return this.worldBlockHitboxes[]


    // }

    /** Used in debugging of hitboxes */
    public void printHitbox(){
        for (int y = 0; y < (int) (this.worldResolution.y); y++) {
            for(int x = 0; x < (int) (this.worldResolution.x); x++){
                if(this.blockMatrix[y][x] != null){
                    System.out.print(this.blockMatrix[y][x].getHitbox() + " ");
                }
            }
            System.out.println("\n");
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

        Block currentBlock;
        for (int y = (int) (this.worldResolution.y) - 1; y >= 0; y--) {
            for(int x = 0; x < (int) (this.worldResolution.x); x++){
                currentBlock = this.blockMatrix[y][x];
                if(currentBlock != null && currentBlock.isSolid()){
                    shapeRenderer.setColor(Color.RED);
                    shapeRenderer.rect(
                        currentBlock.getHitbox().x + 900,
                        currentBlock.getHitbox().y + 200,
                        currentBlock.getHitbox().width,
                        currentBlock.getHitbox().height
                    );
                }else if(currentBlock != null){
                    shapeRenderer.setColor(Color.GREEN);
                    shapeRenderer.rect(
                        currentBlock.getHitbox().x + 900,
                        currentBlock.getHitbox().y + 200,
                        currentBlock.getHitbox().width,
                        currentBlock.getHitbox().height
                    );
                }
            }
        }
        shapeRenderer.end();
    }
}
