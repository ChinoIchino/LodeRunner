package io.github.Chino.LodeRunner.GameInterface.LanConnection.Packets.PacketTypes;

import java.util.List;

import io.github.Chino.LodeRunner.GameInterface.LanConnection.Packets.ByteHandler.ByteBuffer;

public abstract class Packet {
    /**
     * @param inPacket ByteBuffer that will receive all the packet information
     */
    public abstract void read(ByteBuffer inPacket);
    /**
     * @param outPacket ByteBuffer that will override all the packet information
     */
    public abstract void write(ByteBuffer outPacket);
    
    public abstract int getPacketId();
    /**
     * @return A list of all the information kept by the packet in his own specific format
     */
    public abstract List<Object> unpackPacket();

    @Override
    public abstract String toString();
}
