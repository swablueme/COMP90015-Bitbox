package unimelb.bitbox;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.*;

public class attemptBFSPeerFinding {

    ConcurrentLinkedQueue<Object> queue = new ConcurrentLinkedQueue<>();
    volatile HashMap<String, Integer> attemptedVisitList = new HashMap<>();
    Integer count = 0;

    public void visit() {
        for (String peer : attemptedVisitList.keySet()) {
            if (attemptedVisitList.get(peer) != 0) {
                bfs(peer);
            }

        }
        
    }

    public void bfs(String peer) {
        count += 1;
        while (queue != null) {

                
            }
        }

    

}


