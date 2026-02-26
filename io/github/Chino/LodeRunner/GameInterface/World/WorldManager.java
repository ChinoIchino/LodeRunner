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

    private final Texture[][] worldTextures;
    //Rectangle[y][x]
    /** Hitboxes of the world*/
    private final Rectangle[][] worldBlockHitboxes;
    private final Rectangle[][] worldLadderHitboxes;

    public WorldManager(Vector2 worldResolution, Texture[][] worldTextures, Rectangle[][] worldBlockHitboxes, Rectangle[][] worldLadderHitboxes, SpriteBatch batch) {
        this.worldResolution = worldResolution;

        this.worldTextures = worldTextures;

        this.worldBlockHitboxes = worldBlockHitboxes;
        this.worldLadderHitboxes = worldLadderHitboxes;

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
            if(this.worldTextures[currentYIndex][i] != null){
                this.batch.draw(this.worldTextures[currentYIndex][i], currentXPosition, currentYPosition);
            }

            currentXPosition += 32;
        }
    }
    
    public boolean playerDoesntOverlapWorld(Player player){
        for (int i = 0; i < (int) (this.worldResolution.y); i++) {
            for(int j = 0; j < (int) (this.worldResolution.x); j++){
                if((this.worldBlockHitboxes[i][j] != null) && (player.getHitbox().overlaps(this.worldBlockHitboxes[i][j]))){
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
                if((this.worldLadderHitboxes[i][j] != null) && (hitboxOfPlayer.overlaps(this.worldLadderHitboxes[i][j]))){
                    // System.out.println("Player is colliding with a ladder");
                    return this.worldLadderHitboxes[i][j];
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
            if(this.worldBlockHitboxes[level][x] != null){
                if(player.getHitbox().overlaps(this.worldBlockHitboxes[level][x])){
                    return true;
                }
            }
        }
        return false;
    }

    public boolean playerIsOnGround(Player player){
        for (int i = 0; i < (int) (this.worldResolution.y); i++) {
            for(int j = 0; j < (int) (this.worldResolution.x); j++){
                if((this.worldBlockHitboxes[i][j] != null) && (player.getIsOnGroundHitbox().overlaps(this.worldBlockHitboxes[i][j]))){
                    // System.out.println("Player is colliding with a floor");
                    return true;
                }
            }
        }
        return false;
    }

    /** Used in debugging of hitboxes */
    public void printHitbox(){
        for (int y = 0; y < (int) (this.worldResolution.y); y++) {
            for(int x = 0; x < (int) (this.worldResolution.x); x++){
                System.out.print(this.worldBlockHitboxes[y][x] + " ");
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

        Rectangle currentHitbox;
        for (int y = (int) (this.worldResolution.y) - 1; y >= 0; y--) {
            for(int x = 0; x < (int) (this.worldResolution.x); x++){
                currentHitbox = this.worldBlockHitboxes[y][x];
                if(currentHitbox != null){
                    shapeRenderer.rect(
                        currentHitbox.x + 900,
                        currentHitbox.y + 200,
                        currentHitbox.width,
                        currentHitbox.height
                    );
                }
            }
        }
        shapeRenderer.setColor(Color.GREEN);
        for (int y = (int) (this.worldResolution.y) - 1; y >= 0; y--) {
            for(int x = 0; x < (int) (this.worldResolution.x); x++){
                currentHitbox = this.worldLadderHitboxes[y][x];
                if(currentHitbox != null){
                    shapeRenderer.rect(
                        currentHitbox.x + 900,
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
