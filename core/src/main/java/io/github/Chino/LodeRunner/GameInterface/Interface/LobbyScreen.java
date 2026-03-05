package io.github.Chino.LodeRunner.GameInterface.Interface;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

import io.github.Chino.LodeRunner.GameInterface.GDXMain;
import io.github.Chino.LodeRunner.GameInterface.LanConnection.ClientSide;
import io.github.Chino.LodeRunner.GameInterface.LanConnection.Server;

public class LobbyScreen implements Screen{
    private final GDXMain main;

    private Server hostedServer;
    private ClientSide clientSide;

    private String serverPassword;

    private SpriteBatch batch;

    private Texture backgroundTexture;
    private int currentBackgroundXOffset = 0;
    private boolean isBackgroundMovingLeft = false;

    private Stage uiStage;

    private Table tableOfContent;
    private Table tableForScrollPane;

    private Label ipLabel;
    private Label passwordLabel;
    private ScrollPane logScrollPane;
    private TextButton goBackButton;

    public LobbyScreen(GDXMain main) {
        this.main = main;

        this.uiStage = new Stage(new ScreenViewport());

        initButtons();
        initBackground();
    }
    private void initButtons(){
        Skin skin = new Skin(Gdx.files.internal("textbuttonskin/textbuttonSkin.json"));

        this.ipLabel = new Label("", skin);
        this.passwordLabel = new Label("", skin);

        this.tableForScrollPane = new Table();

        this.logScrollPane = new ScrollPane(this.tableForScrollPane);
        this.logScrollPane.setColor(Color.BLACK);
        this.logScrollPane.setSize(400, 500);

        //TODO delete after i tested everything for the logScrollPane
        // for (int i = 0; i < 10; i++) {
        //     Label l = new Label("Item " + i, skin);
        //     this.tableForScrollPane.add(l).left().row();
        //     // this.tableForScrollPane.invalidateHierarchy();
        // }

        this.goBackButton = new TextButton("Go Back", skin);

        this.tableOfContent = new Table();
        this.tableOfContent.setFillParent(false);
        this.tableOfContent.setSize(250, 250);
        this.tableOfContent.setPosition(
            this.uiStage.getViewport().getScreenWidth() / 2 - this.tableOfContent.getWidth() / 2,
            this.uiStage.getViewport().getScreenHeight() / 2 - this.tableOfContent.getHeight() / 2
        );

        this.tableOfContent.add(this.ipLabel).pad(10).row();
        this.tableOfContent.add(this.passwordLabel).pad(10).row();
        this.tableOfContent.add(this.goBackButton).pad(10).row();
        this.tableOfContent.add(this.logScrollPane).pad(10).height(150).expandX().fillX();

        this.uiStage.addActor(this.tableOfContent);
        this.uiStage.setScrollFocus(this.logScrollPane);

        this.goBackButton.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent e, float x, float y){
                if(hostedServer != null){
                    hostedServer.closeServerSocket();
                }
                main.getMultiplayerScreen().setMovingBackgroundInfo(currentBackgroundXOffset, isBackgroundMovingLeft);
                main.setScreen(main.getMultiplayerScreen());
            }
        });

    }
    private void initBackground(){
        this.batch = new SpriteBatch();

        this.backgroundTexture = new Texture("menuBackground.png");
    }

    public void logMessageSend(String message){
        Skin skin = new Skin(Gdx.files.internal("textbuttonskin/textbuttonSkin.json"));

        Label messageLabel = new Label(message, skin);
        
        this.tableForScrollPane.add(messageLabel).expandX().fillX().center().row();
    }

    public void setMovingBackgroundInfo(int currentXOffset, boolean isMovingLeft){
        this.currentBackgroundXOffset = currentXOffset;
        this.isBackgroundMovingLeft = isMovingLeft;
    }
    protected void setLobbyInformationForHost(Server server, String ip, String port, String password){
        this.hostedServer = server;
        
        this.ipLabel.setText("Ip: " + ip);
        this.passwordLabel.setText("Passsword: " + password);
    }
    protected void setLobbyInformationForClient(ClientSide clientSide,String ip, String port, String password){
        this.clientSide = clientSide;
        this.ipLabel.setText("Ip: " + ip);
        this.passwordLabel.setText("Password: " + password);
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

        this.tableOfContent.setPosition(
            this.uiStage.getViewport().getScreenWidth() / 2 - this.tableOfContent.getWidth() / 2,
            this.uiStage.getViewport().getScreenHeight() / 2 - this.tableOfContent.getHeight() / 2
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

        if(this.hostedServer != null){
            this.hostedServer.closeServerSocket();
        }
    }
    
}
