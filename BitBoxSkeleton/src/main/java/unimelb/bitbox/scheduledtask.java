package unimelb.bitbox;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import unimelb.bitbox.util.Document;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public class scheduledtask implements Runnable {

    public static DatagramSocket socket = null;

    scheduledtask(udpSocket toWriteTo) {
        this.socket = udpSocket.clientSock;
        run();
    }

    public void run() {
        System.out.println("thread name: "+Thread.currentThread().getName());
        LocalDateTime dateTime = LocalDateTime.now();
        LocalDateTime dateTimein2 = dateTime.plus(Duration.of(Peer.timeout, ChronoUnit.SECONDS));
        while (true) {
            LocalDateTime current = LocalDateTime.now();
            HashMap<String, ArrayList<Document>> toSend = determineNeedsResending.getList();
            HashMap<Document, ArrayList<Object>> toSendDates = determineNeedsResending.getDateMap();
            System.out.println("");
            for (String user : toSend.keySet()) {
                ArrayList<Document> userMessages = toSend.get(user);
                for (Document message : userMessages) {
                    ArrayList<Object> messageDateAndTries = toSendDates.get(message);
                    LocalDateTime toSendTime = (LocalDateTime) messageDateAndTries.get(0);    
                    if (current.isAfter(toSendTime) && (Integer) messageDateAndTries.get(1)<=Peer.retries) {
                        System.out.println("user is currently: "+user);
                        System.out.println("message is currently: "+message);
                        System.out.println(messageDateAndTries);
                        String toSendMessage = message.toJson();
                        byte[] buffer = toSendMessage.getBytes();
                        try {
                            String mystring = user.split(":")[0];
                            String newstring = mystring.substring(1, mystring.length());
                            System.out.println(InetAddress.getByName(newstring));
                            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, InetAddress.getByName(newstring), Integer.parseInt(user.split(":")[1]));
                            messageDateAndTries.set(1, (Integer) messageDateAndTries.get(1)+1);
                            messageDateAndTries.set(0, ((LocalDateTime) messageDateAndTries.get(0)).plus(Duration.of(10, ChronoUnit.SECONDS)));
                            
                            this.socket.send(packet);
                            System.out.println("SENNTTT");
                            prettyPrinter.print(toSendMessage);
                        } catch (Exception e) {
                            exceptionHandler.handleException(e);
                        }
                    }

                }
            }
            

        }

    }
}
