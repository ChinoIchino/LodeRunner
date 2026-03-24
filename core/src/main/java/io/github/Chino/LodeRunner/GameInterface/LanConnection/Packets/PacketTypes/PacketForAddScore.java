package io.github.Chino.LodeRunner.GameInterface.LanConnection.Packets.PacketTypes;

import java.util.ArrayList;
import java.util.List;

import io.github.Chino.LodeRunner.GameInterface.LanConnection.Packets.ByteHandler.ByteBuffer;

public class PacketForAddScore extends Packet{
    private int valueToAdd;


    @Override
    public void read(ByteBuffer inPacket) {
        inPacket.resetCursor();

        inPacket.writeInt(this.getPacketId());

        inPacket.writeInt(this.valueToAdd);
    }

    @Override
    public void write(ByteBuffer outPacket) {
        outPacket.resetCursor();

        outPacket.readInt();

        this.valueToAdd = outPacket.readInt();
    }

    @Override
    public int getPacketId() {
        return 8;
    }

    @Override
    public List<Object> unpackPacket() {
        List<Object> list = new ArrayList<>();

        list.add(this.valueToAdd);

        return list;
    }

    @Override
    public String toString() {
        return "PacketForAddScore:\n   -Score to add : " + this.valueToAdd;
    }
    
}
