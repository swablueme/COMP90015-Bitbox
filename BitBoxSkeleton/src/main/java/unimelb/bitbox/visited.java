package unimelb.bitbox;

import unimelb.bitbox.util.HostPort;

import java.util.ArrayList;

public class visited {
    public static ArrayList<HostPort> visited = new ArrayList<>();

    public synchronized static void addElement(HostPort element) {
        visited.add(element);
        System.out.println("visited is now: "+visited);
    }
    public synchronized static ArrayList<HostPort> getList() {
        return (ArrayList<HostPort>) visited.clone();
    }
}