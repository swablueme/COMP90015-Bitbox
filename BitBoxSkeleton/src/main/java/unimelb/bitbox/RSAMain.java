package unimelb.bitbox;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.io.File;
import java.io.FileInputStream;
import java.io.DataInputStream;
import java.security.*;
import java.security.spec.RSAPublicKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.nio.file.Paths;
import java.math.BigInteger;

import org.apache.commons.codec.binary.Base64;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.logging.Logger;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemReader;
import unimelb.bitbox.util.Configuration;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class RSAMain {

    protected final static Logger LOGGER = Logger.getLogger(RSAMain.class.getName());

    private static final String PRIVATE_KEY_FILE = "bitboxclient-rsa";

    public static void main(String[] args) throws FileNotFoundException,
            IOException, NoSuchAlgorithmException, NoSuchProviderException {
        Security.addProvider(new BouncyCastleProvider());
        LOGGER.info("BouncyCastle provider added.");

        String pubKeyConfig = Configuration.getConfiguration().get("authorized_keys");
        PrivateKey priv = null;
        PublicKey pub = null;
        SecretKey secret = AESBitbox.generateSKey();

        HashMap<String,PublicKey> keys = new HashMap<>();
        String[] pubKeys = pubKeyConfig.split(",");


        KeyFactory factory = KeyFactory.getInstance("RSA", "BC");

        try {
            priv = generatePrivateKey(factory, PRIVATE_KEY_FILE);
            LOGGER.info(String.format("Instantiated private key: %s", priv));

        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        }


        for (String pubKeyString:pubKeys){
            try {
                String[] pubKeyStrings = pubKeyString.split(" ");
                String base64encoded = pubKeyStrings[1];
                LOGGER.info("identity is:  " + pubKeyStrings[2]);
                AuthorizedKeysDecoder decoder = new AuthorizedKeysDecoder();
                pub = generatePublicKey(factory,decoder.decodePublicKey(base64encoded));

                LOGGER.info(String.format("The private key: %s", priv));

                LOGGER.info(String.format("Instantiated public key: %s",pub));
                keys.put(pubKeyStrings[2],pub);

                //byte[] input = new byte[] { (byte)0xbe, (byte)0xef };

                String inputString = "Testing";
                byte[] input = secret.getEncoded();

                try{
                    System.out.println("input : " + new String(input));
                    SecureRandom random = new SecureRandom();
                    //Cipher cipher1 = Cipher.getInstance("RSA/None/NoPadding", "BC");
                    Cipher cipher1 = Cipher.getInstance("RSA/ECB/PKCS1Padding","BC");
                    cipher1.init(Cipher.ENCRYPT_MODE, pub);
                    byte[] cipherText = cipher1.doFinal(input);
                    System.out.println("cipher: "+ cipherText);
                    // decryption step
                    Cipher cipher2 = Cipher.getInstance("RSA/ECB/PKCS1Padding","BC");
                    //Cipher cipher2 = Cipher.getInstance("RSA/None/NoPadding", "BC");
                    cipher2.init(Cipher.DECRYPT_MODE, priv);
                    byte[] plainText = cipher2.doFinal(cipherText) ;

                    if(Arrays.equals(input,plainText)){
                        LOGGER.info("encrypted and decrypted successfully");
                    } else {
                        LOGGER.info("What the fuck, it's not working!");
                    }
                    System.out.println("plain : " + new String(plainText));

                    SecretKey secretDecrypted = AESBitbox.keyBytesToKey(plainText);
                    if(secret.equals(secretDecrypted)){
                        LOGGER.info("same keys");
                    } else {
                        LOGGER.info("What the fuck, it's not working!");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                //PublicKey pub = generatePublicKey(factory, RESOURCES_DIR + "id_rsa.pub");
                //LOGGER.info(String.format("Instantiated public key: %s", pub));
            } catch (InvalidKeySpecException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }

        }







    }

    private static PrivateKey generatePrivateKey(KeyFactory factory, String filename)
            throws InvalidKeySpecException, FileNotFoundException, IOException {
        PemFile pemFile = new PemFile(filename);
        byte[] content = pemFile.getPemObject().getContent();
        PKCS8EncodedKeySpec privKeySpec = new PKCS8EncodedKeySpec(content);
        return factory.generatePrivate(privKeySpec);
    }

    private static PublicKey generatePublicKey(KeyFactory factory, RSAPublicKeySpec spec)
            throws InvalidKeySpecException, FileNotFoundException, IOException {

        return factory.generatePublic(spec);


    }
    private static String getDecrypted (PrivateKey privateKey, String encrypted) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE,privateKey);

        //FIXME: Really not sure about the encoding here...
        return new String(cipher.doFinal(encrypted.getBytes()),"UTF-8");
    }






}
