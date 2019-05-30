package unimelb.bitbox;

import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemReader;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import unimelb.bitbox.util.Configuration;

import javax.crypto.Cipher;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.FileInputStream;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.logging.Logger;

public class RSA {
    private static Logger log = Logger.getLogger(RSA.class.getName());
    private static final String PRIVATE_KEY_FILE = "bitboxclient_rsa";

    public static void main(String[] args) throws IOException, NumberFormatException, NoSuchAlgorithmException, NoSuchProviderException {

        Security.addProvider(new BouncyCastleProvider());
        log.info("BouncyCastle provider added.");
        String pubKeyConfig = Configuration.getConfiguration().get("authorized_keys");
        PrivateKey privateKey = null;


        try  {
            //read the private key file
            //parse the pem
            //convert PKCS#1 to PKCS#8
            privateKey = createPrivateKeyFromPemFile(PRIVATE_KEY_FILE);
            log.info(String.format("Instantiated private key: %s", privateKey));

        } catch (IOException e) {
            log.warning("Could not read file " + PRIVATE_KEY_FILE);
        } catch (InvalidKeySpecException e){
            log.warning("invalid key");
        }


    }
    private static PrivateKey createPrivateKeyFromPemFile(final String keyFileName) throws IOException,
            InvalidKeySpecException, NoSuchAlgorithmException, NoSuchProviderException {

        // Loads a privte key from the specified key file name
        //final PemReader pemReader = new PemReader(new FileReader(keyFileName));
        final PemReader pemReader = new PemReader(new InputStreamReader(new FileInputStream(keyFileName)));
        final PemObject pemObject = pemReader.readPemObject();
        final byte[] pemContent = pemObject.getContent();
        pemReader.close();
        final PKCS8EncodedKeySpec encodedKeySpec = new PKCS8EncodedKeySpec(pemContent);
        final KeyFactory keyFactory = getKeyFactoryInstance();
        final PrivateKey privateKey = keyFactory.generatePrivate(encodedKeySpec);
        return privateKey;
    }
    private static KeyFactory getKeyFactoryInstance() throws NoSuchAlgorithmException, NoSuchProviderException {
        return KeyFactory.getInstance("RSA","BC");
    }

    private static String getDecrypted (PrivateKey privateKey, String encrypted) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE,privateKey);

        //FIXME: Really not sure about the encoding here...
        return new String(cipher.doFinal(encrypted.getBytes()),"UTF-8");
    }
}
