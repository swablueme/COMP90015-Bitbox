package unimelb.bitbox; 

import java.net.InetAddress;
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
        //loads up your file manager on the regular instance
        ServerMain instance = new ServerMain();
        //load configs from the configuration file
        String port = Configuration.getConfiguration().get("port");
        String host =  Configuration.getConfiguration().get("advertisedName");
        String peers = Configuration.getConfiguration().get("peers");
        
       
        //split all the peers which are seperated by commas from the config file
        String[] mypeers = peers.split(",");
        for (String peer : mypeers) {
            String[] hostport = peer.split(":");
            clientSocket myClient = null;
            try {
                //for each client attempt to create a socket and thread
                //if this fails it's because the client is offline
                myClient = new clientSocket(hostport[0], Integer.parseInt(hostport[1]));                
                pleaseworkClient myClientinstance = new pleaseworkClient(myClient, host, Integer.parseInt(port));
                new Thread(myClientinstance).start();

            } catch (Exception e) {
                exceptionHandler.handleException(e);
                continue;
            }

        }
        //makes a server thread
        
        System.out.println("THESE ARE THE CURRENT PROPERTIES OF THE SERVER: "+ Configuration.getConfiguration());
        new Thread(new pleaseworkServer(host, Integer.parseInt(port))).start();
    }
}
