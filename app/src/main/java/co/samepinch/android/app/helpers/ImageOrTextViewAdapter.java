package co.samepinch.android.app.helpers;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.facebook.drawee.view.SimpleDraweeView;

import java.util.List;

import co.samepinch.android.app.R;

/**
 * Created by imaginationcoder on 8/25/15.
 */
public class ImageOrTextViewAdapter extends ArrayAdapter<ImageOrTextViewAdapter.ImageOrText> {

    public ImageOrTextViewAdapter(Context context, int resource, List<ImageOrText> objects) {
        super(context, resource, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ImageOrText currItem = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.post_create_item, parent, false);
        }

        ViewSwitcher vs = (ViewSwitcher) convertView.findViewById(R.id.post_create_switch);
        if (currItem.getImageUri() == null) {
            TextView textView = (TextView) vs.getChildAt(0);
            textView.setText(currItem.getText());
            vs.setDisplayedChild(0);
        } else {
            SimpleDraweeView imageView = (SimpleDraweeView) vs.getChildAt(1);
            imageView.setImageURI(currItem.getImageUri());
            vs.setDisplayedChild(1);
        }


        return convertView;
    }

    public static class ImageOrText {

        final Uri imageUri;
        final String text;

        public ImageOrText(Uri imageUri, String text) {
            this.imageUri = imageUri;
            this.text = text;
        }

        public Uri getImageUri() {
            return imageUri;
        }

        public String getText() {
            return text;
        }
    }
}
