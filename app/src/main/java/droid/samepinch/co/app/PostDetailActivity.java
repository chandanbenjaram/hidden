package droid.samepinch.co.app;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import droid.samepinch.co.app.helpers.CommentsFragment;
import droid.samepinch.co.app.helpers.Utils;
import droid.samepinch.co.app.helpers.intent.PostDetailsService;
import droid.samepinch.co.app.helpers.widget.SIMView;
import droid.samepinch.co.data.dao.SchemaPostDetails;
import droid.samepinch.co.data.dao.SchemaPosts;
import droid.samepinch.co.data.dto.PostDetails;

import static droid.samepinch.co.app.helpers.AppConstants.APP_INTENT.KEY_UID;

public class PostDetailActivity extends AppCompatActivity implements CommentsFragment.CommentsFragmentCallbackListener {
    private static final Pattern IMG_PATTERN = Pattern.compile("::(.*?)(::)");
    public static final String LOG_TAG = "PostDetailActivity";

    private Intent mServiceIntent;
    LinearLayout mContentLayout;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_postdetail);

        Utils.PreferencesManager.initializeInstance(getApplicationContext());


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        TextView dummyView = (TextView) findViewById(R.id.dummy_txt);
        StringBuilder dummyTxt = new StringBuilder();
        for(int i=0; i< 200; i++){
            dummyTxt.append(System.lineSeparator() + "DUMMY TEXT..." + i);
        }
        dummyView.setText(dummyTxt);

        // Get the ActionBar here to configure the way it behaves.
        final ActionBar ab = getSupportActionBar();
        //ab.setHomeAsUpIndicator(R.drawable.ic_menu); // set a custom icon for the default home button
        ab.setDisplayShowHomeEnabled(true); // show or hide the default home button
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setDisplayShowCustomEnabled(false); // enable overriding the default toolbar layout
        ab.setDisplayShowTitleEnabled(false);

//        LinearLayout contentLayout = (LinearLayout) findViewById(R.id.postdetail_content_layout);

        // get caller data
        Bundle iArgs = getIntent    ().getExtras();
        String postId = iArgs.getString(SchemaPosts.COLUMN_UID);

        PostDetails details = null;
        Cursor cursor = getContentResolver().query(SchemaPostDetails.CONTENT_URI, null, SchemaPostDetails.COLUMN_UID + "=?", new String[]{postId}, null);
        if (cursor.moveToFirst()) {
            details = Utils.cursorToPostDetailsEntity(cursor);
        }
        cursor.close();
        if (details != null) {

            List<String> imageKArr = getImageValues(details.getContent());
            String rightContent = details.getContent();

            Map<String, String> imageKV = details.getImages();
            String leftContent;
            LayoutInflater inflater = LayoutInflater.from(PostDetailActivity.this);
             mContentLayout = (LinearLayout) inflater.inflate(R.layout.layout_postdetails_content, null);
            for (String imgK : imageKArr) {
                // get left of image
                leftContent = StringUtils.substringBefore(rightContent, imgK).replaceAll("::", "");

                // grab right remaining chunk
                rightContent = StringUtils.substringAfter(rightContent, imgK).replaceAll("::", "");
                if (StringUtils.isNotBlank(leftContent)) {
                    TextView tView = new TextView(PostDetailActivity.this);
                    tView.setText(leftContent);
                    mContentLayout.addView(tView);
                }


                SIMView fImageView = new SIMView(PostDetailActivity.this);
                fImageView.populateImageView(imageKV.get(imgK));
                //fImageView.populateImageView("https://i.imgflip.com/ohr0t.gif");
//                fImageView.populateImageView("http://www.targeticse.co.in/wp-content/uploads/2010/03/biology.gif");
                mContentLayout.addView(fImageView);
            }

            if (StringUtils.isNotBlank(rightContent)) {
                TextView tView = new TextView(PostDetailActivity.this);
                tView.setText(rightContent);
                mContentLayout.addView(tView);
            }
        }

        // construct context from preferences if any?
        Bundle iServiceArgs = new Bundle();
        iServiceArgs.putString(KEY_UID.getValue(), postId);


        // call for intent
        mServiceIntent =
                new Intent(getApplicationContext(), PostDetailsService.class);
        mServiceIntent.putExtras(iArgs);
        startService(mServiceIntent);


        if (StringUtils.isNotBlank(postId) && findViewById(R.id.comments_container) != null) {
            if (savedInstanceState != null) {
                return;
            }
            CommentsFragment commentsFragment = new CommentsFragment();
            commentsFragment.setMHeaderView(mContentLayout);
            Bundle args = new Bundle();
            args.putString(SchemaPosts.COLUMN_UID, postId);
            commentsFragment.setArguments(args);

            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.comments_container, commentsFragment);
            transaction.addToBackStack(null);
            transaction.commit();
        }

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


    private static List<String> getImageValues(final String str) {
        final List<String> imgVals = new ArrayList<>();
        final Matcher matcher = IMG_PATTERN.matcher(str);
        while (matcher.find()) {
            imgVals.add(matcher.group(1));
        }
        return imgVals;
    }

    @Override
    public void onCommentClick(int position) {
        Log.i(LOG_TAG, "position=" + position);
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}