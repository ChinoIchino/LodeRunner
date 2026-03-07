package io.github.Chino.LodeRunner.GameInterface.LanConnection.Packets;

import io.github.Chino.LodeRunner.GameInterface.LanConnection.Packets.ByteHandler.ByteBuffer;
import io.github.Chino.LodeRunner.GameInterface.LanConnection.Packets.PacketTypes.Packet;
import io.github.Chino.LodeRunner.GameInterface.LanConnection.Packets.PacketTypes.PacketForLobbyAllPlayerList;
import io.github.Chino.LodeRunner.GameInterface.LanConnection.Packets.PacketTypes.PacketForLobbyChat;
import io.github.Chino.LodeRunner.GameInterface.LanConnection.Packets.PacketTypes.PacketForLobbyPlayersList;

public class PacketDecoder {
    public Packet decodeStream(ByteBuffer bytes){
        bytes.resetCursor();
        int typeOfPacket = bytes.readInt();
        Packet packetToReturn;

        switch (typeOfPacket) {
            // All player List in lobby packet
            case 1:
                packetToReturn = new PacketForLobbyAllPlayerList();
                packetToReturn.write(bytes);
                return packetToReturn;
            // Player list in lobby packet
            case 2:
                packetToReturn = new PacketForLobbyPlayersList();
                packetToReturn.write(bytes);
                return packetToReturn;
            // Chat packet
            case 3:
                packetToReturn = new PacketForLobbyChat();
                packetToReturn.write(bytes);
                return packetToReturn;
            default:
                throw new AssertionError();
        }
    }
}
