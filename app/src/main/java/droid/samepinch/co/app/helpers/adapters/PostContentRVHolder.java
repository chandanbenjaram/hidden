package droid.samepinch.co.app.helpers.adapters;


import android.database.Cursor;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.apache.commons.lang3.StringUtils;

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
                    tView.setText(leftContent);
                    addToView(tView);
                }

                SIMView imgView = new SIMView(mView.getContext());
                imgView.populateImageView(imageKV.get(imgK));
//                imgView.populateImageView("https://i.imgflip.com/ohr0t.gif");
//                imgView.populateImageView("http://www.targeticse.co.in/wp-content/uploads/2010/03/biology.gif");
                addToView(imgView);
            }


            if (StringUtils.isNotBlank(rightContent)) {
                TextView tView = new TextView(mView.getContext());
                tView.setText(rightContent);
                addToView(tView);
            }
        }
    }

    void addToView(View v){
        mViewGroup.addView(v);
    }
}
