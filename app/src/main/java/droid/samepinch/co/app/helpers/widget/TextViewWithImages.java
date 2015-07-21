package droid.samepinch.co.app.helpers.widget;

import android.content.Context;
import android.text.Spannable;
import android.text.style.ImageSpan;
import android.util.AttributeSet;
import android.widget.TextView;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by imaginationcoder on 7/21/15.
 */
public class TextViewWithImages extends TextView {
    private static final Spannable.Factory spannableFactory = Spannable.Factory.getInstance();

    public TextViewWithImages(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }
    public TextViewWithImages(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    public TextViewWithImages(Context context) {
        super(context);
    }
    @Override
    public void setText(CharSequence text, BufferType type) {
        Spannable s = getTextWithImages(getContext(), text);
        super.setText(s, BufferType.SPANNABLE);
    }

    private static boolean addImages(Context context, Spannable spannable) {
        Pattern refImg = Pattern.compile("::(.*?)(::)");
        boolean hasChanges = false;

        Matcher matcher = refImg.matcher(spannable);
        while (matcher.find()) {
            boolean set = true;
            for (ImageSpan span : spannable.getSpans(matcher.start(), matcher.end(), ImageSpan.class)) {
                if (spannable.getSpanStart(span) >= matcher.start()
                        && spannable.getSpanEnd(span) <= matcher.end()
                        ) {
                    spannable.removeSpan(span);
                } else {
                    set = false;
                    break;
                }
            }
            String resname = spannable.subSequence(matcher.start(1), matcher.end(1)).toString().trim();
            int id = context.getResources().getIdentifier(resname, "drawable", context.getPackageName());
            if (set) {
                hasChanges = true;
                spannable.setSpan(  new ImageSpan(context, id),
                        matcher.start(),
                        matcher.end(),
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                );
            }
        }

        return hasChanges;
    }
    private static Spannable getTextWithImages(Context context, CharSequence text) {
        Spannable spannable = spannableFactory.newSpannable(text);
        addImages(context, spannable);
        return spannable;
    }
}