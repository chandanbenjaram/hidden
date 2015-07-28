package droid.samepinch.co.app.helpers.adapters;

        import android.database.Cursor;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;

import org.apache.commons.lang3.StringUtils;

import butterknife.Bind;
import butterknife.ButterKnife;
import droid.samepinch.co.app.R;
import droid.samepinch.co.app.helpers.Utils;
import droid.samepinch.co.data.dto.CommentDetails;
import droid.samepinch.co.data.dto.Commenter;

/**
 * Created by imaginationcoder on 7/27/15.
 */
public class PostCommentsRVHolder extends PostDetailsRVHolder {

    @Bind(R.id.avatar)
    SimpleDraweeView mAvatar;

    @Bind(R.id.avatar_name)
    TextView mAvatarName;

    @Bind(R.id.comment)
    TextView mComment;


    public PostCommentsRVHolder(View itemView) {
        super(itemView);
//        LayoutInflater.from(itemView.getContext()).inflate(R.layout.post_comment_item, (ViewGroup)itemView);
        ButterKnife.bind(this, itemView);
    }

    void onBindViewHolderImpl(Cursor cursor){
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

        mComment.setText(commentDetails.getText());
    }
}
