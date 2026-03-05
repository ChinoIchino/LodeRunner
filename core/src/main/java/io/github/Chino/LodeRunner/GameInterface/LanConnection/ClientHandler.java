package io.github.Chino.LodeRunner.GameInterface.LanConnection;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;

public class ClientHandler implements Runnable{
    public static ArrayList<ClientHandler> clientHandlers = new ArrayList<>();
    protected Socket socket;

    // Protected so the Server class can access it
    protected BufferedInputStream readerStream;
    protected BufferedOutputStream writerStream;

    public ClientHandler(Socket socket){
        try{
            //Connection between server and client
            this.socket = socket;

            this.writerStream = new BufferedOutputStream(this.socket.getOutputStream());
            this.readerStream = new BufferedInputStream(this.socket.getInputStream());
        
            clientHandlers.add(this);
        }catch(IOException e){
            closeEverything(this.socket, this.writerStream, this.readerStream);
        }

    }

    @Override
    public void run() {
        byte[] byteStream;
        int packetType;
        while(this.socket.isConnected()){
            try {
                packetType = this.readerStream.read();
                System.out.println("Got the packet type: " + packetType);
                switch (packetType) {
                    // chat packet
                    case 1:
                        // this.buffer.
                        break;
                    
                    // player list packet
                    case 2:
                        break;
                    default:
                        throw new AssertionError();
                }
                
            } catch (IOException e) {
                System.out.println("\nERROR ClientHandler.java: thread run");
                closeEverything(socket, writerStream, readerStream);
            }
        }
    }

    // Send to each client. Received via ClientSide.java listenForPackets function
    // public void broadcastPacket(ByteBuffer buffer){
    //     for (ClientHandler client : clientHandlers) {
    //         try {
    //             client.writerStream.write(buffer.getBytesList());
    //             client.writerStream.flush();
    //         } catch (IOException e) {
    //             System.out.println("\nERROR ClientHandler.java : Catched in broadcastMessage. About to terminate connection with client.");
    //             client.closeEverything(client.socket, client.writerStream, client.readerStream);
    //             client.removeFromClientHandlers();
    //         }
    //         buffer.flush();
    //     }
    // }

    protected void removeFromClientHandlers(){
        clientHandlers.remove(this); 
    }
    protected void closeEverything(Socket socket, BufferedOutputStream writer, BufferedInputStream reader){
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
}
/*
package io.github.Chino.LodeRunner.GameInterface.LanConnection;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;

import com.badlogic.gdx.Gdx;

import io.github.Chino.LodeRunner.GameInterface.GDXMain;
import io.github.Chino.LodeRunner.GameInterface.LanConnection.Packets.ByteHandler.ByteBuffer;
import io.github.Chino.LodeRunner.GameInterface.LanConnection.Packets.PacketTypes.Packet;
import io.github.Chino.LodeRunner.GameInterface.LanConnection.Packets.PacketTypes.PacketForLobbyChat;

public class ClientHandler implements Runnable{
    public GDXMain main;
    public static ArrayList<ClientHandler> clientHandlers = new ArrayList<>();
    
    private Socket socket;
    
    private BufferedInputStream inBuffer;
    private ByteBuffer inStream;
    private BufferedOutputStream outStream;

    private String clientUsername;

    public ClientHandler(GDXMain main, Socket clientSocket, ByteBuffer buffer) {
        try {
            this.socket = clientSocket;
    
            this.outStream = new BufferedOutputStream(this.socket.getOutputStream());
            this.inBuffer = new BufferedInputStream(this.socket.getInputStream());
            
            this.inStream = buffer;
            this.inBuffer.read(this.inStream.getBytesList());

            this.main = main;

            clientHandlers.add(this);

            PacketForLobbyChat lobbyChat = new PacketForLobbyChat("Jake", "Average message");
            this.broadcastToLobbyLog(lobbyChat);
        } catch (Exception e) {
            this.closeClientHandler();
        }
    }

    @Override
    public void run(){
        Scanner scanner = new Scanner(System.in);
        while (true) {
            PacketForLobbyChat packet = new PacketForLobbyChat("John", "Test message ininini");
            broadcastToLobbyLog(packet);
            scanner.next();
        }
    }

    public void sendMessageToLog(String message){
        Gdx.app.postRunnable(() -> this.main.getLobbyScreen().logMessageSend(message));
    }

    public void broadcastToLobbyLog(Packet packet){
        if(!(packet instanceof PacketForLobbyChat)){
            return;
        }

        for (ClientHandler currClient : clientHandlers) {
            packet.read(this.inStream);
            try {
                currClient.outStream.write(this.inStream.getBytesList());
                currClient.outStream.flush();
            } catch (IOException e) {
                System.out.println("broadcast outStream couldn't write");
            }
        }
        this.inStream.flush();
    }

    private void closeClientHandler(){
        try {
            clientHandlers.remove(this);
            this.inBuffer.close();
            this.outStream.close();
            if(this.socket != null){
                this.socket.close();
            }
        } catch (IOException e) {}
        
    }
}
*/