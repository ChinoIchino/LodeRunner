package io.github.Chino.LodeRunner.GameInterface.LanConnection;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import io.github.Chino.LodeRunner.GameInterface.LanConnection.Packets.ByteHandler.ByteBuffer;

public class Server extends Thread{
    private ServerSocket serverSocket;
    private ByteBuffer buffer = new ByteBuffer(1024);

    public ArrayList<ClientHandler> clientHandlersInServer = new ArrayList<>();
    public ArrayList<String> playerList = new ArrayList<>();

    public Server(ServerSocket serverSocket) {
        this.serverSocket = serverSocket;
    }

    @Override
    public void run(){
        int maxAmmount = 0;
        try{
            while(!this.serverSocket.isClosed()){
                // Wait here until a client connect
                Socket socket = this.serverSocket.accept();
                System.out.println("startServer: A new client connected!");

                ClientHandler clientHandler = new ClientHandler(socket, this);
                sendToClientAllPlayerList(clientHandler);

                clientHandlersInServer.add(clientHandler);
                Thread threadForClientHandler = new Thread(clientHandler);
                threadForClientHandler.start();
            }
        }catch(IOException e){
            System.out.println("\nERROR Server.java: catched IOException in startServer");
            closeServerProperly();
        }
    }

    // Send to each client. Received via ClientSide.java listenForPackets function
    protected void broadcastPacket(byte[] bytes){
        interceptPacket(bytes);

        for (ClientHandler client : clientHandlersInServer) {
            System.out.println("Sending packet via broadcast to client");
            try {
                // Send packet to each clients
                client.writerStream.write(bytes);
                client.writerStream.flush();
            } catch (IOException e) {
                System.out.println("\nERROR ClientHandler.java : Catched in broadcastMessage. About to terminate connection with client.");
                client.closeEverything(client.socket, client.writerStream, client.readerStream);
                removeFromClientHandlers(client);
            }
        }
    }

    // Used before broadcasting, can be used as a packet verificator
    // For now it only remove the player from the player list if the packet have the id 3
    private void interceptPacket(byte[] bytes){
        this.buffer.clear();
        this.buffer.bytes = bytes;
        if(buffer.readInt() == 3){
            int stringSize = this.buffer.readInt();

            this.playerList.remove(this.buffer.readString(stringSize).trim());
        }
        
    }

    private void sendToClientAllPlayerList(ClientHandler client){
        byte[] bytes = TranslateToBytes.toAllPlayerListPacket(playerList);

        try{
            client.writerStream.write(bytes);
            client.writerStream.flush();
        }catch (IOException e){
            System.out.println("\nERROR GameInterface/LanConnection/Server.java: function sendToClientAllPlayerList catched a IOException");
        }
    }

    protected void sendUsernameToPlayerList(String nameToAdd){
        System.out.println("Server: sendUsernameToPlayerList got a new name");
        playerList.add(nameToAdd);

        System.out.println("Current list:");
        for (String name : playerList) {
            System.out.println(name);
        }
        System.out.println("END OF LIST");
    }

    private void removeFromClientHandlers(ClientHandler client){
        clientHandlersInServer.remove(client); 
    }

    public void closeServerProperly(){
        try {
            for (ClientHandler client : clientHandlersInServer) {
                client.closeEverything(client.socket, client.writerStream, client.readerStream);
            }
            clientHandlersInServer.clear();
            playerList.clear();

            if(this.serverSocket != null){
                this.serverSocket.close();
            }
        } catch (IOException e) {
            System.out.println("\nERROR will closing server");
        }
    }
}