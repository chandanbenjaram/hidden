package co.samepinch.android.app.helpers.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import butterknife.Bind;
import butterknife.ButterKnife;
import co.samepinch.android.app.R;

/**
 * Created by imaginationcoder on 7/21/15.
 */
public class ListItem extends RelativeLayout{

    private String mText;
    private Drawable mLeftIcon;

    @Bind(R.id.list_item_left_icon)
    ImageView mLeftIconView;

    @Bind(R.id.list_item_text)
    TextView mTextView;

    public ListItem(Context context){
        super(context);
        init();
        onFinishInflate();
    }

    public ListItem(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
        getAttributes(context, attrs);
    }

    private void init() {
        inflate(getContext(), R.layout.widget_list_item, this);
    }

    private void getAttributes(Context context, AttributeSet attrs) {
        TypedArray typedArray = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.ListItem,
                0, 0);
        try {
            mText = typedArray.getString(R.styleable.ListItem_text);
            mLeftIcon = typedArray.getDrawable(R.styleable.ListItem_leftIcon);

        } finally {
            typedArray.recycle();
        }
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        ButterKnife.bind(this);
        setText(mText);
        setLeftIcon(mLeftIcon);
    }

    public void setLeftIcon(final Drawable leftIcon) {
        if(isInEditMode()) return;
        mLeftIconView.setImageDrawable(leftIcon);
    }

    public void setText(final String text){
        if(isInEditMode()) return;
        mTextView.setText(text);
    }
}
