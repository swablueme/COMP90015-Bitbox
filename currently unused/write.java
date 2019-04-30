package unimelb.bitbox;

import java.io.BufferedReader;
import java.io.BufferedWriter;

public class write implements Runnable {

    public BufferedWriter out = null;
    public BufferedReader in = null;
    public String message = null;

    write(BufferedReader in, BufferedWriter out, String message) {
        this.out = out;
        this.in = in;
        this.message = message;
    }

    public void startWriting() {
        String clientMsg = null;
        try {
            while (true) {
                System.out.println("writing:"+this.message);
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
        startWriting();

    }
}

