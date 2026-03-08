package io.github.Chino.LodeRunner.GameInterface.LanConnection.Packets.PacketTypes;

import java.util.ArrayList;
import java.util.List;

import io.github.Chino.LodeRunner.GameInterface.LanConnection.Packets.ByteHandler.ByteBuffer;

public class PacketForLobbyChat extends Packet{
    private String nameOfPlayer;
    private String message;

    public PacketForLobbyChat() {
        this(null, null);
    }
    public PacketForLobbyChat(String nameOfPlayer, String message) {
        this.nameOfPlayer = nameOfPlayer;
        this.message = message;
    }


    @Override
    public void read(ByteBuffer toEncode){
        toEncode.resetCursor();
        toEncode.writeInt(this.getPacketId());

        toEncode.writeInt(this.nameOfPlayer.length());
        toEncode.writeString(this.nameOfPlayer);
    
        toEncode.writeInt(this.message.length());
        toEncode.writeString(this.message);
    }

    @Override
    public void write(ByteBuffer toDecode) {
        // Just in case we reset the cursor to the start of the list
        toDecode.resetCursor();
        // Skip the id int
        toDecode.readInt();

        int sizeOfUsername = toDecode.readInt();
        this.nameOfPlayer = toDecode.readString(sizeOfUsername);

        int sizeOfMessage = toDecode.readInt();
        this.message = toDecode.readString(sizeOfMessage);
    }

    @Override
    public int getPacketId() {
        return 4;
    }

    @Override
    public List<Object> unpackPacket() {
        List<Object> list = new ArrayList<>();
        
        list.add(this.nameOfPlayer);
        list.add(this.message);

        return list;
    }

    @Override
    public String toString(){
        return "PacketForLobbyChat:\n   -Username : " + this.nameOfPlayer + "\n   -Message : " + this.message;
    }
    
    
}
