package io.github.Chino.LodeRunner.GameInterface.LanConnection.Packets.PacketTypes;

import java.util.List;

import io.github.Chino.LodeRunner.GameInterface.LanConnection.Packets.ByteHandler.ByteBuffer;

public class PacketForLobbyPlayersList extends Packet {
    private String nameOfPlayer;

    public PacketForLobbyPlayersList() {
        this(null);
    }
    public PacketForLobbyPlayersList(String nameOfPlayer) {
        this.nameOfPlayer = nameOfPlayer;
    }


    // read toEncodePacket and override this packet attributs
    @Override
    public void read(ByteBuffer inPacket){
        
    }

    // write into toEncodePacket byte of this packet attributs
    @Override
    public void write(ByteBuffer outPacket) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'write'");
    }
    @Override
    public int getPacketId() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getPacketId'");
    }
    @Override
    public List<Object> unpackPacket() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'unpackPacket'");
    }
    
    
}
