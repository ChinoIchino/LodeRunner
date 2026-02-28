package io.github.Chino.LodeRunner.GameInterface.LanConnection;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;

import com.badlogic.gdx.Screen;

public class ClientHandler implements Runnable{
    public static ArrayList<ClientHandler> clientHandlers = new ArrayList<>();
    
    private Socket socket;
    
    private BufferedReader inStream;
    private PrintWriter outStream;

    private String clientUsername;

    public ClientHandler(Screen clientScreen, Socket clientSocket) {
        try {
            this.socket = clientSocket;
    
            this.outStream = new PrintWriter(this.socket.getOutputStream(), true);
            this.inStream = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
    
            this.clientHandlers.add(this);
            // this.clienScreen.logMessage("SERVER: New Client has connected!");
            
        } catch (Exception e) {
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

    public void send(String message){
        // logMessage(message);
    }
}

