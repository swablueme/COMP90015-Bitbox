package unimelb.bitbox;

import java.io.*;
import java.net.ServerSocket;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;
import java.util.logging.Logger;
import java.net.Socket;
import java.net.ConnectException;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import unimelb.bitbox.util.Configuration;
import unimelb.bitbox.util.Document;
import unimelb.bitbox.util.HostPort;
import unimelb.bitbox.jsonMarshaller.Messages;

//import org.bouncycastle.openssl.PEMParser;
import org.apache.commons.codec.binary.Base64;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import java.util.*;
import java.time.LocalDateTime;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class Peer {

    private static Logger log = Logger.getLogger(Peer.class.getName());

    public static String mode = Configuration.getConfiguration().get("mode");
    public static Integer bufferSize = Integer.parseInt(Configuration.getConfiguration().get("blockSize"));
    public static Integer timeout = Integer.parseInt(Configuration.getConfiguration().get("udpTimeout"));
    public static Integer retries = Integer.parseInt(Configuration.getConfiguration().get("udpRetries"));

    public static void main(String[] args) throws IOException, NumberFormatException, NoSuchAlgorithmException,
            NoSuchProviderException {
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());

        //loads up your file manager on the regular instance
        ServerMain instance = new ServerMain();
        //load configs from the configuration file
        String port = Configuration.getConfiguration().get("port");
        String host = Configuration.getConfiguration().get("advertisedName");
        String peers = Configuration.getConfiguration().get("peers");
        String clientPort = Configuration.getConfiguration().get("clientPort");
        String pubKeyConfig = Configuration.getConfiguration().get("authorized_keys");

        log.info("THIS IS YOUR HOST: " + host + "PORT: " + port + "---------------");

        //Split all the public keys which are separated by commas from the config file
        Security.addProvider(new BouncyCastleProvider());
        log.info("BouncyCastle provider added.");
        HashMap<String, PublicKey> keys = new HashMap<>();
        String[] pubKeys = pubKeyConfig.split(",");
        KeyFactory factory = KeyFactory.getInstance("RSA", "BC");
        ArrayList<pleaseworkClient> attemptedtoconnectclients = new ArrayList<>();
        //split all the peers which are seperated by commas from the config file
        String[] mypeers = peers.split(",");
        udpSocket myUDPClient = null;
        if (mode.equals("udp")) {
            udpSocket.setSocket(port);

        }

        for (String pubKeyString : pubKeys) {
            try {
                String[] pubKeyStrings = pubKeyString.split(" ");
                String base64encoded = pubKeyStrings[1];
                String identity = pubKeyStrings[2];
                log.info("identity is:  " + identity);
                AuthorizedKeysDecoder decoder = new AuthorizedKeysDecoder();
                PublicKey pub = generatePublicKey(factory, decoder.decodePublicKey(base64encoded));

                log.info(String.format("Instantiated public key: %s", pub));
                keys.put(identity, pub);

            } catch (InvalidKeySpecException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        udpSocket toScheduleWrites = null;
        //add them to the queue
        for (String peer : mypeers) {
            HostPort hostPort = new HostPort(peer);

            try {
                //for each client attempt to create a socket and thread
                //if this fails it's because the client is offline
                if (!(visited.getList()).contains(hostPort)) {
                    if (mode.equals("tcp")) {
                        clientSocket myClient = new clientSocket(hostPort.host, hostPort.port);
                        pleaseworkClient myClientinstance = new pleaseworkClient(myClient, host, Integer.parseInt(port));
                        visited.addElement(hostPort);
                        attemptedtoconnectclients.add(myClientinstance);
                        new Thread(myClientinstance).start();
                    } else {
                        udpSocket myClient = new udpSocket(hostPort.host, hostPort.port);
                        pleaseworkClient myClientinstance = new pleaseworkClient(myClient, host, Integer.parseInt(port));
                        toScheduleWrites = myClient;

                        visited.addElement(hostPort);
                        new Thread(myClientinstance).start();

                    }
                }

            } catch (Exception e) {
                //System.out.println("Issue");
                exceptionHandler.handleException(e);
                continue;
            }
        }

        // ==============================
        // Create a server thread
        // ==============================
        if (mode.equals("tcp")) {
            System.out.println("THESE ARE THE CURRENT PROPERTIES OF THE SERVER: " + Configuration.getConfiguration());
            new Thread(new pleaseworkServer(host, Integer.parseInt(port))).start();
            new Thread(new generatePeriodicSyncEvents()).start();
        }

        //start listening on client on client port
        new Thread(() -> listenOnClient(Integer.parseInt(clientPort), keys)).start();

        System.out.println("starting scheduler");

        String newpeer = "localhost:8116";
        String newpeer2 = "localhost:8115";
        String newpeer3 = "localhost:8114";
        //System.out.println("list: " + udpPeerList.getPeers());
        //if (!udpPeerList.isKnownPeer("/" + peer.host + ":" + peer.port)) {
        System.out.println("TRYING PEERLIST");
        System.out.println(peerList.getPeerList());
        try {
            System.out.println(peerList.isKnownPeer(newpeer));
            System.out.println(peerList.isKnownPeer(newpeer2));
            System.out.println(peerList.isKnownPeer(newpeer3));
            Integer idx = (Integer) peerList.isKnownPeer(newpeer2).get(1);
            System.out.println("Trying to get peer");
            clientSocket needed = (peerList.getPeerList()).get(idx);
            System.out.println(needed);
            //System.out.println(udpPeerList.removeKnownPeers(udpPeerList.getPeerList().get(0)));
            // System.out.println("changed list");
        } catch (Exception e) {

        }

        System.out.println(udpPeerList.getPeerList());
        System.out.println("[-----------------p");

        new Thread(new scheduledtask(toScheduleWrites)).start();

    }

    public static void listenOnClient(Integer clientPort, HashMap<String, PublicKey> keys) {
        System.out.println("HERE WE ARE");
        try {
            System.out.println("ATTEMPTING TO MAKE SERVER SOCKET");
            ServerSocket serverSocket = new ServerSocket(clientPort);
            System.out.println("SUCCESSFULLY MADE SERVER SOCKET");

            while (true) {
                Socket client = serverSocket.accept();
                System.out.println("ACCEPING SOCKET");
                new Thread(() -> connectToClient(client, keys)).start();

            }
        } catch (Exception e) {
            System.out.println("EXCEPTION IN LISTENONCLIENT");
            e.printStackTrace();
            exceptionHandler.handleException(e);
        }

    }

    public static void connectToClient(Socket client, HashMap<String, PublicKey> keys) {
        System.out.println("starting connectToClient Class");

        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream(), "UTF-8"));
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(client.getOutputStream(), "UTF-8"));

            //FIXME: should I add another security provider here? Is the security provider global to the whole main class?
            PublicKey publicKey = null;
            SecretKey secretKey = null;

            String received = null;

            //Receiving the auth request
            while (true) {
                if (in.ready()) {
                    received = in.readLine();
                    if (received != null) {
                        log.info("READING: ");
                        prettyPrinter.print(received);
                        break;
                    }
                }
            }

            Document messageOne = Document.parse(received);

            //Check if it is a auth request
            if (messageOne.containsKey("command") && messageOne.getString("command").equals("AUTH_REQUEST")) {

                //check whether if the key is in the config
                if (keys.containsKey(messageOne.getString("identity"))) {

                    //get public key from the keys collection and generate a secret key for the session
                    publicKey = keys.get(messageOne.getString("identity"));
                    secretKey = AESBitbox.generateSKey();

                    //Turn the secret key to the key bytes for encryption
                    byte[] keyBytes = secretKey.getEncoded();
                    String s = new String(keyBytes);
                    System.out.println(s);
                    //random generated for padding
                    //SecureRandom random = new SecureRandom();
                    //int publicKeySize = 256;
                    //byte[] padding = new byte[ publicKeySize - keyBytes.length];
                    //random.nextBytes(padding);
                    //log.info("The length of secret key is" + keyBytes.length);
                    //log.info("The padding size is " + padding.length);
                    //merge two bytes array
                    //ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                    //outputStream.write(keyBytes);
                    //outputStream.write(padding);
                    //byte[] key_padding = outputStream.toByteArray();
                    //backup code: Cipher cipher1 = Cipher.getInstance("RSA/None/NoPadding", "BC");
                    Cipher cipher1 = Cipher.getInstance("RSA/ECB/PKCS1Padding");

                    cipher1.init(Cipher.ENCRYPT_MODE, publicKey);
                    byte[] cipherText = cipher1.doFinal(keyBytes);
                    String encodedEncryptedKey = Base64.encodeBase64String(cipherText);
                    String authResponse = jsonMarshaller.createAUTH_RESPONSE(encodedEncryptedKey);
                    out.write(authResponse + "\n");
                    out.flush();
                    log.info("auth response sent");
                    prettyPrinter.print(authResponse);

                } else {
                    System.out.println("ATTEMPTING TO SEND AN AUTH RESPONSE BACK TO THE CLIENT");
                    //it is a valid request but the key is not in config so send back refuse response
                    String response = jsonMarshaller.createAUTH_RESPONSE();
                    System.out.println("AUTH RESPONSE: " + response);
                    System.out.println(response);
                    System.out.println("SENDING TO THIS CLIENT SOCKET: " + client);
                    out.write(response + "\n");
                    out.flush();
                    log.info("auth response sent");
                    prettyPrinter.print(response);
                    return;
                }
            } else {

                log.info("Incorrect auth request");
                //end the thread
                return;
            }

            //Receiving the command request
            String command_received = null;
            while (true) {
                if (in.ready()) {
                    command_received = in.readLine();
                    if (command_received != null) {
                        log.info("READING FROM PEER: ");
                        prettyPrinter.print(command_received);
                        break;
                    }
                }
            }

            Document messageTwo = Document.parse(command_received);
            String command;
            String command_response = null;

            //Check if it's encrypted as it is supposed to be
            if (messageTwo.containsKey("payload")) {
                String decryptedMessage = AESBitbox.decrypt(messageTwo.getString("payload"), secretKey);
                Document commandRequest = Document.parse(decryptedMessage);

                //Check if the decrypted message contains command
                if (commandRequest.containsKey("command")) {
                    command = commandRequest.getString("command");

                    if (command.equals("LIST_PEERS_REQUEST")) {

                        //the jsonMarshaller will create a message contain all the peers connected
                        command_response = jsonMarshaller.createLIST_PEERS_RESPONSE();

                    } else if (command.equals("CONNECT_PEER_REQUEST")) {

                        //get the peer we are connecting
                        HostPort peer = new HostPort(commandRequest.getString("host"),
                                (int) commandRequest.getLong("port"));

                        //TODO Act on message
                        Messages messageToClient = null;

                        if (mode.equals("tcp")) {
                            try {
                                ArrayList<Object> result = peerList.isKnownPeer(peer.host + ":" + peer.port);
                                if ((Boolean) result.get(0)) {
                                    messageToClient = Messages.connectedToPeer;
                                } else {
                                    clientSocket myClient = new clientSocket(peer.host, peer.port);
                                    pleaseworkClient myClientinstance = new pleaseworkClient(myClient, peer.host, peer.port);
                                    new Thread(myClientinstance).start();
                                    while (true) {
                                        if (myClientinstance.foundPeer == false) {
                                            //then tell client it got rejected and the peerlist is full
                                            messageToClient = Messages.connectionFailed;

                                            break;
                                        } else if (myClientinstance.foundPeer == true) {
                                            //then tell client it got accepted
                                            messageToClient = Messages.connectedToPeer;
                                            break;
                                        }
                                    }
                                }
                            } catch (Exception e) {
                                //FIXME Will there be a excpetion for initiating a client with a fake socket?
                                messageToClient = Messages.connectionFailed;
                                exceptionHandler.handleException(e);

                                //catch (Exception e instanceof ConnectException){
                                //tell the client that there was a connection error
                                //prevent the execution of the next few lines
                            }

                        } else {
                            //TODO:udp
                            try {
                                //;
                                if (udpPeerList.isKnownPeer("/" + peer.host + ":" + String.valueOf(peer.port))) {
                                    messageToClient = Messages.connectedToPeer;
                                } else {
                                    udpSocket myClient = new udpSocket(peer.host, peer.port);
                                    pleaseworkClient myClientinstance = new pleaseworkClient(myClient, peer.host, peer.port);
                                    new Thread(myClientinstance).start();
                                    while (true) {
                                        if (myClientinstance.foundPeer == false) {
                                            //then tell client it got rejected and the peerlist is full
                                            messageToClient = Messages.connectionFailed;

                                            break;
                                        } else if (myClientinstance.foundPeer == true) {
                                            //then tell client it got accepted
                                            messageToClient = Messages.connectedToPeer;
                                            break;
                                        }
                                    }
                                }
                            } catch (Exception e) {
                                //FIXME Will there be a excpetion for initiating a client with a fake socket?
                                messageToClient = Messages.connectionFailed;
                                exceptionHandler.handleException(e);

                                //catch (Exception e instanceof ConnectException){
                                //tell the client that there was a connection error
                                //prevent the execution of the next few lines
                            }
                        }
                        command_response = jsonMarshaller.createClientCONNECT_PEER_RESPONSE(peer, messageToClient);
                    } else if (command.equals("DISCONNECT_PEER_REQUEST")) {

                        //get the peer we are disconnecting
                        HostPort peer = new HostPort(commandRequest.getString("host"),
                                (int) commandRequest.getLong("port"));
                        System.out.println("ATTEMPTING TO GET HOSTPORT ------");

                        Messages messageToClient = null;

                        if (mode.equals("tcp")) {

                            System.out.println("CHECKING IF IN LIST----");
                            System.out.println(peer.host);
                            System.out.println(peer.port);
                            System.out.println("--------------");
                            ArrayList<Object> result = peerList.isKnownPeer(peer.host + ":" + peer.port);
                            if (!((Boolean) result.get(0))) {
                                messageToClient = Messages.connectionNotActive;
                            } else {
                                try {
                                    Integer idx = (Integer) result.get(1);
                                    System.out.println("Trying to get peer");
                                    clientSocket needed = (clientSocket) result.get(2);
                                    peerList.removeKnownPeers(needed);
                                    System.out.println(peerList.getPeerList());
                                } catch (Exception e) {
                                    exceptionHandler.handleException(e);
                                }
                                messageToClient = Messages.disconnectedFromPeer;
                            }
                        } else {
                            //TODO udp
                            if (!udpPeerList.isKnownPeer("/" + peer.host + ":" + peer.port)) {
                                messageToClient = Messages.connectionNotActive;
                            } else {
                                try {
                                    Integer idx = udpPeerList.nuPeerlist.indexOf("/" + peer.host + ":" + peer.port);
                                    udpPeerList.removeKnownPeers((udpPeerList.getPeerList()).get(idx));
                                    System.out.println("After removing one peer, peer list is: " + udpPeerList.getPeerList());
                                } catch (Exception e) {
                                    exceptionHandler.handleException(e);
                                }
                                messageToClient = Messages.disconnectedFromPeer;
                            }
                        }

                        //FIXME
                        command_response = jsonMarshaller.createDISCONNECT_PEER_RESPONSE(peer, messageToClient);

                    } else {
                        log.info("Incorrect command request");
                        //end the thread
                        return;
                    }

                } else {
                    log.info("Incorrect command request");
                    //end the thread
                    return;
                }

            } else {
                log.info("Incorrect command request");
                //end the thread
                return;
            }

            //encrypt the response and send
            log.info("command response ready to send");
            prettyPrinter.print(command_response);
            String encryptedCommandResponse = jsonMarshaller.encryptMessage(secretKey, command_response);
            System.out.println("here, this is the encrypted: " + encryptedCommandResponse);
            out.write(encryptedCommandResponse + "\n");
            System.out.println("attempting to write");
            out.flush();
            System.out.println("attempting to flush");
            log.info("command response encrypted and sent");

            //Terminate the thread
            log.info("Terminating");
            return;
        } catch (Exception e) {
            exceptionHandler.handleException(e);
        }

    }

    private static PublicKey generatePublicKey(KeyFactory factory, RSAPublicKeySpec spec)
            throws InvalidKeySpecException {

        return factory.generatePublic(spec);

    }
}
