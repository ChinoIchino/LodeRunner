package io.github.Chino.LodeRunner.GameInterface.LanConnection.Packets;

import io.github.Chino.LodeRunner.GameInterface.LanConnection.Packets.ByteHandler.ByteBuffer;
import io.github.Chino.LodeRunner.GameInterface.LanConnection.Packets.PacketTypes.Packet;
import io.github.Chino.LodeRunner.GameInterface.LanConnection.Packets.PacketTypes.PacketForLobbyAllPlayerList;
import io.github.Chino.LodeRunner.GameInterface.LanConnection.Packets.PacketTypes.PacketForLobbyChat;
import io.github.Chino.LodeRunner.GameInterface.LanConnection.Packets.PacketTypes.PacketForLobbyPlayersList;
import io.github.Chino.LodeRunner.GameInterface.LanConnection.Packets.PacketTypes.PacketForPlayerListLeave;

public class PacketDecoder {
    public Packet decodeStream(ByteBuffer bytes){
        bytes.resetCursor();
        int typeOfPacket = bytes.readInt();
        Packet packetToReturn;

        switch (typeOfPacket) {
            // Player list: All players in lobby
            case 1:
                packetToReturn = new PacketForLobbyAllPlayerList();
                packetToReturn.write(bytes);
                return packetToReturn;
            // Player list: A player join
            case 2:
                packetToReturn = new PacketForLobbyPlayersList();
                packetToReturn.write(bytes);
                return packetToReturn;
            // Player list: A player leave
            case 3:
                packetToReturn = new PacketForPlayerListLeave();
                packetToReturn.write(bytes);
                return packetToReturn;
            // Chat packet: Multiple messages
            case 4:
                packetToReturn = new PacketForLobbyChat();
                packetToReturn.write(bytes);
                return packetToReturn;
            // Chat packet: Single message
            case 5:
                packetToReturn = new PacketForLobbyChat();
                packetToReturn.write(bytes);
                return packetToReturn;
            default:
                throw new AssertionError();
        }
    }
}
