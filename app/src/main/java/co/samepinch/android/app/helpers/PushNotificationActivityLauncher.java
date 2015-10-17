package co.samepinch.android.app.helpers;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import org.apache.commons.lang3.StringUtils;

import co.samepinch.android.app.PostDetailActivity;
import co.samepinch.android.app.R;
import co.samepinch.android.app.helpers.misc.SPParsePushBroadcastReceiver;
import co.samepinch.android.data.dto.PushNotification;

public class PushNotificationActivityLauncher extends AppCompatActivity {
    public static final String TAG = "PushNotificationActivityLauncher";
    public static final String TYPE_POST = "Post";
    public static final int REQ_CODE = 108;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.root_activity);

        // get caller data
        PushNotification notification = SPParsePushBroadcastReceiver.getAppPushNotification(getIntent());
        PushNotification.Context notificationContext = notification == null ? null : notification.getContext();
        if (notification == null || notificationContext == null) {
            Intent intent = new Intent(PushNotificationActivityLauncher.this, RootActivity.class);
            startActivity(intent);
        }

        if (StringUtils.equalsIgnoreCase(notificationContext.getType(), TYPE_POST)) {
            Bundle iArgs = new Bundle();
            iArgs.putString(AppConstants.K.POST.name(), notification.getContext().getUid());

            Intent intent = new Intent(getApplicationContext(), PostDetailActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtras(iArgs);
            startActivityForResult(intent, REQ_CODE);
        } else {
            // finish
            Intent intent = new Intent(PushNotificationActivityLauncher.this, RootActivity.class);
            startActivity(intent);
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == REQ_CODE) {
                Intent intent = new Intent(PushNotificationActivityLauncher.this, RootActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        }
    }
}