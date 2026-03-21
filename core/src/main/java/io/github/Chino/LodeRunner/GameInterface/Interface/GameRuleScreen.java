package io.github.Chino.LodeRunner.GameInterface.Interface;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

import io.github.Chino.LodeRunner.GameInterface.GDXMain;
import io.github.Chino.LodeRunner.GameInterface.LanConnection.ClientSide;
import io.github.Chino.LodeRunner.GameInterface.LanConnection.Server;
import io.github.Chino.LodeRunner.GameInterface.LanConnection.TranslateToBytes;

public class GameRuleScreen implements Screen{
    private final GDXMain main;

    private Server serverHosted;

    private SpriteBatch batch;

    private Texture backgroundTexture;
    private int currentBackgroundXOffset = 0;
    private boolean isBackgroundMovingLeft = false;

    private Stage uiStage;

    private Table tableOfContent;

    private Label errorLabel;
    private Label usernameLabel;
    private Label isVersusLabel;
    private Label isCoopLabel;
    private Label passwordLabel;
    private Slider gamemodeTypeSlider;
    private TextField usernameTextField;
    private TextField passwordTextField;
    private TextButton createLobbyButton;
    private TextButton goBackButton;

    public GameRuleScreen(GDXMain main) {
        this.main = main;

        this.uiStage = new Stage(new ScreenViewport());

        initButtons();
        initBackground();
    }
    private void initButtons(){
        Skin skin = new Skin(Gdx.files.internal("textbuttonskin/textbuttonSkin.json"));

        this.errorLabel = new Label("", skin);
        this.usernameLabel = new Label("Username: ", skin);
        this.isVersusLabel = new Label("Versus", skin);
        this.isCoopLabel = new Label("Coop", skin);
        this.passwordLabel = new Label("Password: ", skin);

        this.gamemodeTypeSlider = new Slider(0, 1, 1, false, skin);

        this.usernameTextField = new TextField("", skin);
        this.passwordTextField = new TextField("", skin);

        this.createLobbyButton = new TextButton("Create Lobby", skin);
        this.goBackButton = new TextButton("Go Back", skin);

        this.tableOfContent = new Table();
        this.tableOfContent.setFillParent(false);
        this.tableOfContent.setPosition(
            this.uiStage.getViewport().getScreenWidth() + 300,
            this.uiStage.getViewport().getScreenHeight() + this.tableOfContent.getHeight() / 2
        );

        this.tableOfContent.add(this.errorLabel).pad(10).colspan(3).row();
        this.tableOfContent.add(this.usernameLabel).pad(10);
        this.tableOfContent.add(this.usernameTextField).pad(10).colspan(2).row();
        this.tableOfContent.add(this.isVersusLabel).pad(10);
        this.tableOfContent.add(this.gamemodeTypeSlider).pad(10);
        this.tableOfContent.add(this.isCoopLabel).pad(10).row();
        this.tableOfContent.add(this.passwordLabel).pad(10);
        this.tableOfContent.add(this.passwordTextField).pad(10).colspan(2).row();
        this.tableOfContent.add(this.createLobbyButton).pad(10).colspan(3).row();
        this.tableOfContent.add(this.goBackButton).pad(10).colspan(3).center();

        this.uiStage.addActor(this.tableOfContent);

        this.createLobbyButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent e, float x, float y){
                if(isPasswordValid() && isUsernameValid()){
                    try {
                        main.getLobbyScreen().setMovingBackgroundInfo(currentBackgroundXOffset, isBackgroundMovingLeft);
    
                        //TODO implement passwords
                        ServerSocket serverSocket = new ServerSocket(5000);
                        serverHosted = new Server(serverSocket);
                        serverHosted.start();

                        // Add also the host as a client
                        Socket socket = new Socket("localhost", 5000);
                        ClientSide hostClient = new ClientSide(socket, main, usernameTextField.getText());
                        hostClient.start();

                        // System.out.println("about to write the translated username");
                        hostClient.writeStream.write(TranslateToBytes.toPlayerListPacket(usernameTextField.getText()));
                        hostClient.writeStream.flush();
                        
                        main.getLobbyScreen().setMovingBackgroundInfo(currentBackgroundXOffset, isBackgroundMovingLeft);
                        main.getLobbyScreen().setLobbyInformationForHost(
                            serverHosted,
                            hostClient,
                            InetAddress.getLocalHost().getHostAddress(),
                            "5000",
                            usernameTextField.getText(),
                            passwordTextField.getText(),
                            (int) (gamemodeTypeSlider.getValue())
                        );        
                        
                        main.setScreen(main.getLobbyScreen());
                        
                    } catch (IOException ioe) {
                        updateErrorLabel("ServerSocket wasn't created");
                    }
                }
            }
        });
        this.goBackButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent e, float x, float y){
                main.getMultiplayerScreen().setMovingBackgroundInfo(currentBackgroundXOffset, isBackgroundMovingLeft);
                main.setScreen(main.getMultiplayerScreen());
            }
        });

    }
    private void initBackground(){
        this.batch = new SpriteBatch();

        this.backgroundTexture = new Texture("menuBackground.png");
    }

    private boolean isPasswordValid(){
        String passwordFromTextField = this.passwordTextField.getText();
        if(passwordFromTextField.isEmpty()){
            updateErrorLabel("ERROR: Password Is Empty");
            return false;
        }
        if(passwordFromTextField.length() > 10){
            updateErrorLabel("ERROR: Password must be under 10 characters");
            return false;
        }
        return true;
    }
    private boolean isUsernameValid(){
        String usernameFromTextField = this.usernameTextField.getText();
        if(usernameFromTextField.isEmpty()){
            updateErrorLabel("ERROR: Username Is Empty");
            return false;
        }
        if(usernameFromTextField.length() > 14){
            updateErrorLabel("ERROR: Username must be under 14 characters");
            return false;
        }
        return true;
    }
    private void updateErrorLabel(String errorMesssage){
        this.errorLabel.setText(errorMesssage);
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
            this.uiStage.getViewport().getScreenWidth() / 2,
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
    }
    
    
}

