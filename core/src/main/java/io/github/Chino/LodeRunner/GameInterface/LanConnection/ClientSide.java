package io.github.Chino.LodeRunner.GameInterface.LanConnection;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import com.badlogic.gdx.Gdx;

import io.github.Chino.LodeRunner.GameInterface.GDXMain;

public class ClientSide {
    private Socket socket;
    private BufferedReader inStream;
    private PrintWriter outStream;

    public void connectToServer(GDXMain main, String ip, int port) throws IOException{
        this.socket = new Socket(ip, port);

        this.inStream = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
        this.outStream = new PrintWriter(this.socket.getOutputStream(), true);

        new Thread(() ->{
            try {
                while(this.socket.isConnected()){
                    //Blocking until inStream get something
                    String message = inStream.readLine();
                    Gdx.app.postRunnable(() -> main.getLobbyScreen().logMessageSend(message));
                    
                }
            } catch (IOException e) {
                System.err.println("\nERROR GameInterface/LanConnection/ClientSide.java: Function connectToServer catched IOException");
                this.closeClientSide();
            }
        }).start();
    }
    
    public void send(String message){
        outStream.println(message);
    }

    private void closeClientSide(){
        try {
            this.inStream.close();
            this.outStream.close();

            if(this.socket != null){
                this.socket.close();
            }
        } catch (IOException e) {}
    }
}
