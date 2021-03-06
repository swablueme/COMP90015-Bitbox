package unimelb.bitbox;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.ServerSocket;
import java.util.HashMap;
import java.net.InetSocketAddress;
import java.net.BindException;

public class sockets<T> {

    private String type = null;
    private static int clientCount = 0;
    private Socket clientSocket = null;
    public static ServerSocket serverSocket = null;
    public HashMap bufferedStreams = null;

    //server
    sockets(String type, Integer port) {
        System.out.println("making server");
        this.type = type;
        try {
            serverSocket = new ServerSocket(port);
            System.out.println("server port is:" + serverSocket.getLocalPort());
        } catch (Exception e) {
            exceptionHandler.handleException(e);

        }
    }

    //create a new client to try and connect
    sockets(String type, String host, Integer port) {
        System.out.println("making custom client");
        this.type = type;
        try {
            clientSocket = new Socket(host, port);
        } catch (Exception e) {
            exceptionHandler.handleException(e);
        }
        this.bufferedStreams = createBufferedStreams(this.clientSocket);
    }

    //create a new client from the accept
    sockets(String type, Socket clientSocket) {
        System.out.println("making from accepts");
        this.type = type;
        this.clientCount += 1;
        this.clientSocket = clientSocket;
        this.bufferedStreams = createBufferedStreams(this.clientSocket);

    }

    public String getDetails() {
        if (this.type == "client") {
            String details = "client number: " + this.clientCount + "\n"
                    + "remote port: " + this.clientSocket.getPort() + "\n"
                    + "remote hostname: " + this.clientSocket.getInetAddress().getHostName()
                    + "\n" + "local Port: " + clientSocket.getLocalPort() + "\n";
            return details;
        } else {
            return "this port" + serverSocket.getLocalPort();
        }
    }

    public Integer getServerPort() {
        return serverSocket.getLocalPort();
    }

    public <T> HashMap<String, T> createBufferedStreams(Socket clientSocket) {
        BufferedReader in = null;
        BufferedWriter out = null;

        try {
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream(), "UTF-8"));
            out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream(), "UTF-8"));
        } catch (Exception e) {
            exceptionHandler.handleException(e);
        }
        HashMap<String, T> bufferedStreams = new HashMap();
        bufferedStreams.put("inputStream", (T) in);
        bufferedStreams.put("outputStream", (T) out);
        return bufferedStreams;
    }

    public <T extends AutoCloseable> void closeSocket(T socket) {
        if (socket != null) {
            try {
                socket.close();
            } catch (Exception e) {
                exceptionHandler.handleException(e);
            }
        }
    }

    @Override
    public String toString() {
        return getDetails();
    }
}
