package io.github.Chino.LodeRunner.GameInterface.LanConnection;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import io.github.Chino.LodeRunner.GameInterface.LanConnection.Packets.ByteHandler.ByteBuffer;

public class Server extends Thread{
    private ServerSocket serverSocket;
    private ByteBuffer buffer;
    public static ArrayList<ClientHandler> clientHandlersInServer = new ArrayList<>();

    public Server(ServerSocket serverSocket) {
        this.serverSocket = serverSocket;
        this.buffer = new ByteBuffer(1024);
    }

    public void startServer(){
        int maxAmmount = 0;
        try{
            while(!this.serverSocket.isClosed() && maxAmmount != 2){
                // Wait here until a client connect
                Socket socket = this.serverSocket.accept();
                System.out.println("startServer: A new client connected!");

                ClientHandler clientHandler = new ClientHandler(socket);
                clientHandlersInServer.add(clientHandler);
                Thread threadForClientHandler = new Thread(clientHandler);
                threadForClientHandler.start();

                maxAmmount++;
            }
        }catch(IOException e){
            System.out.println("\nERROR Server.java: catched IOException in startServer");
        }
    }

    // Send to each client. Received via ClientSide.java listenForPackets function
    private void broadcastPacket(ByteBuffer buffer){
        byte[] packetInByte = buffer.getBytesList();
        buffer.flush();
        for (ClientHandler client : clientHandlersInServer) {
            try {
                System.out.println("BROADCAST sent");
                client.writerStream.write(packetInByte);
                client.writerStream.flush();
            } catch (IOException e) {
                System.out.println("\nERROR ClientHandler.java : Catched in broadcastMessage. About to terminate connection with client.");
                client.closeEverything(client.socket, client.writerStream, client.readerStream);
                removeFromClientHandlers(client);
            }
        }
    }

    private void removeFromClientHandlers(ClientHandler client){
        clientHandlersInServer.remove(client); 
    }

    public void closeServerSocket(){
        try {
            if(this.serverSocket != null){
                this.serverSocket.close();
            }
        } catch (IOException e) {
            System.out.println("\nERROR will closing server");
        }
    }

    public static void main(String[] args) throws Exception{
        ServerSocket serverSocket = new ServerSocket(5000);
        Server server = new Server(serverSocket);
        server.startServer();

        System.out.println("About to send packages");        
        Thread.sleep(1000);
        ByteBuffer buffer = new ByteBuffer(1024);
        System.out.println("Got the clients : " + clientHandlersInServer.get(0) + " // " + clientHandlersInServer.get(1));
        
        buffer.writeInt(1);
        server.broadcastPacket(buffer);
        buffer.flush();
    
        buffer.writeInt(2);
        server.broadcastPacket(buffer);
        buffer.flush();
    }

}