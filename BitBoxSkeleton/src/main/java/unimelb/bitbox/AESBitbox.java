package unimelb.bitbox;

import javax.crypto.*;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class AESBitbox
{
    /**
     * Encrypt cipher-text with AES-128 using a secret key
     * @param message is the string that is to be encrypted
     * @param sKey is the symmetric key used for decryption
     * @return is the encrypted string
     */
    public static String encrypt(String message, SecretKey sKey)
    {
        String cipherText = null;
        try {
            // Instantiate cipher using AES-128 algorithm
            Cipher cipher = Cipher.getInstance("AES");
            // Set cipher in encryption mode
            cipher.init(Cipher.ENCRYPT_MODE, sKey);

            // Convert message into byte array representation
            byte[] messageBytes = message.getBytes();
            // Use cipher to encrypt messageBytes
            byte[] cipherTextBytes = cipher.doFinal(messageBytes);
            // Encode cipherText to base64
            cipherTextBytes = Base64.getEncoder().encode(cipherTextBytes);
            // Convert cipherTextBytes into string
            cipherText = new String(cipherTextBytes);
        }
        catch (NoSuchAlgorithmException e)
        {
            e.getMessage();
        }
        catch (NoSuchPaddingException e)
        {
            e.getMessage();
        }
        catch (InvalidKeyException e)
        {
            e.getMessage();
        }
        catch (IllegalBlockSizeException e)
        {
            e.getMessage();
        }
        catch (BadPaddingException e)
        {
            e.getMessage();
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
            Cipher cipher = Cipher.getInstance("AES");
            // Set cipher in decryption mode
            cipher.init(Cipher.DECRYPT_MODE, sKey);

            // Decode cipherText from base64 encoding
            byte[] cipherTextBytes = Base64.getDecoder().decode(cipherText.getBytes());
            // Use cipher to decrypt messageBytes
            byte[] messageBytes = cipher.doFinal(cipherTextBytes);
            // Convert messageBytes into String
            message = new String(messageBytes);
        }
        catch (NoSuchAlgorithmException e)
        {
            e.getMessage();
        }
        catch (NoSuchPaddingException e)
        {
            e.getMessage();
        }
        catch (InvalidKeyException e)
        {
            e.getMessage();
        }
        catch (IllegalBlockSizeException e)
        {
            e.getMessage();
        }
        catch (BadPaddingException e)
        {
            e.getMessage();
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
            e.getMessage();
        }
        return null;
    }
}
