package droid.samepinch.co.app.helpers.adapters;

        import android.database.Cursor;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import butterknife.Bind;
import butterknife.ButterKnife;
import droid.samepinch.co.app.R;

/**
 * Created by imaginationcoder on 7/27/15.
 */
public class PostCommentsRVHolder extends PostDetailsRVHolder {

    @Bind(R.id.avatar)
    ImageView mAvatar;

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
        mAvatarName.setText("CB");
        mComment.setText("comment..." + System.nanoTime());
    }
}
