package unimelb.bitbox;

import java.io.IOException;
import java.net.*;

public class UDPClient
{
    private int port;
    private InetAddress address;
    private int bufferSize;
    private DatagramSocket datagramSocket;

    /**
     * Constructor
     *
     * @param port        is the port number used by a datagramPacket to send a packet to a server
     * @param addressName is the address used by a datagramPacket to send a packet to a server
     * @param bufferSize  is the size of the buffer that the UDPClient will use to receive an incoming message
     */
    public UDPClient(int port, String addressName, int bufferSize)
    {
        this.port = port;
        this.bufferSize = bufferSize;
        try
        {
            // InetAddress.getByName() throws an UnknownHostException if addressName is invalid
            this.address = InetAddress.getByName(addressName);
            this.datagramSocket = new DatagramSocket();
        }
        catch (UnknownHostException e)
        {
            System.out.println("UnknownHostException: " + e.getMessage());
        }
        catch (SocketException e)
        {
            System.out.println("SocketException: " + e.getMessage());
        }
    }

    /**
     * send() sends a datagramPacket to a server
     * Note that send() in UDPClient differs to reply() in UDPServer. send() requires a specified port and
     * address of the server to send the packet to the server.
     * @param sendString is the string client sends to a server
     */
    public void send(String sendString)
    {
        try {
            // Convert string message into a byte array
            byte[] buffer = sendString.getBytes();
            // Create a datagram packet
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, address, port);

            // DatagramSocket.send() sends the packet to the assignment address and port and throws an IOException if
            // it fails
            datagramSocket.send(packet);
            System.out.println("Message sent on port " + port + " : " + new String(buffer));
        }
        catch (IOException e)
        {
            System.out.println("IOException: " + e.getMessage());
        }
    }

    /**
     * receiveReply() blocks program to receive replied message from server
     * Note that receiveReply() in UDPClient differs to listen() in UDPServer because a port number is not required to be
     * specified to the datagramSocket. datagramSocket already knows the port and address gvein the packet sent prior
     * to receiving a reply.
     * @return receivedString is a string replied by a server
     */
    public String receiveReply()
    {
        String receivedString = null;
        try {
            // Create buffer of fixed size to receive incoming message
            byte[] buffer = new byte[bufferSize];
            // Create a datagram packet
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

            // DatagramSocket.receive() receives the packet to the assignment address and port
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
}
