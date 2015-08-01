package droid.samepinch.co.app.helpers.adapters;

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
import droid.samepinch.co.app.R;
import droid.samepinch.co.app.helpers.Utils;
import droid.samepinch.co.data.dto.CommentDetails;
import droid.samepinch.co.data.dto.Commenter;

/**
 * Created by imaginationcoder on 7/27/15.
 */
public class PostCommentAddRVHolder extends PostDetailsRVHolder {

    @Bind(R.id.comment_text_id)
    TextView mComment;

    public PostCommentAddRVHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }

    void onBindViewHolderImpl(Cursor cursor) {
        // setup comment
        mComment.setText(cursor.getString(1));
    }
}
