package co.samepinch.android.app.helpers;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import org.apache.commons.lang3.StringUtils;

import co.samepinch.android.app.ActivityFragment;
import co.samepinch.android.app.PostDetailActivity;
import co.samepinch.android.app.R;
import co.samepinch.android.app.helpers.misc.SPParsePushBroadcastReceiver;
import co.samepinch.android.data.dto.PushNotification;

public class PushNotificationActivityLauncher extends AppCompatActivity {
    public static final String TAG = "PushNotificationActivityLauncher";
    public static final String TYPE_DOT = "FOLLOWER";
    public static final String TYPE_POST = "POST";
    public static final String TYPE_GROUP = "GROUP";
    public static final String TYPE_COMMENT = "COMMENT";
    public static final String TYPE_VOTE = "VOTE";
    public static final String TYPE_ADMIN = "ADMIN";
    public static final String TYPE_ROOT = "";

    // Group|Post|Comment|Follower|Vote
    public static final int LAUNCH_TARGET_ACTIVITY = 108;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.root_activity);
        try {
            // get caller data
            PushNotification notification = SPParsePushBroadcastReceiver.getAppPushNotification(getIntent());
            PushNotification.Context notificationContext = notification == null ? null : notification.getContext();
            // empty check
            if (notification == null || notificationContext == null) {
                Intent intent = new Intent(PushNotificationActivityLauncher.this, RootActivity.class);
                startActivity(intent);
            }

            // grab target action
            String actionType = StringUtils.defaultString(notificationContext.getType(), TYPE_ROOT);
            actionType = actionType.toUpperCase();

            // args uid
            String uid = notification.getContext().getUid();

            // target activity args
            Bundle iArgs = new Bundle();

            Class<?> targetActivity = null;
            switch (actionType) {
                case TYPE_DOT:
                    iArgs.putString(AppConstants.K.KEY_DOT.name(), uid);
                    iArgs.putString(AppConstants.K.TARGET_FRAGMENT.name(), AppConstants.K.FRAGMENT_DOTWALL.name());
                    targetActivity = ActivityFragment.class;
                    break;

                case TYPE_POST:
                case TYPE_COMMENT:
                case TYPE_VOTE:
                    iArgs.putString(AppConstants.K.POST.name(), uid);
                    targetActivity = PostDetailActivity.class;
                    break;

                case TYPE_GROUP:
                    iArgs.putString(AppConstants.K.KEY_TAG.name(), uid);
                    iArgs.putString(AppConstants.K.TARGET_FRAGMENT.name(), AppConstants.K.FRAGMENT_TAGWALL.name());
                    targetActivity = ActivityFragment.class;
                    break;

                case TYPE_ADMIN:
                    // store preferences
                    Utils.PreferencesManager.getInstance().setValue(AppConstants.APP_INTENT.KEY_ADMIN_COMMAND.getValue(), uid);
                    targetActivity = RootActivity.class;
                    break;
                default:
                    targetActivity = RootActivity.class;
                    break;
            }

            Intent intent = new Intent(getApplicationContext(), targetActivity);
            intent.putExtras(iArgs);
            startActivityForResult(intent, LAUNCH_TARGET_ACTIVITY);
        } catch (Exception e) {
            Intent intent = new Intent(PushNotificationActivityLauncher.this, RootActivity.class);
            startActivity(intent);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == LAUNCH_TARGET_ACTIVITY) {
            Intent intent = new Intent(PushNotificationActivityLauncher.this, RootActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
    }
}