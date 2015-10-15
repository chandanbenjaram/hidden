package co.samepinch.android.app.helpers;

import android.content.Context;
import android.text.format.DateUtils;

import org.apache.commons.lang3.StringUtils;

import java.util.Date;

/**
 * Created by cbenjaram on 10/13/15.
 */
public class TimeUtils {

    public static final String SECONDS_AGO = "0 minutes ago";
    public static final String MOMENTS_AGO = "a moment ago";

    public static String toHumanLocal(Context context, Date arg0) {
        long arg0Time = arg0.getTime();
        CharSequence sequence = DateUtils.formatDateTime(context, arg0Time, DateUtils.FORMAT_SHOW_TIME| DateUtils.FORMAT_SHOW_DATE|DateUtils.FORMAT_ABBREV_ALL);
        return sequence.toString();
    }

    public static String toHumanRelativePeriod(Date arg0) {
//        int gmtOffset = TimeZone.getDefault().getRawOffset();
////        long now = System.currentTimeMillis() + gmtOffset;

        long arg0Time = arg0.getTime();
        long now = System.currentTimeMillis();

        //Local time zone
        CharSequence sequence = DateUtils.getRelativeTimeSpanString(arg0Time, now, DateUtils.FORMAT_ABBREV_RELATIVE);
        String humanRead = sequence.toString();
        if (StringUtils.equals(humanRead, SECONDS_AGO)) {
            humanRead = MOMENTS_AGO;
        }

        return humanRead;
    }
}