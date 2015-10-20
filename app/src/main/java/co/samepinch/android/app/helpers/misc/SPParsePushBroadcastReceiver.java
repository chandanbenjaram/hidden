package co.samepinch.android.app.helpers.misc;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;

import com.facebook.common.executors.CallerThreadExecutor;
import com.facebook.common.references.CloseableReference;
import com.facebook.datasource.DataSource;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.imagepipeline.common.Priority;
import com.facebook.imagepipeline.core.ImagePipeline;
import com.facebook.imagepipeline.datasource.BaseBitmapDataSubscriber;
import com.facebook.imagepipeline.image.CloseableImage;
import com.facebook.imagepipeline.image.CloseableStaticBitmap;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;
import com.google.gson.Gson;
import com.parse.ParsePushBroadcastReceiver;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;

import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.util.Arrays;
import java.util.concurrent.CountDownLatch;

import co.samepinch.android.app.SPApplication;
import co.samepinch.android.app.helpers.PushNotificationActivityLauncher;
import co.samepinch.android.app.helpers.Utils;
import co.samepinch.android.data.dto.PushNotification;
import co.samepinch.android.rest.ReqNoBody;
import co.samepinch.android.rest.ReqPosts;
import co.samepinch.android.rest.RestClient;

/**
 * Created by imaginationcoder on 10/16/15.
 */
public class SPParsePushBroadcastReceiver extends ParsePushBroadcastReceiver {
    public static final String INTENT_DATA_JSON = "com.parse.Data";

    @Override
    protected void onPushReceive(Context context, Intent intent) {
        super.onPushReceive(context, intent);

        // background
        long ts = System.currentTimeMillis();
        PushNotification notification = getAppPushNotification(intent);
        if (notification != null && Utils.isValidUri(notification.getAlertImage())) {
            Bitmap notificationImg = getLargeIcon(context, intent);
            while (notificationImg == null && System.currentTimeMillis() - ts < 30000) {
                // simply wait for few seconds
            }
        }
    }

    @Override
    protected void onPushOpen(Context context, Intent intent) {
        try {
            Intent targetIntent = new Intent(context, PushNotificationActivityLauncher.class);
            targetIntent.putExtras(intent.getExtras());
            targetIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(targetIntent);
        } catch (Exception e) {
            super.onPushOpen(context, intent);
        }
    }

    @Override
    protected Bitmap getLargeIcon(Context context, Intent intent) {
        Bitmap response = null;
        try {
            PushNotification notification = getAppPushNotification(intent);
            if (notification == null) {
                return super.getLargeIcon(context, intent);
            }
            Uri imgUri = Uri.parse(notification.getAlertImage());
            ImageRequestBuilder imgReqBldr = ImageRequestBuilder.newBuilderWithSource(imgUri);
            imgReqBldr.setRequestPriority(Priority.HIGH);
            ImageRequest imageRequest = imgReqBldr.build();
            response = fetchImage(imageRequest);
        } catch (Exception e) {
            response = null;
        }

        return response == null ? super.getLargeIcon(context, intent) : response;
    }

    public static PushNotification getAppPushNotification(Intent intent) {
        String dataStr = intent.getStringExtra(INTENT_DATA_JSON);
        if (StringUtils.isNotBlank(dataStr)) {
            Gson gson = new Gson();
            return gson.fromJson(dataStr, PushNotification.class);
        }

        return null;
    }

    public Bitmap fetchImage(ImageRequest imageRequest) {
        // check cache first
        Bitmap cached = fetchImageFromCache(imageRequest);
        if (cached != null) {
            return cached;
        }

        ImagePipeline imagePipeline = Fresco.getImagePipeline();
        final DataSource<CloseableReference<CloseableImage>> dataSource =
                imagePipeline.fetchDecodedImage(imageRequest, SPApplication.getContext());

        final ByteArrayOutputStream bitmapdata = new ByteArrayOutputStream();
        dataSource.subscribe(new BaseBitmapDataSubscriber() {
            @Override
            public void onNewResultImpl(Bitmap bitmap) {
                if (dataSource.isFinished() && bitmap != null) {
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, bitmapdata);
                    if (!dataSource.isClosed()) {
                        dataSource.close();
                    }
                }
            }

            @Override
            public void onFailureImpl(DataSource dataSource) {
                if (!dataSource.isClosed()) {
                    dataSource.close();
                }
            }

            @Override
            public void onCancellation(DataSource<CloseableReference<CloseableImage>> dataSource) {
                super.onCancellation(dataSource);
                if (!dataSource.isClosed()) {
                    dataSource.close();
                }
            }
        }, CallerThreadExecutor.getInstance());

        long start = System.currentTimeMillis();
        long end = start;
        // max.wait for 60000ms for image download
        while (!dataSource.isFinished() && (end - start) <= 60000) {
            // keep waiting
            end = System.currentTimeMillis();
        }
        byte[] bitMapArr = bitmapdata.toByteArray();
        Bitmap bitmap = BitmapFactory.decodeByteArray(bitMapArr, 0, bitMapArr.length);
        return bitmap;
    }

    public Bitmap fetchImageFromCache(ImageRequest imageRequest) {
        ImagePipeline imagePipeline = Fresco.getImagePipeline();
        DataSource<CloseableReference<CloseableImage>> dataSource =
                imagePipeline.fetchImageFromBitmapCache(imageRequest, SPApplication.getContext());
        try {
            CloseableReference<CloseableImage> imageReference = dataSource.getResult();
            if (imageReference != null) {
                try {
                    CloseableImage closeableImage = imageReference.get();
                    if (closeableImage instanceof CloseableStaticBitmap) {
                        return ((CloseableStaticBitmap) closeableImage).getUnderlyingBitmap();
                    }
                } finally {
                    CloseableReference.closeSafely(imageReference);
                }
            }
        } catch (Exception e) {
            // muted
        } finally {
            dataSource.close();
        }

        return null;
    }
}
