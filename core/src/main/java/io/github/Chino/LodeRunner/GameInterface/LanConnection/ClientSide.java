package io.github.Chino.LodeRunner.GameInterface.LanConnection;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.Gdx;

import io.github.Chino.LodeRunner.GameInterface.GDXMain;
import io.github.Chino.LodeRunner.GameInterface.Interface.LobbyScreen;
import io.github.Chino.LodeRunner.GameInterface.LanConnection.Packets.ByteHandler.ByteBuffer;
import io.github.Chino.LodeRunner.GameInterface.LanConnection.Packets.PacketDecoder;
import io.github.Chino.LodeRunner.GameInterface.LanConnection.Packets.PacketTypes.Packet;

public class ClientSide extends Thread{
    private volatile boolean isRunning = true;

    public LobbyScreen currentClientLobbyScreen;
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

            this.currentClientLobbyScreen = main.getLobbyScreen();

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
                    System.out.println("Client got the packetId = " + packetType);
                    buffer.resetCursor();
                    // System.out.println("In listenForPackets with the packet type = " + buffer.readInt());
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
                            System.out.println("Got the game mode boolean " + gameModeIsVersus);
                            Gdx.app.postRunnable(() ->{
                                currentClientLobbyScreen.setGameMode(gameModeIsVersus);
                            });
                            // Every items after the game mode are usernames in the lobby
                            for (int i = 1; i < packetItems.size(); i++) {
                                String currentPlayerToAdd = (String) packetItems.get(i);
                                Gdx.app.postRunnable(() -> {
                                    currentClientLobbyScreen.addANewPlayerToList((String) currentPlayerToAdd);
                                });
                            }

                            packetItems.clear();
                            break;

                        // Lobby a new client joined
                        case 2:
                            // Casting String because unpackPacket return a Object List
                            String newPlayer = (String) packetItems.get(0);
                            Gdx.app.postRunnable(() -> {
                                currentClientLobbyScreen.addANewPlayerToList(newPlayer);
                            });
                            // Clearing the list for the next use
                            packetItems.clear();
                            break;
                                    
                        // Lobby a client quit
                        case 3:
                            String playerQuitting = (String) packetItems.get(0);
                            Gdx.app.postRunnable(() -> {
                                currentClientLobbyScreen.removeAPlayerFromTheList(playerQuitting);
                            });
                            packetItems.clear();
                            break;
                        // Lobby Host quit, kick the client
                        case 4:
                            System.out.println("Got a packet id 4");
                            buffer.clear();
                            isRunning = false;
                            Gdx.app.postRunnable(() -> {
                                currentClientLobbyScreen.forceDispose();
                            });
                            break;
                        // Lobby chat packet
                        case 5:
                            String username = (String) packetItems.get(0);
                            String message = (String) packetItems.get(1);
                            Gdx.app.postRunnable(() -> {
                                currentClientLobbyScreen.logMessageSend(username, message);
                            });
                            packetItems.clear();
                            break;
                        // Lobby Host started the game
                        case 6:
                            this.currentClientLobbyScreen.sendToGameInterface();
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