package io.github.Chino.LodeRunner.GameInterface.World;

import java.io.IOException;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import io.github.Chino.LodeRunner.GameInterface.Player.Player;

public class WorldManager {
    private final SpriteBatch batch;

    public Vector2 worldResolution;

    private final Block[][] blockMatrix;

    private final BreakBlockThreadManager breakBlockManager = new BreakBlockThreadManager(this);
    
    public WorldManager(Vector2 worldResolution, Block[][] blockMatrix, SpriteBatch batch) {
        this.worldResolution = worldResolution;
        
        this.blockMatrix = blockMatrix;

        this.batch = batch;
    }
    
    
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

        System.out.println("Got the level " + level + " // and the yPos =" + yPosition);
        for(int x = 0; x < (int) (this.worldResolution.x); x++){
            if(this.blockMatrix[level][x] != null){
                if(player.getHitbox().overlaps(this.blockMatrix[level][x].getHitbox()) && this.blockMatrix[level][x].isSolid()){
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

    public Collectible playerOverlapWithCollectible(Player player){
        int[] possibleLevels = new int[3];
        
        int currentY = (int) this.worldResolution.y * -16;
        for (int i = 0; i < (int) this.worldResolution.y; i++) {
            if(currentY <= player.getPosY() && (currentY + 32) >= player.getPosY()){
                possibleLevels[1] = i;
                break;
            }
        }
        possibleLevels[0] = possibleLevels[1] - 1;
        possibleLevels[2] = possibleLevels[1] + 1;

        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < (int) this.worldResolution.x; x++) {
                if(this.blockMatrix[y][x] != null && this.blockMatrix[y][x] instanceof Collectible){
                    if(this.blockMatrix[y][x].getHitbox().overlaps(player.getHitbox())){
                        // Casted Collectible because if the statement is correctly it must be a Collectible
                        Collectible toReturn = (Collectible) this.blockMatrix[y][x];
                        this.blockMatrix[y][x] = null;
                        return toReturn;
                    }
                }
            }
        }
        return null;
    }

    public void breakBlockAtPos(int x, int y){
        int blockXIndex = ((int) x / 32) + 17;
        int blockYIndex = (((int) y / 32)-1)+5;
        if((int) x / 32 - x%32 >= 16){
            blockXIndex++;
        }else{
            blockXIndex--;
        }
        System.out.println("x : " +blockXIndex + "   y : " + blockYIndex);
        if(this.blockMatrix[blockYIndex][blockXIndex] != null && this.blockMatrix[blockYIndex][blockXIndex] instanceof Block && this.blockMatrix[blockYIndex][blockXIndex].isSolid()){
            this.breakBlockManager.breakBlock(blockXIndex, blockYIndex);
        }else System.out.println("Breaking that is impossible");
    }
    public void setBlockTextureAt(int x,int y,Texture texture){
        this.blockMatrix[y][x].setTexture(texture);
    }
    public void setBlockAt(int x, int y, Block block){
        this.blockMatrix[y][x] = block;
    }

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
