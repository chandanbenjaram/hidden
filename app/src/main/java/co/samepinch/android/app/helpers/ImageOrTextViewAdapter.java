package co.samepinch.android.app.helpers;

import android.content.Context;
import android.net.Uri;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ViewSwitcher;

import com.facebook.drawee.view.SimpleDraweeView;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.List;

import co.samepinch.android.app.R;

/**
 * Created by imaginationcoder on 8/25/15.
 */
public class ImageOrTextViewAdapter extends ArrayAdapter<ImageOrTextViewAdapter.ImageOrText> {

    final List<ImageOrText> mItems;

    public ImageOrTextViewAdapter(Context context, int resource, List<ImageOrText> objects) {
        super(context, resource, objects);
        this.mItems = objects;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final ViewHolder vh;
        final ImageOrText currItem = getItem(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.post_create_item, parent, false);
            ViewSwitcher vs = (ViewSwitcher) convertView.findViewById(R.id.post_create_switch);
            vh = new ViewHolder();
            vh.position = position;
            vh.text = (EditText) vs.findViewById(R.id.post_create_text);
            vh.image = (SimpleDraweeView) vs.findViewById(R.id.post_create_image);
            vh.removeButton = (ImageButton) convertView.findViewById(R.id.post_item_remove);
            convertView.setTag(vh);
        } else {
            vh = (ViewHolder) convertView.getTag();
        }
        // text
        if (vh.textWatcher != null) {
            vh.text.removeTextChangedListener(vh.textWatcher);
        }

        vh.text.setText(currItem.getText());
        vh.image.setImageURI(currItem.getImageUri());

        vh.textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                currItem.setText(editable.toString());
            }
        };
        vh.text.addTextChangedListener(vh.textWatcher);
        vh.removeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // grab next text item
                ImageOrText nextSiblingItem = mItems.get(position + 1);
                if (position > 0 && StringUtils.isNotBlank(nextSiblingItem.getText())) {
                    ImageOrText prevSiblingItem = mItems.get(position - 1);
                    prevSiblingItem.setText(StringUtils.join(prevSiblingItem.getText(), "\n", nextSiblingItem.getText()));
                    mItems.remove(position + 1);
                }

                mItems.remove(position);
                notifyDataSetChanged();
            }
        });

        ViewSwitcher vs = (ViewSwitcher) convertView.findViewById(R.id.post_create_switch);
        if (currItem.getImageUri() == null) {
            vs.setDisplayedChild(0);
        } else {
            vs.setDisplayedChild(1);
        }

        return convertView;
    }

    public static class ViewHolder {
        int position;

        EditText text;
        SimpleDraweeView image;
        ImageButton removeButton;

        TextWatcher textWatcher;
        View.OnClickListener removeClickListener;
    }

    public static class ImageOrText {

        Uri imageUri;
        String text;

        public ImageOrText(Uri imageUri, String text) {
            this.imageUri = imageUri;
            this.text = text;
        }

        public void setImageUri(Uri imageUri) {
            this.imageUri = imageUri;
        }

        public void setText(String text) {
            this.text = text;
        }

        public Uri getImageUri() {
            return imageUri;
        }

        public String getText() {
            return text;
        }

        @Override
        public boolean equals(Object o) {
            return super.equals(o);
        }

        @Override
        public int hashCode() {
            if (imageUri != null) {
                return new HashCodeBuilder().append(imageUri.toString()).toHashCode();
            } else {
                return new HashCodeBuilder().append(text).toHashCode();
            }
        }
    }

}
