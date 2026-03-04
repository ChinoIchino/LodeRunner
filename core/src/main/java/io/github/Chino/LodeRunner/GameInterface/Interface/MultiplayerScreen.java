package io.github.Chino.LodeRunner.GameInterface.Interface;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

import io.github.Chino.LodeRunner.GameInterface.GDXMain;

public class MultiplayerScreen implements Screen{
    private final GDXMain main;

    private SpriteBatch batch;

    private Texture backgroundTexture;
    private int currentBackgroundXOffset = 0;
    private boolean isBackgroundMovingLeft = false;

    private Stage uiStage;

    private Table tableForButtons;

    private TextButton hostButton;
    private TextButton joinButton;
    private TextButton goBackButton;

    public MultiplayerScreen(GDXMain main) {
        this.main = main;

        this.uiStage = new Stage(new ScreenViewport());

        initButtons();
        initBackground();
    }
    private void initButtons(){
        Skin textButtonSkin = new Skin(Gdx.files.internal("textbuttonskin/textbuttonSkin.json"));

        this.hostButton = new TextButton("Host A Game", textButtonSkin);
        this.joinButton = new TextButton("Join A Game", textButtonSkin);
        this.goBackButton = new TextButton("Go Back", textButtonSkin);

        this.tableForButtons = new Table();
        this.tableForButtons.setFillParent(false);
        this.tableForButtons.setSize(250, 250);
        this.tableForButtons.setPosition(
            this.uiStage.getViewport().getScreenWidth() / 2 - this.tableForButtons.getWidth() / 2,
            this.uiStage.getViewport().getScreenHeight() / 2 - this.tableForButtons.getHeight() / 2
        );

        this.tableForButtons.add(this.hostButton).pad(10).row();
        this.tableForButtons.add(this.joinButton).pad(10).row();
        this.tableForButtons.add(this.goBackButton).pad(10);

        this.uiStage.addActor(this.tableForButtons);

        //TODO send to gameruleScreen
        this.hostButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent e, float x, float y){
                main.getGameRuleScreen().setMovingBackgroundInfo(currentBackgroundXOffset, isBackgroundMovingLeft);
                main.setScreen(main.getGameRuleScreen());
            }
        });
        //TODO send to joinClientToHostScreen
        this.joinButton.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent e, float x, float y){
                main.getJoinClientScreen().setMovingBackgroundInfo(currentBackgroundXOffset, isBackgroundMovingLeft);
                main.setScreen(main.getJoinClientScreen());
            }
        });
        this.goBackButton.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent e, float x, float y){
                main.getMenuScreen().setMovingBackgroundInfo(currentBackgroundXOffset, isBackgroundMovingLeft);
                main.setScreen(main.getMenuScreen());
            }
        });

    }
    private void initBackground(){
        this.batch = new SpriteBatch();

        this.backgroundTexture = new Texture("menuBackground.png");
    }

    public void setMovingBackgroundInfo(int currentXOffset, boolean isMovingLeft){
        this.currentBackgroundXOffset = currentXOffset;
        this.isBackgroundMovingLeft = isMovingLeft;
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

    @Override
    public void show() {
        // Change the input proc back to this menu
        // this.uiStage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(this.uiStage);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        // Brown = 150, 89, 39
        Gdx.gl.glClearColor(150 / 255f, 89 / 255f, 39 / 255f, 1f);

        this.moveBackground();
        this.batch.begin();
        this.batch.draw(this.backgroundTexture, this.currentBackgroundXOffset, -200);
        this.batch.end();

        this.uiStage.act(delta);
        this.uiStage.draw();
    }

    @Override
    public void resize(int width, int height) {
        this.uiStage.getViewport().update(width, height, true);

        this.tableForButtons.setPosition(
            this.uiStage.getViewport().getScreenWidth() / 2 - this.tableForButtons.getWidth() / 2,
            this.uiStage.getViewport().getScreenHeight() / 2 - this.tableForButtons.getHeight() / 2
        );
    }

    @Override
    public void pause() {
        //if the game was minimized, set the fps to 10 to save user performance
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
        this.uiStage.dispose();
        this.batch.dispose();
    }
    
    
}
