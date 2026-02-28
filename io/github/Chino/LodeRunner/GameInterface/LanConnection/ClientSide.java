package io.github.Chino.LodeRunner.GameInterface.LanConnection;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientSide {
    private Socket socket;
    private BufferedReader inStream;
    private PrintWriter outStream;

    public void connectToServer(String ip, int port) throws IOException{
        this.socket = new Socket(ip, port);

        this.inStream = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
        this.outStream = new PrintWriter(this.socket.getOutputStream(), true);

        new Thread(() ->{
            try {
                while(true){
                    String message = inStream.readLine();
                }
            } catch (IOException e) {
                System.err.println("\nERROR GameInterface/LanConnection/ClientSide.java: Function connectToServer catched IOException");
            }
        }).start();
    }
    
    public void send(String message){
        outStream.println(message);
    }
}
