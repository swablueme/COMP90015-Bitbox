package unimelb.bitbox;

import java.io.BufferedWriter;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.logging.Logger;

import unimelb.bitbox.util.Configuration;
import unimelb.bitbox.util.FileSystemManager;
import unimelb.bitbox.util.FileSystemObserver;
import unimelb.bitbox.util.FileSystemManager.FileSystemEvent;
import java.util.concurrent.CompletableFuture;
import java.nio.ByteBuffer;
import java.util.Base64;
import unimelb.bitbox.util.Document;
import java.util.concurrent.*;

public class ServerMain implements FileSystemObserver {

    private static Logger log = Logger.getLogger(ServerMain.class.getName());
    protected FileSystemManager fileSystemManager;

    public static BlockingQueue<Document> messageQueue = new LinkedBlockingQueue<Document>();

    public ServerMain() throws NumberFormatException, IOException, NoSuchAlgorithmException {
        fileSystemManager = new FileSystemManager(Configuration.getConfigurationValue("path"), this);
        new actOnMessages(fileSystemManager);
    }

    @Override
    public void processFileSystemEvent(FileSystemEvent fileSystemEvent) {
        Logger log = Logger.getLogger(FileSystemManager.class.getName());
        //converts a fileSystemEvent into json ready to send
        String myjson = jsonMarshaller.fileEventToJson(fileSystemEvent);
        if (Peer.mode.equals("tcp")) {
            ArrayList<clientSocket> peeroutputstreams = peerList.getPeerList();
            if (!(peeroutputstreams.isEmpty())) {
                for (clientSocket myclient : peeroutputstreams) {
                    //sends a fileSystem event that has been triggered into the output stream
                    myclient.write(myjson);
                }
            }

        } else {
            System.out.println("writing event");
            ArrayList<udpSocket> peers= udpPeerList.getPeerList();
            System.out.println("peerlist: "+peers);
            if (!(peers.isEmpty())) {
                for (udpSocket myclient : peers) {
                    System.out.println("we are intending to write to: "+myclient.toHostport());
                    myclient.write(myjson);
                }
            }
        }
    }

}
