package io.github.Chino.LodeRunner.GameInterface.LanConnection.Packets.PacketTypes;

import java.util.ArrayList;
import java.util.List;

import io.github.Chino.LodeRunner.GameInterface.LanConnection.Packets.ByteHandler.ByteBuffer;

public class PacketForNextLevel extends Packet{
    private char[][] map;

    public PacketForNextLevel(char[][] map) {
        this.map = map;
    }
    public PacketForNextLevel() {
        this(null);
    }


    @Override
    public void read(ByteBuffer inPacket) {
        inPacket.resetCursor();

        // Write the packet id
        inPacket.writeInt(this.getPacketId());
            
        inPacket.writeInt(this.map.length);
        inPacket.writeInt(this.map[0].length);

        // Write the currentMap matrix
        for (int y = 0; y < this.map.length; y++) {
            for (int x = 0; x < this.map[y].length; x++) {
                inPacket.writeChar(this.map[y][x]);
            }
        }
    }

    @Override
    public void write(ByteBuffer outPacket) {
        outPacket.resetCursor();

        // Skip the packet id
        outPacket.readInt();

        this.map = new char[outPacket.readInt()][outPacket.readInt()];

        for (int y = 0; y < this.map.length; y++) {
            for (int x = 0; x < this.map[y].length; x++) {
                this.map[y][x] = outPacket.readChar();
            }
        }
    }

    @Override
    public int getPacketId() {
        return 10;
    }

    @Override
    public List<Object> unpackPacket() {
        List<Object> toReturn = new ArrayList<>();

        toReturn.add(map);

        return toReturn;
    }

    @Override
    public String toString() {
        return "PacketForNextLevel\n   -Is map loaded: " + (this.map != null);
    }

}
