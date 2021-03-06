package unimelb.bitbox;

import java.util.*;
import unimelb.bitbox.util.Configuration;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.util.stream.*;
import java.util.logging.Logger;
import java.util.*;

//a peerlist is a list of clientSockets
public class peerList {

    //where we store peers
    private static ArrayList<clientSocket> knownPeers = new ArrayList<>();
    private static ArrayList<clientSocket> outgoing = new ArrayList<>();
    private static final Map<String, String> config = Configuration.getConfiguration();
    private static Logger log = Logger.getLogger(Peer.class.getName());
    public static ArrayList<String> nuPeerlist = null;

    //erm I guess it prints the arraylist...
    public static synchronized String getPeers() {
        return knownPeers.toString();
    }

    //gets a copy of the peerlist
    public static synchronized ArrayList<clientSocket> getPeerList() {
        return (ArrayList<clientSocket>) knownPeers.clone();
    }

    //removes a peer from the peerlist
    public static synchronized Boolean removeKnownPeers(clientSocket peer) {
        return knownPeers.remove(peer);
    }


    public static synchronized ArrayList<Object> isKnownPeer(String peer) {
        ArrayList<Object> result = new ArrayList<>();
        for (int i=0; i<knownPeers.size(); i++) {
            String mypeer = (knownPeers.get(i)).toNewString();
            if (mypeer.equals(peer)) {
                result.add(true);
                result.add(i);
                result.add(knownPeers.get(i));
                
            } else {
                result.add(false);
                result.add(-1);
                result.add("");
            }
                
            
        }
        /*
        nuPeerlist = new ArrayList<>();
        for (clientSocket mypeer : knownPeers) {
            nuPeerlist.add(mypeer.toNewString());
        }
        return nuPeerlist.contains(peer);
                */
        return result;
    }

    //adds peers to the peerlist if it's not fulll
    public static synchronized Boolean addKnownPeers(clientSocket peer) {
        if ((knownPeers.size() - outgoing.size() + 1) <= Integer.parseInt(config.get("maximumIncommingConnections"))) {
            knownPeers.add(peer);
            if (peer.type != "client from server") {
                outgoing.add(peer);
            }
            log.info("Added peer to peerlist");
            return true;
        }
        log.info("Did not add peer to peerlist");
        return false;
    }

    //checks if peer is already in the list
    public static synchronized Boolean isKnownPeer(clientSocket peer) {
        log.info("checking if in peerlist");
        return knownPeers.contains(peer);
    }

    //grabs all output streams of the peers
    public static synchronized ArrayList<BufferedWriter> getOutputStreams() {
        log.info("attempting to return a ArrayList of outputstreams");
        return knownPeers.stream()
                .map(peer -> (BufferedWriter) peer.getBufferedOutputStream())
                .collect(Collectors.toCollection(ArrayList::new));

    }

    //grabs all input streams of the peers
    public static synchronized ArrayList<BufferedReader> getInputStreams() {
        log.info("attempting to return a ArrayList of inputstreams");
        return knownPeers.stream()
                .map(peer -> (BufferedReader) peer.getBufferedInputStream())
                .collect(Collectors.toCollection(ArrayList::new));

    }

    public static boolean isFull() {
        return ((knownPeers.size() - outgoing.size()) == Integer.parseInt(config.get("maximumIncommingConnections")));
    }

}
