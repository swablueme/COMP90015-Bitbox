package unimelb.bitbox;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.HashMap;
import java.util.logging.Logger;
import unimelb.bitbox.util.Document;

public class baseSocket<T> {

    //todo: make getters rather than using public variables
    public String type = null;
    protected HashMap bufferedStreams = null;
    private static Logger log = Logger.getLogger(Peer.class.getName());
    protected static int clientCount = 0;
    protected Integer connRequestServerPort = null;
    protected String connRequestHost = null;
    //can hold a socket
    AutoCloseable socket = null;

    baseSocket(String type) {
        //saves what type of socket it is if read from config or made from server accepts
        this.type = type;
        //log.info("Initialising base socket");
    }

    public void setCONNECTION_REQUESTdetails(Document handshakeDetails) {
        if (type.equals("client from config")) {
            clientCount++;
        }
        Document hostport = (Document) handshakeDetails.get("hostPort");
        this.connRequestServerPort = Integer.parseInt(hostport.get("port").toString());
        this.connRequestHost = (String) ((Document) handshakeDetails.get("hostPort")).getString("host");

    }

    public Integer getconnRequestServerPort() {
        return this.connRequestServerPort;
    }

    public String connRequestHost() {
        return this.connRequestHost;
    }

    //returns the inputstream of the socket
    public T getBufferedInputStream() {
        return (T) bufferedStreams.get("inputStream");
    }

    //returns the output stream of the socket
    public T getBufferedOutputStream() {
        return (T) bufferedStreams.get("outputStream");
    }

    //makes a map storing the buffered streams
    public <T> HashMap<String, T> createBufferedStreams(Socket socket) {
        BufferedReader in = null;
        BufferedWriter out = null;
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
            out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"));
        } catch (Exception e) {
            exceptionHandler.handleException(e);
        }
        HashMap<String, T> bufferedStreams = new HashMap();
        bufferedStreams.put("inputStream", (T) in);
        bufferedStreams.put("outputStream", (T) out);
        return bufferedStreams;
    }

    //close method that should be able to close a ServerSocket or a regular client Socket
    public <T extends AutoCloseable> void close() {
        System.out.println("trying to close socket");
        if (socket != null) {
            try {
                socket.close();
            } catch (Exception e) {
                exceptionHandler.handleException(e);
            } finally {
                log.info("Closing socket");
            }
        }
    }

    public <T extends AutoCloseable> void write(String message) {

    }

    public <T extends AutoCloseable> String toHostport() {
        return "";
    }

    public <T extends AutoCloseable> void setHostPort(InetAddress host, Integer port) {
    }

}
