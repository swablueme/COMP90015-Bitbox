//package unimelb.bitbox;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.util.ArrayList;
import java.net.Socket;

public class Server implements Runnable {

    private volatile sockets server;
    public static ArrayList<Integer> knownServerPorts = new ArrayList<>();

    public static synchronized void modifyknownServerPorts(Integer port) {
        knownServerPorts.add(port);
    }

    public void startServer() {
        System.out.println("starting server");

        knownServerPorts.add(getServer().getServerPort());
        BufferedReader in = null;
        BufferedWriter out = null;
        ArrayList<BufferedReader> knownReaders = new ArrayList<>();
        ArrayList<BufferedWriter> knownWriters = new ArrayList<>();
        while (true) {
            try {
                Socket client = this.server.serverSocket.accept();
                sockets newClient = new sockets("client", client);
                in = (BufferedReader) newClient.bufferedStreams.get("inputStream");
                out = (BufferedWriter) newClient.bufferedStreams.get("outputStream");

                try {
                    out.write("Port request" + "\n");
                    out.flush();
                } catch (Exception e) {
                    exceptionHandler.handleException(e);
                }

                for (int i = 0; i <= knownReaders.size(); i++) {
                    for (Integer port : knownServerPorts) {
                        out.write("Port:" + port + "\n");
                        out.flush();
                    }
                }
                Thread newClientThread = new Thread(new Client(newClient));
                newClientThread.start();
            } catch (Exception e) {
                exceptionHandler.handleException(e);
            }

        }
    }

    @Override
    public void run() {
        System.out.println("starting server run");
        this.server = new sockets("server");
        startServer();
    }
    

    public sockets getServer() {
        return server;
    }
}
