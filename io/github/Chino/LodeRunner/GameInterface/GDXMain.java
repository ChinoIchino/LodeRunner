package io.github.Chino.LodeRunner.GameInterface;

import com.badlogic.gdx.Game;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class GDXMain extends Game {
    // NOT USING FOR NOW, BUT IT WILL BE USEFULL SOON
    // //** For Access to the assets folder of the gradle project */
    // private AssetManager assetManager;

    @Override
    public void create() {
        setScreen(new GameScreen());
        // this.stretchViewport = new StretchViewport(this.SCREEN_WIDTH, this.SCREEN_HEIGH);
    }

    /** When a window resize occur change the resolution */
    @Override
    public void resize(int width, int height){
        super.resize(width, height);
    }

    @Override
    public void render() {
        super.render();

        // Each fram clear and set it to black
        // ScreenUtils.clear(Color.BLACK);
        // Draw again the textures
    }

    @Override
    public void dispose() {
        super.dispose();
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
