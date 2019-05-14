package unimelb.bitbox;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.UnknownHostException;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Logger;
import unimelb.bitbox.util.Configuration;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import unimelb.bitbox.util.Document;
import unimelb.bitbox.util.HostPort;
import java.net.Socket;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.BufferedReader;
import java.io.BufferedWriter;


public class Client {
    private static Logger log = Logger.getLogger(Peer.class.getName());

    public static void main(String[] args) throws IOException, NumberFormatException, NoSuchAlgorithmException{

        String command = null;
        HostPort server = null;
        HostPort peer;

        //TODO:fetch the identity from somewhere
        String identity = "aaron@krusty";//placeholder
        //TODO: fetch the private key and parse it

        //Object that will store the command line arguments
        CmdLineArgs cmdLineArgs = new CmdLineArgs();

        //Parser of command line arguments
        CmdLineParser cmdLineParser = new CmdLineParser(cmdLineArgs);

        try{

            cmdLineParser.parseArgument(args);
            command = cmdLineArgs.getCommand();
            server = cmdLineArgs.getServer();

            //For testing purpose
            System.out.println("the command is " + command);
            System.out.println("the peer connecting to is " + server.toString());


        } catch (CmdLineException e){

            //should I use Logger here?
            System.err.println(e.getMessage());

            //print the usage to help the user understand what the arguments for
            cmdLineParser.printUsage(System.err);
        }

        //Try to connect to the server (Peers)
        Socket socket = null;
        try{
            socket = new Socket(server.host,server.port);
            System.out.println("Connection established");

            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(),"UTF-8"));
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(),"UTF-8"));

            //Ask to be authorized
            out.write(jsonMarshaller.createAUTH_REQUEST(identity) + "\n");
            out.flush();
            System.out.println("Auth request sent"); //for testing purpose

            //wait for the server response
            String auth_response_encrypted = in.readLine();
            System.out.println("Encrypted response received");

            //TODO:decrypted somehow
            String auth_response_decrypted = null;//placeholder
            Document authResponse = Document.parse(auth_response_decrypted);

            if(authResponse.getBoolean("status")){

                String encoded_key = authResponse.getString("AES128");
                System.out.println(authResponse.getString("message"));//FIXME:testing purpose

                //TODO:decode the secret key
                String key = null;//placeholder

            }else{

                //if the key is not found in the Peer, then this client is unauthorized
                System.out.println(authResponse.getString("message"));
                System.exit(0);
            }

        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }




        //FIXME:check if the command is valid. Should've also checked if the server is valid but I'm not sure where
        if(command.equals("list_peers")){


        } else if (command.equals("connect_peer")){

            //get the peer we are connecting
            peer = cmdLineArgs.getPeer();


        } else if (command.equals("disconnect_peer")){

            //get the peer we are disconnecting
            peer = cmdLineArgs.getPeer();
        } else {
            //the command is invalid and print the usage to help the user understand
            cmdLineParser.printUsage(System.err);
        }

    }
}
