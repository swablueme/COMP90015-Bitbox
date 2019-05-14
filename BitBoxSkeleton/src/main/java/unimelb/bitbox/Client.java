package unimelb.bitbox;

import java.io.*;
import java.net.UnknownHostException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Logger;
import unimelb.bitbox.util.Configuration;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import unimelb.bitbox.util.Document;
import unimelb.bitbox.util.HostPort;
import java.net.Socket;
import java.security.PrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.KeyFactory;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemReader;
import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import java.util.Base64;
import javax.crypto.spec.SecretKeySpec;

public class Client {
    private static Logger log = Logger.getLogger(Client.class.getName());
    private static final String PRIVATE_KEY_FILE = "bitboxclient_rsa";

    public static void main(String[] args) throws IOException, NumberFormatException, NoSuchAlgorithmException{

        String command = null;
        HostPort server = null;
        HostPort peer;
        String identity = null;
        PrivateKey privateKey = null;
        String secretKey = null;

        //fetch the private key and parse it
        try  {
            //read the private key file
            //parse the pem
            //convert PKCS#1 to PKCS#8
            privateKey = createPrivateKeyFromPemFile(PRIVATE_KEY_FILE);

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

            //For testing purpose
            System.out.println("the command is " + command);
            System.out.println("the peer connecting to is " + server.toString());


        } catch (CmdLineException e){

            //FIXME:should I use Logger here?
            System.err.println(e.getMessage());

            //print the usage to help the user understand what the arguments for
            cmdLineParser.printUsage(System.err);
        }

        //Try to connect to the server (Peers) and ask to be authorized
        Socket socket = null;
        try{
            socket = new Socket(server.host,server.port);
            System.out.println("Connection established");

            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(),"UTF-8"));
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(),"UTF-8"));

            //Ask to be authorized
            out.write(jsonMarshaller.createAUTH_REQUEST(identity) + "\n");
            out.flush();
            System.out.println("Auth request sent"); //for testing purpose

            //wait for the server response
            String received = in.readLine();
            System.out.println("authenticate response received");
            Document authResponse = Document.parse(received);
            System.out.println(authResponse.toString());

            if(authResponse.getBoolean("status")){

                String encrypted_key = authResponse.getString("AES128");
                System.out.println(authResponse.getString("message"));//FIXME:testing purpose

                //decrypted the key
                String decrypted_key = getDecrypted(privateKey,encrypted_key);

                //FIXME:decode the secret key
                secretKey = new String(Base64.getDecoder().decode(decrypted_key));

            }else{

                //if the key is not found in the Peer, then this client is unauthorized
                System.out.println(authResponse.getString("message"));
                System.exit(0);
            }

            //After authentication, start sending encrypted request and wait for encrypted response
            String request = null;
            //FIXME:check if the command is valid. Should've also checked if the server is valid but I'm not sure where
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
            
            out.write(jsonMarshaller.encryptMessage(secretKey, request) + "\n");
            out.flush();
            System.out.println("command sent");

            String response = in.readLine();
            System.out.println("command response received");
            Document cmdResponse = Document.parse(response);
            System.out.println(cmdResponse.toString());


        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e){
            e.printStackTrace();
        }

        //the Client program terminate at the completion of the command
        System.exit(0);


    }

    /**
     * MIT License
     *
     *     Copyright (c) 2017 Packt
     *
     *     Permission is hereby granted, free of charge, to any person obtaining a copy
     *     of this software and associated documentation files (the "Software"), to deal
     *     in the Software without restriction, including without limitation the rights
     *     to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
     *     copies of the Software, and to permit persons to whom the Software is
     *     furnished to do so, subject to the following conditions:
     *
     *     The above copyright notice and this permission notice shall be included in all
     *     copies or substantial portions of the Software.
     *
     *     THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
     *     IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
     *     FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
     *     AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
     *     LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
     *     OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
     *     SOFTWARE.
     *
     * @param keyFileName
     * @return a private key object
     * @throws IOException
     * @throws InvalidKeySpecException
     * @throws NoSuchAlgorithmException
     * @author Packt
     * @date 2017
     * @available https://www.programcreek.com/java-api-examples/?api=org.bouncycastle.util.io.pem.PemReader
     */
    //FIXME: How to properly cite third-party code?
    private static PrivateKey createPrivateKeyFromPemFile(final String keyFileName) throws IOException,
            InvalidKeySpecException, NoSuchAlgorithmException {

        // Loads a privte key from the specified key file name
        final PemReader pemReader = new PemReader(new FileReader(keyFileName));
        final PemObject pemObject = pemReader.readPemObject();
        final byte[] pemContent = pemObject.getContent();
        pemReader.close();
        final PKCS8EncodedKeySpec encodedKeySpec = new PKCS8EncodedKeySpec(pemContent);
        final KeyFactory keyFactory = getKeyFactoryInstance();
        final PrivateKey privateKey = keyFactory.generatePrivate(encodedKeySpec);
        return privateKey;
    }
    private static KeyFactory getKeyFactoryInstance() throws NoSuchAlgorithmException {
        return KeyFactory.getInstance("RSA");
    }

    private static String getDecrypted (PrivateKey privateKey, String encrypted) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE,privateKey);

        //FIXME: Really not sure about the encoding here...
        return new String(cipher.doFinal(encrypted.getBytes("utf-8") ) );
    }
}
