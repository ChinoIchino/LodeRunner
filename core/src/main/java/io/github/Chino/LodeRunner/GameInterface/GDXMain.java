package io.github.Chino.LodeRunner.GameInterface;

import com.badlogic.gdx.Game;

import io.github.Chino.LodeRunner.GameInterface.Interface.GameOverScreen;
import io.github.Chino.LodeRunner.GameInterface.Interface.GameCoopScreen;
import io.github.Chino.LodeRunner.GameInterface.Interface.GameRuleScreen;
import io.github.Chino.LodeRunner.GameInterface.Interface.GameScreen;
import io.github.Chino.LodeRunner.GameInterface.Interface.JoinClientScreen;
import io.github.Chino.LodeRunner.GameInterface.Interface.LobbyScreen;
import io.github.Chino.LodeRunner.GameInterface.Interface.MenuScreen;
import io.github.Chino.LodeRunner.GameInterface.Interface.MultiplayerScreen;
import io.github.Chino.LodeRunner.GameInterface.Entity.Player;

public class GDXMain extends Game {
    private GameScreen gameScreen;
    private GameCoopScreen gameCoopScreen;
    private MenuScreen menuScreen;
    private MultiplayerScreen multiplayerScreen;
    private GameRuleScreen gameruleScreen;
    private JoinClientScreen joinClientScreen;
    private LobbyScreen lobbyScreen;
    private GameOverScreen gameOverScreen;

    private Player clientPlayer;

    @Override
    public void create() {
        // Create a player that will be used or in solo or in the multiplayer game
        this.clientPlayer = new Player();
        
        initScreens();
        setScreen(this.menuScreen);

    }
    private void initScreens(){
        this.menuScreen = new MenuScreen(this);
        this.multiplayerScreen = new MultiplayerScreen(this);
        this.gameruleScreen = new GameRuleScreen(this);
        this.joinClientScreen = new JoinClientScreen(this);
        this.lobbyScreen = new LobbyScreen(this);
        this.gameOverScreen = new GameOverScreen(this);
    }
    
    public void setNewGameScreen(){
        this.gameScreen = new GameScreen(this);
    }
    public void setNewGameCoopScreen(){
        this.gameCoopScreen = new GameCoopScreen(this);
    }

    public GameScreen getGameScreen() {
        return gameScreen;
    }
    public GameCoopScreen getGameCoopScreen(){
        return gameCoopScreen;
    }
    public MenuScreen getMenuScreen() {
        return menuScreen;
    }
    public MultiplayerScreen getMultiplayerScreen() {
        return multiplayerScreen;
    }
    public GameRuleScreen getGameRuleScreen(){
        return gameruleScreen;
    }
    public JoinClientScreen getJoinClientScreen() {
        return joinClientScreen;
    }
    public LobbyScreen getLobbyScreen() {
        return lobbyScreen;
    }
    public GameOverScreen getGameOverScreen() {
        return gameOverScreen;
    }
    
    public Player getClientPlayer(){
        return this.clientPlayer;
    }

    /** When a window resize occur change the resolution */
    @Override
    public void resize(int width, int height){
        super.resize(width, height);
    }

    @Override
    public void render() {
        super.render();
    }

    @Override
    public void dispose() {
        super.dispose();
    }

}
