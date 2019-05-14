package unimelb.bitbox;

import unimelb.bitbox.util.Document;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import unimelb.bitbox.util.FileSystemManager.FileSystemEvent;
import unimelb.bitbox.util.HostPort;

public class jsonMarshaller {

    public enum Messages {
        ready("file loader ready"),
        fileDeleted("file deleted"),
        fileExistWithSameContent("file already exists with matching content"),
        directoryCreated("directory created"),
        directoryDeleted("directory deleted"),
        unsafePathname("unsafe pathname given"),
        problemCreatingFile("there was a problem creating the file"),
        problemCreatingDirectory("there was a problem creating the directory"),
        problemDeletingFile("there was a problem deleting the file"),
        problemModifyingFile("there was a problem modifying the file"),
        problemDeletingDirectory("there was a problem deleting the directory"),
        pathnameExists("pathname already exists"),
        pathnameNotExists("pathname does not exist"),
        successfulRead("successful read"),
        unsuccessfulRead("unsuccessful read"),
        fileCreated("file created"),
        publicKeyFound("public key found"),
        publicKeyNotFound("public key not found"),
        connectedToPeer("connected to peer"),
        connectionFailed("connection failed"),
        disconnectedFromPeer("disconnected from peer"),
        connectionNotActive("connection not active");

        private String value;

        public String getValue() {
            return value;
        }

        private Messages(String value) {
            this.value = value;
        }

    }

    static String fileEventToJson(FileSystemEvent Event) {
        Document jsoninprogress = new Document();
        String eventName = Event.event.toString();
        jsoninprogress.append("command", eventName + "_REQUEST");
        if ((eventName == "FILE_CREATE") || (eventName == "FILE_DELETE")
                || (eventName == "FILE_MODIFY")) {
            jsoninprogress.append("fileDescriptor", Event.fileDescriptor.toDoc());
            jsoninprogress.append("pathName", Event.pathName);
        } else if ((eventName == "DIRECTORY_CREATE") || (eventName == "DIRECTORY_DELETE")) {
            jsoninprogress.append("pathName", Event.pathName);
        }
        return jsoninprogress.toJson();
    }

    static String createHANDSHAKE(String host, Integer port, String command) {
        Document HANDSHAKE = new Document();
        HANDSHAKE.append("command", command);
        Document HostPort = new Document();
        HostPort.append("host", host);
        HostPort.append("port", port);
        HANDSHAKE.append("hostPort", HostPort);
        return HANDSHAKE.toJson();

    }

    static String createCONNECTION_REFUSED(ArrayList<clientSocket> peerList) {
        Document CONNECTION_REFUSED = new Document();
        CONNECTION_REFUSED.append("message", "connection limit reached");

        CONNECTION_REFUSED.append("command", "CONNECTION_REFUSED");
        ArrayList<Document> peerleest = new ArrayList<>();

        for (clientSocket peer : peerList) {
            Document peers = new Document();
            peers.append("host", peer.connRequestHost());
            peers.append("port", peer.getconnRequestServerPort());
            peerleest.add(peers);
        }

        CONNECTION_REFUSED.append("peers", peerleest);
        return CONNECTION_REFUSED.toJson();

    }

    static String createINVALID_PROTOCOL() {
        Document INVALID_PROTOCOL = new Document();
        INVALID_PROTOCOL.append("command", "INVALID_PROTOCOL");
        INVALID_PROTOCOL.append("message",
                "message must contain a command field as string");
        return INVALID_PROTOCOL.toJson();

    }

    static String createFILE_BYTES_REQUEST(Document file, String fileName,
            Long position, Long length) {
        Document FILE_BYTES_REQUEST = new Document();
        FILE_BYTES_REQUEST.append("command", "FILE_BYTES_REQUEST");
        FILE_BYTES_REQUEST.append("fileDescriptor", file);
        FILE_BYTES_REQUEST.append("pathName", fileName);
        FILE_BYTES_REQUEST.append("position", position);
        FILE_BYTES_REQUEST.append("length", length);
        return FILE_BYTES_REQUEST.toJson();
    }

    static String createFILE_CREATE_RESPONSE(Document file, String fileName,
            Messages message) {
        Document FILE_CREATE_RESPONSE = new Document();
        FILE_CREATE_RESPONSE.append("command", "FILE_CREATE_RESPONSE");
        FILE_CREATE_RESPONSE.append("fileDescriptor", file);
        FILE_CREATE_RESPONSE.append("pathName", fileName);
        FILE_CREATE_RESPONSE.append("message", message.getValue());
        if (message == Messages.ready) {
            FILE_CREATE_RESPONSE.append("status", true);
        } else {
            FILE_CREATE_RESPONSE.append("status", false);
        }
        return FILE_CREATE_RESPONSE.toJson();
    }

    static String createFILE_BYTES_RESPONSE(Document file, String fileName,
            Long position, Long length, Messages message,
            ByteBuffer content) {
        Document FILE_BYTES_RESPONSE = new Document();
        FILE_BYTES_RESPONSE.append("command", "FILE_BYTES_RESPONSE");
        FILE_BYTES_RESPONSE.append("fileDescriptor", file);
        FILE_BYTES_RESPONSE.append("pathName", fileName);
        FILE_BYTES_RESPONSE.append("position", position);
        FILE_BYTES_RESPONSE.append("length", length);
        FILE_BYTES_RESPONSE.append("content",
                base64Handler.byteBufferToBase64(content));
        FILE_BYTES_RESPONSE.append("message", message.getValue());
        if (message == Messages.successfulRead) {
            FILE_BYTES_RESPONSE.append("status", true);
        } else {
            FILE_BYTES_RESPONSE.append("status", false);
        }
        return FILE_BYTES_RESPONSE.toJson();
    }

    static String createFILE_DELETE_RESPONSE(Document file, String fileName,
            Messages message) {
        Document FILE_DELETE_RESPONSE = new Document();
        FILE_DELETE_RESPONSE.append("command", "FILE_DELETE_RESPONSE");
        FILE_DELETE_RESPONSE.append("fileDescriptor", file);
        FILE_DELETE_RESPONSE.append("pathName", fileName);
        FILE_DELETE_RESPONSE.append("message", message.getValue());
        if (message == Messages.fileDeleted) {
            FILE_DELETE_RESPONSE.append("status", true);
        } else {
            FILE_DELETE_RESPONSE.append("status", false);
        }
        return FILE_DELETE_RESPONSE.toJson();

    }

    static String createFILE_MODIFY_RESPONSE(Document file, String fileName,
            Messages message) {
        Document FILE_MODIFY_RESPONSE = new Document();
        FILE_MODIFY_RESPONSE.append("command", "FILE_MODIFY_RESPONSE");
        FILE_MODIFY_RESPONSE.append("fileDescriptor", file);
        FILE_MODIFY_RESPONSE.append("pathName", fileName);
        FILE_MODIFY_RESPONSE.append("message", message.getValue());
        if (message == Messages.ready) {
            FILE_MODIFY_RESPONSE.append("status", true);
        } else {
            FILE_MODIFY_RESPONSE.append("status", false);
        }
        return FILE_MODIFY_RESPONSE.toJson();
    }

    //makes a json (in string format) for a DIRECTORY_CREATE_REQUEST
    //message is an enum
    //command is the command we want in the json
    //pathName is the pathName we want in the json
    static String createDIRECTORY_CREATE_REQUEST(String pathName) {
        Document DIRECTORY_CREATE_REQUEST = new Document();
        DIRECTORY_CREATE_REQUEST.append("command", "DIRECTORY_CREATE_REQUEST");
        DIRECTORY_CREATE_REQUEST.append("pathName", pathName);

        return DIRECTORY_CREATE_REQUEST.toJson();
    }
    //makes a json (in string format) for a DIRECTORY_CREATE_RESPONSE
    static String createDIRECTORY_CREATE_RESPONSE(String pathName, Messages message) {
        Document DIRECTORY_CREATE_RESPONSE = new Document();
        DIRECTORY_CREATE_RESPONSE.append("command", "DIRECTORY_CREATE_RESPONSE");
        DIRECTORY_CREATE_RESPONSE.append("pathName", pathName);
        DIRECTORY_CREATE_RESPONSE.append("message", message.getValue());
        if(message == Messages.directoryCreated){
            DIRECTORY_CREATE_RESPONSE.append("status",true);
        }else{
            DIRECTORY_CREATE_RESPONSE.append("status",false);
        }
        return DIRECTORY_CREATE_RESPONSE.toJson();
    }
    static String createDIRECTORY_DELETE_RESPONSE(String pathName, Messages message){
        Document DIRECTORY_DELETE_RESPONSE = new Document();
        DIRECTORY_DELETE_RESPONSE.append("command","DIRECTORY_DELETE_RESPONSE");
        DIRECTORY_DELETE_RESPONSE.append("pathName",pathName);
        DIRECTORY_DELETE_RESPONSE.append("message", message.getValue());
        if (message == Messages.directoryDeleted) {
            DIRECTORY_DELETE_RESPONSE.append("status", true);
        } else {
            DIRECTORY_DELETE_RESPONSE.append("status", false);
        }

        return DIRECTORY_DELETE_RESPONSE.toJson();
    }
    static String createAUTH_REQUEST(String identity){
        Document AUTH_REQUEST = new Document();
        AUTH_REQUEST.append("command","AUTH_REQUEST");
        AUTH_REQUEST.append("identity", identity);
        return AUTH_REQUEST.toString();
    }
    static String createAUTH_RESPONSE(){
        Document AUTH_RESPONSE = new Document();
        AUTH_RESPONSE.append("command","AUTH_RESPONSE");
        AUTH_RESPONSE.append("status", false);
        AUTH_RESPONSE.append("message", Messages.publicKeyNotFound.getValue());
        return AUTH_RESPONSE.toString();
    }
    static String createAUTH_RESPONSE(String key){
        Document AUTH_RESPONSE = new Document();
        AUTH_RESPONSE.append("command","AUTH_RESPONSE");
        AUTH_RESPONSE.append("AES128", key);
        AUTH_RESPONSE.append("status", true);
        AUTH_RESPONSE.append("message",Messages.publicKeyFound.getValue());
        return AUTH_RESPONSE.toString();
    }
    static String createLIST_PEERS_REQUEST(){
        Document LIST_PEERS_REQUEST = new Document();
        LIST_PEERS_REQUEST.append("command","LIST_PEERS_REQUEST");
        return LIST_PEERS_REQUEST.toString();
    }
    static String createLIST_PEERS_RESPONSE(){
        Document LIST_PEERS_RESPONSE = new Document();
        LIST_PEERS_RESPONSE.append("command","LIST_PEERS_RESPONSE");
        ArrayList<Document> peerleest = new ArrayList<>();

        //FIXME: Please check if the usage of peerList is correct
        for (clientSocket peer : peerList.getPeerList()) {
            Document peers = new Document();
            peers.append("host", peer.connRequestHost());
            peers.append("port", peer.getconnRequestServerPort());
            peerleest.add(peers);
        }
        LIST_PEERS_RESPONSE.append("peers", peerleest);
        return LIST_PEERS_RESPONSE.toString();
    }
    static String createClientCONNECT_PEER_REQUEST(HostPort peer){
        Document CONNECT_PEER_REQUEST = new Document();
        CONNECT_PEER_REQUEST.append("command","CONNECT_PEER_REQUEST");
        CONNECT_PEER_REQUEST.append("host", peer.host);
        CONNECT_PEER_REQUEST.append("host", peer.port);
        return CONNECT_PEER_REQUEST.toString();
    }
    static String createClientCONNECT_PEER_RESPONSE(HostPort peer, Messages message){
        Document CONNECT_PEER_RESPONSE = new Document();
        CONNECT_PEER_RESPONSE.append("command","CONNECT_PEER_RESPONSE");
        CONNECT_PEER_RESPONSE.append("host", peer.host);
        CONNECT_PEER_RESPONSE.append("host", peer.port);
        if(message == Messages.connectedToPeer){
            CONNECT_PEER_RESPONSE.append("status", true);
        } else {
            CONNECT_PEER_RESPONSE.append("status", false);
        }
        CONNECT_PEER_RESPONSE.append("message", message.getValue());
        return CONNECT_PEER_RESPONSE.toString();
    }
    static String createDISCONNECT_PEER_REQUEST(HostPort peer){
        Document DISCONNECT_PEER_REQUEST = new Document();
        DISCONNECT_PEER_REQUEST.append("command","DISCONNECT_PEER_REQUEST");
        DISCONNECT_PEER_REQUEST.append("host", peer.host);
        DISCONNECT_PEER_REQUEST.append("host", peer.port);
        return DISCONNECT_PEER_REQUEST.toString();
    }
    static String createDISCONNECT_PEER_RESPONSE(HostPort peer, Messages message){
        Document DISCONNECT_PEER_RESPONSE = new Document();
        DISCONNECT_PEER_RESPONSE.append("command","DISCONNECT_PEER_RESPONSE");
        DISCONNECT_PEER_RESPONSE.append("host", peer.host);
        DISCONNECT_PEER_RESPONSE.append("host", peer.port);
        if(message == Messages.disconnectedFromPeer){
            DISCONNECT_PEER_RESPONSE.append("status", true);
        } else {
            DISCONNECT_PEER_RESPONSE.append("status", false);
        }
        DISCONNECT_PEER_RESPONSE.append("message", message.getValue());
        return DISCONNECT_PEER_RESPONSE.toString();
    }
    //TODO
    static String encryptMessage(String key, String messages ) {
        Document encryptedMessage = new Document();

        //TODO: encrypt the message with secret key
        encryptedMessage.append("payload", "");//TODO

        return encryptedMessage.toString();
    }


}
