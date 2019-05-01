package unimelb.bitbox;

import java.net.Socket;
import java.util.logging.Logger;
import unimelb.bitbox.util.Document;
import java.io.BufferedReader;
import java.io.BufferedWriter;

public class clientSocket extends baseSocket {

    private static Logger log = Logger.getLogger(Peer.class.getName());
    
    //numbers of clients connected
    private static int clientCount = 0;
    private Socket clientSock = null;

    //values from CONNECTION_REQUEST
    private Integer connRequestServerPort = null;
    private String connRequestHost = null;

    //create new client from configuration file
    clientSocket(String host, Integer port) {
        super("client from config");
        try {
            clientSock = new Socket(host, port);

//            Kai's socket timeout exception!!!
//            Socket timeout set to 5 seconds
            socket.setSoTime(5000);

            super.bufferedStreams = super.createBufferedStreams(this.clientSock);
        }
        catch (SocketTimeOut e)
        {
//            Handle exception if socket times out

        }
        catch (Exception e) {
            exceptionHandler.handleException(e);
        }
        
        //log.info("made a new client from configuration file");
    }

    //create a new client from the accept
    clientSocket(Socket clientSocket) {
        super("client from server");
        this.clientCount += 1;
        this.clientSock = clientSocket;
        super.bufferedStreams = super.createBufferedStreams(this.clientSock);
        //log.info("made a new client from accept");
    }

    //this method writes a message to our socket
    public void write(String message) {
        BufferedWriter out = (BufferedWriter) super.getBufferedOutputStream();
        try {
            out.write(message + "\n");
            out.flush();
            System.out.println("wrote");
            prettyPrinter.print(message);
        } catch (Exception e) {
            System.out.println("failed to write message");
            prettyPrinter.print(message);
            exceptionHandler.handleException(e);
        }
    }

    public void setCONNECTION_REQUESTdetails(Document handshakeDetails) {
        if (super.type.equals("client from config")) {
            clientCount++;
        }
        Document hostport = (Document) handshakeDetails.get("hostPort");
        this.connRequestServerPort = Integer.parseInt(hostport.get("port").toString());
        this.connRequestHost = (String) ((Document) handshakeDetails.get("hostPort")).getString("host");
        //System.out.printf("set port %d, host %s \n", connRequestServerPort, connRequestHost);

    }

    public String toString() {
        String details = "client number: " + this.clientCount + "\n"
                + "remote port: " + this.connRequestServerPort + "\n"
                + "remote hostname: " + this.connRequestHost;
        return details;
    }

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
            if ((this.connRequestServerPort == other.connRequestServerPort)
                    && (this.connRequestHost == other.connRequestHost)) {
                return true;
            }
            return false;
        }
        return false;
    }
}
