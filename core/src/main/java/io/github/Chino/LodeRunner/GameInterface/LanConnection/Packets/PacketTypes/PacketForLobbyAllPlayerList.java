package io.github.Chino.LodeRunner.GameInterface.LanConnection.Packets.PacketTypes;

import java.util.ArrayList;
import java.util.List;

import io.github.Chino.LodeRunner.GameInterface.LanConnection.Packets.ByteHandler.ByteBuffer;

public class PacketForLobbyAllPlayerList extends Packet{
    private ArrayList<String> allPlayers = new ArrayList<>();

    @Override
    public void read(ByteBuffer toEncode) {
        toEncode.resetCursor();
        toEncode.writeInt(this.getPacketId());

        throw new UnsupportedOperationException("Unimplemented method 'read'");

    }

    @Override
    public void write(ByteBuffer toDecode) {
        // Just in case reset the cursor to the start
        toDecode.resetCursor();
        // Skip the id of the packet
        toDecode.readInt();
        // Get the size of the list
        int playerListSize = toDecode.readInt();

        int currentNameSize;
        for (int i = 0; i < playerListSize; i++) {
            currentNameSize = toDecode.readInt();
            allPlayers.add(toDecode.readString(currentNameSize));
        }
    }

    @Override
    public int getPacketId() {
        return 1;
    }

    @Override
    public List<Object> unpackPacket() {
        List<Object> toReturn = new ArrayList<>();

        for (String name : this.allPlayers) {
            toReturn.add(name);
        }

        return toReturn;
    }

    @Override
    public String toString() {
        String toReturn = "PacketForLobbyAllPlayerList:";

        for (String name : this.allPlayers) {
            toReturn += "\n    -" + name;
        }

        return toReturn;
    }
    
}
