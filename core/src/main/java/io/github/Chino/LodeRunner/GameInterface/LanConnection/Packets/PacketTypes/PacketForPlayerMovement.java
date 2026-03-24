package io.github.Chino.LodeRunner.GameInterface.LanConnection.Packets.PacketTypes;

import java.util.ArrayList;
import java.util.List;

import io.github.Chino.LodeRunner.GameInterface.LanConnection.Packets.ByteHandler.ByteBuffer;

public class PacketForPlayerMovement extends Packet{
    private int playerId;

    private int animationId;

    private int positionX;
    private int positionY;

    public PacketForPlayerMovement() {
        // -1 so it dont move any player (id's start at 0)
        this(-1, -1, 0 ,0);
    }
    public PacketForPlayerMovement(int playerId, int animationId, int posX, int posY) {
        this.playerId = playerId;
        this.animationId = animationId;
        this.positionX = posX;
        this.positionY = posY;
    }


    // read toEncodePacket and override this packet attributs
    @Override
    public void read(ByteBuffer inPacket){
        inPacket.resetCursor();

        inPacket.writeInt(this.getPacketId());
        
        inPacket.writeInt(this.playerId);

        inPacket.writeInt(this.animationId);

        inPacket.writeInt(this.positionX);
        inPacket.writeInt(this.positionY);
    }

    // write into toEncodePacket byte of this packet attributs
    @Override
    public void write(ByteBuffer outPacket) {
        outPacket.resetCursor();
        outPacket.readInt();
        
        this.playerId = outPacket.readInt();

        this.animationId = outPacket.readInt();

        this.positionX = outPacket.readInt();
        this.positionY = outPacket.readInt();
    }
    @Override
    public int getPacketId() {
        return 7;
    }
    @Override
    public List<Object> unpackPacket() {
        List<Object> list = new ArrayList<>();
        
        list.add(this.playerId);
        list.add(this.animationId);
        list.add(this.positionX);
        list.add(this.positionY);

        return list;
    }
    
    @Override //TODO change the toString to include animationId
    public String toString(){
        return "PacketForPlayerMovement:\n   -Player Id : " + this.playerId 
            + "\n   -Position: " + this.positionX + "x // " + this.positionY + "y";
    }
}
