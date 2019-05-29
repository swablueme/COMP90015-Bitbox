package unimelb.bitbox;

import java.io.BufferedReader;

import unimelb.bitbox.util.Document;
import unimelb.bitbox.util.HostPort;
import java.net.DatagramPacket;
import java.net.SocketException;
import java.net.InetAddress;
import java.io.*;
import org.json.JSONException;
import java.util.*;

public class pleaseworkClient<T extends baseSocket> implements Runnable {

    T myclient = null;
    BufferedReader in = null;
    Integer myport = null;
    String myhost = null;
    public Boolean foundPeer = null;
    peerQueue peerQueue;

    pleaseworkClient(T myclient, String myhost, Integer myport) {

        if (Peer.mode.equals("tcp")) {
            this.in = (BufferedReader) myclient.getBufferedInputStream();
        }
        this.myclient = myclient;
        this.myport = myport;
        this.myhost = myhost;
        this.peerQueue = new peerQueue();

        //if the client was not created via client accepts from the server 
        //and instead from configuration then it is the first one to try sending
        //requests
        if (myclient.type != "client from server") {
            myclient.write(jsonMarshaller.createHANDSHAKE(this.myhost, this.myport, "HANDSHAKE_REQUEST"));
            System.out.println("current queue is: " + this.peerQueue.toString());
            System.out.println("Connecting to: " + myclient.toHostport());
        }
    }

    pleaseworkClient(T myclient, String myhost, Integer myport, peerQueue myQueue) {
        if (Peer.mode.equals("tcp")) {
            this.in = (BufferedReader) myclient.getBufferedInputStream();
        }
        this.myclient = myclient;
        this.myport = myport;
        this.myhost = myhost;
        this.peerQueue = myQueue;

        //if the client was not created via client accepts from the server
        //and instead from configuration then it is the first one to try sending
        //requests
        if (Peer.mode.equals("tcp")) {
            if ((myclient.type != "client from server")
                    && (peerList.isKnownPeer((clientSocket) myclient) != true)) {
                myclient.write(jsonMarshaller.createHANDSHAKE(this.myhost, this.myport, "HANDSHAKE_REQUEST"));
                System.out.println("current queue is: " + this.peerQueue.toString());
                System.out.println("Connecting to: " + myclient.toHostport());
            }
        }

    }

    public void clientMain() {

        while (true) {
            try {
                if (Peer.mode.equals("tcp")) {
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
                            myclient.close();
                            return;
                        }
                    }

                } else {
                    System.out.println("trying to read UDP");
                    byte[] buffer = new byte[30000];
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

                    ((udpSocket) myclient).clientSock.receive(packet);
                    String receivedString = new String(packet.getData(), 0, packet.getLength());
                    System.out.println("READING: ");
                    try {
                        prettyPrinter.print(receivedString);
                    } catch (JSONException e) {
                        try (PrintStream out = new PrintStream(new FileOutputStream("filename.txt"))) {
                            out.print(receivedString);
                        }
                    }

                    Document message = Document.parse(receivedString);
                    System.out.println(message.toString());
                    new Thread(readMessages(message, packet.getAddress(), packet.getPort())).start();
                    System.out.println("found peer is: " + foundPeer);
                    if (foundPeer == false) {
                        System.out.println("Found peer was not found, exiting from socket");
                        myclient.close();
                        return;
                    }

                }
            } catch (SocketException e) {
                return;
            } catch (Exception e) {

                exceptionHandler.handleException(e);

            }

        }
    }

    public <T extends baseSocket> String readMessages(Document message, Object... OriginalSender) {
        InetAddress address = null;
        Integer port = null;
        if (OriginalSender.length == 2) {
            address = (InetAddress) OriginalSender[0];
            port = (Integer) OriginalSender[1];
            this.myclient.setHostPort(address, port);
        }
        //if we are the client connecting to another server
        System.out.println("parsing");
        if (message.getString("command").equals("HANDSHAKE_RESPONSE")) {
            //saves the contents of message into variables
            myclient.setCONNECTION_REQUESTdetails(message);
            if (Peer.mode.equals("tcp")) {
                if (peerList.isKnownPeer((clientSocket) myclient) != true) {
                    peerList.addKnownPeers((clientSocket) myclient);
                    foundPeer = true;
                    System.out.println("our peerlist is now: " + peerList.getPeers());
                    actOnMessages.generateSyncEvents();
                } else {
                    System.out.println("Oops!Duplicate connections!");
                    foundPeer = false;

                }
            } else {
                if (udpPeerList.isKnownPeer((udpSocket) myclient) != true) {
                    udpPeerList.addKnownPeers(((udpSocket) myclient).clone());
                    foundPeer = true;
                    System.out.println("our udp peerlist is now: " + udpPeerList.getPeers());
                    actOnMessages.generateSyncEvents();
                } else {
                    System.out.println("Oppps!Duplicate connections!");
                    foundPeer = false;
                }

            }
        } //what else? do we close the socket?
        //if we are the server owo
        else if (message.getString(
                "command").equals("HANDSHAKE_REQUEST")) {
            System.out.println("received a request");
            myclient.setCONNECTION_REQUESTdetails(message);
            System.out.println("set parameters");
            //if our peer is not on the peerlist already
            if (Peer.mode.equals("tcp")) {
                if (peerList.isKnownPeer((clientSocket) myclient) != true) {
                    System.out.println("peer is unknown");
                    //try adding the peer and if it exceeds the count this will return false
                    if (peerList.addKnownPeers((clientSocket) myclient)) {
                        System.out.println("our peerlist is now: " + peerList.getPeers());
                        myclient.write(jsonMarshaller.createHANDSHAKE(this.myhost, this.myport, "HANDSHAKE_RESPONSE"));
                        foundPeer = true;
                        actOnMessages.generateSyncEvents();
                    } //if our list is too full already
                    else {
                        myclient.write(jsonMarshaller.createCONNECTION_REFUSED(peerList.getPeerList()));
                        foundPeer = false;
                    }
                }
            }
            else if (Peer.mode.equals("udp")) {
                if (udpPeerList.isKnownPeer((udpSocket) myclient) != true) {
                    System.out.println("peer is unknown");
                    //try adding the peer and if it exceeds the count this will return false
                    if (udpPeerList.addKnownPeers(((udpSocket) myclient).clone())) {
                        System.out.println("our udp peerlist is now: " + udpPeerList.getPeers());
                        myclient.write(jsonMarshaller.createHANDSHAKE(this.myhost, this.myport, "HANDSHAKE_RESPONSE"));
                        foundPeer = true;
                        actOnMessages.generateSyncEvents();
                    } //if our list is too full already
                    else {
                        myclient.write(jsonMarshaller.createCONNECTION_REFUSED(peerList.getPeerList()));
                        foundPeer = false;
                    }
                }

            } //if the peer is trying to connect a SECOND TIME!
            else {
                myclient.write(jsonMarshaller.createINVALID_PROTOCOL());
                foundPeer = false;

            }
            //if we the peer got rejected
        } else if (message.getString(
                "command").equals("CONNECTION_REFUSED")) {
            //TODO： Determine if this connection is from command or Peer
            ArrayList<Document> receivedPeers = (ArrayList<Document>) message.get("peers");
            this.peerQueue.add(receivedPeers);
            while (!this.peerQueue.isEmpty()) {
                try {
                    HostPort hostPort = this.peerQueue.pop();
                    if (!(visited.getList()).contains(hostPort)) {
                        clientSocket nextClient = new clientSocket(hostPort.host, hostPort.port);
                        visited.addElement(hostPort);
                        System.out.println("visited list: " + visited.getList());
                        pleaseworkClient myClientinstance = new pleaseworkClient(nextClient, this.myhost, this.myport, this.peerQueue);
                        new Thread(myClientinstance).start();
                        break;
                    }

                } catch (Exception e) {
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
        } else if (message.getString(
                "command").equals("FILE_CREATE_REQUEST")) {
            String responseMessage = actOnMessages.fileCreateResponse(message);
            myclient.write(responseMessage);
            jsonunMarshaller producedmessage = new jsonunMarshaller(Document.parse(responseMessage));
            //what do we do if unsafepathname
            if ((producedmessage.getMessage()).equals("file loader ready")) {
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
        } else if (message.getString(
                "command").equals("FILE_BYTES_REQUEST")) {
            String responseMessage = actOnMessages.fileBytesRequestResponse(message);
            myclient.write(responseMessage);
        } else if (message.getString(
                "command").equals("FILE_BYTES_RESPONSE")) {
            String responseMessage = actOnMessages.processReceivedFile(message);
            if (!responseMessage.equals("done") && !responseMessage.equals("issue")) {
                myclient.write(responseMessage);
            }
        } else if (message.getString(
                "command").equals("FILE_DELETE_REQUEST")) {
            String responseMessage = actOnMessages.fileDeleteResponse(message);
            myclient.write(responseMessage);
        } else if (message.getString(
                "command").equals("DIRECTORY_CREATE_REQUEST")) {
            String responseMessage = actOnMessages.directoryCreateRequestResponse(message);
            myclient.write(responseMessage);
        } else if (message.getString(
                "command").equals("DIRECTORY_DELETE_REQUEST")) {
            String responseMessage = actOnMessages.directoryDeleteRequestResponse(message);
            myclient.write(responseMessage);
        } else if (message.getString(
                "command").equals("FILE_MODIFY_REQUEST")) {
            String responseMessage = actOnMessages.fileModifyRequestResponse(message);
            myclient.write(responseMessage);
            jsonunMarshaller producedmessage = new jsonunMarshaller(Document.parse(responseMessage));
            if ((producedmessage.getMessage()).equals("file loader ready")) {
                String bytesRequest = actOnMessages.fileBytesRequest("FILE_MODIFY_REQUEST", message);
                myclient.write(bytesRequest);
            }
        }

        System.out.println(
                "returning");

        return "";
    }

    @Override
    public void run() {
        System.out.println("starting client");
        clientMain();
    }

}
