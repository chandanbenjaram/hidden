package droid.samepinch.co.app.helpers.adapters;

        import android.view.View;
        import android.widget.TextView;

        import droid.samepinch.co.app.R;

/**
 * Created by imaginationcoder on 7/27/15.
 */
public class PostCommentsRVHolder extends PostDetailsRVHolder {
    TextView mCommentText;

    public PostCommentsRVHolder(View itemView) {
        super(itemView);
        mCommentText = (TextView) mView.findViewById(R.id.post_comment);
    }
}
