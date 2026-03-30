package io.github.Chino.LodeRunner.GameInterface.LanConnection.Packets;

import io.github.Chino.LodeRunner.GameInterface.LanConnection.Packets.ByteHandler.ByteBuffer;
import io.github.Chino.LodeRunner.GameInterface.LanConnection.Packets.PacketTypes.Packet;
import io.github.Chino.LodeRunner.GameInterface.LanConnection.Packets.PacketTypes.PacketForAddScore;
import io.github.Chino.LodeRunner.GameInterface.LanConnection.Packets.PacketTypes.PacketForLobbyChat;
import io.github.Chino.LodeRunner.GameInterface.LanConnection.Packets.PacketTypes.PacketForLobbyEssentials;
import io.github.Chino.LodeRunner.GameInterface.LanConnection.Packets.PacketTypes.PacketForPlayerJoin;
import io.github.Chino.LodeRunner.GameInterface.LanConnection.Packets.PacketTypes.PacketForPlayerListLeave;
import io.github.Chino.LodeRunner.GameInterface.LanConnection.Packets.PacketTypes.PacketForPlayerMovement;
import io.github.Chino.LodeRunner.GameInterface.LanConnection.Packets.PacketTypes.PseudoPacket;

public class PacketDecoder {
    public Packet decodeStream(ByteBuffer bytes){
        bytes.resetCursor();
        int typeOfPacket = bytes.readInt();
        Packet packetToReturn;

        switch (typeOfPacket) {
            // Player list: All players in lobby
            case 1:
                bytes.readInt();
                packetToReturn = new PacketForLobbyEssentials();
                packetToReturn.write(bytes);
                return packetToReturn;
            // Player list: A player join
            case 2:
                packetToReturn = new PacketForPlayerJoin();
                packetToReturn.write(bytes);
                return packetToReturn;
            // Player list: A player leave
            case 3:
                packetToReturn = new PacketForPlayerListLeave();
                packetToReturn.write(bytes);
                return packetToReturn;
            // Pseudo packet: Host quit the lobby
            case 4:
                packetToReturn = new PseudoPacket(4);
                packetToReturn.write(bytes);
                return packetToReturn;
            // Chat packet: Single message
            case 5:
                packetToReturn = new PacketForLobbyChat();
                packetToReturn.write(bytes);
                return packetToReturn;
            // Pseudo packet: Host started the game
            case 6:
                return new PseudoPacket(6);
            // Game packet: A player has moved
            case 7:
                packetToReturn = new PacketForPlayerMovement();
                packetToReturn.write(bytes);
                return packetToReturn;
            // Game packet: Score need to change
            case 8:
                packetToReturn = new PacketForAddScore();
                packetToReturn.write(bytes);
                return packetToReturn;
            // Game packet: Next level entrance open
            case 9:
                return new PseudoPacket(9);
            // Game packet: Send to next level
            case 10:
                return new PseudoPacket(10);
                

            default:
                System.out.println(
                    "\nError: GameInterface/LanConnection/Packets/PacketDecoder.java : Got the packet with the id "
                    + typeOfPacket + ". This packet type doesn't exist in the switch case."
                );
                throw new AssertionError();
        }
    }
}
