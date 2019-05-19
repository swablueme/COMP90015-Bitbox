package unimelb.bitbox; 

import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.IOException;
import java.net.ServerSocket;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Logger;
import java.net.Socket;

import unimelb.bitbox.util.Configuration;
import unimelb.bitbox.util.Document;
import unimelb.bitbox.util.HostPort;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.security.PublicKey;

import org.bouncycastle.openssl.PEMParser;

import java.util.*;

public class Peer {

    private static Logger log = Logger.getLogger(Peer.class.getName());

    public static void main(String[] args) throws IOException, NumberFormatException, NoSuchAlgorithmException {

        //loads up your file manager on the regular instance
        ServerMain instance = new ServerMain();
        //load configs from the configuration file
        String port = Configuration.getConfiguration().get("port");
        String clientPort = Configuration.getConfiguration().get("clientPort");
        String host =  Configuration.getConfiguration().get("advertisedName");
        String peers = Configuration.getConfiguration().get("peers");
        String pubKeyConfig = Configuration.getConfiguration().get("authorized_keys");

        //Split all the public keys which are separated by commas from the config file
        ArrayList<PublicKey> PublicKeys = new ArrayList<>();
        String[] pubKeys = pubKeyConfig.split(",");
        for (String pubKey:pubKeys){

        }


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
        new Thread(() -> listenOnClient(Integer.parseInt(clientPort)));
        
        /* infinite loop that checks if we have found a peer
        while(true) {
            for (pleaseworkClient client:attemptedtoconnectclients) {
                System.out.println(client.foundPeer);
            }
            
        }
        */
    }
    public static void listenOnClient(Integer clientPort){
        try {
            ServerSocket serverSocket = new ServerSocket(clientPort);

            while (true) {
                Socket client = serverSocket.accept();
                new Thread(() -> connectToClient(client));

            }
        } catch (Exception e) {
            exceptionHandler.handleException(e);
        }

    }
    public static void connectToClient(Socket client){

        try{

            BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream(),"UTF-8"));
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(client.getOutputStream(),"UTF-8"));

            String received = null;
            if(in.ready()){ received = in.readLine();}
            System.out.println("READING: ");
            prettyPrinter.print(received);
            Document messageOne = Document.parse(received);
            if(messageOne.getString("command").equals("AUTH_REQUEST")
                    ){
                messageOne.getString("identity");

            } else {

                System.out.println("Incorrect auth request");
                return;
            }
            //TODO: Read and parse the auth request
            //TODO: Check if public key is in Config (need to parse the pub_keys in config first)
            //TODO: create an AES key
            //TODO: pad it if needed, the AES key has to be at least the length of pub_key
            //TODO: Send back auth response
            //TODO: Read and parse command
            //TODO: create response accordingly
            //TODO: encrypt the response and send back

            //Terminate the thread
            return;




        } catch (Exception e) {
            exceptionHandler.handleException(e);
        }


    }
    public static void readPublicKey (String pubKeyPem){

        PEMParser parser = new PEMParser (new StringReader(pubKeyPem));

    }
}
