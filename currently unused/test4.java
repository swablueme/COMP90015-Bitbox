package unimelb.bitbox;

import java.io.BufferedReader;
import java.io.BufferedWriter;

public class test4 implements Runnable {

    public BufferedWriter out = null;
    public BufferedReader in = null;
    public String message = null;

    test4(BufferedWriter out, BufferedReader in, String message) {
        this.out = out;
        this.in = in;
        this.message = message;

    }

    public static void startWriting(BufferedWriter out, BufferedReader in, String message) {
        String clientMsg = null;
        try {

            while (true) {
                System.out.println("WRITING+:"+message);
                out.write(message + "\n");
                out.flush();
                break;
            }
        } catch (Exception e) {
            exceptionHandler.handleException(e);
        }
    }

    @Override
    public void run() {
        startWriting(out, in, message);

    }
}
