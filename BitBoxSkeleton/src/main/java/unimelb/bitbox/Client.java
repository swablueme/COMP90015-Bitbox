package unimelb.bitbox;

import java.io.*;
import java.net.UnknownHostException;
import java.security.*;
import java.security.spec.RSAPublicKeySpec;
import java.util.logging.Logger;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.Arrays;
import unimelb.bitbox.util.Configuration;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import unimelb.bitbox.util.Document;
import unimelb.bitbox.util.HostPort;
import java.net.Socket;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;

import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemReader;

import javax.crypto.*;
import org.apache.commons.codec.binary.Base64;
import javax.crypto.spec.SecretKeySpec;

public class Client {
    private static Logger log = Logger.getLogger(Client.class.getName());
    private static final String PRIVATE_KEY_FILE = "bitboxclient_rsa";

    public static void main(String[] args) throws IOException, NumberFormatException, NoSuchAlgorithmException,
            NoSuchProviderException{

        String command = null;
        HostPort server = null;
        HostPort peer;
        String identity = null;
        PrivateKey privateKey = null;
        SecretKey secretKey = null;

        Security.addProvider(new BouncyCastleProvider());
        log.info("BouncyCastle provider added.");
        KeyFactory factory = KeyFactory.getInstance("RSA", "BC");

        //fetch the private key and parse it
        try  {
            //read the private key file
            //parse the pem
            //convert PKCS#1 to PKCS#8
            privateKey = generatePrivateKey(factory, PRIVATE_KEY_FILE);
            log.info(String.format("Instantiated private key: %s", privateKey));

        } catch (IOException e) {
            log.warning("Could not read file " + PRIVATE_KEY_FILE);
        } catch (InvalidKeySpecException e){
            log.warning("invalid key");
        }

        //Object that will store the command line arguments
        CmdLineArgs cmdLineArgs = new CmdLineArgs();

        //Parser of command line arguments
        CmdLineParser cmdLineParser = new CmdLineParser(cmdLineArgs);

        try{

            cmdLineParser.parseArgument(args);
            command = cmdLineArgs.getCommand();
            server = cmdLineArgs.getServer();
            identity = cmdLineArgs.getIdentity();

            log.info("the command is " + command);
            log.info("the peer connecting to is " + server.toString());


        } catch (CmdLineException e){

            log.warning(e.getMessage());

            //print the usage to help the user understand what the arguments for
            cmdLineParser.printUsage(System.err);
        }

        //Try to connect to the server (Peers) and ask to be authorized
        Socket socket = null;
        try{
            socket = new Socket(server.host,server.port);
            log.info("Connection established");

            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(),"UTF-8"));
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(),"UTF-8"));

            //Ask to be authorized
            String authrequest = jsonMarshaller.createAUTH_REQUEST(identity);
            out.write( authrequest + "\n");
            out.flush();
            log.info("Auth request sent");
            prettyPrinter.print(authrequest);

            //wait for the server response
            String received = null;
            if(in.ready()){ received = in.readLine();}

            log.info("authenticate response received");
            Document authResponse = Document.parse(received);
            prettyPrinter.print(received);

            if(authResponse.getBoolean("status")){

                //get the key bytes that has been encoded with 64
                String encrypted_key = authResponse.getString("AES128");
                log.info(authResponse.getString("message"));

                //decoded and decrypted the key bytes
                byte[] plainText = getDecrypted(Base64.decodeBase64(encrypted_key),privateKey);

                //extract the first 128 bytes
                //byte[] keyBytes = Arrays.copyOfRange(plainText,0,127);

                //generate the key from the key bytes
                secretKey = AESBitbox.keyBytesToKey(plainText);

            }else{

                //if the key is not found in the Peer, then this client is unauthorized
                log.info(authResponse.getString("message"));
                log.info("Terminating");
                System.exit(0);
            }

            //After authentication, start sending encrypted request and wait for encrypted response
            String request = null;
            if(command.equals("list_peers")){

                request = jsonMarshaller.createLIST_PEERS_REQUEST();

            } else if (command.equals("connect_peer")){

                //get the peer we are connecting
                peer = cmdLineArgs.getPeer();
                request = jsonMarshaller.createClientCONNECT_PEER_REQUEST(peer);

            } else if (command.equals("disconnect_peer")){

                //get the peer we are disconnecting
                peer = cmdLineArgs.getPeer();
                request = jsonMarshaller.createDISCONNECT_PEER_REQUEST(peer);

            } else {
                //the command is invalid and print the usage to help the user understand
                cmdLineParser.printUsage(System.err);
                System.exit(0);
            }

            log.info("command ready to be sent");
            prettyPrinter.print(request);

            out.write(jsonMarshaller.encryptMessage(secretKey, request) + "\n");
            out.flush();
            log.info("command sent");

            String response = null;
            if(in.ready()){response = in.readLine();}
            Document encrypted_response = Document.parse(response);
            log.info("command response received");
            if(encrypted_response.containsKey("payload")){

                String decryptedMessage = AESBitbox.decrypt(encrypted_response.getString("payload"),secretKey);
                Document command_response = Document.parse(decryptedMessage);
                prettyPrinter.print(decryptedMessage);}

        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e){
            e.printStackTrace();
        }

        //the Client program terminate at the completion of the command
        log.info("Terminating");
        System.exit(0);


    }

    /**
     * A method to generate a private key object from the private key in pem file
     *
     * @param factory
     * @param filename
     * @return a private Key
     * @throws InvalidKeySpecException
     * @throws FileNotFoundException
     * @throws IOException
     * @availablity https://github.com/txedo/bouncycastle-rsa-pem-read
     */
    private static PrivateKey generatePrivateKey(KeyFactory factory, String filename)
            throws InvalidKeySpecException, FileNotFoundException, IOException {
        PemFile pemFile = new PemFile(filename);
        byte[] content = pemFile.getPemObject().getContent();
        PKCS8EncodedKeySpec privKeySpec = new PKCS8EncodedKeySpec(content);
        return factory.generatePrivate(privKeySpec);
    }

    private static byte[] getDecrypted (byte[] cipherText, PrivateKey privateKey) throws NoSuchAlgorithmException,
            NoSuchPaddingException, NoSuchProviderException, InvalidKeyException, IllegalBlockSizeException,
            BadPaddingException {

        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding","BC");
        //Cipher cipher2 = Cipher.getInstance("RSA/None/NoPadding", "BC");
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        byte[] plainText = cipher.doFinal(cipherText) ;

        return plainText;
    }
}
