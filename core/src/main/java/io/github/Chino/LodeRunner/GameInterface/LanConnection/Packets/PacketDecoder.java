package io.github.Chino.LodeRunner.GameInterface.LanConnection.Packets;

import io.github.Chino.LodeRunner.GameInterface.LanConnection.Packets.ByteHandler.ByteBuffer;
import io.github.Chino.LodeRunner.GameInterface.LanConnection.Packets.PacketTypes.Packet;
import io.github.Chino.LodeRunner.GameInterface.LanConnection.Packets.PacketTypes.PacketForLobbyChat;
import io.github.Chino.LodeRunner.GameInterface.LanConnection.Packets.PacketTypes.PacketForLobbyPlayersList;

public class PacketDecoder {
    public Packet decodeByte(ByteBuffer bytes){
        int typeOfPacket = bytes.readInt();
        Packet packetToReturn;
        switch (typeOfPacket) {
            // Chat packets
            case 1:
                packetToReturn = new PacketForLobbyChat();
                packetToReturn.write(bytes);
                return packetToReturn;
            case 2:
                packetToReturn = new PacketForLobbyPlayersList();
                packetToReturn.write(bytes);
                return packetToReturn;
            default:
                throw new AssertionError();
        }
    }
}
