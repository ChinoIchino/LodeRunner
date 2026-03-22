package io.github.Chino.LodeRunner.GameInterface.LanConnection;

import java.util.ArrayList;

import io.github.Chino.LodeRunner.GameInterface.LanConnection.Packets.ByteHandler.ByteBuffer;

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
        System.out.println("TranslateToBytes got the bool: " + isVersus);
        if(isVersus){
            buffer.writeInt(1);
            System.out.println("Wrote 1 in packet");
        }else{
            buffer.writeInt(0);
            System.out.println("Wrote 0 in packet");
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
}

