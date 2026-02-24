package io.github.Chino.LodeRunner.GameInterface;

import com.badlogic.gdx.Game;

public class GDXMain extends Game {

    @Override
    public void create() {
        setScreen(new GameScreen());
    }

    /** When a window resize occur change the resolution */
    @Override
    public void resize(int width, int height){
        super.resize(width, height);
    }

    @Override
    public void render() {
        super.render();
    }

    @Override
    public void dispose() {
        super.dispose();
    }

}
