package io.github.Chino.LodeRunner.GameInterface.LanConnection;

import java.util.ArrayList;
import java.util.List;

import io.github.Chino.LodeRunner.GameInterface.Entity.Player;
import io.github.Chino.LodeRunner.GameInterface.LanConnection.Packets.ByteHandler.ByteBuffer;
import io.github.Chino.LodeRunner.GameInterface.World.Collectible;

public class TranslateToBytes{
    // It compact the string into the "protocol" of a PacketForLobbyPlayersList
    /**
     * @param toEncode Username of the player to encode
     * @return byte[] list in the packet id 2 format
     */
    public static byte[] toPlayerListPacket(String toEncode){
        ByteBuffer buffer = new ByteBuffer(1024);
        buffer.writeInt(2);

        int stringSize = toEncode.length();
        buffer.writeInt(stringSize);
        buffer.writeString(toEncode);

        return buffer.getBytesList();
    } 
    /**
     * @param toEncode Username of the player to encode
     * @return byte[] list in the packet id 3 format
     */
    public static byte[] toPlayerLeaveListPacket(String toEncode){
        ByteBuffer buffer = new ByteBuffer(1024);
        buffer.writeInt(3);

        int stringSize = toEncode.length();
        buffer.writeInt(stringSize);
        buffer.writeString(toEncode);

        return buffer.bytes;
    }
    // Added the firstMap so the level creation isn't local anymore
    /**
     * @param isVersus Gamemode of the current session
     * @param firstMap Map of the first level
     * @param players All the players currently in the session
     * @return byte[] list in the packet id 1 format
     */
    public static byte[] toLobbyEssentials(boolean isVersus, char[][] firstMap, String... players){
        ByteBuffer buffer = new ByteBuffer(1024);
        buffer.writeInt(1);

        // Write the game mode Based on the isVersus boolean
        if(isVersus){
            buffer.writeInt(1);
        }else{
            buffer.writeInt(0);
        }
        
        // Add the current player list
        if(players != null){
            buffer.writeInt(players.length);
    
            int stringSize;
            for (String currentName : players) {
                // Need to trim currentName as precaution
                currentName = currentName.trim();
    
                stringSize = currentName.length();
                
                buffer.writeInt(stringSize);
                buffer.writeString(currentName);
            }
        }else{
            buffer.writeInt(0);
        }

        if(firstMap != null){
            buffer.writeInt(firstMap.length);
            buffer.writeInt(firstMap[0].length);
    
            for (int y = 0; y < firstMap.length; y++) {
                for (int x = 0; x < firstMap[y].length; x++) {
                    buffer.writeChar(firstMap[y][x]);
                }
            }
        }else{
            buffer.writeInt(0);
        }

        return buffer.bytes;
    }
    /**
     * @param maps All the maps to use from WorldFile.txt
     * @return byte[] list in the packet id 11 format
     */
    public static byte[] toMapsPacket(ArrayList<char[][]> maps){
        ByteBuffer buffer = new ByteBuffer(1024);
        
        // Write the packet id
        buffer.writeInt(11);

        // Write the ammount of levels in the packet
        buffer.writeInt(maps.size());

        for (char[][] currentMap: maps) {
            // Write the current map y and x resolution
            buffer.writeInt(currentMap.length);
            buffer.writeInt(currentMap[0].length);


            // Write the letter matrix for the current map
            for (int y = 0; y < currentMap.length; y++) {
                for (int x = 0; x < currentMap[y].length; x++) {
                    buffer.writeChar(currentMap[y][x]);
                }
            }
        }
        return buffer.getBytesList();
    }

    /**
     * @param username Username of the player
     * @param message Message of the player
     * @return byte[] list in the packet id 5 format
     */
    public static byte[] toLobbyChatMessage(String username, String message){
        ByteBuffer buffer = new ByteBuffer(1024);
        buffer.writeInt(5);

        buffer.writeInt(username.length());
        buffer.writeString(username);

        buffer.writeInt(message.length());
        buffer.writeString(message);

        return buffer.bytes;
    }
    // textureId: 0 = idle / 1 = moving left / 2 = moving right
    /**
     * @param player The player that is moving
     * @param textureId 0 = idle / 1 = moving left / 2 = moving right
     * @return byte[] list in the packet id 7 format
     */
    public static byte[] toPlayerMovement(Player player, int textureId){
        ByteBuffer buffer = new ByteBuffer(1024);

        buffer.writeInt(7);

        buffer.writeInt(player.getId());

        buffer.writeInt(textureId);

        buffer.writeInt(player.getPosX());
        buffer.writeInt(player.getPosY());

        return buffer.getBytesList();
    }
    /**
     * @param collectibleInformations Need to contain index 1 : the collectible, index 2: y index of the item based on the world, index 3 : x index of the item based on the world
     * @param playerId The id of the player
     * @param gameModeId 0 = Versus / 1 = Coop
     * @return byte[] list in the packet id 8 format
     */
    public static byte[] toPlayerScoreAdd(List<Object> collectibleInformations,int playerId,int gameModeId){
        ByteBuffer buffer = new ByteBuffer(1024);

        buffer.writeInt(8);
        
        buffer.writeInt(gameModeId);
        
        Collectible collectible = (Collectible) collectibleInformations.get(0);
        buffer.writeInt(collectible.getScore());

        buffer.writeInt((int) collectibleInformations.get(1));
        buffer.writeInt((int) collectibleInformations.get(2));
        buffer.writeInt(playerId);

        return buffer.getBytesList();
    }

    /**
     * @param x Index x of the block based on the world matrix
     * @param y Index y of the block based on the world matrix
     * @return byte[] list in the packet id 11 format
     */
    public static byte[] toBreakBlock(int x,int y){
        ByteBuffer buffer = new ByteBuffer(1024);
        buffer.resetCursor();

        buffer.writeInt(11);

        buffer.writeInt(x);
        buffer.writeInt(y);
        return buffer.getBytesList();
    }
    /**
     * @param aiId Id of the ai
     * @param nearestPlayerId Id of the nearest player
     * @param animationId 0 = idle / 1 = moving left / 2 = moving right
     * @param positionX Position x of the ai
     * @param positionY Position y of the ai
     * @return byte[] list in the packet id 12 format
     */
    public static byte[] toAIMovement(int aiId,int nearestPlayerId,int animationId,int positionX,int positionY){
        ByteBuffer buffer = new ByteBuffer(1024);
        buffer.resetCursor();

        buffer.writeInt(12);
        
        buffer.writeInt(aiId);
        buffer.writeInt(nearestPlayerId);
        
        
        buffer.writeInt(animationId);
        
        buffer.writeInt(positionX);
        buffer.writeInt(positionY);
        return buffer.getBytesList();
    }
}

