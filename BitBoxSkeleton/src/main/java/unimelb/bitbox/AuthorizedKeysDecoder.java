package unimelb.bitbox;

import org.apache.commons.codec.binary.Base64;

import java.math.BigInteger;
import java.security.spec.RSAPublicKeySpec;

/**
 *
 * @author WhiteFang34
 * @modifiedby Ziping Chen
 * @availablility https://stackoverflow.com/questions/3531506/using-public-key-from-authorized-keys-with-java-security
 */
public class AuthorizedKeysDecoder {
    private byte[] bytes;
    private int pos;

    public RSAPublicKeySpec decodePublicKey(String base64encoded) throws Exception {
        bytes = Base64.decodeBase64(base64encoded.getBytes());
        pos = 0;

        int len = decodeInt();
        pos += len;

        BigInteger e = decodeBigInt();
        BigInteger m = decodeBigInt();
        RSAPublicKeySpec spec = new RSAPublicKeySpec(m, e);
        return spec;

    }

    private int decodeInt() {
        return ((bytes[pos++] & 0xFF) << 24) | ((bytes[pos++] & 0xFF) << 16)
                | ((bytes[pos++] & 0xFF) << 8) | (bytes[pos++] & 0xFF);
    }

    private BigInteger decodeBigInt() {
        int len = decodeInt();
        byte[] bigIntBytes = new byte[len];
        System.arraycopy(bytes, pos, bigIntBytes, 0, len);
        pos += len;
        return new BigInteger(bigIntBytes);
    }
}
