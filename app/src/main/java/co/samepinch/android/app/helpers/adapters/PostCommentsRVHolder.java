package co.samepinch.android.app.helpers.adapters;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.flipboard.bottomsheet.BottomSheetLayout;

import org.apache.commons.lang3.StringUtils;

import butterknife.Bind;
import butterknife.ButterKnife;
import co.samepinch.android.app.ActivityFragment;
import co.samepinch.android.app.R;
import co.samepinch.android.app.helpers.AppConstants;
import co.samepinch.android.app.helpers.Utils;
import co.samepinch.android.app.helpers.module.DaggerStorageComponent;
import co.samepinch.android.app.helpers.module.StorageComponent;
import co.samepinch.android.data.dto.CommentDetails;
import co.samepinch.android.data.dto.Commenter;
import co.samepinch.android.data.dto.User;

/**
 * Created by imaginationcoder on 7/27/15.
 */
public class PostCommentsRVHolder extends PostDetailsRVHolder {

    @Bind(R.id.avatar)
    SimpleDraweeView mAvatar;

    @Bind(R.id.avatar_name)
    TextView mAvatarName;

    @Bind(R.id.avatar_handle)
    TextView mAvatarHandle;

    @Bind(R.id.comment_upvote)
    TextView mCommentUpvote;

    @Bind(R.id.comment_date)
    TextView mCommentDate;

    @Bind(R.id.comment)
    TextView mComment;

    @Bind(R.id.comment_menu)
    ImageView commentMenu;

    boolean mAnonymous;
    String mCommenterUID;

    public PostCommentsRVHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }

    void onBindViewHolderImpl(Cursor cursor) {
        CommentDetails commentDetails = Utils.cursorToCommentDetails(cursor);
        String fName, lName, photo, handle;
        mAnonymous = commentDetails.getAnonymous();
        View.OnClickListener dotClick = null;
        if (mAnonymous) {
            StorageComponent component = DaggerStorageComponent.create();
            User anonyOwner = component.provideAnonymousDot();
            fName = anonyOwner.getFname();
            lName = anonyOwner.getLname();
            photo = anonyOwner.getPhoto();
            handle = anonyOwner.getPinchHandle();
        } else {
            final Commenter commenter = commentDetails.getCommenter();
            fName = commenter.getFname();
            lName = commenter.getLname();
            photo = commenter.getPhoto();
            handle = commenter.getPinchHandle();
            dotClick = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // TARGET
                    Bundle args = new Bundle();
                    args.putString(AppConstants.K.TARGET_FRAGMENT.name(), AppConstants.K.FRAGMENT_DOTWALL.name());
                    // data
                    args.putString(AppConstants.K.KEY_DOT.name(), commenter.getUid());

                    // intent
                    Intent intent = new Intent(mView.getContext(), ActivityFragment.class);
                    intent.putExtras(args);

                    mView.getContext().startActivity(intent);
                }
            };
        }

        if (StringUtils.isBlank(photo)) {
            mAvatarName.setVisibility(View.VISIBLE);
            mAvatar.setVisibility(View.INVISIBLE);

            String name = StringUtils.join(StringUtils.substring(fName, 0, 1), StringUtils.substring(lName, 0, 1));
            mAvatarName.setText(name);
            if (dotClick != null) {
                mAvatarName.setOnClickListener(dotClick);
            }

        } else {
            mAvatar.setVisibility(View.VISIBLE);
            mAvatarName.setVisibility(View.GONE);
            Utils.setupLoadingImageHolder(mAvatar, photo);

            if (dotClick != null) {
                mAvatar.setOnClickListener(dotClick);
            }
        }

        // setup handle
        String pinchHandle = String.format(mView.getContext().getString(R.string.pinch_handle), handle);
        mAvatarHandle.setText(pinchHandle);

        // setup counts
        mCommentUpvote.setText(StringUtils.defaultString(Integer.toString(commentDetails.getUpvoteCount()), "0"));

        // setup counts
        mCommentDate.setText(Utils.dateToString(commentDetails.getCreatedAt()));

        // setup comment
        mComment.setText(commentDetails.getText());


        commentMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BottomSheetLayout bottomSheet = (BottomSheetLayout) mView.getRootView().findViewById(R.id.bottomsheet);
                bottomSheet.showWithSheetView(LayoutInflater.from(mView.getContext()).inflate(R.layout.bs_comment_menu, bottomSheet, false));
            }
        });
    }
}
