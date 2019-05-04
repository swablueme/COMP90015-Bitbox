package unimelb.bitbox;

import unimelb.bitbox.util.Document;
import unimelb.bitbox.util.HostPort;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

public class peerQueue {

    private Queue<HostPort> queue;

    peerQueue(){
        this.queue = new LinkedList<>();
    }
    public synchronized void add(HostPort hostPort){
        if(!(visited.getList()).contains(hostPort)) {
            this.queue.offer(hostPort);
        }
    }
    public synchronized void add(ArrayList<Document> peerList){
        for(Document peer:peerList) this.add(new HostPort(peer));
    }
    public synchronized static void visit(HostPort hostPort){
        visited.addElement(hostPort);
    }
    public synchronized HostPort pop(){
        HostPort hostPort = this.queue.poll();
        return hostPort;
    }
    public synchronized boolean isEmpty(){
        return this.queue.isEmpty();
    }

    @Override
    public String toString() {
        return (this.queue.toString() + "\n" + visited.getList());
    }
}
