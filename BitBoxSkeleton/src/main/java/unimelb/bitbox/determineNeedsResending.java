package unimelb.bitbox;

import java.time.Duration;
import java.util.HashMap;
import java.util.ArrayList;
import unimelb.bitbox.util.HostPort;
import unimelb.bitbox.util.Document;
import java.util.Objects;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

//attempt to record all messages that have been written and to who
public class determineNeedsResending {

    public static HashMap<String, ArrayList<Document>> messageListMap = new HashMap<>();
    public static HashMap<Document, String> handshakeMap = new HashMap<>();
    public static HashMap<Document, ArrayList<Object>> dateMap = new HashMap<>();

    public static void addConnected(String user) {
        System.out.println("TRYING TO ADD: " + user);
        synchronized (messageListMap) {
            user = "/" + user.split("/")[1];

            System.out.println("added: " + messageListMap.put(user, new ArrayList<Document>()));
            System.out.println("Now it is: " + messageListMap);

        }
    }

    public synchronized static void addMessage(String user, Document message) {

        String host = user.split("/")[0];
        String port = user.split(":")[1];

        user = "/" + user.split("/")[1];
        jsonunMarshaller received = new jsonunMarshaller(message);

        if (!messageListMap.containsKey(user)) {
            addConnected(user);
            System.out.println("added usage, messagelist is now: " + messageListMap);
        }

        if ((received.getCommand()).contains("HANDSHAKE_REQUEST")) {
            String correctResponse = jsonMarshaller.createHANDSHAKE(host, Integer.parseInt(port), "HANDSHAKE_RESPONSE");
            System.out.println("new corrected document: ");
            prettyPrinter.print(correctResponse);
            handshakeMap.put(message, correctResponse);
        }
        if ((received.getCommand()).contains("REQUEST")) {
            ArrayList<Document> currentMessages = messageListMap.get(user);
            currentMessages.add(message);
            System.out.println("adding document: " + message);
            messageListMap.put(user, currentMessages);
            ArrayList<Object> al = new ArrayList<>();
            al.add(LocalDateTime.now().plus(Duration.of(Peer.timeout, ChronoUnit.MILLIS)));
            al.add(0);
            dateMap.put(message, al);

        }
        System.out.println("map after addition: " + messageListMap);
    }

    public static void removeMessage(String user, Document message) {
        synchronized (messageListMap) {
            user = "/" + user.split("/")[1];
            ArrayList<Document> currentMessages = messageListMap.get(user);
            System.out.println("removing: " + message);
            currentMessages.remove(message);
            messageListMap.put(user, currentMessages);
        }
    }

    public synchronized static HashMap<String, ArrayList<Document>> getList() {
        return (HashMap<String, ArrayList<Document>>) messageListMap.clone();
    }

    public synchronized static HashMap<Document, ArrayList<Object>> getDateMap() {
        return (HashMap<Document, ArrayList<Object>>) dateMap.clone();
    }

    public synchronized static void processReply(String sender, Document message) {

        sender = "/" + sender.split("/")[1];
        System.out.println("CURRENT MESSAGE LIST");
        System.out.println(messageListMap);
        System.out.println("________________");
        ArrayList<Document> currentMessages = messageListMap.get(sender);
        //all requests need a response
        jsonunMarshaller received = new jsonunMarshaller(message);
        Document hasBeenReceived = null;
        for (Document msg : currentMessages) {
            jsonunMarshaller sent = new jsonunMarshaller(msg);
            String sentcommand = sent.getCommand();

            if (sentcommand.contains("REQUEST")) {
                sentcommand = sentcommand.replace("REQUEST", "RESPONSE");

            }
            //String[] keys = new String[]{"fileDescriptor", "pathName", "position", "length"};
            String[] FILE = new String[]{"md5", "lastModified", "fileSize", "pathName"};
            String[] FILE_BYTES = new String[]{"md5", "lastModified", "fileSize", "pathName", "position", "length"};
            String[] DIRECTORY = new String[]{"pathName"};
            Boolean isSame = false;
            System.out.println("Received command: " + received.getCommand() + "Sent command: " + sentcommand);
            if ((received.getCommand()).equals(sentcommand)) {
                if ((received.getCommand()).contains("FILE")) {
                    if ((received.getCommand()).contains("BYTES")) {
                        isSame = processKey(sent, received, FILE_BYTES);
                    } else {
                        isSame = processKey(sent, received, FILE);
                    }
                } else if ((received.getCommand()).contains("DIRECTORY")) {
                    isSame = processKey(sent, received, DIRECTORY);
                } else if ((received.getCommand()).contains("HANDSHAKE")) {
                    try {
                        Document ShouldHaveReceived = Document.parse(handshakeMap.get(msg));
                        System.out.println("searching for handshake response message: ");
                        prettyPrinter.print(ShouldHaveReceived.toJson());
                        if ((getHostPort(ShouldHaveReceived)).equals(getHostPort(message))) {
                            isSame = true;

                        }
                    } catch (Exception e) {
                        System.out.println(e.getMessage());
                        continue;
                    }
                }

                /*
                 if ((received.getFileSize()).equals(sent.getFileSize()) &&
                 (received.getmd5()).equals(sent.getmd5()) &&
                 (received.getpathName()).equals(sent.getpathName())&&
                 (received.getLength()).equals(sent.getLength())&&
                 (received.getPosition()).equals(sent.getPosition())&&
                 (received.getlastmodified()).equals(sent.getlastmodified())) {
                 */
            }
            if (isSame == true) {
                hasBeenReceived = msg;
                System.out.println("the correct document was received");
                break;
            }

        }
        if (hasBeenReceived != null) {
            System.out.println("removing");
            currentMessages.remove(hasBeenReceived);
            dateMap.remove(hasBeenReceived);
            System.out.println("list is now: " + currentMessages);
        } else {
            System.out.println("no relevant document to remove");
            System.out.println("list is now: " + currentMessages);
        }

    }

    public static Boolean processKey(jsonunMarshaller sent, jsonunMarshaller received, String[] keys) {
        for (String key : keys) {
            if (key.equals("md5")) {
                if (!(received.getmd5()).equals(sent.getmd5())) {
                    return false;
                }
            } else if (key.equals("lastModified")) {
                if (!((received.getlastmodified()).equals(sent.getlastmodified()))) {
                    return false;
                }
            } else if (key.equals("fileSize")) {
                if (!((received.getFileSize()).equals(sent.getFileSize()))) {
                    return false;
                }
            } else if (key.equals("pathName")) {
                if (!((received.getpathName()).equals(sent.getpathName()))) {
                    return false;
                }
            } else if (key.equals("position")) {
                if (!((received.getPosition()).equals(sent.getPosition()))) {
                    return false;
                }
            } else if (key.equals("length")) {
                if (!((received.getLength()).equals(sent.getLength()))) {
                    return false;
                }
            }
        }
        return true;
    }

    public static String getHostPort(Document message) {
        Document hostport = (Document) message.get("hostPort");
        String connRequestServerPort = hostport.get("port").toString();
        String connRequestHost = (String) hostport.getString("host");
        return connRequestHost + ":" + connRequestServerPort;
    }
}
