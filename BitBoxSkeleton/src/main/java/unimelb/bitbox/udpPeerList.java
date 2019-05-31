package unimelb.bitbox;

import java.util.ArrayList;
import java.util.Map;
import java.util.logging.Logger;
import unimelb.bitbox.util.Configuration;

public class udpPeerList {

    private static ArrayList<udpSocket> knownPeers = new ArrayList<>();
    private static ArrayList<udpSocket> outgoing = new ArrayList<>();
    private static final Map<String, String> config = Configuration.getConfiguration();
    private static Logger log = Logger.getLogger(Peer.class.getName());

    public static synchronized String getPeers() {
        return knownPeers.toString();
    }

    public static synchronized ArrayList<udpSocket> getPeerList() {
        return (ArrayList<udpSocket>) knownPeers.clone();
    }
    //removes a peer from the peerlist
    public static synchronized Boolean removeKnownPeers(udpSocket peer) {
       return knownPeers.remove(peer);
    }
    
     //adds peers to the peerlist if it's not fulll
    public static synchronized Boolean addKnownPeers(udpSocket peer) {
        if ((knownPeers.size() - outgoing.size() + 1) <= Integer.parseInt(config.get("maximumIncommingConnections"))) {
            knownPeers.add(peer);
            if (peer.type != "client from server") {
                outgoing.add(peer);
            }
            log.info("Added peer to udppeerlist");
            return true;
        }
        log.info("Did not add peer to udppeerlist");
        return false;
    }

    //checks if peer is already in the list
    public static synchronized Boolean isKnownPeer(udpSocket peer) {
        log.info("checking if in udppeerlist");
        return knownPeers.contains(peer);
    }

    public static boolean isFull() {
        return ((knownPeers.size() - outgoing.size()) == Integer.parseInt(config.get("maximumIncommingConnections")));
    }
}
