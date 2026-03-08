package io.github.Chino.LodeRunner.GameInterface.Interface;

import java.io.IOException;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

import io.github.Chino.LodeRunner.GameInterface.GDXMain;
import io.github.Chino.LodeRunner.GameInterface.LanConnection.ClientSide;
import io.github.Chino.LodeRunner.GameInterface.LanConnection.Server;
import io.github.Chino.LodeRunner.GameInterface.LanConnection.TranslateToBytes;

public class LobbyScreen implements Screen{
    private final GDXMain main;

    private Server hostedServer;
    private ClientSide clientSide;

    private String username;
    private String serverPassword;

    private SpriteBatch batch;

    private Texture backgroundTexture;
    private int currentBackgroundXOffset = 0;
    private boolean isBackgroundMovingLeft = false;

    private Stage uiStage;
    private static Skin skin = new Skin(Gdx.files.internal("textbuttonskin/textbuttonSkin.json"));

    private Table tableOfContent;
    private Table tableOfPlayersList;
    private Table tablePlayersContent;
    private Table tableRightSide;

    private Label ipLabel;
    private Label passwordLabel;
    private ScrollPane playerListScroll;
    private ScrollPane logScrollPane;
    private TextButton goBackButton;

    public LobbyScreen(GDXMain main) {
        this.main = main;

        this.uiStage = new Stage(new ScreenViewport());

        initButtons();
        initBackground();
    }
    private void initButtons(){
        this.tableOfContent = new Table();
        this.tableOfContent.setFillParent(true);
        this.uiStage.addActor(this.tableOfContent);

        // Left Side
        this.tableOfPlayersList = new Table();
        this.tablePlayersContent = new Table();
        this.tablePlayersContent.align(Align.top);

        Label playersLabel = new Label("Players:", skin);
        this.tablePlayersContent.add(playersLabel).left().expandX().fillX().row();

        this.playerListScroll = new ScrollPane(this.tablePlayersContent, skin);

        this.tableOfPlayersList.add(this.playerListScroll).align(Align.right).grow();
        this.tableOfContent.add(this.tableOfPlayersList).width(Gdx.graphics.getWidth() * 0.40f).height(Gdx.graphics.getHeight() * 0.75f).pad(10);

        //Right Side
        this.ipLabel = new Label("", skin);
        this.passwordLabel = new Label(this.serverPassword, skin);
        this.goBackButton = new TextButton("Go Back", skin);

        this.tableRightSide = new Table();

        this.tableRightSide.add(ipLabel).center().row();
        this.tableRightSide.add(passwordLabel).center().row();
        this.tableRightSide.add(goBackButton).center().row();

        this.tableOfContent.add(this.tableRightSide).width(Gdx.graphics.getWidth() * 0.40f).height(Gdx.graphics.getHeight() * 0.75f).pad(10);
        this.tableOfContent.setDebug(true); // TODO delete after testing

        this.goBackButton.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent e, float x, float y){
                try {
                    clientSide.writeStream.write(TranslateToBytes.toPlayerLeaveListPacket(clientSide.username));
                    clientSide.writeStream.flush();
                } catch (IOException ioe) {}

                if(hostedServer != null){
                    hostedServer.closeServerProperly();
                }else{
                    clientSide.closeEverything();
                }

                resetPlayerList();

                main.getMultiplayerScreen().setMovingBackgroundInfo(currentBackgroundXOffset, isBackgroundMovingLeft);
                main.setScreen(main.getMultiplayerScreen());
            }
        });

    }
    private void initBackground(){
        this.batch = new SpriteBatch();

        this.backgroundTexture = new Texture("menuBackground.png");
    }

    public void addANewPlayerToList(String username){
        Label nameLabel = new Label(username, skin);
        System.out.println("In addANewPlayerToList");
        Gdx.app.postRunnable(() ->{
            this.tablePlayersContent.add(nameLabel).expandX().fillX().row();
        });
    }
    public void removeAPlayerFromTheList(String username){
        Label currentLabel;

        for(Actor actor: this.tablePlayersContent.getChildren()){
            // Just in case verify if the actor is a label
            if(actor instanceof Label){
                currentLabel = (Label) actor;
                if(currentLabel.getText().toString().equals(username.trim())){
                    System.out.println("In lobbyScreen about to remove a username");
                    currentLabel.remove();
                    break;
                }
            }
        }
    }
    private void resetPlayerList(){
        this.tablePlayersContent.clear();
        Label playersLabel = new Label("Players:", skin);
        this.tablePlayersContent.add(playersLabel).expandX().fillX().row();
    }

    public void logMessageSend(String message){
        Label messageLabel = new Label(message, skin);
        
        this.tableRightSide.add(messageLabel).expandX().fillX().center().row();
    }

    public void setMovingBackgroundInfo(int currentXOffset, boolean isMovingLeft){
        this.currentBackgroundXOffset = currentXOffset;
        this.isBackgroundMovingLeft = isMovingLeft;
    }
    protected void setLobbyInformationForHost(Server server, String ip, String port, String username, String password){
        this.hostedServer = server;
        this.username = username;
        this.ipLabel.setText("Ip: " + ip);
        this.passwordLabel.setText("Password: " + password);
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
        this.uiStage.getViewport().update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
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
            this.hostedServer.closeServerProperly();
        }
    }
    
}
