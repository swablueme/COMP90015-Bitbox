package unimelb.bitbox;

import java.io.BufferedReader;
import java.io.BufferedWriter;

public class Client implements Runnable {

    sockets myclient = null;
    BufferedReader in = null;
    BufferedWriter out = null;

    Client(sockets myclient) {
        this.myclient = myclient;
        this.in = (BufferedReader) myclient.bufferedStreams.get("inputStream");
        this.out = (BufferedWriter) myclient.bufferedStreams.get("outputStream");
        System.out.println("starting client------------");
        System.out.println("myclient:(this server's)" + myclient);
    }

    public void startClient() {
        while (true) {
            try {
                String received = this.in.readLine();
                if (received != null) {
                    System.out.println("received message: " + received);
                    if (received.contains("Port request")) {
                        System.out.println("writing!");
                        try {
                            out.write("Port:" + Integer.toString(myclient.getServerPort()) + "\n");
                            out.flush();
                        } catch (Exception e) {
                            exceptionHandler.handleException(e);

                        }
                    }
                    if (received.contains("Port:")) {
                        System.out.println("got a port message");
                        Integer port = Integer.parseInt(received.split(":")[1]);
                        if (!Server.knownServerPorts.contains(port) && port != myclient.getServerPort()) {
                            Server.modifyknownServerPorts(port);
                            sockets anotherclient = new sockets("client", "localhost", port);
                            Thread newClientThread = new Thread(new Client(anotherclient));
                        }

                    }
                }
            } catch (Exception e) {
                exceptionHandler.handleException(e);
            }
        }
    }

    @Override   
    public void run() {
        startClient();
    }
}
