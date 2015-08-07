package co.samepinch.android.app.helpers.widget;

import android.content.Context;
import android.text.Spannable;
import android.text.style.ImageSpan;
import android.widget.TextView;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by imaginationcoder on 7/21/15.
 */
public class TextViewWithImages extends TextView {
    private static final Spannable.Factory spannableFactory = Spannable.Factory.getInstance();

    public TextViewWithImages(Context context) {
        super(context);
    }

//    @Override
//    public void setText(CharSequence text, BufferType type) {
//        Spannable s = getTextWithImages(getContext(), text);
//        super.setText(s, BufferType.SPANNABLE);
//    }

    public void setText(CharSequence text, Map<String, String> imageKV) {
        Spannable s = getTextWithImages(getContext(), text, imageKV);
        super.setText(s, BufferType.SPANNABLE);

    }

    private static boolean addImages(Context context, Spannable spannable, Map<String, String> imageKV) {
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
            if(imageKV !=null && imageKV.containsKey(resname)){
                resname = imageKV.get(resname);
            }
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
    private static Spannable getTextWithImages(Context context, CharSequence text, Map<String, String> imageKV) {
        Spannable spannable = spannableFactory.newSpannable(text);
        addImages(context, spannable, imageKV);
        return spannable;
    }
}