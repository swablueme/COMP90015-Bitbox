package unimelb.bitbox;

import java.io.*;
import java.net.ServerSocket;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;
import java.util.logging.Logger;
import java.net.Socket;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import unimelb.bitbox.util.Configuration;
import unimelb.bitbox.util.Document;
import unimelb.bitbox.util.HostPort;
import unimelb.bitbox.jsonMarshaller.Messages;

import org.bouncycastle.openssl.PEMParser;
import org.apache.commons.codec.binary.Base64;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import java.util.*;
import java.time.LocalDateTime;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.net.DatagramSocket;

public class Peer {

    private static Logger log = Logger.getLogger(Peer.class.getName());

    public static String mode = Configuration.getConfiguration().get("mode");
    public static Integer bufferSize = Integer.parseInt(Configuration.getConfiguration().get("blockSize"));

    public static void main(String[] args) throws IOException, NumberFormatException, NoSuchAlgorithmException,
            NoSuchProviderException {

        //loads up your file manager on the regular instance
        ServerMain instance = new ServerMain();
        //load configs from the configuration file
        String port = Configuration.getConfiguration().get("port");
        String host = Configuration.getConfiguration().get("advertisedName");
        String peers = Configuration.getConfiguration().get("peers");
        String clientPort = Configuration.getConfiguration().get("clientPort");
        String pubKeyConfig = Configuration.getConfiguration().get("authorized_keys");

        log.info("HOST: " + host + "PORT: " + port);

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
                        //System.out.println("visited list: " + visited.getList());
                        attemptedtoconnectclients.add(myClientinstance);
                        new Thread(myClientinstance).start();
                    } else {
                        udpSocket myClient = new udpSocket(hostPort.host, hostPort.port);
                        pleaseworkClient myClientinstance = new pleaseworkClient(myClient, host, Integer.parseInt(port));
                        new Thread(new scheduledtask(myClient)).start();
                        visited.addElement(hostPort);
                        //System.out.println("starting");
                        new Thread(myClientinstance).start();
                        break;

                    }
                }
                //wait some time for the CONNECTION_REFUSED to be added to the peerFinding list
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

    }

    public static void listenOnClient(Integer clientPort, HashMap<String, PublicKey> keys) {
        try {
            ServerSocket serverSocket = new ServerSocket(clientPort);

            while (true) {
                Socket client = serverSocket.accept();
                new Thread(() -> connectToClient(client, keys));

            }
        } catch (Exception e) {
            exceptionHandler.handleException(e);
        }

    }

    public static void connectToClient(Socket client, HashMap<String, PublicKey> keys) {

        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream(), "UTF-8"));
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(client.getOutputStream(), "UTF-8"));

            //FIXME: should I add another security provider here? Is the security provider global to the whole main class?
            PublicKey publicKey = null;
            SecretKey secretKey = null;

            String received = null;

            //Receiving the auth request
            if (in.ready()) {
                received = in.readLine();
            }
            log.info("READING: ");
            prettyPrinter.print(received);
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

                    Cipher cipher1 = Cipher.getInstance("RSA/ECB/PKCS1Padding","BC");

                    cipher1.init(Cipher.ENCRYPT_MODE, publicKey);
                    byte[] cipherText = cipher1.doFinal(keyBytes);
                    String encodedEncryptedKey = Base64.encodeBase64String(cipherText);
                    String authResponse = jsonMarshaller.createAUTH_RESPONSE(encodedEncryptedKey);
                    out.write(authResponse + "\n");
                    log.info("auth response sent");
                    prettyPrinter.print(authResponse);

                } else {

                    //it is a valid request but the key is not in config so send back refuse response
                    String response = jsonMarshaller.createAUTH_RESPONSE();
                    out.write(response + "\n");
                    out.flush();
                    log.info("auth response sent");
                    prettyPrinter.print(response);
                }
            } else {

                log.info("Incorrect auth request");
                //end the thread
                return;
            }

            //Receiving the command request
            String command_received = null;
            if (in.ready()) {
                command_received = in.readLine();
            }
            log.info("READING: ");
            prettyPrinter.print(command_received);
            Document messageTwo = Document.parse(command_received);
            String command;
            String command_response;

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
                                commandRequest.getInteger("port"));

                        //TODO Act on message
                        Messages message1 = Messages.connectedToPeer;
                        Messages message2 = Messages.connectionFailed;
                        /* infinite loop that checks if we have found a peer

                        
                         try {
                         clientSocket myClient = new clientSocket(hostPort.host, hostPort.port);
                         pleaseworkClient myClientinstance = new pleaseworkClient(myClient, host, Integer.parseInt(port));
                         new Thread(myClientinstance).start();
                         while (true) {
                            if (myClient.foundPeer == false) {
                                //then tell client it got rejected and the peerlist is full
                                break;
                        } else if (myClient.foundPeer == true) {
                            //then tell client it got accepted
                            break;
                         } catch (Exception e instanceof ConnectException) {
                         //tell the client that there was a connection error
                         //prevent the execution of the next few lines
                         }
                        
                        
                         
                         */

                        //FIXME
                        command_response = jsonMarshaller.createClientCONNECT_PEER_RESPONSE(peer, message1);

                    } else if (command.equals("DISCONNECT_PEER_REQUEST")) {

                        //get the peer we are disconnecting
                        HostPort peer = new HostPort(commandRequest.getString("host"),
                                commandRequest.getInteger("port"));
                        Messages message1 = Messages.disconnectedFromPeer;
                        Messages message2 = Messages.connectionNotActive;

                        //FIXME
                        command_response = jsonMarshaller.createDISCONNECT_PEER_RESPONSE(peer, message2);

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
            out.write(encryptedCommandResponse + "\n");
            out.flush();
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
