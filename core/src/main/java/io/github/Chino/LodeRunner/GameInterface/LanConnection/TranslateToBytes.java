package io.github.Chino.LodeRunner.GameInterface.LanConnection;

import java.util.ArrayList;
import java.util.List;

import io.github.Chino.LodeRunner.GameInterface.LanConnection.Packets.ByteHandler.ByteBuffer;
import io.github.Chino.LodeRunner.GameInterface.Player.Player;
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
    public static byte[] toLobbyEssentials(boolean isVersus, String... players){
        ByteBuffer buffer = new ByteBuffer(1024);
        buffer.writeInt(1);

        // Write the game mode Based on the isVersus boolean
        if(isVersus){
            buffer.writeInt(1);
        }else{
            buffer.writeInt(0);
        }

        buffer.writeInt(players.length);

        int stringSize;
        for (String currentName : players) {
            // Need to trim currentName as precaution
            currentName = currentName.trim();

            stringSize = currentName.length();
            
            buffer.writeInt(stringSize);
            buffer.writeString(currentName);
        }

        return buffer.bytes;
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
    public static byte[] toPlayerScoreAdd(List<Object> collectibleInformations){
        ByteBuffer buffer = new ByteBuffer(1024);

        buffer.writeInt(8);
        
        Collectible collectible = (Collectible) collectibleInformations.get(0);
        buffer.writeInt(collectible.getScore());

        buffer.writeInt((int) collectibleInformations.get(1));
        buffer.writeInt((int) collectibleInformations.get(2));

        return buffer.getBytesList();
    }
}

