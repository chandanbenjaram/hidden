package droid.samepinch.co.app.helpers.widget;

import android.content.Context;
import android.util.AttributeSet;

/**
 * Created by imaginationcoder on 7/28/15.
 */
public class NullSafeRecylerView extends android.support.v7.widget.RecyclerView {
    public NullSafeRecylerView(Context context) {
        super(context);
    }

    public NullSafeRecylerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public NullSafeRecylerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void scrollBy(int x, int y) {
        try {
            super.scrollBy(x, y);
        } catch (NullPointerException nlp) {
            // muted
        }
    }
}
