package unimelb.bitbox;

import java.net.ConnectException;
import java.net.Socket;
import java.net.InetSocketAddress;
import java.util.logging.Logger;

import org.apache.maven.wagon.ConnectionException;
import unimelb.bitbox.util.Document;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.net.SocketException;

public class clientSocket extends baseSocket {

    private static Logger log = Logger.getLogger(Peer.class.getName());

    private Socket clientSock = null;
    private String temporaryHost = null;
    private Integer temporaryPort = null;

    //create new client from configuration file
    clientSocket(String host, Integer port) {
        super("client from config");
        try {
            clientSock = new Socket(host, port);
            clientSock.setSoTimeout(2000);
            clientSock.setSendBufferSize(3000000);
            this.temporaryHost = host;
            this.temporaryPort = port;
            System.out.println("stats on the newly created server: "+this.temporaryHost+this.temporaryPort);
            super.bufferedStreams = super.createBufferedStreams(this.clientSock);
        } catch (Exception e) {
            exceptionHandler.handleException(e);
        }

        //log.info("made a new client from configuration file");
    }

    //create a new client from the accept
    clientSocket(Socket clientSocket) {
        super("client from server");
        super.clientCount += 1;
        this.clientSock = clientSocket;
        super.bufferedStreams = super.createBufferedStreams(this.clientSock);
        //log.info("made a new client from accept");
    }

    //this method writes a message to our socket
    @Override
    public synchronized void write(String message) {
        BufferedWriter out = (BufferedWriter) super.getBufferedOutputStream();
        try {

            //message.substring(0, 20);
            System.out.println(message.length());
            out.write(message);
            out.write("\n");
            out.flush();
            System.out.println("wrote");
            prettyPrinter.print(message);
        } catch (SocketException e) {
            
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
        
        String details = "client number: " + this.clientCount + "\n"
                + "remote port: " + this.connRequestServerPort + "\n"
                + "remote hostname: " + this.connRequestHost;
        return details;
    }

    public String toNewString() {
        return this.connRequestHost + ":"+ this.connRequestServerPort;
    }
    
    @Override
    public String toHostport() {

        return (clientSock.getRemoteSocketAddress().toString().split("/")[0] + ":" + clientSock.getPort());
    }
    
    public String getTemporaryHost() {
        return this.temporaryHost;
    }
    
     public Integer getTemporaryPort() {
        return this.temporaryPort;
    }

    /*
     //gets the port that the client initially either sent or received in 
     //the handshake request or response
     public Integer getconnRequestServerPort() {
     return this.connRequestServerPort;
     }
     //gets the host/name thingo that the client initially either sent or 
     //received in the handshake request or response
     public String connRequestHost() {
     return this.connRequestHost;
     }
     */
    @Override
    public void close() {
        super.socket = clientSock;
        super.close();
    }

    //completely untested
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof clientSocket) {
            clientSocket other = (clientSocket) obj;
            
            if (this.getconnRequestServerPort() == null && this.connRequestHost() == null
                    && other.getconnRequestServerPort() != null && other.connRequestHost() != null) {
                if ((this.getTemporaryPort() == other.getconnRequestServerPort())
                        && (this.getTemporaryHost()).equals(other.connRequestHost())) {
                    System.out.println("THE OBJECTS WERE EQUAL");
                    return true;
                }
            }
            else if ((this.getconnRequestServerPort() == other.getconnRequestServerPort())
                        && (this.connRequestHost().equals(other.connRequestHost()))) {
                    return true;
                }
                return false;
            }
            return false;
        }

    }
