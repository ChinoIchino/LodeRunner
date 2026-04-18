package io.github.Chino.LodeRunner.GameInterface.LanConnection.Packets.PacketTypes;

import java.util.ArrayList;
import java.util.List;

import io.github.Chino.LodeRunner.GameInterface.LanConnection.Packets.ByteHandler.ByteBuffer;

public class PacketForAIMovement extends Packet{
    private int aiId;
    private int nearestPlayerId;

    private int animationId;

    private int positionX;
    private int positionY;

    public PacketForAIMovement() {
        // -1 so it dont move any player (id's start at 0)
        this(-1, -1,-1, 0 ,0);
    }
    public PacketForAIMovement(int aiId, int nearestPlayerId, int animationId, int posX, int posY) {
        this.aiId = aiId;
        this.nearestPlayerId = nearestPlayerId;
        this.animationId = animationId;
        this.positionX = posX;
        this.positionY = posY;
    }


    // read toEncodePacket and override this packet attributs
    @Override
    public void read(ByteBuffer inPacket){
        inPacket.resetCursor();

        inPacket.writeInt(this.getPacketId());
        
        inPacket.writeInt(this.aiId);
        inPacket.writeInt(this.nearestPlayerId);
        
        
        inPacket.writeInt(this.animationId);
        
        inPacket.writeInt(this.positionX);
        inPacket.writeInt(this.positionY);
    }

    // write into toEncodePacket byte of this packet attributs
    @Override
    public void write(ByteBuffer outPacket) {
        outPacket.resetCursor();
        outPacket.readInt();

        this.aiId = outPacket.readInt();
        
        this.nearestPlayerId = outPacket.readInt();

        this.animationId = outPacket.readInt();

        this.positionX = outPacket.readInt();
        this.positionY = outPacket.readInt();
    }
    @Override
    public int getPacketId() {
        return 12;
    }
    @Override
    public List<Object> unpackPacket() {
        List<Object> list = new ArrayList<>();
        
        list.add(this.aiId);
        list.add(this.nearestPlayerId);
        list.add(this.animationId);
        list.add(this.positionX);
        list.add(this.positionY);

        return list;
    }
    
    @Override
    public String toString(){
        return "PacketForPlayerMovement:\n   - AIId :"  + this.aiId + "\n   -Player Id : " + this.nearestPlayerId + "\n   -AnimationId : " + this.animationId
            + "\n   -Position: " + this.positionX + "x // " + this.positionY + "y";
    }
}
