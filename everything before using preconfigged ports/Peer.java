package unimelb.bitbox;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Logger;
import java.net.Socket;
import java.net.ConnectException;
import java.util.concurrent.CompletableFuture;
import unimelb.bitbox.util.Configuration;
import unimelb.bitbox.util.Document;
import unimelb.bitbox.util.FileSystemManager;

public class Peer {

    private static Logger log = Logger.getLogger(Peer.class.getName());

    public static void main(String[] args) throws IOException, NumberFormatException, NoSuchAlgorithmException {
        /*
    	System.setProperty("java.util.logging.SimpleFormatter.format",
                "[%1$tc] %2$s %4$s: %5$s%n");
        log.info("BitBox Peer starting...");
        Configuration.getConfiguration();
        ServerMain instance = new ServerMain();
         */


        //eg Thread t4 = new Thread(new test4(knownWriters.get(i), knownReaders.get(i), knownPeers.toString()));
        /*
        System.out.println("starting peer");
        Thread t1 = new Thread(new Server());
        t1.start();
        System.out.println("starting ");
        Thread t2 = new Thread(new Client(new sockets("client", "localhost", 4444)));
        t2.start();
         */

        //System.out.println(Configuration.getConfiguration());

        //System.out.println(jsonHandler.createINVALID_PROTOCOL());
        
        //CONVERT IMAGE TO BASE 64
        ServerMain instance = new ServerMain();
        instance.testFileWritten();

        
        

    }

}
