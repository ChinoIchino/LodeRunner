package io.github.Chino.LodeRunner.GameInterface;

import java.io.IOException;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.viewport.StretchViewport;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class GDXMain extends ApplicationAdapter {
    /** The txt that contain the canvas of the world to draw */
    private final String WORLD_FILE = "WorldFile";

    private final int SCREEN_WIDTH = 320;
    private final int SCREEN_HEIGH = 180;
    
    /** Class that manage the WORLD_FILE
     *  and create the level based on it */
    private WorldCreator worldCreator;

    //** Resize the window size based on the resolution */
    private StretchViewport stretchViewport;

    // NOT USING FOR NOW, BUT IT WILL BE USEFULL SOON
    // //** For Access to the assets folder of the gradle project */
    // private AssetManager assetManager;
    
    private SpriteBatch batch;
    private Texture logoImage;

    @Override
    public void create() {
        this.batch = new SpriteBatch();
        this.logoImage = new Texture("libgdx.png");

        this.stretchViewport = new StretchViewport(this.SCREEN_WIDTH, this.SCREEN_HEIGH);

        this.worldCreator = new WorldCreator(this.batch, this.WORLD_FILE);
        // try {
        //     this.worldCreator.drawWorld(SCREEN_WIDTH, SCREEN_HEIGH);
        // } catch (IOException e) {
        //     System.err.println("\nERROR GameInterface/GDXMain.java: Function create catched IOException while drawing the world based on the WORLD_FILE");
        //     e.printStackTrace();
        // }
    }

    /** When a window resize occur change the resolution */
    @Override
    public void resize(int width, int height){
        this.stretchViewport.update(width, height);

    }

    @Override
    public void render() {
        this.stretchViewport.apply();

        // Each fram clear and set it to black
        // ScreenUtils.clear(Color.BLACK);
        // Draw again the textures
        draw();
    }

    @Override
    public void dispose() {
        batch.dispose();
        this.logoImage.dispose();
    }

    private void draw(){
        // ScreenUtils.clear(Color.BLACK);
        stretchViewport.apply();
        this.batch.setProjectionMatrix(stretchViewport.getCamera().combined);
        // this.batch.begin();
        
        // The screen have the size of 1000x1000 so x = -500 / y = -500
        // Represent the bottom left of the screen 
        // batch.draw(this.logoImage, -500, -500, 1000, 1000);
        // this.batch.end();
        
        try {
            this.worldCreator.drawWorld(this.SCREEN_WIDTH, this.SCREEN_HEIGH);
        } catch (IOException e) {
            System.err.println("\nERROR GameInterface/GDXMain: Function draw catched IOException");
            e.printStackTrace();
        }
    }
}

/*
public class WindowSetup{
    private static Lwjgl3ApplicationConfiguration getDefaultConfiguration() {
    Lwjgl3ApplicationConfiguration configuration = new Lwjgl3ApplicationConfiguration();
    configuration.setTitle("Drop");
    configuration.useVsync(true);
    configuration.setForegroundFPS(Lwjgl3ApplicationConfiguration.getDisplayMode().refreshRate + 1);
    configuration.setWindowedMode(800, 500); // this line changes the size of the window
    configuration.setWindowIcon("libgdx128.png", "libgdx64.png", "libgdx32.png", "libgdx16.png");

    return configuration;
}
} */
