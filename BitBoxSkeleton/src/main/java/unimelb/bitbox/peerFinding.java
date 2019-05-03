package unimelb.bitbox;

import unimelb.bitbox.util.HostPort;
import unimelb.bitbox.util.Document;

import java.util.LinkedList;
import java.util.Queue;

import java.util.ArrayList;

public class peerFinding {

    static Queue<HostPort> queue = new LinkedList<>();
    static ArrayList<HostPort> visited = new ArrayList<>();

    public static void add(HostPort hostPort){
        if(!visited.contains(hostPort)) {
            queue.offer(hostPort);
            visited.add(hostPort);
        }
    }
    public static void add(ArrayList<Document> peerList){
        for(Document peer:peerList) {
            peerFinding.add(new HostPort(peer));
        }
    }
    public static HostPort pop(){
        return queue.poll();
    }
    public static boolean isEmpty(){
        return queue.isEmpty();
    }
}
