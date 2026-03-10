package io.github.Chino.LodeRunner.GameInterface.LanConnection;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientHandler implements Runnable{
    private String username;

    private Server server;

    protected Socket socket;

    // Protected so the Server class can access it
    protected BufferedInputStream readerStream;
    protected BufferedOutputStream writerStream;

    public ClientHandler(Socket socket, Server server){
        try{
            //Connection between server and client
            this.socket = socket;

            this.writerStream = new BufferedOutputStream(this.socket.getOutputStream());
            this.readerStream = new BufferedInputStream(this.socket.getInputStream());

            this.server = server;
        }catch(IOException e){
            closeEverything(this.socket, this.writerStream, this.readerStream);
        }

    }

    // When we write into the writeBuffer of CliendSide it end here
    @Override
    public void run() {
        byte[] bytes = new byte[1024];
        int packetType;

        //Before we get in the loop we wait for the username
        try {
            int sizeOfItem = this.readerStream.read(bytes);
            this.username = new String(bytes, 0, sizeOfItem).trim();
            
            server.sendUsernameToPlayerList(this.username);
            
            this.server.broadcastPacket(TranslateToBytes.toPlayerListPacket(this.username));
            
        } catch (IOException e) {}

        while(this.socket.isConnected()){
            try {
                this.readerStream.read(bytes);
                System.out.println("ClientHandler received a packet about to send to the server.");
                server.broadcastPacket(bytes);
            } catch (IOException e) {
                // System.out.println("\nERROR ClientHandler.java: thread run");
                closeEverything(this.socket, this.writerStream, this.readerStream);
            }
        }
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