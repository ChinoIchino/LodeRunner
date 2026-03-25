package io.github.Chino.LodeRunner.GameInterface.LanConnection;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;

import io.github.Chino.LodeRunner.GameInterface.LanConnection.Packets.ByteHandler.ByteBuffer;
import io.github.Chino.LodeRunner.GameInterface.Player.Player;

public class Server extends Thread{
    private ServerSocket serverSocket;
    private ByteBuffer buffer = new ByteBuffer(1024);

    private volatile boolean isRunning = true;

    public ArrayList<ClientHandler> clientHandlersInServer = new ArrayList<>();
    public ArrayList<Player> playerList = new ArrayList<>();

    private boolean hostAlreadyJoined = false;
    private boolean isVersus;

    private int score = 0;

    public Server(ServerSocket serverSocket) {
        this.serverSocket = serverSocket;
    }

    @Override
    public void run(){
        while(isRunning){
            try{
                // Wait here until a client connect
                Socket socket = this.serverSocket.accept();

                ClientHandler clientHandler = new ClientHandler(socket, this);
                if(this.hostAlreadyJoined){
                    sendToClientLobbyEssentials(clientHandler);
                }

                clientHandlersInServer.add(clientHandler);
                Thread threadForClientHandler = new Thread(clientHandler);
                threadForClientHandler.start();

            }catch(IOException e){
                System.out.println("\nERROR Server.java: catched IOException in startServer");
                if(!isRunning){
                    break;
                }
            }
        }
    }

    // Send to each client. Received via ClientSide.java listenForPackets function
    protected synchronized void broadcastPacket(byte[] bytes){
        interceptPacket(bytes);

        for (ClientHandler client : clientHandlersInServer) {
            try {
                if(!client.socket.isClosed()){
                    // Send packet to each clients
                    client.writerStream.write(bytes);
                    client.writerStream.flush();
                }
            } catch (IOException e) {
                System.out.println("\nERROR ClientHandler.java : Catched in broadcastMessage. About to terminate connection with client.");
                client.closeEverything(client.socket, client.writerStream, client.readerStream);
                removeFromClientHandlers(client);
            }
        }
    }

    // Used before broadcasting, can be used as a packet verificator
    private void interceptPacket(byte[] bytes){
        this.buffer.clear();
        this.buffer.bytes = bytes;
        
        int packetType = this.buffer.readInt();

        // Type 1: Init the server informations when the host join
        if(packetType == 1 && !this.hostAlreadyJoined){
            // Rewrite only once the game mode when the first client send the game mode information
            this.isVersus = this.buffer.readInt() == 1;
            this.hostAlreadyJoined = true;
        }

        // Type 3: A player quit the lobby
        if(packetType == 3){
            int stringSize = this.buffer.readInt();
            String usernameToFind = this.buffer.readString(stringSize).trim();

            for(Player player: playerList){
                if(player.getUsername().equals(usernameToFind)){
                    this.playerList.remove(player);
                    break;
                }
            }
        }
        
        // Type 8: A player has interacted with a collectible
        if(packetType == 8){
            int toAdd = buffer.readInt();

            buffer.resetCursor();
            buffer.writeInt(8);

            // Rewrite the score in the packet
            buffer.writeInt(this.score + toAdd);

            this.score += toAdd;
        }
    }

    private void sendToClientLobbyEssentials(ClientHandler client){
        // Convert from ArrayList<String> into String[]
        String[] list = getPlayerUsernamesList();
        byte[] bytes = TranslateToBytes.toLobbyEssentials(this.isVersus , list);

        try{
            client.writerStream.write(bytes);
            client.writerStream.flush();
        }catch (IOException e){
            System.out.println("\nERROR GameInterface/LanConnection/Server.java: function sendToClientAllPlayerList catched a IOException");
        }
    }
    private String[] getPlayerUsernamesList(){
        String[] toReturn = new String[this.playerList.size()];
        
        for (int i = 0; i < this.playerList.size(); i++) {
            toReturn[i] = this.playerList.get(i).getUsername(); 
        }

        return toReturn;
    }

    protected void sendUsernameToPlayerList(String username){
        // TODO change id to this.playerList.size()
        playerList.add(new Player(username, -1));
    }

    private void removeFromClientHandlers(ClientHandler client){
        clientHandlersInServer.remove(client); 
    }

    public synchronized void closeServerProperly(){
        try {
        ByteBuffer buffer = new ByteBuffer(4);
        buffer.writeInt(4);

        byte[] packet = Arrays.copyOf(buffer.getBytesList(), buffer.getBytesList().length);
        broadcastPacket(packet);

        // buffer.clear();

        this.isRunning = false;

        if(this.serverSocket != null && !this.serverSocket.isClosed()){
            this.serverSocket.close();
        }

        for (ClientHandler client : clientHandlersInServer) {
            client.closeEverything(client.socket, client.writerStream, client.readerStream);
        }

        clientHandlersInServer.clear();
        playerList.clear();

        } catch (IOException e) {
            System.out.println("\nERROR GameInterface/LanConnection/Server.java: While closing server");
        }
    }

    @Override
    public String toString(){
        return "Server[Is Running = " + this.isRunning + "]";
    }
}