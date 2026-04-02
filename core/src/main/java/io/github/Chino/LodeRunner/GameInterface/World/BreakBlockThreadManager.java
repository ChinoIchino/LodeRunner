package io.github.Chino.LodeRunner.GameInterface.World;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Rectangle;

// TODO make the block breaking functions

public class BreakBlockThreadManager{
    private Texture fullBlock = new Texture("data/textures/blocks/dirt.png");
    private Texture threeQuarterBlock = new Texture("data/textures/blocks/3quarterDirt.png");
    private Texture halfBlock = new Texture("data/textures/blocks/halfDirt.png");
    private Texture quarterBlock = new Texture("data/textures/blocks/quarterDirt.png");

    private WorldManager worldManager;

    private Semaphore semaphore;

    private int indexX;
    private int indexY;

    BreakBlockThreadManager(WorldManager worldManager) {
        this.worldManager = worldManager;

        this.semaphore = new Semaphore();
    }

    public synchronized void breakBlock(int indexX, int indexY) {
        this.semaphore.takeToken();
        this.indexX = indexX;
        this.indexY = indexY;
        Thread blockBreakingThread = new Thread(blockBreakingRunnable);
        blockBreakingThread.start();
    }

    private final Runnable blockBreakingRunnable = () ->{
        int indexXOfBlock = this.indexX;
        int indexYOfBlock = this.indexY; 
        int indexXHitbox,indexYHitbox;
        // release the token after the runnable have saved his block to break
        semaphore.releaseToken();
        try{
            worldManager.setBlockTextureAt(indexXOfBlock, indexYOfBlock, this.threeQuarterBlock);
            // System.out.println("3/4");
            Thread.sleep(400);
            worldManager.setBlockTextureAt(indexXOfBlock, indexYOfBlock, this.halfBlock);
            // System.out.println("2/4");
            Thread.sleep(400);
            worldManager.setBlockTextureAt(indexXOfBlock, indexYOfBlock, this.quarterBlock);
            // System.out.println("1/4");
            Thread.sleep(400);
            worldManager.setBlockAt(indexXOfBlock, indexYOfBlock, null);
            // System.out.println("block has disapear");
            Thread.sleep(2500);
            worldManager.setBlockAt(indexXOfBlock, indexYOfBlock, new Block(this.quarterBlock, new Rectangle(), true));
            // System.out.println("1/4");
            Thread.sleep(400);
            worldManager.setBlockTextureAt(indexXOfBlock, indexYOfBlock, this.halfBlock);
            Thread.sleep(400);
            worldManager.setBlockTextureAt(indexXOfBlock, indexYOfBlock, this.threeQuarterBlock);
            // System.out.println("3/4");
            Thread.sleep(400);
            // System.out.println("4/4");
            indexXHitbox = (indexXOfBlock)*32 - (this.worldManager.getBlockMatrix()[0].length/2)*32;
            indexYHitbox = (indexYOfBlock)*32 - (this.worldManager.getBlockMatrix().length/2)*32;
            worldManager.setBlockAt(indexXOfBlock, indexYOfBlock, new Block(fullBlock, new Rectangle(indexXHitbox, indexYHitbox, 32, 32), true));
            // System.out.println("block re create");
        }catch(InterruptedException e){
            System.out.println("\nError GameInterface/World/BreakBlockThreadManager.java: catched InterruptedException while running thread to break a block");
        }
    };
}

class Semaphore{
    private int token = 1;

    public Semaphore() {}

    public synchronized void takeToken(){
        if(this.token == 0){
            try {
                wait();
            } catch (InterruptedException e) {
                System.err.println("ERROR GameInterface/World/BreakBlockThreadManager.java: Thread catched InterruptedException will waiting for semaphore token");
                e.printStackTrace();
            }
        }
        this.token--;
    }
    public synchronized void releaseToken(){
        this.token = 1;
        this.notify();
    }
}