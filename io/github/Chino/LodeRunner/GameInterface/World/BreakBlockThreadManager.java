package io.github.Chino.LodeRunner.GameInterface.World;

// TODO make the block breaking functions

public class BreakBlockThreadManager{
    // TODO Change the main brick and modify also every slabs
    // private Texture fullBlock = new Texture("data/textures/blocks/brick.png");
    // private Texture threeQuarterBlock = new Texture("data/textures/blocks/3Quarterbrick.png");
    // private Texture halfBlock = new Texture("data/textures/blocks/Halfbrick.png");
    // private Texture quarterBlock = new Texture("data/textures/blocks/Quarterbrick.png");

    private WorldManager worldManager;

    private Semaphore semaphore;

    private int indexX;
    private int indexY;

    BreakBlockThreadManager(WorldManager worldManager) {
        this.worldManager = worldManager;

        this.semaphore = new Semaphore();
    }

    // public synchronized void breakBlock(int indexX, int indexY) {
    //     this.semaphore.takeToken();
    //     Thread blockBreakingThread = new Thread(blockBreakingRunnable);
    //     blockBreakingThread.start();
    // }

    // private final Runnable blockBreakingRunnable = () ->{
    //     int indeXOfBlock = ;
    //     int indexYOfBlock = ; 
    //     // release the token after the runnable have saved his block to break
    //     semaphore.releaseToken();
    // };
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