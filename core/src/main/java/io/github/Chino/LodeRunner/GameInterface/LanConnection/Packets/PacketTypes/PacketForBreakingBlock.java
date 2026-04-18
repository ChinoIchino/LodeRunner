package io.github.Chino.LodeRunner.GameInterface.LanConnection.Packets.PacketTypes;

import java.util.ArrayList;
import java.util.List;

import io.github.Chino.LodeRunner.GameInterface.LanConnection.Packets.ByteHandler.ByteBuffer;

public class PacketForBreakingBlock extends Packet{

    private int positionX;
    private int positionY;

    public PacketForBreakingBlock() {
        this.positionX = 0;
        this.positionY = 0;
    }
    public PacketForBreakingBlock(int posX, int posY) {
        this.positionX = posX;
        this.positionY = posY;
    }


    // read toEncodePacket and override this packet attributs
    @Override
    public void read(ByteBuffer inPacket){
        inPacket.resetCursor();

        inPacket.writeInt(this.getPacketId());

        inPacket.writeInt(this.positionX);
        inPacket.writeInt(this.positionY);
    }

    // write into toEncodePacket byte of this packet attributs
    @Override
    public void write(ByteBuffer outPacket) {
        outPacket.resetCursor();
        outPacket.readInt();


        this.positionX = outPacket.readInt();
        this.positionY = outPacket.readInt();
    }
    @Override
    public int getPacketId() {
        return 11;
    }
    @Override
    public List<Object> unpackPacket() {
        List<Object> list = new ArrayList<>();

        list.add(this.positionX);
        list.add(this.positionY);

        return list;
    }
    
    @Override
    public String toString(){
        return "PacketForPlayerMovement:\n   -Position: " + this.positionX + "x // " + this.positionY + "y";
    }
}
