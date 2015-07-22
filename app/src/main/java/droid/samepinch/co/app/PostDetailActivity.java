package droid.samepinch.co.app;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.internal.widget.ViewUtils;
import android.support.v7.widget.Toolbar;
import android.text.Layout;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ImageSpan;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.facebook.common.references.CloseableReference;
import com.facebook.datasource.DataSource;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.backends.pipeline.PipelineDraweeController;
import com.facebook.drawee.drawable.ScalingUtils;
import com.facebook.drawee.generic.GenericDraweeHierarchy;
import com.facebook.drawee.generic.GenericDraweeHierarchyBuilder;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.common.ResizeOptions;
import com.facebook.imagepipeline.core.ImagePipeline;
import com.facebook.imagepipeline.image.CloseableImage;
import com.facebook.imagepipeline.request.BasePostprocessor;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;
import com.facebook.imagepipeline.request.Postprocessor;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import droid.samepinch.co.app.helpers.Utils;
import droid.samepinch.co.app.helpers.intent.PostDetailsService;
import droid.samepinch.co.app.helpers.widget.IImageKV;
import droid.samepinch.co.app.helpers.widget.TextViewWithImages;
import droid.samepinch.co.data.dao.SchemaPostDetails;
import droid.samepinch.co.data.dao.SchemaPosts;
import droid.samepinch.co.data.dto.Post;
import droid.samepinch.co.data.dto.PostDetails;

import static droid.samepinch.co.app.helpers.AppConstants.APP_INTENT.KEY_UID;

public class PostDetailActivity extends AppCompatActivity {

    private Intent mServiceIntent;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_postdetail);

        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Get the ActionBar here to configure the way it behaves.
        final ActionBar ab = getSupportActionBar();
        //ab.setHomeAsUpIndicator(R.drawable.ic_menu); // set a custom icon for the default home button
        ab.setDisplayShowHomeEnabled(true); // show or hide the default home button
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setDisplayShowCustomEnabled(true); // enable overriding the default toolbar layout
        ab.setDisplayShowTitleEnabled(false);

        LinearLayout contentLayout = (LinearLayout) findViewById(R.id.postdetail_content_layout);
        ViewGroup.LayoutParams lParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        // get caller data
        Bundle iArgs = getIntent().getExtras();
        String postId = iArgs.getString(SchemaPosts.COLUMN_UID);

        PostDetails details = null;
        Cursor cursor = getContentResolver().query(SchemaPostDetails.CONTENT_URI, null, SchemaPostDetails.COLUMN_UID + "=?", new String[]{postId}, null);
        if (cursor.moveToFirst()) {
            details = Utils.cursorToPostDetailsEntity(cursor);
        }

        if (details != null) {
            TextView content = (TextView) findViewById(R.id.postdetail_content);
            ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

            SpannableStringBuilder contentBldr = new SpannableStringBuilder();

            List<String> imageKArr = getImageValues(details.getContent());

            String fullContent = details.getContent();
            Map<String, String> imageKV = details.getImages();
            String strToAdd;
            String nl = System.getProperty ("line.separator");
            for(String imgK : imageKArr){

                strToAdd = StringUtils.substringBefore(fullContent, imgK).replaceAll("::", "");
                //strToAdd +=nl;
                int start = StringUtils.indexOf(fullContent, strToAdd);
                int end = start + strToAdd.length();
                if(StringUtils.isNotBlank(strToAdd)){
                    contentBldr.append(strToAdd);
                }

                fullContent = StringUtils.substringAfter(fullContent, imgK);

                String imgV = imageKV.get(imgK);
                ImageRequest imageRequest =
                        ImageRequestBuilder.newBuilderWithSource(Uri.parse(imgV)).setResizeOptions(
                                new ResizeOptions(150, 150))
                                .build();
//                DraweeController controller = Fresco.newDraweeControllerBuilder()
//                        .setImageRequest(imageRequest)
//                        .setAutoPlayAnimations(true)
//                        .build();
//                SimpleDraweeView sdView = new SimpleDraweeView(this);
//                sdView.setController(controller);


                ImageSpan imgSpan = new ImageSpan(getApplicationContext(), image);

                //                Drawable d = sdView.getDrawable();
//                d.setBounds(0, 0, 150, 150);
//                BitmapDrawable drawable = (BitmapDrawable) sdView.getDrawable();
//
//                ImageSpan imgSpan = new ImageSpan(getApplicationContext(), drawable.getBitmap());
//
////                contentBldr.append(System.getProperty("line.separator"));
//                //contentBldr.setSpan(imgSpan, end, end+1, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
//                contentBldr.setSpan(imgSpan, contentBldr.length()-1, contentBldr.length(), 0);
            }

            content.setText(contentBldr);
            content.setMovementMethod(LinkMovementMethod.getInstance());
        }


        // construct context from preferences if any?
        Bundle iServiceArgs = new Bundle();
        iServiceArgs.putString(KEY_UID.getValue(), postId);

        // call for intent
        mServiceIntent =
                new Intent(getApplicationContext(), PostDetailsService.class);
        mServiceIntent.putExtras(iArgs);
        startService(mServiceIntent);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void loadBackdrop() {
        final ImageView imageView = (ImageView) findViewById(R.id.backdrop);
        //Glide.with(this).load(Cheeses.getRandomCheeseDrawable()).centerCrop().into(imageView);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.sample_actions, menu);
        return true;
    }

    private static final Pattern IMG_PATTERN = Pattern.compile("::(.*?)(::)");

    private static List<String> getImageValues(final String str) {
        final List<String> imgVals = new ArrayList<>();
        final Matcher matcher = IMG_PATTERN.matcher(str);
        while (matcher.find()) {
            imgVals.add(matcher.group(1));
        }
        return imgVals;
    }
}