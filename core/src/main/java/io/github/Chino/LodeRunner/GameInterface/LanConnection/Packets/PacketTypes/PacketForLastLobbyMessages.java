package io.github.Chino.LodeRunner.GameInterface.LanConnection.Packets.PacketTypes;

import java.util.ArrayList;
import java.util.List;

import io.github.Chino.LodeRunner.GameInterface.LanConnection.Packets.ByteHandler.ByteBuffer;

public class PacketForLastLobbyMessages extends Packet{
    private ArrayList<String> allUsernames = new ArrayList<>();
    private ArrayList<String> allMessages = new ArrayList<>();

    @Override
    public void read(ByteBuffer inPacket) {
        inPacket.resetCursor();

        inPacket.writeInt(getPacketId());

        inPacket.writeInt(this.allUsernames.size());
    
        for (int i = 0; i < this.allUsernames.size(); i++) {
            inPacket.writeInt(this.allUsernames.get(i).length());
            inPacket.writeString(this.allUsernames.get(i));

            inPacket.writeInt(this.allMessages.get(i).length());
            inPacket.writeString(this.allMessages.get(i));
        }
    }

    @Override
    public void write(ByteBuffer outPacket) {
        this.allUsernames.clear();
        this.allMessages.clear();

        outPacket.resetCursor();
        outPacket.readInt();
        
        int ammountOfMessages = outPacket.readInt();
        int sizeOfUsername;
        int sizeOfMessage;
        for(int i = 0; i < ammountOfMessages; i++){
            sizeOfUsername = outPacket.readInt();
            this.allUsernames.add(outPacket.readString(sizeOfUsername));

            sizeOfMessage = outPacket.readInt();
            this.allMessages.add(outPacket.readString(sizeOfMessage));
        }
    }

    @Override
    public int getPacketId() {
        return 4;
    }

    @Override
    public List<Object> unpackPacket() {
        List<Object> list = new ArrayList<>();

        for (int i = 0; i < this.allUsernames.size(); i++) {
            list.add(this.allUsernames.get(i));
            list.add(this.allMessages.get(i));    
        }

        return list;
    }

    @Override
    public String toString() {
        String toReturn = "PacketForLastLobbyMessages:";

        for (int i = 0; i < this.allUsernames.size(); i++) {
            toReturn += "\n    -Username: " + this.allUsernames.get(i) + " Message: " + this.allMessages.get(i);
        }

        return toReturn;
    }
    
}
