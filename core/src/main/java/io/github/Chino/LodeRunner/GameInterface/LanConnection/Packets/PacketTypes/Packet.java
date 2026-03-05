package io.github.Chino.LodeRunner.GameInterface.LanConnection.Packets.PacketTypes;

import java.util.List;

import io.github.Chino.LodeRunner.GameInterface.LanConnection.Packets.ByteHandler.ByteBuffer;

public abstract class Packet {
    public abstract void read(ByteBuffer inPacket);
    public abstract void write(ByteBuffer outPacket);
    
    public abstract int getPacketId();
    public abstract List<Object> unpackPacket();
}
