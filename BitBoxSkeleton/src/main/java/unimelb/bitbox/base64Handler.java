package unimelb.bitbox;

import java.nio.ByteBuffer;
import java.util.Base64;

public class base64Handler
{
    //converts a byte buffer such as the one gotten from readFile into a base64 
    //buffer
    static String byteBufferToBase64(ByteBuffer bytebuffer)
    {
        return Base64.getEncoder().encodeToString(bytebuffer.array());
    }
    //converts a base64 string into a byte buffer for writing into the file 
    //after running the fileloader to reserve a spot for the file 
    static ByteBuffer base64toByteBuffer(String base64)
    {
        byte[] bytearray = Base64.getDecoder().decode(base64);
        ByteBuffer bytebuffer = ByteBuffer.wrap(bytearray);
        return bytebuffer;
    }
}