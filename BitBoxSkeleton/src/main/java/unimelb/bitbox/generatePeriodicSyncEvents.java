package unimelb.bitbox;

import unimelb.bitbox.util.Configuration;

public class generatePeriodicSyncEvents implements Runnable {

    private void timedEvents() {
        try {
//            System.out.println("==================SYNCING NOW KAI==================");
            Integer syncInterval = Integer.parseInt(Configuration.getConfiguration().get("syncInterval"));
            Thread.sleep(syncInterval * 1000);
            actOnMessages.generateSyncEvents();
        }
        catch(Exception e) {
            exceptionHandler.handleException(e);
        }

    }

    @Override
    public void run() {
        while (true) {
            timedEvents();
        }
    }
}
