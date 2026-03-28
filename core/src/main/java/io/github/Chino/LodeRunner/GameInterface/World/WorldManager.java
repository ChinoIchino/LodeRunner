package io.github.Chino.LodeRunner.GameInterface.World;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
        ArrayList<ArrayList<Integer>> possibleLevels = getPossibleLevels(player);

        for (Integer i: possibleLevels.get(0)) {
            for(Integer j: possibleLevels.get(1)){
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
    public Rectangle playerOverlapWithALadder(Player player){
        ArrayList<ArrayList<Integer>> possibleLevels = getPossibleLevels(player);

        for (Integer i: possibleLevels.get(0)) {
            for(Integer j: possibleLevels.get(1)){
                if((this.blockMatrix[i][j] != null) 
                    && (player.getHitbox().overlaps(this.blockMatrix[i][j].getHitbox())
                    && this.blockMatrix[i][j].isClimbable()
                )){
                    // System.out.println("Player is colliding with a ladder");
                    return this.blockMatrix[i][j].getHitbox();
                }
            }
        }
        return null;
    }

    public boolean playerOverlapWithFloor(Player player){
        ArrayList<ArrayList<Integer>> possibleLevels = getPossibleLevels(player);

        for(Integer y: possibleLevels.get(0)){
            for(Integer x: possibleLevels.get(1)){
                if(this.blockMatrix[y][x] != null){
                    if(player.getHitbox().overlaps(this.blockMatrix[y][x].getHitbox()) && this.blockMatrix[y][x].isSolid()){
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public boolean playerIsOnGround(Player player){
        ArrayList<ArrayList<Integer>> possibleLevels = getPossibleLevels(player);

        for (Integer i: possibleLevels.get(0)) {
            for(Integer j: possibleLevels.get(1)){
                if((this.blockMatrix[i][j] != null) && (player.getIsOnGroundHitbox().overlaps(this.blockMatrix[i][j].getHitbox()))){
                    // System.out.println("Player is colliding with a floor");
                    return true;
                }
            }
        }
        return false;
    }

    public List<Object> playerOverlapWithCollectible(Player player){
        ArrayList<ArrayList<Integer>> possibleLevels = getPossibleLevels(player);

        for (Integer y: possibleLevels.get(0)) {
            for (Integer x: possibleLevels.get(1)) {
                if(this.blockMatrix[y][x] != null && this.blockMatrix[y][x] instanceof Collectible){
                    if(this.blockMatrix[y][x].getHitbox().overlaps(player.getHitbox())){
                        // System.out.println("Colliding with a collectible");

                        List<Object> toReturn = new ArrayList<>();
                        // Casted Collectible because if the statement is correctly it must be a Collectible
                        toReturn.add((Collectible) this.blockMatrix[y][x]);
                        toReturn.add(y);
                        toReturn.add(x);
                        
                        this.blockMatrix[y][x] = null;
                        return toReturn;
                    }
                }
            }
        }
        return null;
    }

    private ArrayList<ArrayList<Integer>> getPossibleLevels(Player player){
        ArrayList<ArrayList<Integer>> possibleLevels = new ArrayList<>();
        ArrayList<Integer> yPossibleLevels = new ArrayList<>();
        
        int currentY = (int) this.worldResolution.y * -16;
        for (int i = 0; i < (int) this.worldResolution.y; i++) {
            if(currentY <= player.getPosY() && (currentY + 32) >= player.getPosY()){
                yPossibleLevels.add(i);
                break;
            }
            currentY += 32;
        }

        if(yPossibleLevels.get(0) != 0){
            yPossibleLevels.add(yPossibleLevels.get(0) - 1);
        }
        if(this.worldResolution.y - 1 != yPossibleLevels.get(0)){
            yPossibleLevels.add(yPossibleLevels.get(0) + 1);
        }

        possibleLevels.add(yPossibleLevels);

        ArrayList<Integer> xPossibleLevels = new ArrayList<>();

        int currentX = (int) this.worldResolution.x * -16;
        for(int i = 0; i < (int) this.worldResolution.x; i++){
            if(currentX <= player.getPosX() && (currentX + 32) >= player.getPosX()){
                xPossibleLevels.add(i);
                break;
            }
            currentX += 32;
        }

        if(xPossibleLevels.get(0) != 0){
            xPossibleLevels.add(xPossibleLevels.get(0) - 1);
        }
        if(this.worldResolution.x - 1 != xPossibleLevels.get(0)){
            xPossibleLevels.add(xPossibleLevels.get(0) + 1);
        }

        possibleLevels.add(xPossibleLevels);

        return possibleLevels;
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

    // Debugger for the function getPossibleLevels
    public void printDebugForPossibleLevels(ArrayList<ArrayList<Integer>> possibleLevels){
        System.out.print("Got the levels: \nY: " + possibleLevels.get(0).get(0) + " / " + possibleLevels.get(0).get(1));
        if(possibleLevels.get(0).size() == 3){
            System.out.print(" / " + possibleLevels.get(0).get(2)); 
        }
        System.out.print("\nX: " + possibleLevels.get(1).get(0) + " / " + possibleLevels.get(1).get(1));
        if(possibleLevels.get(1).size() == 3){
            System.out.println(" / " + possibleLevels.get(1).get(2));
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
