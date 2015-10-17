package co.samepinch.android.app.helpers.misc;

import android.app.Activity;
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
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;
import com.parse.ParsePushBroadcastReceiver;

import java.io.ByteArrayOutputStream;

import co.samepinch.android.app.SPApplication;

/**
 * Created by imaginationcoder on 10/16/15.
 */
public class SPParsePushBroadcastReceiver extends ParsePushBroadcastReceiver {

    @Override
    protected void onPushReceive(Context context, Intent intent) {
        super.onPushReceive(context, intent);
    }

    @Override
    protected Class<? extends Activity> getActivity(Context context, Intent intent) {
        return super.getActivity(context, intent);
    }

    @Override
    protected Bitmap getLargeIcon(Context context, Intent intent) {
        try {
            String path = "http://imgsv.imaging.nikon.com/lineup/dslr/d800/img/sample01/img_02.png";
            ImageRequestBuilder imgReqBldr = ImageRequestBuilder.newBuilderWithSource(Uri.parse(path));
            imgReqBldr.setRequestPriority(Priority.HIGH);
            ImageRequest imageRequest = imgReqBldr.build();

            return fetchImage(imageRequest);
        } catch (Exception e) {
            // muted
        }

        return super.getLargeIcon(context, intent);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        // track open analytics
        // ParseAnalytics.trackAppOpenedInBackground(intent);
        super.onReceive(context, intent);
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
                    dataSource.close();
                }
            }

            @Override
            public void onFailureImpl(DataSource dataSource) {
                // No cleanup required here.
            }
        }, CallerThreadExecutor.getInstance());
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
                } finally {
                    CloseableReference.closeSafely(imageReference);
                }
            }
        } catch (Exception e) {
            boolean isTrue = e == null;
            e.printStackTrace();

        } finally {
            dataSource.close();
        }

        return null;
    }
}
