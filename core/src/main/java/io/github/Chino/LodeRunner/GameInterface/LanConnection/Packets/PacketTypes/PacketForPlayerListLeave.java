package io.github.Chino.LodeRunner.GameInterface.LanConnection.Packets.PacketTypes;

import java.util.ArrayList;
import java.util.List;

import io.github.Chino.LodeRunner.GameInterface.LanConnection.Packets.ByteHandler.ByteBuffer;

public class PacketForPlayerListLeave extends Packet{
    private String nameOfPlayer;

    
    public PacketForPlayerListLeave() {
        this(null);
    }
    public PacketForPlayerListLeave(String nameOfPlayer) {
        this.nameOfPlayer = nameOfPlayer;
    }

    @Override
    public void read(ByteBuffer inPacket) {
        inPacket.resetCursor();
        inPacket.writeInt(getPacketId());

        int sizeOfName = this.nameOfPlayer.length();
        inPacket.writeInt(sizeOfName);
        inPacket.writeString(this.nameOfPlayer);
    }

    @Override
    public void write(ByteBuffer outPacket) {
        outPacket.resetCursor();
        outPacket.readInt();

        int sizeToRead = outPacket.readInt();
        this.nameOfPlayer = outPacket.readString(sizeToRead);
    }

    @Override
    public int getPacketId() {
        return 3;
    }

    @Override
    public List<Object> unpackPacket() {
        List<Object> list = new ArrayList<>();
        
        list.add(this.nameOfPlayer);

        return list;
    }

    @Override
    public String toString() {
        return "PacketForLobbyPlayerListLeave:\n   -Username : " + this.nameOfPlayer;
    }
    
}
