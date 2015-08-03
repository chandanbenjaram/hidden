package droid.samepinch.co.app.helpers;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.View;
import android.widget.TextView;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;

import org.apache.commons.lang3.time.DateFormatUtils;
import org.json.JSONObject;
import org.springframework.http.ResponseEntity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import droid.samepinch.co.app.ActivityFragment;
import droid.samepinch.co.data.dao.SchemaComments;
import droid.samepinch.co.data.dao.SchemaDots;
import droid.samepinch.co.data.dao.SchemaPostDetails;
import droid.samepinch.co.data.dao.SchemaPosts;
import droid.samepinch.co.data.dto.CommentDetails;
import droid.samepinch.co.data.dto.Commenter;
import droid.samepinch.co.data.dto.Post;
import droid.samepinch.co.data.dto.PostDetails;
import droid.samepinch.co.data.dto.User;
import droid.samepinch.co.rest.RestClient;

import static droid.samepinch.co.app.helpers.AppConstants.API.DEFAULT_DATE_FORMAT;
import static droid.samepinch.co.app.helpers.AppConstants.KV.CLIENT_ID;
import static droid.samepinch.co.app.helpers.AppConstants.KV.CLIENT_SECRET;
import static droid.samepinch.co.app.helpers.AppConstants.KV.GRANT_TYPE;
import static droid.samepinch.co.app.helpers.AppConstants.KV.SCOPE;

/**
 * Created by imaginationcoder on 7/4/15.
 */
public class Utils {
    private static final Pattern IMG_PATTERN = Pattern.compile("::(.*?)(::)");

    public static User cursorToUserEntity(Cursor cursor) {
        if (cursor == null) {
            return null;
        }

        User user = new User();
        int uidIndex;
        int fNameIndex;
        int lNameIndex;
        int prefNameIndex;
        int pinchHandleIndex;
        if ((uidIndex = cursor.getColumnIndex(SchemaDots.COLUMN_UID)) != -1) {
            user.setUid(cursor.getString(uidIndex));
        }
        if ((fNameIndex = cursor.getColumnIndex(SchemaDots.COLUMN_FNAME)) != -1) {
            user.setFname(cursor.getString(fNameIndex));
        }
        if ((lNameIndex = cursor.getColumnIndex(SchemaDots.COLUMN_LNAME)) != -1) {
            user.setLname(cursor.getString(lNameIndex));
        }
        if ((prefNameIndex = cursor.getColumnIndex(SchemaDots.COLUMN_PREF_NAME)) != -1) {
            user.setPrefName(cursor.getString(prefNameIndex));
        }
        if ((pinchHandleIndex = cursor.getColumnIndex(SchemaDots.COLUMN_PINCH_HANDLE)) != -1) {
            user.setPinchHandle(cursor.getString(pinchHandleIndex));
        }

        return user;
    }

    public static Post cursorToPostEntity(Cursor cursor) {
        if (cursor == null) {
            return null;
        }

        Post post = new Post();
        int createdAtIndex;
        int uidIndex;
        int contentIndex;
        int imagesIndex;
        int commentCountIndex;
        int upvoteCountIndex;
        int viewsIndex;
        int anonymousIndex;
        int commentersIndex;
        int tagsIndex;
        if ((createdAtIndex = cursor.getColumnIndex(SchemaPosts.COLUMN_CREATED_AT)) != -1) {
            post.setCreatedAt(new Date(cursor.getLong(createdAtIndex)));
        }
        if ((uidIndex = cursor.getColumnIndex(SchemaPosts.COLUMN_UID)) != -1) {
            post.setUid(cursor.getString(uidIndex));
        }

        if ((contentIndex = cursor.getColumnIndex(SchemaPosts.COLUMN_CONTENT)) != -1) {
            post.setContent(cursor.getString(contentIndex));
        }
        if ((imagesIndex = cursor.getColumnIndex(SchemaPosts.COLUMN_IMAGES)) != -1) {
            post.setImagesFromDB(cursor.getString(imagesIndex));
        }

        if ((commentCountIndex = cursor.getColumnIndex(SchemaPosts.COLUMN_COMMENT_COUNT)) != -1) {
            post.setCommentCount(cursor.getInt(commentCountIndex));
        }

        if ((upvoteCountIndex = cursor.getColumnIndex(SchemaPosts.COLUMN_UPVOTE_COUNT)) != -1) {
            post.setUpvoteCount(cursor.getInt(upvoteCountIndex));
        }

        if ((viewsIndex = cursor.getColumnIndex(SchemaPosts.COLUMN_VIEWS)) != -1) {
            post.setViews(cursor.getInt(viewsIndex));
        }

        if ((anonymousIndex = cursor.getColumnIndex(SchemaPosts.COLUMN_ANONYMOUS)) != -1) {
            post.setAnonymous(cursor.getInt(anonymousIndex) == 1);
        }

        if ((commentersIndex = cursor.getColumnIndex(SchemaPosts.COLUMN_COMMENTERS)) != -1) {
            post.setCommentersFromDB(cursor.getString(commentersIndex));
        }

        if ((tagsIndex = cursor.getColumnIndex(SchemaPosts.COLUMN_TAGS)) != -1) {
            post.setTagsFromDB(cursor.getString(tagsIndex));
        }
//
//        if (post.getAnonymous()) {
//            return post;
//        }

        int ownerFNameIndex;
        int ownerLNameIndex;
        int ownerPrefNameIndex;
        int ownerPinchHandleIndex;
        int ownerPhotoUrlIndex;
        int ownerUIdIndex;
        User user = new User();
        if ((ownerUIdIndex = cursor.getColumnIndex(SchemaPosts.COLUMN_OWNER)) != -1) {
            user.setUid(cursor.getString(ownerUIdIndex));
        }

        if ((ownerFNameIndex = cursor.getColumnIndex(SchemaDots.COLUMN_FNAME)) != -1) {
            user.setFname(cursor.getString(ownerFNameIndex));
        }

        if ((ownerLNameIndex = cursor.getColumnIndex(SchemaDots.COLUMN_LNAME)) != -1) {
            user.setLname(cursor.getString(ownerLNameIndex));
        }

        if ((ownerPrefNameIndex = cursor.getColumnIndex(SchemaDots.COLUMN_PREF_NAME)) != -1) {
            user.setPrefName(cursor.getString(ownerPrefNameIndex));
        }

        if ((ownerPinchHandleIndex = cursor.getColumnIndex(SchemaDots.COLUMN_PINCH_HANDLE)) != -1) {
            user.setPinchHandle(cursor.getString(ownerPinchHandleIndex));
        }

        if ((ownerPhotoUrlIndex = cursor.getColumnIndex(SchemaDots.COLUMN_PHOTO_URL)) != -1) {
            user.setPhoto(cursor.getString(ownerPhotoUrlIndex));
        }

        post.setOwner(user);
        return post;
    }

    public static PostDetails cursorToPostDetailsEntity(Cursor cursor) {
        if (cursor == null) {
            return null;
        }

        PostDetails details = new PostDetails();
        int uidIndex;
        int contentIndex;
        int imagesIndex;
        int largeImagesIndex;
        int commentCountIndex;
        int upvoteCountIndex;
        int viewsIndex;
        int anonymousIndex;
        int createdAtIndex;
        int commentersIndex;
        int tagsIndex;
        if ((uidIndex = cursor.getColumnIndex(SchemaPostDetails.COLUMN_UID)) != -1) {
            details.setUid(cursor.getString(uidIndex));
        }

        if ((contentIndex = cursor.getColumnIndex(SchemaPostDetails.COLUMN_CONTENT)) != -1) {
            details.setContent(cursor.getString(contentIndex));
        }
        if ((imagesIndex = cursor.getColumnIndex(SchemaPostDetails.COLUMN_IMAGES)) != -1) {
            details.setImagesFromDB(cursor.getString(imagesIndex));
        }
        if ((largeImagesIndex = cursor.getColumnIndex(SchemaPostDetails.COLUMN_LARGE_IMAGES)) != -1) {
            details.setLargeImagesFromDB(cursor.getString(largeImagesIndex));
        }

        if ((commentCountIndex = cursor.getColumnIndex(SchemaPostDetails.COLUMN_COMMENT_COUNT)) != -1) {
            details.setCommentCount(cursor.getInt(commentCountIndex));
        }

        if ((upvoteCountIndex = cursor.getColumnIndex(SchemaPostDetails.COLUMN_UPVOTE_COUNT)) != -1) {
            details.setUpvoteCount(cursor.getInt(upvoteCountIndex));
        }

        if ((viewsIndex = cursor.getColumnIndex(SchemaPostDetails.COLUMN_VIEWS)) != -1) {
            details.setViews(cursor.getInt(viewsIndex));
        }

        if ((anonymousIndex = cursor.getColumnIndex(SchemaPostDetails.COLUMN_ANONYMOUS)) != -1) {
            details.setAnonymous(cursor.getInt(anonymousIndex) == 1);
        }

//        if ((commentersIndex = cursor.getColumnIndex(SchemaPostDetails.COLUMN_COMMENTERS)) != -1) {
//            post.setCommentersFromDB(cursor.getString(commentersIndex));
//        }

        if ((tagsIndex = cursor.getColumnIndex(SchemaPostDetails.COLUMN_TAGS)) != -1) {
            details.setTagsFromDB(cursor.getString(tagsIndex));
        }
//
        // dot related
        User owner = new User();
        details.setOwner(owner);
        int dotUIDIndex;
        if ((dotUIDIndex = cursor.getColumnIndex(SchemaPostDetails.COLUMN_OWNER)) != -1) {
            owner.setUid(cursor.getString(dotUIDIndex));
        }

        return details;
    }

    public static CommentDetails cursorToCommentDetails(Cursor cursor) {
        if (cursor == null) {
            return null;
        }

        CommentDetails dto = new CommentDetails();
        int uidIndex;
        if ((uidIndex = cursor.getColumnIndex(SchemaComments.COLUMN_UID)) != -1) {
            dto.setUid(cursor.getString(uidIndex));
        }

        int createdAtIndex;
        if ((createdAtIndex = cursor.getColumnIndex(SchemaComments.COLUMN_CREATED_AT)) != -1) {
            dto.setCreatedAt(new Date(cursor.getLong(createdAtIndex)));
        }

        int anonymousIndex;
        if ((anonymousIndex = cursor.getColumnIndex(SchemaComments.COLUMN_ANONYMOUS)) != -1) {
            dto.setAnonymous(cursor.getInt(anonymousIndex) == 1);
        }

        int textIndex;
        if ((textIndex = cursor.getColumnIndex(SchemaComments.COLUMN_TEXT)) != -1) {
            dto.setText(cursor.getString(textIndex));
        }

        int upvoteCountIndex;
        if ((upvoteCountIndex = cursor.getColumnIndex(SchemaComments.COLUMN_UPVOTE_COUNT)) != -1) {
            dto.setUpvoteCount(cursor.getInt(upvoteCountIndex));
        }

        // commenter related
        Commenter commenterDto = new Commenter();
        dto.setCommenter(commenterDto);

        int commenterUidIndex;
        if ((commenterUidIndex = cursor.getColumnIndex(SchemaComments.COLUMN_DOT_UID)) != -1) {
            commenterDto.setUid(cursor.getString(commenterUidIndex));
        }

        int commenterFnameIndex;
        if ((commenterFnameIndex = cursor.getColumnIndex(SchemaComments.COLUMN_DOT_FNAME)) != -1) {
            commenterDto.setFname(cursor.getString(commenterFnameIndex));
        }

        int commenterLnameIndex;
        if ((commenterLnameIndex = cursor.getColumnIndex(SchemaComments.COLUMN_DOT_LNAME)) != -1) {
            commenterDto.setLname(cursor.getString(commenterLnameIndex));
        }

        int commenterHandleIndex;
        if ((commenterHandleIndex = cursor.getColumnIndex(SchemaComments.COLUMN_DOT_PINCH_HANDLE)) != -1) {
            commenterDto.setPinchHandle(cursor.getString(commenterHandleIndex));
        }

        int commenterPhotoIndex;
        if ((commenterPhotoIndex = cursor.getColumnIndex(SchemaComments.COLUMN_DOT_PHOTO_URL)) != -1) {
            commenterDto.setPhoto(cursor.getString(commenterPhotoIndex));
        }

        return dto;
    }



    public static void setupLoadingImageHolder(SimpleDraweeView iView, String imageUrl) {
        Uri uri = Uri.parse(imageUrl);
        ImageRequest request = ImageRequestBuilder.newBuilderWithSource(uri)
                .setProgressiveRenderingEnabled(true) //Progressive loading
                .build();
        DraweeController controller = Fresco.newDraweeControllerBuilder()
                .setImageRequest(request)
                .setOldController(iView.getController())
                .build();
        iView.setController(controller);
    }

    public static String emptyIfNull(String arg0) {
        return arg0 == null ? "" : arg0;
    }


    public static String getAppToken(boolean renew) {
        Utils.PreferencesManager pref = Utils.PreferencesManager.getInstance();
        String token = pref.getValue(AppConstants.API.ACCESS_TOKEN.getValue());
        if (!renew && token != null) {
            return token;
        }

        //time to fetch a token
        final Map<String, String> payload = new HashMap<>();
        payload.put(CLIENT_ID.getKey(), CLIENT_ID.getValue());
        payload.put(CLIENT_SECRET.getKey(), CLIENT_SECRET.getValue());
        payload.put(SCOPE.getKey(), SCOPE.getValue());
        payload.put(GRANT_TYPE.getKey(), GRANT_TYPE.getValue());

        ResponseEntity<Map> response = RestClient.INSTANCE.handle().postForEntity(AppConstants.API.CLIENTAUTH.getValue(), payload, Map.class);
        Map<String, String> responseEntity = response.getBody();
        for (Map.Entry<String, String> entry : responseEntity.entrySet()) {
            pref.setValue(String.valueOf(entry.getKey()), String.valueOf(entry.getValue()));
        }

        return pref.getValue(AppConstants.API.ACCESS_TOKEN.getValue());
    }

    public static class PreferencesManager {

        private static PreferencesManager sInstance;
        private final SharedPreferences mPref;

        private PreferencesManager(Context context) {
            mPref = context.getSharedPreferences(AppConstants.API.SHARED_PREFS_NAME.getValue(), Context.MODE_PRIVATE);
        }

        public static synchronized void initializeInstance(Context context) {
            if (sInstance == null) {
                sInstance = new PreferencesManager(context);
            }
        }

        public static synchronized PreferencesManager getInstance() {
            if (sInstance == null) {
                throw new IllegalStateException(PreferencesManager.class.getSimpleName() +
                        " is not initialized, call initializeInstance(..) method first.");
            }
            return sInstance;
        }

        public void setValue(String key, String val) {
            mPref.edit()
                    .putString(key, val)
                    .commit();
        }

        public String getValue(String key) {
            return mPref.getString(key, "");
        }

        public void setValue(String key, Map<String, String> val) {
            // null val deletes preferences enty
            if (val == null) {
                remove(key);
                return;
            }
            JSONObject jsonObject = new JSONObject(val);
            String valStr = jsonObject.toString();
            setValue(key, valStr);
        }

        public <T> Map<String, T> getValueAsMap(String key) {
            Map<String, T> outputMap = new HashMap<>();
            try {
                String jsonString = mPref.getString(key, (new JSONObject()).toString());
                JSONObject jsonObject = new JSONObject(jsonString);
                Iterator<String> keysItr = jsonObject.keys();
                while (keysItr.hasNext()) {
                    String k = keysItr.next();
                    T _value = (T) jsonObject.get(k);
                    outputMap.put(k, _value);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return outputMap;
        }

        public void remove(String key) {
            mPref.edit()
                    .remove(key)
                    .commit();
        }

        public boolean clear() {
            return mPref.edit()
                    .clear()
                    .commit();
        }
    }

    public static Date string2Date(String stringDate) {
        Date date = null;
        if (stringDate == null) {
            return date;
        }

        SimpleDateFormat sdf = new SimpleDateFormat(DEFAULT_DATE_FORMAT.getValue());
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));

        try {
            date = sdf.parse(stringDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return date;
    }

    public static String dateToString(long ts){
        long localTS = ts - TimeZone.getDefault().getRawOffset();
        return DateFormatUtils.format(localTS, DEFAULT_DATE_FORMAT.getValue());
    }

    public static String dateToString(Date date){
        if(date == null){
            return "";
        }
        return DateFormatUtils.format(date, DEFAULT_DATE_FORMAT.getValue());
    }

    public static List<String> getImageValues(final String str) {
        final List<String> imgVals = new ArrayList<>();
        final Matcher matcher = IMG_PATTERN.matcher(str);
        while (matcher.find()) {
            imgVals.add(matcher.group(1));
        }
        return imgVals;
    }

    public static void markTags(final Context context, TextView view, String[] tags) {
        SpannableStringBuilder spanTxt = new SpannableStringBuilder();

        for (final String tag : tags) {
            spanTxt.append(tag);
            spanTxt.setSpan(new ClickableSpan() {
                @Override
                public void onClick(View widget) {
                    // TARGET
                    Bundle args = new Bundle();
                    args.putString(AppConstants.K.TARGET_FRAGMENT.name(), AppConstants.K.FRAGMENT_TAGWALL.name());
                    // data
                    args.putString(AppConstants.K.KEY_TAG.name(), tag);

                    // intent
                    Intent intent = new Intent(context, ActivityFragment.class);
                    intent.putExtras(args);

                    context.startActivity(intent);
                }

                @Override
                public void updateDrawState(TextPaint ds) {
                    super.updateDrawState(ds);
                    ds.setUnderlineText(false);
                    ds.setColor(Color.BLUE);
                }
            }, spanTxt.length() - tag.length(), spanTxt.length(), 0);
            spanTxt.append(" ");
        }

        view.setMovementMethod(LinkMovementMethod.getInstance());
        view.setText(spanTxt, TextView.BufferType.SPANNABLE);
    }
}
