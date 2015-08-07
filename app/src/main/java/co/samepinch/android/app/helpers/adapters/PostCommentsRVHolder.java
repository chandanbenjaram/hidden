package co.samepinch.android.app.helpers.adapters;

import android.database.Cursor;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.flipboard.bottomsheet.BottomSheetLayout;

import org.apache.commons.lang3.StringUtils;

import butterknife.Bind;
import butterknife.ButterKnife;
import co.samepinch.android.app.R;
import co.samepinch.android.app.helpers.Utils;
import co.samepinch.android.data.dto.CommentDetails;
import co.samepinch.android.data.dto.Commenter;

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

    public PostCommentsRVHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }

    void onBindViewHolderImpl(Cursor cursor) {
        CommentDetails commentDetails = Utils.cursorToCommentDetails(cursor);
        Commenter commenter = commentDetails.getCommenter();
        if (TextUtils.isEmpty(commenter.getPhoto())) {
            mAvatarName.setVisibility(View.VISIBLE);
            mAvatar.setVisibility(View.INVISIBLE);

            String name = StringUtils.join(StringUtils.substring(commenter.getFname(), 0, 1), StringUtils.substring(commenter.getLname(), 0, 1));
            mAvatarName.setText(name);
//            vh.mAvatarName.setOnClickListener(dotClick);
        } else {
            mAvatar.setVisibility(View.VISIBLE);
            mAvatarName.setVisibility(View.GONE);

            // set image
//            Utils.setupLoadingImageHolder(vh.mAvatarView, commenter.getPhoto());
//            vh.mAvatarView.setOnClickListener(dotClick);
            Utils.setupLoadingImageHolder(mAvatar, commenter.getPhoto());
        }

        // setup handle
        String pinchHandle = String.format(mView.getContext().getString(R.string.pinch_handle), commenter.getPinchHandle());
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
