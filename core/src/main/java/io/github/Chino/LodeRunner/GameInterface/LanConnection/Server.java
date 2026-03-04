package io.github.Chino.LodeRunner.GameInterface.LanConnection;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import io.github.Chino.LodeRunner.GameInterface.GDXMain;

public class Server extends Thread{
    private ServerSocket serverSocket;
    private String password;
    private GDXMain main;

    public Server(ServerSocket serverSocket, GDXMain main, String serverPassword){
        this.serverSocket = serverSocket;
        this.main = main;
        this.password = serverPassword;
    }

    @Override
    public void run(){
        while(!this.serverSocket.isClosed()){
            try {
                Socket clientSocket = this.serverSocket.accept();
                // Gdx.app.postRunnable(() -> this.main.getLobbyScreen().logMessageSend("A new client connected!"));
                System.out.println("A new client connected!");

                ClientHandler ClientHandler = new ClientHandler(this.main, clientSocket);
                
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