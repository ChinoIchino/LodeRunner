package io.github.Chino.LodeRunner.GameInterface.World;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Queue;

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
    
    public boolean entityDoesntOverlapWorld(Rectangle hitboxOfEntity){
        double left   = hitboxOfEntity.x;
        double right  = hitboxOfEntity.x + hitboxOfEntity.width-1;
        double bottom = hitboxOfEntity.y;
        double top    = hitboxOfEntity.y + hitboxOfEntity.height-1;

        int blockLeft   = (int)Math.floor(left / 32.0)+this.blockMatrix[0].length /2;;
        int blockRight  = (int)Math.floor(right / 32.0)+this.blockMatrix[0].length /2;;
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
        int blockX = ((int)(hitboxOfEntity.x / 32))+this.blockMatrix[0].length/2;
        int blockY = (((int)(hitboxOfEntity.y/ 32))+this.blockMatrix.length/2)-1;
        // System.out.println("Checking block at: " + blockX + "," + (blockY-1));
        // System.out.println("Player Y: " +(entity.getHitbox().y));
        if(blockY<=0)return null;
        for (int i = 0; i < (int) (this.worldResolution.y); i++) {
            for(int j = 0; j < (int) (this.worldResolution.x); j++){
                if((this.blockMatrix[i][j] != null) && (this.blockMatrix[i][j].isLadder()) && (hitboxOfEntity.overlaps(this.blockMatrix[i][j].getHitbox()))) return this.blockMatrix[i][j].getHitbox();
            }
        }
        return null;
    }

    public boolean isLadderUnderEntity(Entity entity){
        int blockX = ((int)(entity.getHitbox().x / 32))+getBlockMatrix()[0].length/2;
        int blockYBottom = (((int) (entity.getHitbox().y-1)/ 32)+getBlockMatrix().length/2)-1;
        if(blockX <0 || blockX >=getBlockMatrix()[0].length || blockYBottom < 0 || blockYBottom >=getBlockMatrix().length|| getBlockMatrix()[blockYBottom][blockX] == null) return false;
        return getBlockMatrix()[blockYBottom][blockX].isLadder();
    }

    //TODO To fix, getting the level of the player but cant get the block for a reason
    public boolean entityOverlapWithFloor(Entity entity){
        int level = 0;
        int yPosition = (int) (this.worldResolution.y) * -10;
        for (int j = (int) this.worldResolution.y; j >= 0; j++) {
            if(entity.getPosY() < yPosition){
                break;
            }
            yPosition += 32;
            level++;
        }
        // this.printHitbox();
        // System.out.println("Got the level " + level + " // and the yPos =" + yPosition);
        for(int x = 0; x < (int) (this.worldResolution.x); x++){
            if(this.blockMatrix[level][x] != null){
                if(entity.getHitbox().overlaps(this.blockMatrix[level][x].getHitbox())){
                    return true;
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

    //TODO adapt this for any map height not only for map that void is at y: -200
    public boolean entityFellOutTheWorld(Entity entity){
        return entity.getPosY() < -200;
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

        }
        return neighbors;
    }

    public ArrayList<int[]> findPath(int startX, int startY, int goalX, int goalY){
        int worldHeight = this.blockMatrix.length;
        int worldWidth = this.blockMatrix[0].length;

        // System.out.println("ia: " + startX + "," + startY);
        // System.out.println("player: " + goalX + "," + goalY);

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
            System.out.println("x : " +blockXIndex + "   y : " + blockYIndex);
            if(this.blockMatrix[blockYIndex][blockXIndex] != null && this.blockMatrix[blockYIndex][blockXIndex] instanceof Block && this.blockMatrix[blockYIndex][blockXIndex].isSolid()){
                this.breakBlockManager.breakBlock(blockXIndex, blockYIndex);
            }else System.out.println("Breaking that is impossible");
        }else System.out.println("Breaking that is impossible");
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
