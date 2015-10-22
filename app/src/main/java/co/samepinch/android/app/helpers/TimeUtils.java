package co.samepinch.android.app.helpers;

import android.content.Context;
import android.text.format.DateUtils;

import com.aviary.android.feather.common.utils.DateTimeUtils;

import org.apache.commons.lang3.StringUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by cbenjaram on 10/13/15.
 */
public class TimeUtils {
    public static final TimeZone GMT_TIME_ZONE = TimeZone.getTimeZone("GMT");
    public static final TimeZone LOCAL_TIME_ZONE = TimeZone.getDefault();
    static final String DATEFORMAT = "yyyy-MM-dd HH:mm:ss";

    public static final String SECONDS_AGO = "0 minutes ago";
    public static final String MOMENTS_AGO = "a moment ago";

    public static String toHumanLocal(Context context, Date arg0) {
        if(arg0 == null){
            return StringUtils.EMPTY;
        }
        long arg0Time = arg0.getTime();
        CharSequence sequence = DateUtils.formatDateTime(context, arg0Time, DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_ABBREV_ALL);
        return sequence.toString();
    }

    public static String toHumanRelativePeriod(Date arg0) {
        if(arg0 == null){
            return StringUtils.EMPTY;
        }
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


    // source:: http://stackoverflow.com/questions/308683/how-can-i-get-the-current-date-and-time-in-utc-or-gmt-in-java
    public static Date currentTimeInGMT() {
        try {
            SimpleDateFormat dftGMT = new SimpleDateFormat(DATEFORMAT, Locale.getDefault());
            dftGMT.setTimeZone(GMT_TIME_ZONE);

            //Local time zone
            SimpleDateFormat dftLOCAL = new SimpleDateFormat(DATEFORMAT, Locale.getDefault());
            dftLOCAL.setTimeZone(LOCAL_TIME_ZONE);

            //Time in GMT
            return dftLOCAL.parse(dftGMT.format(new Date()));
        } catch (Exception e) {
            return new Date();
        }
    }
}