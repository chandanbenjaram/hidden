package droid.samepinch.co.app.helpers;

import android.database.Cursor;
import android.net.Uri;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;

import droid.samepinch.co.data.dao.SchemaDots;
import droid.samepinch.co.data.dao.SchemaPosts;
import droid.samepinch.co.data.dto.Post;
import droid.samepinch.co.data.dto.User;

/**
 * Created by imaginationcoder on 7/4/15.
 */
public class Utils {
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
            user.setPrefName(cursor.getString(pinchHandleIndex));
        }

        return user;
    }

    public static Post cursorToPostEntity(Cursor cursor) {
        if (cursor == null) {
            return null;
        }

        Post post = new Post();
        int uidIndex;
        int contentIndex;
        int commentCountIndex;
        int upvoteCountIndex;
        int viewsIndex;
        int anonymousIndex;
        int createdAtIndex;
        int commentersIndex;
        int tagsIndex;
        if ((uidIndex = cursor.getColumnIndex(SchemaPosts.COLUMN_UID)) != -1) {
            post.setUid(cursor.getString(uidIndex));
        }

        if ((contentIndex = cursor.getColumnIndex(SchemaPosts.COLUMN_CONTENT)) != -1) {
            post.setContent(cursor.getString(contentIndex));
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
}
