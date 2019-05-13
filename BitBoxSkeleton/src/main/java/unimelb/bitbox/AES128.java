package com.kaisoon;

import javax.crypto.*;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class AES128
{
    /**
     * Encrypt cipher-text with AES-128 using a secret key
     * @param message
     * @param sKey is the secret key
     * @return
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

        System.out.println("AES encrypted message: " + cipherText);
        return cipherText;
    }

    /**
     * Decrypts cipher-text with AES-128 using a secret key
     * @param cipherText
     * @param sKey is the secret key
     * @return
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

        System.out.println("AES decrypted message: " + message);
        return message;
    }
}
