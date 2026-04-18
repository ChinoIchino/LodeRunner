package io.github.Chino.LodeRunner.GameInterface.LanConnection;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.Gdx;

import io.github.Chino.LodeRunner.GameInterface.GDXMain;
import io.github.Chino.LodeRunner.GameInterface.LanConnection.Packets.ByteHandler.ByteBuffer;
import io.github.Chino.LodeRunner.GameInterface.LanConnection.Packets.PacketDecoder;
import io.github.Chino.LodeRunner.GameInterface.LanConnection.Packets.PacketTypes.Packet;

public class ClientSide extends Thread{
    private volatile boolean isRunning = true;

    private GDXMain main;
    private boolean isVersus;
    
    public String username;

    private Socket socket;

    private BufferedInputStream readStream;
    public BufferedOutputStream writeStream;

    /** Used to decode byte[] into predetermined packets */
    private PacketDecoder packetDecoder;

    private ByteBuffer buffer;

    public ClientSide(Socket socket, GDXMain main, String username) {
        try{
            this.socket = socket;
    
            this.readStream = new BufferedInputStream(this.socket.getInputStream());
            this.writeStream = new BufferedOutputStream(this.socket.getOutputStream());

            this.packetDecoder = new PacketDecoder();

            this.buffer = new ByteBuffer(1024);

            this.main = main;

            this.username = username;
        }catch(IOException e){
            // If the client is force started, it don't goes in the loop
            isRunning = false;
            closeEverything(this.socket, this.writeStream, this.readStream);
        }
    }

    // "Event Listener" that react when Server.java broadcastPacket give out a packet
    @Override
    public void run(){
        Packet decodedPacket;
        int packetType;
        List<Object> packetItems = new ArrayList<>();

        while(isRunning){
            try{
                readStream.read(buffer.bytes);

                buffer.resetCursor();
                packetType = buffer.readInt();

                if(packetType > 0){
                    buffer.resetCursor();

                    decodedPacket = packetDecoder.decodeStream(buffer);
                    buffer.clear();
                    // System.out.println("Client got the packet: " + decodedPacket.toString());
                            
                    //Getting all the attributs from the packet into a List
                    packetItems = decodedPacket.unpackPacket();

                    switch(decodedPacket.getPacketId()){
                        // Lobby essentials (player list, and game mode)
                        case 1:
                            // First element is the boolean isVersus
                            final boolean gameModeIsVersus = (boolean) packetItems.get(0);
                            // Second element is the map
                            final char[][] firstMap = (char[][]) packetItems.get(1);

                            Gdx.app.postRunnable(() ->{
                                this.main.getLobbyScreen().setGameModeAndFirstMap(gameModeIsVersus, firstMap);
                            });

                            // Every last elements in the list are players from the player list
                            for (int i = 0; i < packetItems.size() - 2; i++) {
                                final String currentPlayerToAdd = (String) packetItems.get(i + 2);
                                System.out.println("Got the name: " + currentPlayerToAdd);
                                Gdx.app.postRunnable(() -> {
                                    this.main.getLobbyScreen().addANewPlayerToList((String) currentPlayerToAdd);
                                });
                            }

                            packetItems.clear();
                            break;
                        
                        // Lobby a new client joined
                        case 2:
                            // Casting String because unpackPacket return a Object List
                            String newPlayer = (String) packetItems.get(0);
                            Gdx.app.postRunnable(() -> {
                                this.main.getLobbyScreen().addANewPlayerToList(newPlayer);
                            });
                            // Clearing the list for the next use
                            packetItems.clear();
                            break;
                                    
                        // Lobby a client quit
                        case 3:
                            String playerQuitting = (String) packetItems.get(0);
                            Gdx.app.postRunnable(() -> {
                                this.main.getLobbyScreen().removeAPlayerFromTheList(playerQuitting);
                            });
                            packetItems.clear();
                            break;
                        // Lobby Host quit, kick the client
                        case 4:
                            buffer.clear();
                            isRunning = false;
                            Gdx.app.postRunnable(() -> {
                                this.main.getLobbyScreen().forceDispose();
                            });
                            break;
                        // Lobby chat packet
                        case 5:
                            String username = (String) packetItems.get(0);
                            String message = (String) packetItems.get(1);
                            Gdx.app.postRunnable(() -> {
                                this.main.getLobbyScreen().logMessageSend(username, message);
                            });
                            packetItems.clear();
                            break;
                        // Lobby Host started the game
                        case 6:
                            this.isVersus = this.main.getLobbyScreen().isVersus();
                            this.main.getLobbyScreen().sendToGameInterface();
                            break;
                        // Player has moved in the game
                        case 7:
                            if(isVersus){
                                this.main.getGameVersusScreen().handlePlayersDisplay(packetItems);
                            }else{
                                this.main.getGameCoopScreen().handlePlayersDisplay(packetItems);
                            }
                            break;
                        // Player update score label
                        case 8:
                            int newScore = (int) packetItems.get(0);
                            int yIndex = (int) packetItems.get(1);
                            int xIndex = (int) packetItems.get(2);
                            int playerId = (int) packetItems.get(3);
                            Gdx.app.postRunnable(() -> {
                                if(isVersus){
                                    this.main.getGameVersusScreen().updateScoreLabel(playerId,newScore, yIndex, xIndex);
                                }else this.main.getGameCoopScreen().updateScoreLabel(newScore, yIndex, xIndex);
                            });
                            break;
                        case 9:
                            Gdx.app.postRunnable(() -> {
                                if(isVersus){
                                    this.main.getGameVersusScreen().getWorldManager().openExitToNextLevel();
                                }
                                else {
                                    this.main.getGameCoopScreen().getWorldManager().openExitToNextLevel();
                                }
                            });
                            break;
                        case 10:
                            final char[][] map = (char[][]) packetItems.get(0);
                            Gdx.app.postRunnable(() -> {
                                if(isVersus){
                                    this.main.getGameVersusScreen().sendToNextLevel(map);
                                }else{
                                    this.main.getGameCoopScreen().sendToNextLevel(map);
                                }
                            });
                            break;
                        case 11:
                            final int blockX = (int) packetItems.get(0); 
                            final int blockY = (int) packetItems.get(1); 
                            Gdx.app.postRunnable(() -> {
                                if(isVersus){
                                    this.main.getGameVersusScreen().getWorldManager().breakBlockAtPos(blockX, blockY);
                                }else{
                                    this.main.getGameCoopScreen().getWorldManager().breakBlockAtPos(blockX, blockY);
                                }
                            });
                            break;
                        case 12:
                            final int aiId = (int) packetItems.get(0); 
                            final int nearestPlayerId = (int) packetItems.get(1); 
                            final int animationId = (int) packetItems.get(2); 
                            final int posX = (int) packetItems.get(3); 
                            final int posY = (int) packetItems.get(4); 
                            Gdx.app.postRunnable(() -> {
                                this.main.getGameCoopScreen().setAIInfoForGuest(aiId, nearestPlayerId, animationId, posX, posY);
                            });
                            break;
                        default:
                            break;
                        
                    }
                }
            }catch(IOException e){
                isRunning = false;
                closeEverything(socket, writeStream, readStream);
                break;
            }
        }
    }

    protected void closeEverything(Socket socket, BufferedOutputStream writer, BufferedInputStream reader) {
        try{
            if(socket != null){
                socket.close();
            }
            if(writer != null){
                writer.close();
            }
            if(reader != null){
                reader.close();
            }
        }catch(IOException e){}
    }
    public void closeEverything() {
                try{
            if(this.socket != null){
                this.socket.close();
            }
            if(this.writeStream != null){
                this.writeStream.close();
            }
            if(this.readStream != null){
                this.readStream.close();
            }
        }catch(IOException e){}
    }
}