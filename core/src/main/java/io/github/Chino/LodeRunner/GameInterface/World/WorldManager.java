package io.github.Chino.LodeRunner.GameInterface.World;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Queue;
import java.util.List;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import io.github.Chino.LodeRunner.GameInterface.Entity.*;

public class WorldManager {
    private final SpriteBatch batch;

    public Vector2 worldResolution;
    
    private final Block[][] blockMatrix;

    private final BreakBlockThreadManager breakBlockManager = new BreakBlockThreadManager(this);

    private int ammountOfCollectible;
    
    public WorldManager(Vector2 worldResolution, Block[][] blockMatrix, SpriteBatch batch, int ammountOfCollectible) {
        this.worldResolution = worldResolution;
        
        this.blockMatrix = blockMatrix;

        this.batch = batch;

        this.ammountOfCollectible = ammountOfCollectible;
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
    public boolean entityDoesntOverlapWorld(Rectangle hitboxOfEntity){
        double left   = hitboxOfEntity.x;
        double right  = hitboxOfEntity.x + hitboxOfEntity.width-1;
        double bottom = hitboxOfEntity.y;
        double top    = hitboxOfEntity.y + hitboxOfEntity.height-1;

        int blockLeft   = (int)Math.floor(left / 32.0)+this.blockMatrix[0].length /2;
        int blockRight  = (int)Math.floor(right / 32.0)+this.blockMatrix[0].length /2;
        int blockBottom = (int)Math.floor(bottom / 32.0)+this.blockMatrix.length /2;
        int blockTop    = (int)Math.floor(top / 32.0)+this.blockMatrix.length /2;

            for (int i = blockBottom; i <= blockTop; i++) {
                for(int j = blockLeft; j <= blockRight; j++){

                    if (i < 0 || i >= blockMatrix.length) continue;
                    if (j < 0 || j >= blockMatrix[0].length) continue;
                    
                    if(
                        (this.blockMatrix[i][j] != null)
                        && this.blockMatrix[i][j].isSolid() 
                        && hitboxOfEntity.overlaps(this.blockMatrix[i][j].getHitbox())
                    ){
                        return false;
                    }
                }
            }    
        return true;
    }

    public Rectangle entityOverlapWithALadder(Rectangle hitboxOfEntity){
        double left   = hitboxOfEntity.x;
        double right  = hitboxOfEntity.x + hitboxOfEntity.width-1;
        double bottom = hitboxOfEntity.y;
        double top    = hitboxOfEntity.y + hitboxOfEntity.height-1;

        int blockLeft   = (int)Math.floor(left / 32.0)+this.blockMatrix[0].length /2;
        int blockRight  = (int)Math.floor(right / 32.0)+this.blockMatrix[0].length /2;
        int blockBottom = (int)Math.floor(bottom / 32.0)+this.blockMatrix.length /2;
        int blockTop    = (int)Math.floor(top / 32.0)+this.blockMatrix.length /2;

            for (int i = blockBottom; i <= blockTop; i++) {
                for(int j = blockLeft; j <= blockRight; j++){

                    if (i < 0 || i >= blockMatrix.length) continue;
                    if (j < 0 || j >= blockMatrix[0].length) continue;
                    
                    if(
                        (this.blockMatrix[i][j] != null)
                        && this.blockMatrix[i][j].isLadder() 
                        && hitboxOfEntity.overlaps(this.blockMatrix[i][j].getHitbox())
                    ){
                        return this.blockMatrix[i][j].getHitbox();
                    }
                }
            }    
        return null;
    }

    // // When the player touch the top part of the map it means the player accessed the next level
    public boolean playerOverlapWithNextLevel(Player player){
        // Added the -20 so the player dont need to go all the way up the ladder
        // System.out.println(player.getPosY() + " >= " + (this.worldResolution.y * 16 - 20));
        return player.getPosY() >= (this.worldResolution.y * 16 - 20);
    }

    public boolean isLadderUnderEntity(Entity entity){
        int blockX = ((int)(entity.getHitbox().x / 32))+getBlockMatrix()[0].length/2;
        int blockYBottom = (((int) (entity.getHitbox().y-1)/ 32)+getBlockMatrix().length/2)-1;
        if(blockX <0 || blockX >=getBlockMatrix()[0].length || blockYBottom < 0 || blockYBottom >=getBlockMatrix().length|| getBlockMatrix()[blockYBottom][blockX] == null) return false;
        return getBlockMatrix()[blockYBottom][blockX].isLadder();
    }

    public boolean entityOverlapWithFloor(Entity entity){
        ArrayList<ArrayList<Integer>> possibleLevels = getPossibleLevels(entity);
        if(possibleLevels.size()==0) return false;

        for(Integer y: possibleLevels.get(0)){
            for(Integer x: possibleLevels.get(1)){
                if(this.blockMatrix[y][x] != null){
                    if(entity.getHitbox().overlaps(this.blockMatrix[y][x].getHitbox()) && this.blockMatrix[y][x].isSolid()){
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public boolean entityIsOnGround(Entity entity){
        for (int i = 0; i < (int) (this.worldResolution.y); i++) {
            for(int j = 0; j < (int) (this.worldResolution.x); j++){
                if((this.blockMatrix[i][j] != null) && (entity.getIsOnGroundHitbox().overlaps(this.blockMatrix[i][j].getHitbox()))){
                    // System.out.println("Player is colliding with a floor");
                    return true;
                }
            }
        }
        return false;
    }
    public boolean entityReachGroungWithALadder(Entity entity){
        Block blockUnderEntity = null;
        int blockX = ((int) entity.getPosX() / 32)+this.blockMatrix[0].length/2;
        int blockY = (((int) entity.getPosY()/ 32)+this.blockMatrix.length/2)-1;
        if(blockX >=0 && blockY >= 0)blockUnderEntity = this.blockMatrix[blockY][blockX];
        if(blockUnderEntity != null)if(blockUnderEntity.isSolid()){
            return true;
        }
        return false;
    }

    public boolean entityFellOutTheWorld(Entity entity){
        return entity.getPosY() < this.getBottomYPosition()-32;
    }

    public List<Object> playerOverlapWithCollectible(Player player){
        ArrayList<ArrayList<Integer>> possibleLevels = getPossibleLevels(player);
        if (possibleLevels.size() == 0 ) return null;

        for (Integer y: possibleLevels.get(0)) {
            for (Integer x: possibleLevels.get(1)) {
                if(this.blockMatrix[y][x] != null && this.blockMatrix[y][x] instanceof Collectible){
                    if(this.blockMatrix[y][x].getHitbox().overlaps(player.getHitbox())){

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

    private ArrayList<ArrayList<Integer>> getPossibleLevels(Entity entity){
        ArrayList<ArrayList<Integer>> possibleLevels = new ArrayList<>();
        ArrayList<Integer> yPossibleLevels = new ArrayList<>();
        ArrayList<Integer> xPossibleLevels = new ArrayList<>();
        double left   = entity.getHitbox().x;
        double right  = entity.getHitbox().x + entity.getHitbox().width-1;
        double bottom = entity.getHitbox().y;
        double top    = entity.getHitbox().y + entity.getHitbox().height-1;

        int blockLeft   = (int)Math.floor(left / 32.0)+this.blockMatrix[0].length /2;
        int blockRight  = (int)Math.floor(right / 32.0)+this.blockMatrix[0].length /2;
        int blockBottom = (int)Math.floor(bottom / 32.0)+this.blockMatrix.length /2;
        int blockTop    = (int)Math.floor(top / 32.0)+this.blockMatrix.length /2;

            for (int i = blockBottom; i <= blockTop; i++) {
                if (i < 0 || i >= blockMatrix.length) continue;
                yPossibleLevels.add(i);
                for(int j = blockLeft; j <= blockRight; j++){

                    if (i < 0 || i >= blockMatrix.length) continue;
                    if (j < 0 || j >= blockMatrix[0].length) continue;
                    
                    xPossibleLevels.add(j);
                }
            }    

        possibleLevels.add(yPossibleLevels);
        possibleLevels.add(xPossibleLevels);
        return possibleLevels;
    }

    //Take the bottom left block and return his y position
    public int getBottomYPosition(){
        return (int) (this.blockMatrix[0][0].getHitbox().y);
    }

    public void openExitToNextLevel(){
        Object[] ladderInformations = getNearestTopLadderInformations();

        // Start changing blocks above the found ladder
        int currentYHitboxOffset = (int) ((Block) ladderInformations[0]).getHitbox().y + 32;
        int xHitboxPosition = (int) ((Block) ladderInformations[0]).getHitbox().x;
        for (int y = (int) ladderInformations[1] + 1; y < this.worldResolution.y; y++) {
            // System.out.println("About to replace the block at indexes: " + y + " x " + ladderInformations[2]);
            // Use the replaced block to set the same hitbox for the ladder
            this.setBlockAt(
                (int) ladderInformations[2],
                y,
                new Block(WorldCreator.ladderTexture,
                new Rectangle(xHitboxPosition, currentYHitboxOffset, 32, 32),
                false,
                true
            ));

            currentYHitboxOffset += 32;
        }
    }

    // Search for the first ladder from the top of the world
    // Return a Object[Block ladderFound, int yIndexOfLadder, int xIndexOfLadder]
    private Object[] getNearestTopLadderInformations(){
        Object[] ladderInformations = new Object[3];
        
        for(int y = (int) this.worldResolution.y - 1; y >= 0; y--){
            for(int x = 0; x < this.worldResolution.x; x++){
                if(this.blockMatrix[y][x] != null && this.blockMatrix[y][x].isLadder()){
                    // System.out.println("Found " + this.blockMatrix[y][x].toString() + "\nAt the pos: y = " + y + " // x = " + x);
                    
                    ladderInformations[0] = this.blockMatrix[y][x];
                    ladderInformations[1] = y;
                    ladderInformations[2] = x;
                    return ladderInformations;
                }
            }
        }
        return null;
    }

    //pathfindings system : BFS best option after some researh

    public ArrayList<int[]> getNeighbors(int x, int y){
        ArrayList<int[]> neighbors = new ArrayList<>();
        int[] neighbor;

        boolean isOnLadder= this.blockMatrix[y][x] != null && this.blockMatrix[y][x].isLadder();
        boolean isOnGround = y>0 && this.blockMatrix[y-1][x] != null && this.blockMatrix[y-1][x].isSolid();

        if(isOnLadder){ // if ai is on ladder, he can go on the four direction if it's not solid

            if(x+1 < this.blockMatrix[0].length && (this.blockMatrix[y][x+1] == null||(this.blockMatrix[y][x+1] != null && !this.blockMatrix[y][x+1].isSolid()))) {
                
                if(y>0 && (this.blockMatrix[y-1][x+1] == null)){neighbor = new int[]{x+1,y-1}; neighbors.add(neighbor);}
                else{neighbor = new int[]{x+1,y}; neighbors.add(neighbor);}

            }

            if(x>0 && (this.blockMatrix[y][x-1] == null||(this.blockMatrix[y][x-1] != null && !this.blockMatrix[y][x-1].isSolid()))) {
                
                if(y>0 && (this.blockMatrix[y-1][x-1] == null)){neighbor = new int[]{x-1,y-1}; neighbors.add(neighbor);}
                else{neighbor = new int[]{x-1,y}; neighbors.add(neighbor);}
            
            }

            if(y+1<this.blockMatrix.length && this.blockMatrix[y+1][x]==null){
                if(x>0 && (this.blockMatrix[y][x-1] != null && this.blockMatrix[y][x-1].isSolid()) && (this.blockMatrix[y+1][x-1] == null||this.blockMatrix[y+1][x-1] != null && !this.blockMatrix[y][x-1].isSolid())){neighbor = new int[]{x-1,y+1}; neighbors.add(neighbor);}

                if(x+1 < this.blockMatrix[0].length && (this.blockMatrix[y][x+1] != null && this.blockMatrix[y][x+1].isSolid()) && (this.blockMatrix[y+1][x+1] == null||this.blockMatrix[y+1][x+1] != null && !this.blockMatrix[y][x+1].isSolid())){neighbor = new int[]{x+1,y+1}; neighbors.add(neighbor);}
            }
            
            if(y+1 < this.blockMatrix.length && (this.blockMatrix[y+1][x] == null||(this.blockMatrix[y+1][x] != null && !this.blockMatrix[y+1][x].isSolid()))) {neighbor = new int[]{x,y+1}; neighbors.add(neighbor);}

            if(y>0 && (this.blockMatrix[y-1][x] == null||(this.blockMatrix[y-1][x] != null && !this.blockMatrix[y-1][x].isSolid()))) {neighbor = new int[]{x,y-1}; neighbors.add(neighbor);}

        }else if(isOnGround){ // if ai walkOn ground, she only can go left/right, not climb
        
            if(x+1 < this.blockMatrix[0].length && (this.blockMatrix[y][x+1] == null||(this.blockMatrix[y][x+1] != null && !this.blockMatrix[y][x+1].isSolid()))) {

                if(y>0 && (this.blockMatrix[y-1][x+1] == null)){neighbor = new int[]{x+1,y-1}; neighbors.add(neighbor);}
                else{neighbor = new int[]{x+1,y}; neighbors.add(neighbor);}
        
            }

            if(x>0 && (this.blockMatrix[y][x-1] == null||(this.blockMatrix[y][x-1] != null && !this.blockMatrix[y][x-1].isSolid()))) {
                
                if(y>0 && (this.blockMatrix[y-1][x-1] == null)){neighbor = new int[]{x-1,y-1}; neighbors.add(neighbor);}
                else{neighbor = new int[]{x-1,y}; neighbors.add(neighbor);}
        
            }
        
        }else{ // if is not on ladder and any ground below, so ai must fall

            if(y>0 && (this.blockMatrix[y-1][x] == null)) {neighbor = new int[]{x,y-1}; neighbors.add(neighbor);}

        }return neighbors;
    }


    public ArrayList<int[]> findPath(int startX, int startY, int goalX, int goalY){
        int worldHeight = this.blockMatrix.length;
        int worldWidth = this.blockMatrix[0].length;

        int[][] parentX = new int[worldHeight][worldWidth];
        int[][] parentY = new int[worldHeight][worldWidth];
        Queue<int[]> queue = new LinkedList<>();
        boolean[][] visited = new boolean[worldHeight][worldWidth];

        if(startY < 0 || startY >= worldHeight){
            return new ArrayList<>();
        }
        if(startX < 0 || startX >= worldWidth){
            return new ArrayList<>();
        }

        queue.add(new int[]{startX, startY});
        visited[startY][startX] = true;

        for(int y = 0; y < worldHeight; y++){
            for(int x = 0; x < worldWidth; x++){
                parentX[y][x] = -1;
                parentY[y][x] = -1;
            }
        }

        while(!queue.isEmpty()){
            int[] current = queue.poll();
            int x = current[0];
            int y = current[1];

            if(x == goalX && y == goalY) break;
            for(int[] neighbor : getNeighbors(x, y)){
                int nx = neighbor[0];
                int ny = neighbor[1];
                if(!visited[ny][nx]){
                    visited[ny][nx] = true;
                    parentX[ny][nx] = x;
                    parentY[ny][nx] = y;
                    queue.add(new int[]{nx, ny});
                }
            }
        }

        if(goalY >= this.blockMatrix.length) goalY--;
        if(goalY < 0) goalY++;
        if(goalX >= this.blockMatrix[0].length) goalX--;
        if(goalX < 0) goalX++;

        if(!visited[goalY][goalX]){
            return new ArrayList<>();
        }

        ArrayList<int[]> path = new ArrayList<>();

        int x = goalX;
        int y = goalY;

        while(x != startX || y != startY){
            path.add(new int[]{x, y});

            int px = parentX[y][x];
            int py = parentY[y][x];

            x = px;
            y = py;
        }
        path.add(new int[]{startX, startY});
        Collections.reverse(path);

        return path;
    }

    public void breakBlockAtPos(int x, int y){
        int blockXIndex = ((int) x / 32)+this.blockMatrix[0].length/2;
        int blockYIndex = (((int) y / 32)+this.blockMatrix.length/2)-1;
        if(blockXIndex >=0 && blockYIndex >= 0){
            if(this.blockMatrix[blockYIndex][blockXIndex] != null && this.blockMatrix[blockYIndex][blockXIndex] instanceof Block && this.blockMatrix[blockYIndex][blockXIndex].isSolid()){
                this.breakBlockManager.breakBlock(blockXIndex, blockYIndex);
            }
        }
    }
    public void setBlockTextureAt(int x,int y,Texture texture){
        this.blockMatrix[y][x].setTexture(texture);
    }
    public void setBlockAt(int x, int y, Block block){
        this.blockMatrix[y][x] = block;
    }
    public Block[][] getBlockMatrix() {
        return blockMatrix;
    }

    public void reduceAmountOfCollectible(){
        this.ammountOfCollectible--;
    }

    public boolean isThereCollectibles(){
        return this.ammountOfCollectible > 0;
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
