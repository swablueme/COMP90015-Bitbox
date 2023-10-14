package unimelb.bitbox;

import unimelb.bitbox.util.Configuration;

public class generatePeriodicSyncEvents implements Runnable {

    private void timedEvents() {
        try {
            Integer syncInterval = Integer.parseInt(Configuration.getConfiguration().get("syncInterval"));
            Thread.sleep(syncInterval);
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
