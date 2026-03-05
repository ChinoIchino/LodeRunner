package io.github.Chino.LodeRunner.GameInterface.LanConnection;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;

import io.github.Chino.LodeRunner.GameInterface.LanConnection.Packets.ByteHandler.ByteBuffer;
import io.github.Chino.LodeRunner.GameInterface.LanConnection.Packets.PacketDecoder;
import io.github.Chino.LodeRunner.GameInterface.LanConnection.Packets.PacketEncoder;
import io.github.Chino.LodeRunner.GameInterface.LanConnection.Packets.PacketTypes.Packet;

public class ClientSide{
    private Socket socket;

    private BufferedInputStream readStream;
    private BufferedOutputStream writeStream;

    /** Used to decode byte[] into predetermined packets */
    private PacketDecoder packetDecoder;
    /** Used to encode packets to be sent to other clients/server */
    private PacketEncoder packetEncoder;

    private ByteBuffer buffer;

    public ClientSide(Socket socket) {
        try{
            this.socket = socket;
    
            this.readStream = new BufferedInputStream(this.socket.getInputStream());
            this.writeStream = new BufferedOutputStream(this.socket.getOutputStream());

            this.packetDecoder = new PacketDecoder();
            this.packetEncoder = new PacketEncoder();

            this.buffer = new ByteBuffer(1024);

        }catch(IOException e){
            closeEverything(this.socket, this.writeStream, this.readStream);
        }
    }

    // "Event Listener" that react when ClientHandler.java broadcastPacket give out a packet
    public void listenForPackets(){
        new Thread(new Runnable() {
            @Override
            public void run(){
                Packet decodedPacket;
                int packetType;
                while(socket.isConnected()){
                //     client.writerStream.write(buffer.getBytesList());
                // client.writerStream.flush();
                    try{
                        readStream.read(buffer.bytes, 0, 1024);
                        if(buffer.readInt() != 0){
                            // System.out.println("In listen for packets got the type " + packetType);
                            buffer.resetCursor();
                            decodedPacket = packetDecoder.decodeByte(buffer);
                        }
                        buffer.flush();
                    }catch(IOException e){
                        closeEverything(socket, writeStream, readStream);
                    }
                }
            }
        }).start();
    }

    protected void closeEverything(Socket socket, BufferedOutputStream writer, BufferedInputStream reader) {
                try{
            if(socket != null){
                socket.close();
            }
            if(writer != null){
                writer.close();
            }
            if(reader != null){
                reader.close();
            }
        }catch(IOException e){}
    }

    public static void main(String[] args) throws Exception{
        Socket socket = new Socket("10.192.56.61", 5000);
        ClientSide client = new ClientSide(socket);
        client.listenForPackets();

    }
    
}