package droid.samepinch.co.app.helpers.intent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import droid.samepinch.co.app.helpers.AppConstants;

/**
 * Created by imaginationcoder on 6/26/15.
 */
public class BroadcastNotifier {
    private LocalBroadcastManager mBroadcaster;

    public BroadcastNotifier(Context context) {
        mBroadcaster = LocalBroadcastManager.getInstance(context);
    }

    public void broadcastIntentWithState(String status) {
        broadcastIntentWithState(Integer.valueOf(status));
    }

    public void broadcastIntentWithState(int status) {
        Intent localIntent = new Intent();
        localIntent.setAction(AppConstants.KV.BROADCAST_ACTION.getValue());

        // Puts the status into the Intent
        localIntent.putExtra(AppConstants.KV.EXTENDED_DATA_STATUS.getValue(), status);
        localIntent.addCategory(Intent.CATEGORY_DEFAULT);

        // Broadcasts the Intent
        mBroadcaster.sendBroadcast(localIntent);
    }

    /**
     * Uses LocalBroadcastManager to send an {@link String} containing a logcat message.
     * {@link Intent} has the action {@code BROADCAST_ACTION} and the category {@code DEFAULT}.
     *
     * @param logData a {@link String} to insert into the log.
     */
    public void notifyProgress(String logData) {

        Intent localIntent = new Intent();

        localIntent.setAction(AppConstants.KV.BROADCAST_ACTION.getValue());
        localIntent.putExtra(AppConstants.KV.EXTENDED_DATA_STATUS.getValue(), -1);

        localIntent.putExtra(AppConstants.KV.EXTENDED_STATUS_LOG.getValue(), logData);
        localIntent.addCategory(Intent.CATEGORY_DEFAULT);

        mBroadcaster.sendBroadcast(localIntent);
    }
}
