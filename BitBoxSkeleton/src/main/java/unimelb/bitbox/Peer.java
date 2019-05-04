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
import unimelb.bitbox.util.HostPort;

import java.util.*;

public class Peer {

    private static Logger log = Logger.getLogger(Peer.class.getName());

    public static void main(String[] args) throws IOException, NumberFormatException, NoSuchAlgorithmException {

        //loads up your file manager on the regular instance
        ServerMain instance = new ServerMain();
        //load configs from the configuration file
        String port = Configuration.getConfiguration().get("port");
        String host =  Configuration.getConfiguration().get("advertisedName");
        String peers = Configuration.getConfiguration().get("peers");

        ArrayList<pleaseworkClient> attemptedtoconnectclients= new ArrayList<>();
        //split all the peers which are seperated by commas from the config file
        String[] mypeers = peers.split(",");

        //add them to the queue
        for (String peer:mypeers){
            HostPort hostPort = new HostPort(peer);
            clientSocket myClient = null;

            try {
                //for each client attempt to create a socket and thread
                //if this fails it's because the client is offline
                if (!(visited.getList()).contains(hostPort)) {
                    myClient = new clientSocket(hostPort.host, hostPort.port);
                    pleaseworkClient myClientinstance = new pleaseworkClient(myClient, host, Integer.parseInt(port));
                    visited.addElement(hostPort);
                    System.out.println("visited list: "+visited.getList());
                    attemptedtoconnectclients.add(myClientinstance);
                    new Thread(myClientinstance).start();
                }
                //wait some time for the CONNECTION_REFUSED to be added to the peerFinding list
            } catch (Exception e) {
                exceptionHandler.handleException(e);
                continue;
            }
        }

        // ==============================
        // Create a server thread
        // ==============================
        System.out.println("THESE ARE THE CURRENT PROPERTIES OF THE SERVER: "+ Configuration.getConfiguration());
        new Thread(new pleaseworkServer(host, Integer.parseInt(port))).start();
        new Thread(new generatePeriodicSyncEvents()).start();
        
        /* infinite loop that checks if we have found a peer
        while(true) {
            for (pleaseworkClient client:attemptedtoconnectclients) {
                System.out.println(client.foundPeer);
            }
            
        }
        */
        
                
                
    }
}
