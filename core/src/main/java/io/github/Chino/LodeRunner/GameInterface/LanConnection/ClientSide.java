package io.github.Chino.LodeRunner.GameInterface.LanConnection;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import io.github.Chino.LodeRunner.GameInterface.GDXMain;
import io.github.Chino.LodeRunner.GameInterface.Interface.LobbyScreen;
import io.github.Chino.LodeRunner.GameInterface.LanConnection.Packets.ByteHandler.ByteBuffer;
import io.github.Chino.LodeRunner.GameInterface.LanConnection.Packets.PacketDecoder;
import io.github.Chino.LodeRunner.GameInterface.LanConnection.Packets.PacketTypes.Packet;

public class ClientSide{
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
            closeEverything(this.socket, this.writeStream, this.readStream);
        }
    }

    // "Event Listener" that react when Server.java broadcastPacket give out a packet
    public void listenForPackets(){
        new Thread(new Runnable() {
            @Override
            public void run(){
                Packet decodedPacket;
                List<Object> packetItems = new ArrayList<>();

                while(socket.isConnected()){
                    try{
                        readStream.read(buffer.bytes);
                        
                        if(buffer.readInt() > 0){
                            buffer.resetCursor();
                            System.out.println("In listenForPackets with the packet type = " + buffer.readInt());
                            buffer.resetCursor();

                            decodedPacket = packetDecoder.decodeStream(buffer);
                            buffer.clear();
                            System.out.println("Client got the packet: " + decodedPacket.toString());
                            
                            //Getting all the attributs from the packet into a List
                            packetItems = decodedPacket.unpackPacket();

                            switch(decodedPacket.getPacketId()){
                                    // Lobby all player list
                                    case 1:
                                        for (Object item : packetItems) {
                                            currentClientLobbyScreen.addANewPlayerToList((String) item);
                                        }
                                        packetItems.clear();
                                        break;

                                        // Lobby a new client joined
                                    case 2:
                                        // Casting String because unpackPacket return a Object List
                                        currentClientLobbyScreen.addANewPlayerToList((String) packetItems.get(0));
                                        // Clearing the list for the next use
                                        packetItems.remove(0);
                                        break;
                                    case 3:
                                        System.out.println("About to remove " + packetItems.get(0));
                                        currentClientLobbyScreen.removeAPlayerFromTheList((String) packetItems.get(0));
                                        packetItems.remove(0);
                                    // Lobby chat packet
                                    case 4:

                            }
                        }
                    }catch(IOException e){
                        System.out.println("ABOUT TO CLOSE CONNECTION!!!!!!!!!");
                        closeEverything(socket, writeStream, readStream);
                        //End the while loop (for some reason socket.isConnected() doesn't seem to do the job)
                        break;
                    }
                }
            }
        }).start();
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
                socket.close();
            }
            if(this.writeStream != null){
                this.writeStream.close();
            }
            if(this.readStream != null){
                readStream.close();
            }
        }catch(IOException e){}
    }
}