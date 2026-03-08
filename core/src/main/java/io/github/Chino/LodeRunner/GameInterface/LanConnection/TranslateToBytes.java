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
    public static byte[] toAllPlayerListPacket(ArrayList<String> players){
        ByteBuffer buffer = new ByteBuffer(1024);
        buffer.writeInt(1);
        buffer.writeInt(players.size());

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
}

