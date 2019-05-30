package unimelb.bitbox;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.security.SecureRandom;
import java.util.logging.Logger;
import java.io.ByteArrayOutputStream;

public class AESBitbox
{
    /**
     * Encrypt cipher-text with AES-128 using a secret key
     * @param message is the string that is to be encrypted
     * @param sKey is the symmetric key used for decryption
     * @return is the encrypted string
     */

    private static Logger log = Logger.getLogger(Client.class.getName());

    public static String encrypt(String message, SecretKey sKey)
    {

        String cipherText = null;
        try {
            // Instantiate cipher using AES-128 algorithm
            Cipher cipher = Cipher.getInstance("AES/ECB/NoPadding");
            // Set cipher in encryption mode
            cipher.init(Cipher.ENCRYPT_MODE, sKey);

            //random generated for padding
            SecureRandom random = new SecureRandom();
            String messageWithNewLine = message + "\n";
            int blockSize = 16;
            byte[] padding = new byte[blockSize - (messageWithNewLine.getBytes().length % blockSize)];
            random.nextBytes(padding);
            log.info("The length of message is" + messageWithNewLine.getBytes().length + " bytes");
            log.info("The padding size is " + padding.length + " bytes");

            //merge two bytes array
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            outputStream.write(messageWithNewLine.getBytes());
            outputStream.write(padding);
            byte[] messageBytes = outputStream.toByteArray();

            // Convert message into byte array representation
            //byte[] messageBytes = message.getBytes();

            // Use cipher to encrypt messageBytes
            byte[] cipherTextBytes = cipher.doFinal(messageBytes);
            // Encode cipherText to base64
            cipherTextBytes = Base64.getEncoder().encode(cipherTextBytes);
            // Convert cipherTextBytes into string
            cipherText = new String(cipherTextBytes);
        }
        catch (NoSuchAlgorithmException e)
        {
            exceptionHandler.handleException(e);
        }
        catch (NoSuchPaddingException e)
        {
            exceptionHandler.handleException(e);
        }
        catch (InvalidKeyException e)
        {
            exceptionHandler.handleException(e);
        }
        catch (IllegalBlockSizeException e)
        {
            exceptionHandler.handleException(e);
        }
        catch (BadPaddingException e)
        {
            exceptionHandler.handleException(e);
        }
        catch (IOException e){
            exceptionHandler.handleException(e);
        }

//        System.out.println("AES encrypted message: " + cipherText);
        return cipherText;
    }

    /**
     * Decrypts cipher-text with AES-128 using a secret key
     * @param cipherText is the string that is to be decrypted
     * @param sKey is the symmetric key used for decryption
     * @return is a decrypted string
     */
    public static String decrypt(String cipherText, SecretKey sKey)
    {
        String message = null;
        try {
            // Instantiate cipher using AES-128 algorithm
            Cipher cipher = Cipher.getInstance("AES/ECB/NoPadding");
            // Set cipher in decryption mode
            cipher.init(Cipher.DECRYPT_MODE, sKey);

            // Decode cipherText from base64 encoding
            byte[] cipherTextBytes = Base64.getDecoder().decode(cipherText.getBytes());
            // Use cipher to decrypt messageBytes
            byte[] messageBytes = cipher.doFinal(cipherTextBytes);
            // Convert messageBytes into String
            String messageWithPadding = new String(messageBytes);
            message = messageWithPadding.split("\n")[0];
        }
        catch (NoSuchAlgorithmException e)
        {
            exceptionHandler.handleException(e);
        }
        catch (NoSuchPaddingException e)
        {
            exceptionHandler.handleException(e);
        }
        catch (InvalidKeyException e)
        {
            exceptionHandler.handleException(e);
        }
        catch (IllegalBlockSizeException e)
        {
            exceptionHandler.handleException(e);
        }
        catch (BadPaddingException e)
        {
            exceptionHandler.handleException(e);
        }

//        System.out.println("AES decrypted message: " + message);
        return message;
    }

    /**
     * generateSKey() generates a symmetric key using the AES-128 algorithm
     * @return a symmetric key
     * @return null if the function fails
     */
    public static SecretKey generateSKey()
    {
        try {
            // Static method from KeyGenerator class returns an KeyGenerator object used to generate an AES-128 key
            KeyGenerator generator = KeyGenerator.getInstance("AES");
            // Generate AES-128 key
            SecretKey secretKey = generator.generateKey();
            return secretKey;
        }
        catch (NoSuchAlgorithmException e)
        {
            exceptionHandler.handleException(e);
        }
        return null;
    }
    public static SecretKey keyBytesToKey(byte[] keybytes){
        return new SecretKeySpec(keybytes,"AES");
    }
}
