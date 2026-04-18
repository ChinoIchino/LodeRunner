package io.github.Chino.LodeRunner.GameInterface.Interface;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import io.github.Chino.LodeRunner.GameInterface.GDXMain;

public class GameEndScreen implements Screen{

    private GDXMain main;
    
    private SpriteBatch batch;

    private BitmapFont bitmapFont;
    
    private Texture backgroundTexture;

    private boolean isFinish;
    
    public GameEndScreen(GDXMain gdxMain,boolean isFinish){
        this.main = gdxMain;
        this.bitmapFont = new BitmapFont();
        this.isFinish = isFinish;
    }

    @Override
    public void show() {
        this.initBackground();
        Gdx.graphics.setForegroundFPS(60);
    }

    private void initBackground() {
        this.batch = new SpriteBatch();
        this.backgroundTexture = new Texture("menuBackground.png");
    }

    @Override
    public void render(float delta) {
        handleClientInput();
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        // Brown = 150, 89, 39
        Gdx.gl.glClearColor(150 / 255f, 89 / 255f, 39 / 255f, 1f);

        //TODO: print score and rework the font placement + background full black
        this.batch.begin();
        this.batch.draw(this.backgroundTexture,0,0);
        this.bitmapFont.getData().setScale(3, 3);
        if(isFinish){
            this.bitmapFont.draw(batch, "GAME IS FINISH !", 100,300); 
        }else{
            this.bitmapFont.draw(batch, "GAME OVER !", 100,300); 
        }
        this.bitmapFont.getData().setScale(2, 2);
        this.bitmapFont.draw(batch, "Press ENTER to return to Menu", 100, 200);
        this.batch.end();
    }

    public void handleClientInput(){
        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)){
            this.main.setScreen(main.getMenuScreen());
        }
    }


    @Override
    public void resize(int width, int height) {
    }

    @Override
    public void pause() {
        Gdx.graphics.setForegroundFPS(10);
    }

    @Override
    public void resume() {
        Gdx.graphics.setForegroundFPS(60);
    }

    @Override
    public void hide() {
    }

    @Override
    public void dispose() {
        this.batch.dispose();
        this.bitmapFont.dispose();
        this.backgroundTexture.dispose();
    }
    
}
