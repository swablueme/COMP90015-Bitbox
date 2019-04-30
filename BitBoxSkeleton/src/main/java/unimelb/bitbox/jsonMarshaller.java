package unimelb.bitbox;

import unimelb.bitbox.util.Document;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import unimelb.bitbox.util.FileSystemManager.FileSystemEvent;

public class jsonMarshaller {

    public enum Messages {
        ready("file loader ready"),
        fileDeleted("file deleted"),
        unsafePathname("unsafe pathname given"),
        problemCreatingFile("there was a problem creating the file"),
        problemDeletingFile("there was a problem deleting the file"),
        pathnameExists("pathname already exists"),
        successfulRead("successful read"),
        unsuccessfulRead("unsuccessful read");

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
        Document peers = new Document();
        CONNECTION_REFUSED.append("command", "CONNECTION_REFUSED");
        for (clientSocket peer : peerList) {
            peers.append("host", peer.connRequestHost());
            peers.append("port", peer.getconnRequestServerPort());
        }

        CONNECTION_REFUSED.append("peers", peers);
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
            Integer position, Integer length) {
        Document FILE_BYTES_REQUEST = new Document();
        FILE_BYTES_REQUEST.append("command", "FILE_BYTES_REQUEST");
        FILE_BYTES_REQUEST.append("fileDescriptor", file);
        FILE_BYTES_REQUEST.append("pathName", fileName);
        FILE_BYTES_REQUEST.append("position", position);
        FILE_BYTES_REQUEST.append("length", length);
        return FILE_BYTES_REQUEST.toJson();
    }

    static String createFILE_BYTES_RESPONSE(Document file, String fileName,
            Integer position, Integer length, Messages message,
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

}
