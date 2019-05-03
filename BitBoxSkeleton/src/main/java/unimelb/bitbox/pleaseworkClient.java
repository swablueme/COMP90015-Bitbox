package unimelb.bitbox;

import java.io.BufferedReader;

import unimelb.bitbox.util.Document;
import unimelb.bitbox.util.HostPort;

import java.util.*;

public class pleaseworkClient implements Runnable {

    clientSocket myclient = null;
    BufferedReader in = null;
    Integer myport = null;
    String myhost = null;
    public Boolean foundPeer = null;
    peerQueue peerQueue;

    pleaseworkClient(clientSocket myclient, String myhost, Integer myport) {;
        this.in = (BufferedReader) myclient.getBufferedInputStream();
        this.myclient = myclient;
        this.myport = myport;
        this.myhost = myhost;
        this.peerQueue = new peerQueue();
        //if the client was not created via client accepts from the server 
        //and instead from configuration then it is the first one to try sending
        //requests
        if (myclient.type != "client from server") {
            myclient.write(jsonMarshaller.createHANDSHAKE(this.myhost, this.myport, "HANDSHAKE_REQUEST"));
        }
    }

    pleaseworkClient(clientSocket myclient, String myhost, Integer myport,peerQueue myQueue) {;
        this.in = (BufferedReader) myclient.getBufferedInputStream();
        this.myclient = myclient;
        this.myport = myport;
        this.myhost = myhost;
        this.peerQueue = myQueue;
        //if the client was not created via client accepts from the server
        //and instead from configuration then it is the first one to try sending
        //requests
        if ((myclient.type != "client from server")
                && (peerList.isKnownPeer(myclient) != true)) {
            myclient.write(jsonMarshaller.createHANDSHAKE(this.myhost, this.myport, "HANDSHAKE_REQUEST"));
        }
    }

    public void clientMain() {
        try {
            while (true) {
                if (this.in.ready()) {
                    String received = this.in.readLine();
                    if (received != null) {
                        //displays message received
                        System.out.println("READING: ");
                        prettyPrinter.print(received);
                        Document message = Document.parse(received);
                        new Thread(readMessages(message)).start();
                        //if we were rejected or we don't like the peer :(
                        if (foundPeer == false) {
                            System.out.println("Found peer was not found, exiting from socket");
                            myclient.close();
                            return;
                        }
                    }
                    if (received == null) {
                        System.out.println("PEER CONNECTION WAS CLOSED");
                    }
                }
            }
        } catch (Exception e) {
            exceptionHandler.handleException(e);
        }
    }

    public String readMessages(Document message) {
        //if we are the client connecting to another server
        if (message.getString("command").equals("HANDSHAKE_RESPONSE")) {
            //saves the contents of message into variables
            myclient.setCONNECTION_REQUESTdetails(message);
            if (peerList.isKnownPeer(myclient) != true) {
                peerList.addKnownPeers(myclient);
                foundPeer = true;
                System.out.println("our peerlist is now: " + peerList.getPeers());
                actOnMessages.generateSyncEvents();
            }else{
                System.out.println("Oppps!Duplicate connections!");
                foundPeer = false;

            }
            //what else? do we close the socket?
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
                    actOnMessages.generateSyncEvents();
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
            this.peerQueue.add(receivedPeers);
            while(!this.peerQueue.isEmpty()) {
                try {
                    HostPort hostPort = this.peerQueue.pop();
                    clientSocket nextClient = new clientSocket(hostPort.host, hostPort.port);
                    pleaseworkClient myClientinstance = new pleaseworkClient(nextClient, this.myhost, this.myport, this.peerQueue);
                    new Thread(myClientinstance).start();
                    break;
                }catch (Exception e) {
                    exceptionHandler.handleException(e);
                    continue;
                }
            }
            /*
             for (Document Peer:receivedPeers) {
             System.out.println((String) Peer.getString("host"));
             System.out.println(Peer.get("port").toString());
             }
             System.out.println(receivedPeers);
             */
            foundPeer = false;
        } else if (message.getString("command").equals("FILE_CREATE_REQUEST")) {
            String responseMessage = actOnMessages.fileCreateResponse(message);
            myclient.write(responseMessage);
            jsonunMarshaller producedmessage = new jsonunMarshaller(Document.parse(responseMessage));
            //what do we do if unsafepathname
            System.out.println(producedmessage.getMessage());
            if (!(producedmessage.getMessage()).equals("pathname already exists")
                    && !(producedmessage.getMessage()).equals("unsafe pathname given") 
                    && !(producedmessage.getMessage()).equals("file created")) {
                String bytesRequest = actOnMessages.fileBytesRequest("FILE_CREATE_REQUEST", message);
                myclient.write(bytesRequest);
            }

            /*
             String newPath = message.getString("pathName");
             try {
             ServerMain.messageQueue.put(message);
             } catch (Exception e) {
             exceptionHandler.handleException(e);
             }
             */
        } else if (message.getString("command").equals("FILE_BYTES_REQUEST")) {
            System.out.println("got a request");
            String responseMessage = actOnMessages.fileBytesRequestResponse(message);
            myclient.write(responseMessage);
        } else if (message.getString("command").equals("FILE_BYTES_RESPONSE")) {
            String responseMessage = actOnMessages.processReceivedFile(message);
            if (!responseMessage.equals("done") && !responseMessage.equals("issue")) {
                myclient.write(responseMessage);
            }
        } else if (message.getString("command").equals("FILE_DELETE_REQUEST")) {
            String responseMessage = actOnMessages.fileDeleteResponse(message);
            myclient.write(responseMessage);
        } else if (message.getString("command").equals("DIRECTORY_CREATE_REQUEST")) {
            String responseMessage = actOnMessages.directoryCreateRequestResponse(message);
            myclient.write(responseMessage);
        } else if (message.getString("command").equals("DIRECTORY_DELETE_REQUEST")) {
            String responseMessage = actOnMessages.directoryDeleteRequestResponse(message);
            myclient.write(responseMessage);
        } else if (message.getString("command").equals("FILE_MODIFY_REQUEST")) {
            String responseMessage = actOnMessages.fileModifyRequestResponse(message);
            myclient.write(responseMessage);
            jsonunMarshaller producedmessage = new jsonunMarshaller(Document.parse(responseMessage));
            if (!(producedmessage.getMessage()).equals("file already exists with matching content")
                    && !(producedmessage.getMessage()).equals("unsafe pathname given")
                    && !(producedmessage.getMessage()).equals("pathname already exists")) {
                String bytesRequest = actOnMessages.fileBytesRequest("FILE_MODIFY_REQUEST", message);
                myclient.write(bytesRequest);
            }
        }

        return "";
    }

    @Override
    public void run() {
        System.out.println("starting client");
        clientMain();
    }

    public static class peerQueue {

        private Queue<HostPort> queue;
        private ArrayList<HostPort> visited;

        peerQueue(){
            this.queue = new LinkedList<>();
            this.visited = new ArrayList<>();
        }
        public void add(HostPort hostPort){
            if(!this.visited.contains(hostPort)) {
                this.queue.offer(hostPort);
                this.visited.add(hostPort);
            }
        }
        public void add(ArrayList<Document> peerList){
            for(Document peer:peerList) this.add(new HostPort(peer));
        }
        public void visit(HostPort hostPort){
            this.visited.add(hostPort);
        }
        public HostPort pop(){
            return this.queue.poll();
        }
        public boolean isEmpty(){
            return this.queue.isEmpty();
        }
    }
}
