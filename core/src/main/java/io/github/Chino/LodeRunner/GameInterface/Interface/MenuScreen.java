package io.github.Chino.LodeRunner.GameInterface.Interface;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

import io.github.Chino.LodeRunner.GameInterface.GDXMain;

/** Manage the menu screen that is display at the very start of the game */
public class MenuScreen implements Screen {
    private GDXMain main;

    private final int SCREEN_WIDTH = 854;
    private final int SCREEN_HEIGH = 480;

    // UI part:
    private SpriteBatch batch;
    private Stage uiStage;
    private final ScreenViewport screenViewport = new ScreenViewport();

    private Texture backgroundTexture;
    private int currentBackgroundXOffset = 0;
    private boolean isBackgroundMovingLeft = false;

    private Table tableForButtons;

    private TextButton playButton;
    private TextButton multiplayerButton;
    private TextButton leaderboardButton;

    public MenuScreen(GDXMain main) {
        this.main = main;

        this.uiStage = new Stage(this.screenViewport);

        initButtons();
        initBackground();

    }

    @Override
    public void show() {
        Gdx.graphics.setForegroundFPS(60);
        Gdx.input.setInputProcessor(this.uiStage);
    }

    // TODO finish init
    private void initButtons() {
        Skin skin = new Skin(Gdx.files.internal("textButtonskin/textbuttonSkin.json"));

        Gdx.input.setInputProcessor(this.uiStage);

        this.playButton = new TextButton("Play", skin);
        this.multiplayerButton = new TextButton("Multiplayer", skin);
        this.leaderboardButton = new TextButton("Leaderboard", skin);

        this.tableForButtons = new Table();
        this.tableForButtons.setFillParent(false);
        this.tableForButtons.setSize(250, 250);
        this.tableForButtons.setPosition(
                this.screenViewport.getScreenWidth() / 2 - this.tableForButtons.getWidth() / 2,
                this.screenViewport.getScreenHeight() / 2 - this.tableForButtons.getHeight() / 2);

        this.tableForButtons.add(this.playButton).pad(10).row();
        this.tableForButtons.add(this.multiplayerButton).pad(10).row();
        this.tableForButtons.add(this.leaderboardButton).pad(10);

        // Adding the listeners to every buttons
        this.playButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent e, float x, float y) {
                main.setNewGameScreen();
                main.setScreen(main.getGameScreen());
                main.getGameScreen().initAIinWorld();
            }
        });
        this.multiplayerButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent e, float x, float y) {
                main.getMultiplayerScreen().setMovingBackgroundInfo(currentBackgroundXOffset, isBackgroundMovingLeft);
                main.setScreen(main.getMultiplayerScreen());
            }
        });
        // TODO send client to leaderboard screen
        this.leaderboardButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent e, float x, float y) {

            }
        });

        this.uiStage.addActor(this.tableForButtons);
    }

    private void initBackground() {
        this.batch = new SpriteBatch();

        this.backgroundTexture = new Texture("menuBackground.png");
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

    private void moveBackground() {
        if (this.isBackgroundMovingLeft) {
            this.currentBackgroundXOffset -= 1;
            if (this.currentBackgroundXOffset < -1100) {
                this.isBackgroundMovingLeft = false;
            }
        } else {
            this.currentBackgroundXOffset += 1;
            if (this.currentBackgroundXOffset > -200) {
                this.isBackgroundMovingLeft = true;
            }
        }
    }

    public void setMovingBackgroundInfo(int currentXOffset, boolean isMovingLeft) {
        this.currentBackgroundXOffset = currentXOffset;
        this.isBackgroundMovingLeft = isMovingLeft;
    }

    // TODO Not working :(
    private void displayTableHitbox() {
        ShapeRenderer sp = new ShapeRenderer();

        sp.begin(ShapeRenderer.ShapeType.Line);
        sp.setColor(Color.RED);
        sp.rect(this.tableForButtons.getOriginX(), this.tableForButtons.getOriginY(), this.tableForButtons.getWidth(),
                this.tableForButtons.getHeight());
        sp.end();
    }

    @Override
    public void resize(int width, int height) {
        this.uiStage.getViewport().update(width, height, true);

        this.tableForButtons.setPosition(
                this.screenViewport.getScreenWidth() / 2 - this.tableForButtons.getWidth() / 2,
                this.screenViewport.getScreenHeight() / 2 - this.tableForButtons.getHeight() / 2);
    }

    @Override
    public void pause() {
        // if the game was minimized, set the fps to 10 to save user performance
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
