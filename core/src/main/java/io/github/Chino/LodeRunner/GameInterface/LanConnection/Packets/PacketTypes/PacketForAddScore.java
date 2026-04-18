package io.github.Chino.LodeRunner.GameInterface.LanConnection.Packets.PacketTypes;

import java.util.ArrayList;
import java.util.List;

import io.github.Chino.LodeRunner.GameInterface.LanConnection.Packets.ByteHandler.ByteBuffer;

public class PacketForAddScore extends Packet{
    private int valueToAdd;
    private int yIndex;
    private int xIndex;
    private int playerId;
    private int gameMode;

    public PacketForAddScore(int gameMode,int valueToAdd, int yIndex, int xIndex,int playerId) {
        this.gameMode = gameMode;
        this.valueToAdd = valueToAdd;
        this.yIndex = yIndex;
        this.xIndex = xIndex;
        this.playerId = playerId;
    }
    public PacketForAddScore(int valueToAdd, int yIndex, int xIndex) {
        this.valueToAdd = valueToAdd;
        this.yIndex = yIndex;
        this.xIndex = xIndex;
        this.playerId = 0;
    }
    public PacketForAddScore() {
        this(0,0, -1, -1,0);
    }


    @Override
    public void read(ByteBuffer inPacket) {
        inPacket.resetCursor();

        inPacket.writeInt(this.getPacketId());

        inPacket.writeInt(this.gameMode);

        inPacket.writeInt(this.valueToAdd);

        inPacket.writeInt(this.yIndex);
        inPacket.writeInt(this.xIndex);
        inPacket.writeInt(this.playerId);
    }

    @Override
    public void write(ByteBuffer outPacket) {
        outPacket.resetCursor();

        outPacket.readInt();

        this.gameMode = outPacket.readInt();

        this.valueToAdd = outPacket.readInt();

        this.yIndex = outPacket.readInt();
        this.xIndex = outPacket.readInt();
        this.playerId = outPacket.readInt();
    }

    @Override
    public int getPacketId() {
        return 8;
    }

    @Override
    public List<Object> unpackPacket() {
        List<Object> list = new ArrayList<>();

        list.add(this.gameMode);
        list.add(this.valueToAdd);
        list.add(this.yIndex);
        list.add(this.xIndex);
        list.add(this.playerId);

        return list;
    }

    @Override
    public String toString() {
        return "PacketForAddScore:\n   -Score to add : " + this.valueToAdd;
    }
    
}
