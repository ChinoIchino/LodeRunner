package io.github.Chino.LodeRunner.GameInterface.LanConnection;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;

import com.badlogic.gdx.Gdx;

import io.github.Chino.LodeRunner.GameInterface.GDXMain;

public class ClientHandler implements Runnable{
    public GDXMain main;
    public static ArrayList<ClientHandler> clientHandlers = new ArrayList<>();
    
    private Socket socket;
    
    private BufferedReader inStream;
    private PrintWriter outStream;

    private String clientUsername;

    public ClientHandler(GDXMain main, Socket clientSocket) {
        try {
            this.socket = clientSocket;
    
            this.outStream = new PrintWriter(this.socket.getOutputStream(), true);
            this.inStream = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));

            this.main = main;

            broadcastToLobbyLog("A New Client Has Joined");
            clientHandlers.add(this);
        } catch (Exception e) {
            this.closeClientHandler();
        }
    }

    @Override
    public void run(){
        while (true) { 
            try{
                String message = inStream.readLine();
                // logMessage(message);
            }catch(IOException e){
                System.err.println("\nERROR GameInterface/LanConnection/ClientHandler.java: Function run cathed a IOException while sending a message");
            }
        }
    }

    public void sendMessageToLog(String message){
        Gdx.app.postRunnable(() -> this.main.getLobbyScreen().logMessageSend(message));
    }

    public void broadcastToLobbyLog(String message){
        for (ClientHandler currClient : clientHandlers) {
            currClient.outStream.append(message);
        }
    }

    private void closeClientHandler(){
        try {
            clientHandlers.remove(this);
            this.inStream.close();
            this.outStream.close();
            if(this.socket != null){
                this.socket.close();
            }
        } catch (IOException e) {}
        
    }
}

