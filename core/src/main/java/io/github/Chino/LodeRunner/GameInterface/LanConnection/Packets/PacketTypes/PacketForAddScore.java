package io.github.Chino.LodeRunner.GameInterface.LanConnection.Packets.PacketTypes;

import java.util.ArrayList;
import java.util.List;

import io.github.Chino.LodeRunner.GameInterface.LanConnection.Packets.ByteHandler.ByteBuffer;

public class PacketForAddScore extends Packet{
    private int valueToAdd;
    private int yIndex;
    private int xIndex;

    public PacketForAddScore(int valueToAdd, int yIndex, int xIndex) {
        this.valueToAdd = valueToAdd;
        this.yIndex = yIndex;
        this.xIndex = xIndex;
    }
    public PacketForAddScore() {
        this(0, -1, -1);
    }


    @Override
    public void read(ByteBuffer inPacket) {
        inPacket.resetCursor();

        inPacket.writeInt(this.getPacketId());

        inPacket.writeInt(this.valueToAdd);

        inPacket.writeInt(this.yIndex);
        inPacket.writeInt(this.xIndex);
    }

    @Override
    public void write(ByteBuffer outPacket) {
        outPacket.resetCursor();

        outPacket.readInt();

        this.valueToAdd = outPacket.readInt();

        this.yIndex = outPacket.readInt();
        this.xIndex = outPacket.readInt();
    }

    @Override
    public int getPacketId() {
        return 8;
    }

    @Override
    public List<Object> unpackPacket() {
        List<Object> list = new ArrayList<>();

        list.add(this.valueToAdd);
        list.add(this.yIndex);
        list.add(this.xIndex);

        return list;
    }

    @Override
    public String toString() {
        return "PacketForAddScore:\n   -Score to add : " + this.valueToAdd;
    }
    
}
