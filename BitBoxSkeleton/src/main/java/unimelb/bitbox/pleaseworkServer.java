package unimelb.bitbox;

import java.io.BufferedReader;
import java.net.ServerSocket;
import java.net.Socket;
import unimelb.bitbox.util.Document;

public class pleaseworkServer implements Runnable {

    Integer myport = null;
    String myhost = null;
    
    pleaseworkServer(String myhost, Integer myport) {
        this.myport = myport;
        this.myhost = myhost;
    }

    public void Servermain() {
        try {
            ServerSocket serverSocket = new ServerSocket(myport);

            while (true) {
                Socket client = serverSocket.accept();       
                clientSocket myclient = new clientSocket(client);
                new Thread(new pleaseworkClient(myclient, this.myhost, this.myport)).start();
                
            }
        } catch (Exception e) {
            exceptionHandler.handleException(e);
        }

    }

    @Override
    public void run() {
        System.out.println("starting server run");
        Servermain();
    }

}
