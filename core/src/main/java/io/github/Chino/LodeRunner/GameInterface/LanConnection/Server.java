package io.github.Chino.LodeRunner.GameInterface.LanConnection;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;

import io.github.Chino.LodeRunner.GameInterface.LanConnection.Packets.ByteHandler.ByteBuffer;
import io.github.Chino.LodeRunner.GameInterface.Entity.Player;

public class Server extends Thread{
    private ServerSocket serverSocket;

    private ByteBuffer buffer = new ByteBuffer(1024);

    private volatile boolean isRunning = true;

    public ArrayList<ClientHandler> clientHandlersInServer = new ArrayList<>();
    public ArrayList<Player> playerList = new ArrayList<>();

    private boolean hostAlreadyJoined = false;
    private boolean isVersus;

    // Variables for the maps to not be local
    private int currentLevel = 1;
    private ArrayList<char[][]> maps = new ArrayList<>();

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
                System.out.println("\nWARN Server.java: server socket was closed");
                if(!isRunning){
                    break;
                }
            }
        }
    }

    // Send to each client. Received via ClientSide.java listenForPackets function
    protected synchronized void broadcastPacket(byte[] bytes){
        // if interceptPacket return false, it dont send the current packet
        if(!interceptPacket(bytes)){
            System.out.println("A packet was denied for broadcast");
            return;
        }

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

    // Verify the packet that was send by the users
    // If the packet is only only for the server or have invalid informations return false
    private boolean interceptPacket(byte[] bytes){
        this.buffer.clear();
        this.buffer.bytes = bytes;
        
        int packetType = this.buffer.readInt();

        // Type 11: Init all the levels via the host worldFile.txt
        if(packetType == 11 && !this.hostAlreadyJoined){
            this.loadMapsOnServer(this.buffer);
            return false;
        }

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
        if(packetType == 8 && buffer.readInt() == 0){
            int toAdd = buffer.readInt();

            buffer.resetCursor();
            buffer.writeInt(8);

            // Rewrite the score in the packet
            buffer.writeInt(this.score + toAdd);

            this.score += toAdd;
        }

        // Type 10: The next level need to be loaded
        if(packetType == 10){
            if(!this.loadNextMapInBuffer(buffer)){
                ByteBuffer noNextMapBuffer = new ByteBuffer(8);
                noNextMapBuffer.writeInt(14);
                broadcastPacket(noNextMapBuffer.getBytesList());
                return false;
            }
        }

        return true;
    }

    private void sendToClientLobbyEssentials(ClientHandler client){
        // Convert from ArrayList<String> into String[]
        String[] list = getPlayerUsernamesList();
        byte[] bytes = TranslateToBytes.toLobbyEssentials(this.isVersus, this.maps.get(0), list);

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
        playerList.add(new Player(username, -1));
    }

    private void removeFromClientHandlers(ClientHandler client){
        clientHandlersInServer.remove(client); 
    }

    private void loadMapsOnServer(ByteBuffer buffer){
        // Just in case start from the beginning of the buffer
        buffer.resetCursor();

        // Skip the id of the packet
        buffer.readInt();

        int ammountOfLevels = buffer.readInt();

        for(int i = 0; i < ammountOfLevels; i++) {
            char[][] currentMap = new char[buffer.readInt()][buffer.readInt()];
            
            for(int y = 0; y < currentMap.length; y++) {
                for(int x = 0; x < currentMap[y].length; x++) {
                    currentMap[y][x] = buffer.readChar();
                }
            }    

            this.maps.add(currentMap);
        }
    }
    private boolean loadNextMapInBuffer(ByteBuffer buffer){
        if(this.maps.size() <= this.currentLevel){
            return false;
        }
        char[][] nextLevelMap = this.maps.get(this.currentLevel++);

        
        buffer.resetCursor();

        buffer.writeInt(10);
        
        buffer.writeInt(nextLevelMap.length);
        buffer.writeInt(nextLevelMap[0].length);

        for (int y = 0; y < nextLevelMap.length; y++) {
            for (int x = 0; x < nextLevelMap[y].length; x++) {
                buffer.writeChar(nextLevelMap[y][x]);
            }
        }
        return true;
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

    private void debugPrintLoadedMaps(){
        int count = 1;
        for (char[][] map : this.maps) {
            System.out.println("Map number " + count + " :");
            for (int y = 0; y < map.length; y++) {
                for (int x = 0; x < map[y].length; x++) {
                    System.out.print(map[y][x] + ", ");
                }
                System.out.println();
            }
            System.out.println("End Map number " + count++);
        }
    }

    @Override
    public String toString(){
        return "Server[Is Running = " + this.isRunning + "]";
    }
}