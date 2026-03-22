package io.github.Chino.LodeRunner.GameInterface.LanConnection.Packets.PacketTypes;

import java.util.List;

import io.github.Chino.LodeRunner.GameInterface.LanConnection.Packets.ByteHandler.ByteBuffer;

// This packet is used for pseudo packets that have only a id. This is the reason why it have empty functions.
public class PseudoPacket extends Packet{
    private final int idNumber;

    public PseudoPacket(int idNumber) {
        this.idNumber = idNumber;
    }

    @Override
    public void read(ByteBuffer inPacket) {
    }

    @Override
    public void write(ByteBuffer outPacket) {
    }

    @Override
    public int getPacketId() {
        return this.idNumber;
    }

    @Override
    public List<Object> unpackPacket() {
        return null;
    }

    @Override
    public String toString() {
        return "PseudoPacket:\n    -IdNumber: " + this.idNumber;
    }
    
}
