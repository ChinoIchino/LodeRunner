package io.github.Chino.LodeRunner.GameInterface.Interface;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

import io.github.Chino.LodeRunner.GameInterface.GDXMain;
import io.github.Chino.LodeRunner.GameInterface.Leaderboard.LeaderboardCreator;
import io.github.Chino.LodeRunner.GameInterface.Leaderboard.LeaderboardHandler;

public class LeaderboardScreen implements Screen {
    private GDXMain main;

    private ScreenViewport screenViewport = new ScreenViewport();

    private SpriteBatch batch;
    private Stage uiStage;

    private LeaderboardHandler leaderboardHandler;

    private Texture backgroundTexture;
    private int currentBackgroundXOffset = 0;
    private boolean isBackgroundMovingLeft = false;

    public LeaderboardScreen(GDXMain main){
        this.main = main;

        initBackground();

        // TODO put connection to database in a thread
        this.leaderboardHandler = new LeaderboardCreator().createLeaderboardHandler();
    }

    private void initBackground(){
        this.batch = new SpriteBatch();
        this.uiStage = new Stage(this.screenViewport);

        this.backgroundTexture = new Texture("menuBackground.png");
    }

    @Override
    public void show() {
        Gdx.graphics.setForegroundFPS(60);
        Gdx.input.setInputProcessor(this.uiStage);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        
        this.moveBackground();
        this.batch.begin();
        this.batch.draw(this.backgroundTexture, this.currentBackgroundXOffset, -200);
        this.batch.end();
    }

    private void moveBackground(){
        if(this.isBackgroundMovingLeft){
            this.currentBackgroundXOffset -= 1;
            if(this.currentBackgroundXOffset < -1100){
                this.isBackgroundMovingLeft = false;
            }
        }else{
            this.currentBackgroundXOffset += 1;
            if(this.currentBackgroundXOffset > -200){
                this.isBackgroundMovingLeft = true;
            }
        }
    }
    public void setMovingBackgroundInfo(int currentXOffset, boolean isMovingLeft){
        this.currentBackgroundXOffset = currentXOffset;
        this.isBackgroundMovingLeft = isMovingLeft;
    } 

    @Override
    public void resize(int width, int height) {
        this.uiStage.getViewport().update(width, height, true);
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
        this.uiStage.dispose();
    }
    


}
