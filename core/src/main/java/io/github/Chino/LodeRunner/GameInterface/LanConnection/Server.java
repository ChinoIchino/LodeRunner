package io.github.Chino.LodeRunner.GameInterface.LanConnection;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class Server extends Thread{
    private ServerSocket serverSocket;

    public static ArrayList<ClientHandler> clientHandlersInServer = new ArrayList<>();
    public static ArrayList<String> playerList = new ArrayList<>();

    public Server(ServerSocket serverSocket) {
        this.serverSocket = serverSocket;
    }

    @Override
    public void run(){
        int maxAmmount = 0;
        try{
            while(!this.serverSocket.isClosed() && maxAmmount != 3){
                // Wait here until a client connect
                Socket socket = this.serverSocket.accept();
                System.out.println("startServer: A new client connected!");

                ClientHandler clientHandler = new ClientHandler(socket, this);
                sendToClientAllPlayerList(clientHandler);

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
    protected void broadcastPacket(byte[] bytes){
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

    private void sendToClientAllPlayerList(ClientHandler client){
        byte[] bytes = TranslateToBytes.toAllPlayerListPacket(playerList);

        try{
            client.writerStream.write(bytes);
            client.writerStream.flush();
        }catch (IOException e){
            System.out.println("\nERROR GameInterface/LanConnection/Server.java: function sendToClientAllPlayerList catched a IOException");
        }
    }

    protected void sendUsernameToPlayerList(byte[] bytes){
        System.out.println("Server: sendUsernameToPlayerList got a new name");
        playerList.add(new String(bytes));

        System.out.println("Current list:");
        for (String name : playerList) {
            System.out.println(name);
        }
        System.out.println("END OF LIST");
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
}