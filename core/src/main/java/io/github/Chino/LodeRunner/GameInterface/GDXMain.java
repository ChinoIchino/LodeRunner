package io.github.Chino.LodeRunner.GameInterface;

import com.badlogic.gdx.Game;

import io.github.Chino.LodeRunner.GameInterface.Entity.Player;
import io.github.Chino.LodeRunner.GameInterface.Interface.GameCoopScreen;
import io.github.Chino.LodeRunner.GameInterface.Interface.GameEndScreen;
import io.github.Chino.LodeRunner.GameInterface.Interface.GameRuleScreen;
import io.github.Chino.LodeRunner.GameInterface.Interface.GameScreen;
import io.github.Chino.LodeRunner.GameInterface.Interface.GameVersusScreen;
import io.github.Chino.LodeRunner.GameInterface.Interface.JoinClientScreen;
import io.github.Chino.LodeRunner.GameInterface.Interface.LeaderboardScreen;
import io.github.Chino.LodeRunner.GameInterface.Interface.LobbyScreen;
import io.github.Chino.LodeRunner.GameInterface.Interface.MenuScreen;
import io.github.Chino.LodeRunner.GameInterface.Interface.MultiplayerScreen;
import io.github.Chino.LodeRunner.GameInterface.Interface.Other.IntroCinematicScreen;
import io.github.Chino.LodeRunner.GameInterface.Leaderboard.LeaderboardCreator;
import io.github.Chino.LodeRunner.GameInterface.Leaderboard.LeaderboardHandler;

public class GDXMain extends Game {
    private GameScreen gameScreen;
    private GameCoopScreen gameCoopScreen;
    private GameVersusScreen gameVersusScreen;
    private MenuScreen menuScreen;
    private MultiplayerScreen multiplayerScreen;
    private GameRuleScreen gameruleScreen;
    private JoinClientScreen joinClientScreen;
    private LobbyScreen lobbyScreen;
    private GameEndScreen gameEndScreen;
    private LeaderboardScreen leaderboardScreen;
    private IntroCinematicScreen introScreen;

    private LeaderboardHandler leaderboardHandler;

    private Player clientPlayer;

    @Override
    public void create() {
        // Create a player that will be used or in solo or in the multiplayer game
        this.clientPlayer = new Player();
        
        initScreens();
        setScreen(this.menuScreen);

        this.leaderboardHandler = new LeaderboardCreator().createLeaderboardHandler();
    }
    private void initScreens(){
        this.menuScreen = new MenuScreen(this);
        this.multiplayerScreen = new MultiplayerScreen(this);
        this.gameruleScreen = new GameRuleScreen(this);
        this.joinClientScreen = new JoinClientScreen(this);
        this.lobbyScreen = new LobbyScreen(this);
        this.introScreen = new IntroCinematicScreen(this);
        this.leaderboardScreen = new LeaderboardScreen(this);
    }
    public void setNewGameEndScreen(boolean isFinish,int score){
        this.gameEndScreen = new GameEndScreen(this, isFinish,score);
    }
    public void setNewGameScreen(){
        this.gameScreen = new GameScreen(this);
    }
    public void setNewGameCoopScreen(){
        this.gameCoopScreen = new GameCoopScreen(this);
    }
    public void setNewGameVersusScreen(){
        this.gameVersusScreen = new GameVersusScreen(this);
    }

    public GameScreen getGameScreen() {
        return gameScreen;
    }
    public GameCoopScreen getGameCoopScreen(){
        return gameCoopScreen;
    }
    public GameVersusScreen getGameVersusScreen() {
        return gameVersusScreen;
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
    public GameEndScreen getGameEndScreen() {
        return gameEndScreen;
    }
    public IntroCinematicScreen getIntroScreen(){
        return introScreen;
    }
    public LeaderboardScreen getLeaderboardScreen(){
        return leaderboardScreen;
    }
    public LeaderboardHandler getLeaderboardHandler() {
        return leaderboardHandler;
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
