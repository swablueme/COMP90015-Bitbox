package unimelb.bitbox;

import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.util.logging.Logger;
import unimelb.bitbox.util.Document;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.net.DatagramPacket;
import java.net.InetAddress;
import unimelb.bitbox.util.HostPort;

public class udpSocket extends baseSocket {

    private static Logger log = Logger.getLogger(Peer.class.getName());
    public static DatagramSocket clientSock = null;
    private Integer handshakePort = null;
    private InetAddress handshakeHost = null;

    public static void setSocket(String port) {
        try {
            clientSock = new DatagramSocket(Integer.parseInt(port));
        } catch (Exception e) {
            exceptionHandler.handleException(e);
        }
    }

    udpSocket(String host, Integer port) {
        super("UDP socket");
        this.handshakePort = port;
        try {
            this.handshakeHost = InetAddress.getByName(host);
            //clientSock.setSoTimeout(1000);
        } catch (Exception e) {
            exceptionHandler.handleException(e);
        }
    }

    @Override
    public void setHostPort(InetAddress host, Integer port) {
        handshakePort = port;
        handshakeHost = host;
    }
    
    @Override
    public udpSocket clone() {
        return new udpSocket(this.handshakeHost.getHostAddress(), this.handshakePort);
    }

//this method writes a message to our socket
    @Override
    public synchronized void write(String message) {
        try {
            byte[] buffer = message.getBytes();
            Document toRecord = Document.parse(message);
            determineNeedsResending.addMessage(toHostport(), toRecord);
            //if (super.connRequestServerPort == null && super.connRequestHost == null) {
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, handshakeHost, this.handshakePort);
            //}
            clientSock.send(packet);

            System.out.println("WROTE:"+toRecord);
            prettyPrinter.print(message);
        } catch (Exception e) {
            System.out.println("failed to write message");
            prettyPrinter.print(message);
            exceptionHandler.handleException(e);
        }
    }

    public void setCONNECTION_REQUESTdetails(Document handshakeDetails) {
        super.setCONNECTION_REQUESTdetails(handshakeDetails);
    }

    public String toString() {
        return toHostport();
    }

    @Override
    public String toHostport() {
        return (this.handshakeHost.toString() + ":" + this.handshakePort);
    }

    //gets the port that the client initially either sent or received in 
    //the handshake request or response
    public Integer getconnRequestServerPort() {
        return super.connRequestServerPort;
    }

    //gets the host/name thingo that the client initially either sent or 
    //received in the handshake request or response
    public String connRequestHost() {
        return super.connRequestHost;
    }

    @Override
    public void close() {
        super.socket = clientSock;
        super.close();
    }

}
