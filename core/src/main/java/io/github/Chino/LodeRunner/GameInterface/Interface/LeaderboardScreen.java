package io.github.Chino.LodeRunner.GameInterface.Interface;

import java.util.ArrayList;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

import io.github.Chino.LodeRunner.GameInterface.GDXMain;
import io.github.Chino.LodeRunner.GameInterface.Leaderboard.LeaderboardCreator;
import io.github.Chino.LodeRunner.GameInterface.Leaderboard.LeaderboardHandler;

public class LeaderboardScreen implements Screen {
    private GDXMain main;

    private ScreenViewport screenViewport = new ScreenViewport();

    private SpriteBatch batch;
    private Stage uiStage;

    private final Skin skin = new Skin(Gdx.files.internal("textbuttonskin/textbuttonSkin.json"));

    private Table tableOfLeaderboardLabel;

    private LeaderboardHandler leaderboardHandler;

    private Texture backgroundTexture;
    private int currentBackgroundXOffset = 0;
    private boolean isBackgroundMovingLeft = false;

    private boolean isOnSoloLeaderboard = true;

    public LeaderboardScreen(GDXMain main){
        this.main = main;

        initBackground();
        initInterface();

        this.leaderboardHandler = new LeaderboardCreator().createLeaderboardHandler();
        // by default show the solo leaderboard
        setupSoloLeaderboard();

        //TODO Delete all line under this one in the constructor, this is all for testing purpose
        // this.leaderboardHandler.addPlayerToSoloLeaderboardDatabase("PlayerAdded", 1200);
        // this.leaderboardHandler.addPlayerToSoloLeaderboardDatabase("PlayerAddedn2", 400);
        // this.leaderboardHandler.addPlayerToSoloLeaderboardDatabase("PlayerAddedn3", 100);

        ArrayList<String> names = new ArrayList<>();
        names.add("Gp4N1");
        names.add("Gp4n2");
        names.add("Gp4n3");

        this.leaderboardHandler.addPlayersToCoopLeaderboardDatabase(names, 3000);
    }

    private void initBackground(){
        this.batch = new SpriteBatch();
        this.uiStage = new Stage(this.screenViewport);

        this.backgroundTexture = new Texture("menuBackground.png");
    }

    private void initInterface(){
        Gdx.input.setInputProcessor(this.uiStage);

        Table tableOfContent = new Table();
        tableOfContent.setFillParent(true);

        Table tableOfButtonsOnTop = new Table();

        TextButton soloButton = new TextButton("Solo", skin);
        TextButton coopButton = new TextButton("Coop", skin);

        tableOfButtonsOnTop.add(soloButton).padRight(15);
        tableOfButtonsOnTop.add(coopButton);
        tableOfContent.add(tableOfButtonsOnTop).expandX().fillX().top().padBottom(10).row();
        
        this.tableOfLeaderboardLabel = new Table();
        tableOfContent.add(this.tableOfLeaderboardLabel).expand().fill().center().row();
        
        Table tableOfButtonsOnBottom = new Table();

        TextButton goBackButton = new TextButton("Go Back", skin);
        TextButton refreshButton = new TextButton("Refresh", skin);

        tableOfButtonsOnBottom.add(goBackButton).padRight(15);
        tableOfButtonsOnBottom.add(refreshButton);
        tableOfContent.add(tableOfButtonsOnBottom).expandX().fillX().bottom().padBottom(10);

        soloButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent e, float x, float y){
                setupSoloLeaderboard();
                isOnSoloLeaderboard = true;
            }
        });

        coopButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent e, float x, float y){
                setupCoopLeaderboard();
                isOnSoloLeaderboard = false;
            }
        });

        goBackButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent e, float x, float y){
                main.getMenuScreen().setMovingBackgroundInfo(currentBackgroundXOffset, isBackgroundMovingLeft);
                main.setScreen(main.getMenuScreen());
            }
        });

        refreshButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent e, float x, float y){
                if(isOnSoloLeaderboard){
                    setupSoloLeaderboard();
                }else{
                    setupCoopLeaderboard();
                }
            }
        });
        this.uiStage.addActor(tableOfContent);
    }

    @Override
    public void show() {
        Gdx.graphics.setForegroundFPS(60);
        Gdx.input.setInputProcessor(this.uiStage);

        // Refresh the leaderboards when the player access to the interface
        this.setupCoopLeaderboard();
        this.setupSoloLeaderboard();
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        
        this.moveBackground();
        this.batch.begin();
        this.batch.draw(this.backgroundTexture, this.currentBackgroundXOffset, -200);
        this.batch.end();

        this.uiStage.act(delta);
        this.uiStage.draw();

        this.uiStage.setDebugAll(true);
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

    private void setupSoloLeaderboard(){
        // tableOfLeaderboardLabel.add(new Label("Player 1", skin)).expandY().fillY().center().row();

        this.tableOfLeaderboardLabel.clear();

        ArrayList<Object> leaderboardDatabaseInformations = this.leaderboardHandler.fetchSoloLeaderboardDatabase(10);

        String currentInformations = "";
        for (int i = 0; i < leaderboardDatabaseInformations.size(); i += 2) {
            currentInformations += (i / 2 + 1) + " : " + leaderboardDatabaseInformations.get(i) + " Score : " + leaderboardDatabaseInformations.get(i + 1);
            this.tableOfLeaderboardLabel.add(new Label(currentInformations, skin)).expandY().fillY().row();

            currentInformations = "";
        }
    }
    private void setupCoopLeaderboard(){
        this.tableOfLeaderboardLabel.clear();

        ArrayList<Object> leaderboardDatabaseInformations = this.leaderboardHandler.fetchCoopLeaderboardDatabase(10);

        String currentInformations = "";
        for (int i = 0; i < leaderboardDatabaseInformations.size(); i += 2) {
            currentInformations += (i / 2 + 1) + " : " + leaderboardDatabaseInformations.get(i) + " Score : " + leaderboardDatabaseInformations.get(i + 1);
            this.tableOfLeaderboardLabel.add(new Label(currentInformations, skin)).expandY().fillY().row();

            currentInformations = "";
        }
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
