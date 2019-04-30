package unimelb.bitbox;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.net.Socket;
import java.net.ServerSocket;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import unimelb.bitbox.util.Document;
import java.util.*;

public class pleaseworkClient implements Runnable {

    clientSocket myclient = null;
    BufferedReader in = null;
    Integer myport = null;
    String myhost = null;
    public Boolean foundPeer = null;

    pleaseworkClient(clientSocket myclient, String myhost, Integer myport) {;
        this.in = (BufferedReader) myclient.getBufferedInputStream();
        this.myclient = myclient;
        this.myport = myport;
        this.myhost = myhost;
        //if the client was not created via client accepts from the server 
        //and instead from configuration then it is the first one to try sending
        //requests
        if (myclient.type != "client from server") {
            myclient.write(jsonMarshaller.createHANDSHAKE(this.myhost, this.myport, "HANDSHAKE_REQUEST"));
        }
    }

    public void clientMain() {
        try {
            while (true) {
                String received = this.in.readLine();
                if (received != null) {
                    //displays message received
                    System.out.println("READING: " + received);
                    Document message = Document.parse(received);
                    readMessages(message);
                    //if we were rejected or we don't like the peer :(
                    if (foundPeer == false) {
                        System.out.println("Found peer was not found, exiting from socket");
                        myclient.close();
                        return;
                    }
                }
            }
        } catch (Exception e) {
            exceptionHandler.handleException(e);
        }
    }

    public void readMessages(Document message) {
        //if we are the client connecting to another server
        if (message.getString("command").equals("HANDSHAKE_RESPONSE")) {
            //saves the contents of message into variables
            myclient.setCONNECTION_REQUESTdetails(message);
            if (peerList.isKnownPeer(myclient) != true) {
                peerList.addKnownPeers(myclient);
                foundPeer = true;
                System.out.println("our peerlist is now: " + peerList.getPeers());
            }
            //if we are the server owo
        } else if (message.getString("command").equals("HANDSHAKE_REQUEST")) {
            myclient.setCONNECTION_REQUESTdetails(message);
            System.out.println("set parameters");
            //if our peer is not on the peerlist already
            if (peerList.isKnownPeer(myclient) != true) {
                System.out.println("peer is unknown");
                //try adding the peer and if it exceeds the count this will return false
                if (peerList.addKnownPeers(myclient)) {
                    System.out.println("our peerlist is now: " + peerList.getPeers());
                    myclient.write(jsonMarshaller.createHANDSHAKE(this.myhost, this.myport, "HANDSHAKE_RESPONSE"));
                    foundPeer = true;
                } //if our list is too full already
                else {
                    myclient.write(jsonMarshaller.createCONNECTION_REFUSED(peerList.getPeerList()));
                    foundPeer = false;
                }
            } //if the peer is trying to connect a SECOND TIME!
            else {
                myclient.write(jsonMarshaller.createINVALID_PROTOCOL());
                foundPeer = false;

            }
            //if we the peer got rejected
        } else if (message.getString("command").equals("CONNECTION_REFUSED")) {
            ArrayList<Document> receivedPeers = (ArrayList<Document>) message.get("peers");
            /*
             for (Document Peer:receivedPeers) {
             System.out.println((String) Peer.getString("host"));
             System.out.println(Peer.get("port").toString());
             }
             System.out.println(receivedPeers);
             */

            foundPeer = false;
        } else if (message.getString("command").equals("FILE_CREATE_REQUEST")) {
            String newPath = message.getString("pathName");
            try {
                ServerMain.messageQueue.put(message);
            } catch (Exception e) {
                exceptionHandler.handleException(e);
            }

        }
    }

    @Override
    public void run() {
        System.out.println("starting client");
        clientMain();
    }

}
