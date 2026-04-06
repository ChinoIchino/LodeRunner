package io.github.Chino.LodeRunner.GameInterface.LanConnection.Packets.PacketTypes;

import java.util.ArrayList;
import java.util.List;

import io.github.Chino.LodeRunner.GameInterface.LanConnection.Packets.ByteHandler.ByteBuffer;

public class PacketForPlayerJoin extends Packet {
    private String nameOfPlayer;

    public PacketForPlayerJoin() {
        this(null);
    }
    public PacketForPlayerJoin(String nameOfPlayer) {
        this.nameOfPlayer = nameOfPlayer;
    }


    // read toEncodePacket and override this packet attributs
    @Override
    public void read(ByteBuffer inPacket){
        inPacket.resetCursor();

        inPacket.writeInt(this.getPacketId());
        
        int nameOfPlayerSize = this.nameOfPlayer.length();
        inPacket.writeInt(nameOfPlayerSize);
        inPacket.writeString(this.nameOfPlayer);
    }

    // write into toEncodePacket byte of this packet attributs
    @Override
    public void write(ByteBuffer outPacket) {
        outPacket.resetCursor();
        
        outPacket.readInt();
        
        int sizeOfPlayerName = outPacket.readInt();
        this.nameOfPlayer = outPacket.readString(sizeOfPlayerName);
    }
    @Override
    public int getPacketId() {
        return 2;
    }
    @Override
    public List<Object> unpackPacket() {
        List<Object> list = new ArrayList<>();
        
        list.add(this.nameOfPlayer);

        return list;
    }
    
    @Override
    public String toString(){
        return "PacketForLobbyPlayerList:\n   -Username : " + this.nameOfPlayer;
    }
    
}
