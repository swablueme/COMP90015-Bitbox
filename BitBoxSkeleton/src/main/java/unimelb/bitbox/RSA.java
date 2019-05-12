package com.kaisoon;

import java.math.BigInteger;
import java.security.SecureRandom;

/**
 * @author Kai Soon
 * RSA is written to be used in conjunction with the Bitbox distributed file system written in this project. When a
 * peer is created in the system, an RSA object should be instantiated. Upon construction of the object, a set of
 * public/private key pair will be generated. The program can freely access the public key using the getPublicKey()
 * method.
 * The following example will demonstrate how two peers can use this class to encrypt
 * and decrypt transmitted messages:
 * - Should Peer1 wants to send an encrypted message to Peer2, Peer1 shall first instantiate an RSA object
 * - Peer1 shall access the public key of the RSA object and sends it to Peer2
 * - Peer2 shall use encrypt(String message, BigInteger publicKey) to encrypt the message, where the
 * argument publicKey is the publicKey of Peer1
 * - Peer2 shall send the cipherText to Peer1
 * - Peer1 shall then decrypt the cipherText using decrypt(String message)
 *
 * random is a cryptographically secure random number used in generating numbers
 * N is the modulus of the RSA encryption and its the product of two prime numbers
 * e is the public encryption key used to encrypt messages
 * d is the private decryption key used to decrypt cipher-text
 * BITLENGTH is the number of bits the two distinct prime numbers p and q will have
 */
public class RSA
{
    private SecureRandom random;
    private BigInteger N = null;
    private BigInteger e = null;
    private BigInteger d = null;
    // THIS MUST BE A LARGE NUMBER FOR THE GENERATED KEYS TO WORK!!
    // Note that for keys to be in ssh-keygen format, BITLENGTH should be at least 1024.
    private final int BITLENGTH = 1024;

    // ============================== Constructor
    /**
     * Instantiate an RSA object.
     * generateKeys() is called upon the construction of an object such that all all keys are available and ready for
     * use in encryptiona nd decryption.
     */
    public RSA()
    {
        this.random = new SecureRandom();
        this.generateKeys();
    }

    // ============================== Getters
    /**
     * @return An array of two BigIntegers which is public key pair generated.
     */
    public BigInteger[] getPublicKey()
    {
        BigInteger[] publicKey = new BigInteger[2];
        publicKey[0] = e;
        publicKey[1] = N;

        return publicKey;
    }

    // ============================== Methods
    /**
     * generateKeys() generates the e, d, and N values that are components of public/private key pair.
     * Public and private key can be accessed with getter methods.
     */
    public void generateKeys()
    {
        // p and q have to be two distinct prime numbers
        BigInteger p;
        BigInteger q;
        do {
            p = BigInteger.probablePrime(BITLENGTH, random);
            q = BigInteger.probablePrime(BITLENGTH, random);
        } while (p.equals(q));

        // N = p*q
        this.N = p.multiply(q);
        // phi = (p-1)*(q-1)
        BigInteger phi = (p.subtract(BigInteger.ONE)).multiply(q.subtract(BigInteger.ONE));
        // e has to be a coPrime of phi and N
        this.e = coPrime(phi, N);
        // d has to be a value that satisfy the equation (e*d) mod phi = 1
        // d is said to be the modular inverse of e with phi as the modulus
        this.d = e.modInverse(phi);

        // ========== FOR DEBUGGING
//        System.out.println("p: " + p);
//        System.out.println("q: " + q);
//        System.out.println("phi: " + phi);
//        System.out.println("Modulus: " + N);
//        System.out.println("Public key: " + e);
//        System.out.println("Private key: " + d + "\n");
//
//        System.out.println("BitLength of Modulus: " + N.bitLength());
//        System.out.println("BitLength of Public Key: " + e.bitLength());
//        System.out.println("BitLength of Private Key: " + d.bitLength() + "\n");
    }

    /**
     * coPrime() is used to generate a number e that is coPrime of phi and N
     * Two numbers are co-prime of each other if their greatest common divisor GCD is equal to 1
     * gcd(N, e) = 1
     * @param phi is the Euler phi of the RSA encryption
     * @return e which is a co-prime of N and is the public encryption key
     */
    private BigInteger coPrime(BigInteger phi, BigInteger N)
    {
        // e must be within the range of [1, phi], therefore the bitLength of e must be te same as the bitLength of phi
        BigInteger e = new BigInteger(phi.bitLength(), random);

        // e has to satisfy two conditions to be the co-prime from phi:
        // gcd(phi, e) != 1 and gcd(N, e) != 1
        while (!phi.gcd(e).equals(BigInteger.ONE) || !N.gcd(e).equals(BigInteger.ONE))
        {
            // Randomly generate a new e each time the above conditions are not satisfied
            e = new BigInteger(phi.bitLength(), random);
        }
        return e;
    }

    /**
     * encrypt() converts a string into a cipherText in the form of a BigInteger with a provided public key
     * @param message is the string message to be encrypted
     * @param publicKey is a array consisting of [publicKey, modulus]
     * @return A BitInteger cipherText
     */
    public BigInteger encrypt(String message, BigInteger[] publicKey)
    {
        // Convert message into its corresponding byte array
        byte[] messageBytes = message.getBytes();
        // The concatenated array of bytes represents a BigInteger. Convert the array of bytes into a BigInteger.
        BigInteger messageInt = new BigInteger(messageBytes);
        // Create cipherText where c = (m^e) mod N
        BigInteger cipherText = messageInt.modPow(publicKey[0], publicKey[1]);

        // ========== FOR DEBUGGING
//            System.out.println("Encrypted message: " + new String(cipherText.toByteArray()) + "\n");

        return cipherText;
    }

    /**
     * decrypt() converts a cipherText back into its original string message
     * @param cipherText is a BigInteger cipherText
     * @return The original string message if keys are generated and decryption is successful
     * @return null if keys are not generated and encryption cannot be performed
     */
    public String decrypt(BigInteger cipherText)
    {
        if ((N == null) || (e == null) || (d == null))
        {
            System.out.println("Error: Keys have not been generated");
            return null;
        }
        else
        {
            // Convert cipherText back to original message's BigInteger representation
            BigInteger messageInt = cipherText.modPow(d, N);
            // Tokenize BigInteger into a byte array with each byte representing a character in the original message
            byte[] messageBytes = messageInt.toByteArray();
            // Convert byte array into original string message
            String message = new String(messageBytes);

            // ========== FOR DEBUGGING
//            System.out.println("Decrypted message: " + message + "\n");

            return message;
        }
    }

}