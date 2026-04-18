package io.github.Chino.LodeRunner.GameInterface.LanConnection;

import java.util.ArrayList;
import java.util.List;

import io.github.Chino.LodeRunner.GameInterface.LanConnection.Packets.ByteHandler.ByteBuffer;
import io.github.Chino.LodeRunner.GameInterface.Entity.Player;
import io.github.Chino.LodeRunner.GameInterface.World.Collectible;

public class TranslateToBytes{
    // It compact the string into the "protocol" of a PacketForLobbyPlayersList
    public static byte[] toPlayerListPacket(String toEncode){
        ByteBuffer buffer = new ByteBuffer(1024);
        buffer.writeInt(2);

        int stringSize = toEncode.length();
        buffer.writeInt(stringSize);
        buffer.writeString(toEncode);

        return buffer.getBytesList();
    } 
    public static byte[] toPlayerLeaveListPacket(String toEncode){
        ByteBuffer buffer = new ByteBuffer(1024);
        buffer.writeInt(3);

        int stringSize = toEncode.length();
        buffer.writeInt(stringSize);
        buffer.writeString(toEncode);

        return buffer.bytes;
    }
    // Added the firstMap so the level creation isn't local anymore
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

    public static byte[] toLobbyChatMessages(ArrayList<String> usernameList, ArrayList<String> messageList){
        ByteBuffer buffer = new ByteBuffer(1024);
        buffer.writeInt(4);
        buffer.writeInt(usernameList.size());

        int sizeOfUsername;
        int sizeOfMessage;
        for (int i = 0; i < usernameList.size(); i++) {
            sizeOfUsername = usernameList.get(i).length();
            buffer.writeInt(sizeOfUsername);
            buffer.writeString(usernameList.get(i));

            sizeOfMessage = messageList.get(i).length();
            buffer.writeInt(sizeOfMessage);
            buffer.writeString(messageList.get(i));
        }

        return buffer.bytes;
    }
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
    public static byte[] toPlayerMovement(Player player, int textureId){
        ByteBuffer buffer = new ByteBuffer(1024);

        buffer.writeInt(7);

        buffer.writeInt(player.getId());

        buffer.writeInt(textureId);

        buffer.writeInt(player.getPosX());
        buffer.writeInt(player.getPosY());

        return buffer.getBytesList();
    }
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

    public static byte[] toBreakBlock(int x,int y){
        ByteBuffer buffer = new ByteBuffer(1024);
        buffer.resetCursor();

        buffer.writeInt(11);

        buffer.writeInt(x);
        buffer.writeInt(y);
        return buffer.getBytesList();
    }
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

