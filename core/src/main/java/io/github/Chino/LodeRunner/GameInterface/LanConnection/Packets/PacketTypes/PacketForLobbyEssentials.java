package io.github.Chino.LodeRunner.GameInterface.LanConnection.Packets.PacketTypes;

import java.util.ArrayList;
import java.util.List;

import io.github.Chino.LodeRunner.GameInterface.LanConnection.Packets.ByteHandler.ByteBuffer;


// Changed this packet from PacketForLobbyAllPlayerList into PacketForLobbyEssentials
// Because it will contain also the game mode, so it contain all the essentials
public class PacketForLobbyEssentials extends Packet{
    private boolean isVersus;
    private char[][] firstMap = null;
    private ArrayList<String> allPlayers = new ArrayList<>();

    @Override
    public void read(ByteBuffer toEncode) {
        throw new UnsupportedOperationException("Unimplemented method 'read'");
    }

    @Override
    public void write(ByteBuffer toDecode) {
        // Just in case reset the cursor to the start
        toDecode.resetCursor();
        // Skip the id of the packet
        toDecode.readInt();

        // Verify the game mode // Versus = 1 / Coop = 0
        int gameMode = toDecode.readInt();
        this.isVersus = gameMode == 1;


        
        // Decode the players currently in the lobby
        // Get the size of the list
        int playerListSize = toDecode.readInt();
        
        int currentNameSize;
        for (int i = 0; i < playerListSize; i++) {
            currentNameSize = toDecode.readInt();
            this.allPlayers.add(toDecode.readString(currentNameSize));
        }

        // Decode the first level
        this.firstMap = new char[toDecode.readInt()][toDecode.readInt()];
        for (int y = 0; y < this.firstMap.length; y++) {
            for (int x = 0; x < this.firstMap[y].length; x++) {
                this.firstMap[y][x] = toDecode.readChar();
            }
        }
    }

    @Override
    public int getPacketId() {
        return 1;
    }

    @Override
    public List<Object> unpackPacket() {
        List<Object> toReturn = new ArrayList<>();

        toReturn.add(isVersus);

        toReturn.add(this.firstMap);

        // toReturn.add(this.allPlayers.size());
        for (String name : this.allPlayers) {
            toReturn.add(name);
        }


        return toReturn;
    }

    @Override
    public String toString() {
        String toReturn = "PacketForLobbyEssentials:\n    -IsVersus = " + this.isVersus + "\nPlayer list:";

        toReturn += "\n   -Is map loaded: " + (this.firstMap != null);

        for (String name : this.allPlayers) {
            toReturn += "\n    -" + name;
        }
        return toReturn;
    }
}
