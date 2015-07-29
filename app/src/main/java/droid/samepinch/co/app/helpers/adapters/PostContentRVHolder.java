package droid.samepinch.co.app.helpers.adapters;


import android.database.Cursor;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bluejamesbond.text.DocumentView;
import com.bluejamesbond.text.style.TextAlignment;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import java.util.List;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;
import droid.samepinch.co.app.R;
import droid.samepinch.co.app.helpers.Utils;
import droid.samepinch.co.app.helpers.widget.SIMView;
import droid.samepinch.co.data.dto.PostDetails;

/**
 * Created by imaginationcoder on 7/27/15.
 */
public class PostContentRVHolder extends PostDetailsRVHolder{

    @Bind(R.id.post_details_content)
    ViewGroup mViewGroup;

    @Bind(R.id.post_dot_with_handle)
    TextView mPostDotWithHandle;

    @Bind(R.id.post_vote_count)
    TextView mPostVoteCount;

    @Bind(R.id.post_views_count)
    TextView mPostViewsCount;

    public PostContentRVHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
        setIsRecyclable(false);
    }

    void onBindViewHolderImpl(Cursor cursor){
        // do nothing in base
        PostDetails details = null;
        if (cursor.moveToFirst()) {
            details = Utils.cursorToPostDetailsEntity(cursor);
        }

        if (details != null) {
            mPostDotWithHandle.setText("N/A");
            mPostVoteCount.setText(String.valueOf(details.getUpvoteCount() == null? 0 : details.getUpvoteCount()));
            mPostViewsCount.setText(String.valueOf(details.getViews() == null? 0: details.getViews()));

            List<String> imageKArr = Utils.getImageValues(details.getContent());
            String rightContent = details.getContent();

            Map<String, String> imageKV = details.getImages();
            for (String imgK : imageKArr) {
                // get left of image
                String leftContent = StringUtils.substringBefore(rightContent, imgK).replaceAll("::", "");

                // grab right remaining chunk
                rightContent = StringUtils.substringAfter(rightContent, imgK).replaceAll("::", "");
                if (StringUtils.isNotBlank(leftContent)) {
                    TextView tView = new TextView(mView.getContext());
                    tView.setPadding(5, 0, 5, 0);
                    tView.setText(leftContent);
                    addToView(tView);

//                    DocumentView documentView = new DocumentView(mView.getContext(), DocumentView.PLAIN_TEXT);
//                    documentView.getDocumentLayoutParams().setTextAlignment(TextAlignment.JUSTIFIED);
//                    documentView.setText(leftContent);
//                    addToView(documentView);


                }

                SIMView imgView = new SIMView(mView.getContext());
                imgView.populateImageView(imageKV.get(imgK));
//                imgView.populateImageView("https://i.imgflip.com/ohr0t.gif");
//                imgView.populateImageView("http://www.targeticse.co.in/wp-content/uploads/2010/03/biology.gif");
                addToView(imgView);
            }


            if (StringUtils.isNotBlank(rightContent)) {
                TextView tView = new TextView(mView.getContext());
                tView.setPadding(5, 0, 5, 0);
                tView.setText(rightContent);
                addToView(tView);
            }

            // add tags at the end
            String tags = details.getTagsForDB();
            if(StringUtils.isNotBlank(tags)){
                TextView tView = new TextView(mView.getContext());
                tView.setGravity(View.TEXT_ALIGNMENT_CENTER);
                Utils.markTags(mView.getContext(), tView, tags.split(","));
                addToView(tView);
            }
        }
    }

    void addToView(View v){
        mViewGroup.addView(v);
    }
}
