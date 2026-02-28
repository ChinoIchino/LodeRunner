package io.github.Chino.LodeRunner.GameInterface.LanConnection;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import com.badlogic.gdx.Screen;

public class Server extends Thread{
    private ServerSocket serverSocket;
    private String password;
    private Screen hostScreen;

    public Server(ServerSocket serverSocket, Screen hostScreen, String serverPassword){
        this.serverSocket = serverSocket;
        this.hostScreen = hostScreen;
        this.password = serverPassword;
    }

    @Override
    public void run(){
        while(!this.serverSocket.isClosed()){
            try {
                Socket clientSocket = this.serverSocket.accept();
                System.out.println("A new client connected!");

                ClientHandler ClientHandler = new ClientHandler(this.hostScreen, clientSocket);
                
                Thread clientThread = new Thread(ClientHandler);
                clientThread.start();
            
            } catch (IOException e) {
                System.err.println("\nERROR: GameInterface/LanConnection/Server.java: Function run catched a IOException");
            }
        }
    }

    public ServerSocket getServerSocket() {
        return serverSocket;
    }

    public void closeServerSocket(){
        try {
            if(this.serverSocket != null){
                System.out.println("Closed socket");
                this.serverSocket.close();
            }
        } catch (IOException e) {
            System.err.println("\nERROR GameInterface/LanConnection/Server.java: Function closeServerSocket catched IOException");
        }
    }

    
}