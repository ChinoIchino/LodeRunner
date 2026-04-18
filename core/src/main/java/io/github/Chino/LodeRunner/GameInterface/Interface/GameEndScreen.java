package io.github.Chino.LodeRunner.GameInterface.Interface;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

import io.github.Chino.LodeRunner.GameInterface.GDXMain;

public class GameEndScreen implements Screen{

    private GDXMain main;

    private boolean isFinish;

    private Table tableContent;
    private Stage stage;

    private int score;

    /**
     * 
     * @param gdxMain
     * @param isFinish need it to know if it's game over
     * @param score is the final score
     */
    public GameEndScreen(GDXMain gdxMain,boolean isFinish,int score){
        this.main = gdxMain;
        this.isFinish = isFinish;
        this.score = score;
    }

    @Override
    public void show() {
        Skin skin = new Skin(Gdx.files.internal("textbuttonskin/textbuttonSkin.json"));
        this.tableContent = new Table();
        this.tableContent.setFillParent(true);
        this.stage = new Stage(new ScreenViewport());
        Label labelTOBackToMenu = new Label("Press ENTER to return to Menu", skin);
        Label labelForGameEnd;
        if(isFinish){
            labelForGameEnd = new Label("YOU REACH THE END !", skin);
        }else{
            labelForGameEnd = new Label("GAME OVER !", skin);
            
        }
        Label labelForScore = new Label("You have " + this.score + " points !", skin);

        tableContent.add(labelForGameEnd).row();
        tableContent.add(labelForScore).row();
        tableContent.add(labelTOBackToMenu).row();


        this.stage.addActor(tableContent);

        Gdx.graphics.setForegroundFPS(60);
    }


    @Override
    public void render(float delta) {
        handleClientInput();
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
        this.stage.act(delta);
        this.stage.draw();

    }

    /**
     * To comeback to menu
     */
    public void handleClientInput(){
        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)){
            this.main.setScreen(main.getMenuScreen());
            this.main.getLobbyScreen().closeServer();
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
        this.stage.dispose();
    }
    
}
