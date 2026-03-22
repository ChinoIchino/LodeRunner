package io.github.Chino.LodeRunner.GameInterface.Interface;

import java.io.IOException;
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
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

import io.github.Chino.LodeRunner.GameInterface.GDXMain;
import io.github.Chino.LodeRunner.GameInterface.LanConnection.ClientSide;
import io.github.Chino.LodeRunner.GameInterface.LanConnection.TranslateToBytes;

public class JoinClientScreen implements Screen{
    //TODO make that its possible that multiple servers are on the same connection via port modifications
    private GDXMain main;

    private SpriteBatch batch;

    private Texture backgroundTexture;
    private int currentBackgroundXOffset = 0;
    private boolean isBackgroundMovingLeft = false;

    private Stage uiStage;

    private Table tableOfContent;

    private Label errorLabel;
    private Label ipAdresseLabel;
    private Label portLabel;
    private Label usernameLabel;
    private Label passwordLabel;
    private TextField ipAdresseTextField;
    private TextField portTextField;
    private TextField usernameTextField;
    private TextField passwordTextField;
    private TextButton submitButton;
    private TextButton goBackButton;

    public JoinClientScreen(GDXMain main) {
        this.main = main;

        this.uiStage = new Stage(new ScreenViewport());

        initButtons();
        initBackground();
    }
    private void initButtons(){
        Skin skin = new Skin(Gdx.files.internal("textbuttonskin/textbuttonSkin.json"));

        this.errorLabel = new Label("", skin);
        this.usernameLabel = new Label("Username: ", skin);
        this.ipAdresseLabel = new Label("Ip: ", skin);
        this.passwordLabel = new Label("Password: ", skin);

        this.usernameTextField = new TextField("", skin);
        this.ipAdresseTextField = new TextField("", skin);
        this.passwordTextField = new TextField("", skin);

        this.submitButton = new TextButton("Submit", skin);
        this.goBackButton = new TextButton("Go Back", skin);

        this.tableOfContent = new Table();
        this.tableOfContent.setFillParent(false);
        this.tableOfContent.setPosition(
            this.uiStage.getViewport().getScreenWidth() + 300,
            this.uiStage.getViewport().getScreenHeight() + this.tableOfContent.getHeight() / 2
        );

        this.tableOfContent.add(this.errorLabel).pad(10).colspan(3).align(Align.center).row();
        this.tableOfContent.add(this.usernameLabel).pad(10).align(Align.center);
        this.tableOfContent.add(this.usernameTextField).pad(10).colspan(2).row();
        this.tableOfContent.add(this.ipAdresseLabel).pad(10).align(Align.center);
        this.tableOfContent.add(this.ipAdresseTextField).pad(10).align(Align.center).row();
        this.tableOfContent.add(this.passwordLabel).pad(10).align(Align.center);
        this.tableOfContent.add(this.passwordTextField).pad(10).align(Align.center).row();
        this.tableOfContent.add(this.submitButton).pad(10).colspan(3).align(Align.center).row();
        this.tableOfContent.add(this.goBackButton).pad(10).colspan(3).center();

        this.uiStage.addActor(this.tableOfContent);

        this.submitButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent e, float x, float y){
                // If the connection succeed, client get sent to the Lobby screen
                connectToServer();
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

    private void connectToServer(){
        // Added that in a thread so the game dont freeze when the user try to connect to a server
        Thread tryToConnectThread = new Thread(connectToServerRunnable);
        tryToConnectThread.start();
    }

    private final Runnable connectToServerRunnable = () ->{
        // Added that in a thread so the game dont freeze when the user try to connect to a server
        String ipFromTextField = this.ipAdresseTextField.getText();
        String passwordFromTextField = this.passwordTextField.getText();

        if(!(isPasswordValid() && isUsernameValid())){
            return;
        }

        try {
            Socket socket = new Socket(ipFromTextField, 5000);

            ClientSide client = new ClientSide(socket, this.main, this.usernameTextField.getText());
            client.start();
            
            System.out.println("about to write the translated username");
            client.writeStream.write(TranslateToBytes.toPlayerListPacket(this.usernameTextField.getText()));
            client.writeStream.flush();
            
            // Used so Gdx handle the rendering/UI, else it send a error.
            Gdx.app.postRunnable(() ->{
                main.getLobbyScreen().setMovingBackgroundInfo(currentBackgroundXOffset, isBackgroundMovingLeft);
                main.getLobbyScreen().setLobbyInformationForClient(client, usernameTextField.getText(), ipFromTextField, "5000", passwordFromTextField);
                main.setScreen(this.main.getLobbyScreen());
            });
        } catch (IOException e) {
            Gdx.app.postRunnable(() -> updateErrorLabel("ERROR: Couldn't Connect To Server"));
        }
        Gdx.app.postRunnable(() -> updateErrorLabel("ERROR: Didn't Found Server"));
    };

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