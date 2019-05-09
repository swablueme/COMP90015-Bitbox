package unimelb.bitbox;

import java.io.IOException;
import java.net.*;

public class UDPServer
{
    private int port;
    private int bufferSize;
    private DatagramPacket packet = null;
    private DatagramSocket datagramSocket;

    /**
     * Constructor
     * @param port        is the port number that the UDPServer listens to when receiving incoming message from a
     *                    client
     * @param bufferSize  is the size of the buffer that the UDPServer will use to receive an incoming message
     */
    public UDPServer(int port, int bufferSize)
    {
        this.port = port;
        this.bufferSize = bufferSize;
        try {
            this.datagramSocket = new DatagramSocket(port);
        }
        catch (SocketException e)
        {
            System.out.println("SocketException: " + e.getMessage());
        }
    }

    /**
     * listen() blocks program and listens to a port for incoming message from a client
     * Note that listen() in UDPServer differs to receive() in UDPClient because a port number has to
     * be specified to the datagramSocket.
     * @return receivedString is a string recevied by the server from a client
     */
    public String listen()
    {
        String receivedString = null;
        try {
            // Create buffer of fixed size to receive incoming message
            byte[] buffer = new byte[bufferSize];
            // Create a datagram packet
            packet = new DatagramPacket(buffer, buffer.length);

            // DatagramSocket.receive() receives the packet to the assignment address and port
            System.out.println("Datagram Server ready to receive packet on port " + port + "...");
            datagramSocket.receive(packet);
            receivedString = new String(buffer);
            System.out.println("Message received on port " + port + " : " + receivedString);
        }

        catch (IOException e)
        {
            System.out.println("IOException: " + e.getMessage());
        }

        return receivedString;
    }

    /**
     * reply() replies a message recevied from a client
     * Note that reply() in UDPServer differs to send() in UDPClient. reply() uses the received port and address from
     * a previously received message from a client to reply the client.
     * @param replyString is the string server sends to client as a reply
     */
    public void reply(String replyString)
    {
        try {
            // Convert string message into a byte array
            byte[] buffer = replyString.getBytes();
            // Get address of sender(client) from the previously received packet
            InetAddress address = packet.getAddress();
            // Get port number of sender(client) from the previously received packet
            port = packet.getPort();
            // Create NEW replyPacket to be sent back to sender(client). This packet is not stored in the class.
            DatagramPacket replyPacket = new DatagramPacket(buffer, buffer.length, address, port);

            // DatagramSocket.send() sends the packet to the assignment address and port and throws an IOException if
            // it fails
            datagramSocket.send(replyPacket);
            System.out.println("Message sent on port " + port + " : " + new String(buffer));
        }
        catch (IOException e)
        {
            System.out.println("IOException: " + e.getMessage());
        }
    }
}
