package io.github.Chino.LodeRunner.GameInterface.LanConnection.Packets;

import io.github.Chino.LodeRunner.GameInterface.LanConnection.Packets.ByteHandler.ByteBuffer;
import io.github.Chino.LodeRunner.GameInterface.LanConnection.Packets.PacketTypes.Packet;

public class PacketEncoder{
    private ByteBuffer buffer;

    public PacketEncoder() {
        this.buffer = new ByteBuffer(1024);
    }


    public ByteBuffer encodePacket(Packet packetToEncode){
        this.buffer.clear();
        packetToEncode.read(this.buffer);
        return this.buffer;
    }
}