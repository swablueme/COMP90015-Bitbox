package unimelb.bitbox;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.UnknownHostException;
import java.security.NoSuchAlgorithmException;
import java.net.ConnectException;
import java.util.logging.Logger;

public class exceptionHandler {

    private static Logger log = Logger.getLogger(Peer.class.getName());

    public static void handleException(Exception e) {
        if (e instanceof ConnectException) {
            log.info("connection exception reached");
        } else if ((e instanceof UnknownHostException) || (e instanceof IOException)
                || (e instanceof UnsupportedEncodingException) || (e instanceof NoSuchAlgorithmException)) {
            e.printStackTrace();
        } else {
            e.printStackTrace();
        }
        return;
    }

}
