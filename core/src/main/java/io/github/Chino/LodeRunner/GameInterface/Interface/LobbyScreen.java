package io.github.Chino.LodeRunner.GameInterface.Interface;

import java.io.IOException;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

import io.github.Chino.LodeRunner.GameInterface.GDXMain;
import io.github.Chino.LodeRunner.GameInterface.LanConnection.ClientSide;
import io.github.Chino.LodeRunner.GameInterface.LanConnection.Packets.ByteHandler.ByteBuffer;
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
    private final static Skin skin = new Skin(Gdx.files.internal("textbuttonskin/textbuttonSkin.json"));

    private Table tableOfContent;
    
    private Table tableLeftSide;
    private Table tablePlayersContent;
    private Table tableChatContent;
    private Table tableChatInput;
    
    private Table tableRightSide;

    private Label ipLabel;
    private Label passwordLabel;
    private Label gameModeLabel;
    private ScrollPane playerListScroll;
    private ScrollPane chatScrollPane;
    private TextButton goBackButton;
    private TextButton startButton;
    private TextField chatTextField;

    public LobbyScreen(GDXMain main) {
        this.main = main;

        this.uiStage = new Stage(new ScreenViewport());

        initInterface();
        initBackground();
    }
    private void initInterface(){
        this.tableOfContent = new Table();
        this.tableOfContent.setFillParent(true);
        this.uiStage.addActor(this.tableOfContent);

        //Top Left Side
        this.tableLeftSide = new Table();
        this.tablePlayersContent = new Table();
        this.tablePlayersContent.align(Align.top);

        Label playersLabel = new Label("Players:", skin);
        this.tablePlayersContent.add(playersLabel).expandX().fillX().row();

        this.playerListScroll = new ScrollPane(this.tablePlayersContent, skin);

        this.tableLeftSide.add(this.playerListScroll).align(Align.right).grow().padBottom(20).row();
        this.tableOfContent.add(this.tableLeftSide).top().width(Gdx.graphics.getWidth() * 0.40f).height(Gdx.graphics.getHeight() * 0.75f).pad(10);
        
        // Bottom Left Side
        this.tableChatContent = new Table();
        this.tableChatContent.top();

        this.chatScrollPane = new ScrollPane(this.tableChatContent, skin);
        this.chatScrollPane.setScrollingDisabled(true, false);
        this.chatScrollPane.setScrollbarsVisible(false);
        this.tableLeftSide.add(this.chatScrollPane).growX().height(Gdx.graphics.getHeight() * 0.35f).padBottom(1).row();

        // Chat input table
        this.tableChatInput = new Table();

        this.chatTextField = new TextField("", skin);

        tableChatInput.add(this.chatTextField).growX().padRight(5);

        this.tableLeftSide.add(tableChatInput).growX().expandX().row();

        //Right Side
        this.ipLabel = new Label("", skin);
        this.passwordLabel = new Label(this.serverPassword, skin);
        this.gameModeLabel = new Label("", skin);
        this.goBackButton = new TextButton("Go Back", skin);

        this.tableRightSide = new Table();

        this.tableRightSide.add(this.ipLabel).center().padBottom(10).row();
        this.tableRightSide.add(this.passwordLabel).center().padBottom(10).row();
        this.tableRightSide.add(this.gameModeLabel).center().padBottom(10).row();
        this.tableRightSide.add(this.goBackButton).center().padBottom(10).row();

        this.tableOfContent.add(this.tableRightSide).width(Gdx.graphics.getWidth() * 0.40f).height(Gdx.graphics.getHeight() * 0.75f).pad(10);
        this.tableOfContent.setDebug(true); // TODO delete after testing

        this.chatTextField.addListener(new InputListener(){
            @Override
            public boolean keyUp (InputEvent event, int keycode) {
                // Enter key is 66
                if(keycode == 66 && !chatTextField.getText().isEmpty()){
                    sendMessageToChat(chatTextField.getText());
                    chatTextField.setText("");
                    return true;
                }
                return false;
            }
        });

        this.goBackButton.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent e, float x, float y){
                try {
                    clientSide.writeStream.write(TranslateToBytes.toPlayerLeaveListPacket(clientSide.username));
                    clientSide.writeStream.flush();
                } catch (IOException ioe) {}

                if(hostedServer != null){
                    hostedServer.closeServerProperly();
                    // hostedServer = null;
                }else{
                    clientSide.closeEverything();
                    // clientSide = null;
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

    // This function is only called via the server
    public void logMessageSend(String username, String message){
        if(username.isEmpty() || message.isEmpty()){
            return;
        }

        Label label = new Label(username + ": " + message, skin);
        // Wrap so the label doesn't modify the scroll pane horizontal position
        label.setWrap(true);
        Gdx.app.postRunnable(() ->{
            // Add the message via a label to the scroll pane of the chat
            this.tableChatContent.add(label).left().expandX().fillX().row();

            // Goes to the end of the chat after the label was added
            this.tableChatContent.layout();
            this.chatScrollPane.layout();
            this.chatScrollPane.setScrollPercentY(this.chatScrollPane.getMaxY());
        });
    }
    private void sendMessageToChat(String message){
        try{
            this.clientSide.writeStream.write(TranslateToBytes.toLobbyChatMessage(this.username, message));
            this.clientSide.writeStream.flush();
        }catch(IOException e){
            System.out.println("ERROR: GameInterface/Interface/LobbyScreen.java: function sendMessageToChat catched IOException while sending a message to ClientHandler.java");
        }
    }

    public void setMovingBackgroundInfo(int currentXOffset, boolean isMovingLeft){
        this.currentBackgroundXOffset = currentXOffset;
        this.isBackgroundMovingLeft = isMovingLeft;
    }
    protected void setLobbyInformationForHost(Server server, ClientSide hostClient, String ip, String port, String username, String password){
        this.hostedServer = server;
        this.clientSide = hostClient;
        this.username = username;
        
        Gdx.app.postRunnable(() ->{
            this.ipLabel.setText("Ip: " + ip);
            
            this.passwordLabel.setText("Password: " + password);
        });

        // Add a extra button for the host to start the game
        Gdx.app.postRunnable(() -> {
            this.startButton = new TextButton("Start", skin);
    
            this.tableRightSide.add(startButton).center().row();
    
            this.startButton.addListener(new ClickListener(){
                @Override
                public void clicked(InputEvent e, float x, float y){
                    try {
                        ByteBuffer buffer = new ByteBuffer(4);
                        // Id to enter the game
                        buffer.writeInt(6);
                        
                        //Sending a pseudo packet to every player that the game started 
                        clientSide.writeStream.write(buffer.getBytesList());
                        clientSide.writeStream.flush();
                    } catch (IOException ioe) {
                        System.out.println("ERROR GameInterface/Interface/LobbyScreen.java: Host catched IOException will starting a game");
                    }

                }
            });
        });
    }
    protected void setLobbyInformationForClient(ClientSide clientSide, String username ,String ip, String port, String password){
        this.clientSide = clientSide;
        this.username = username;

        Gdx.app.postRunnable(() ->{
            this.ipLabel.setText("Ip: " + ip);
            this.passwordLabel.setText("Password: " + password);
        });
    }

    public void setGameMode(boolean isVersus){
        if(isVersus){
            Gdx.app.postRunnable(() ->{
                this.gameModeLabel.setText("Mode: Versus");
            });
        }else{
            Gdx.app.postRunnable(() ->{
                this.gameModeLabel.setText("Mode: Coop");
            });
        }
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

    public void sendToGameInterface(){
        // TODO versus and coop gamescreens
        // if()
        // this.main.setScreen(this.main.getGame);
    }

    @Override
    public void show() {
        // Recreate a sprite batch when a new lobby interface is displayed
        this.batch = new SpriteBatch();
        this.uiStage = new Stage(new ScreenViewport());

        Gdx.input.setInputProcessor(this.uiStage);
        this.uiStage.getViewport().update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
        
        initInterface();
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
        }else if(this.clientSide != null){
            this.clientSide.closeEverything();
        }
    }
    // Used only when the host of the lobby quit, so the users must be kicked from the interface
    public void forceDispose(){
        System.out.println("\nABOUT TO FORCE DISPOSE");
        
        Gdx.app.postRunnable(() ->{
            dispose();
            this.main.setScreen(this.main.getMultiplayerScreen());
        });
    }
    
}
