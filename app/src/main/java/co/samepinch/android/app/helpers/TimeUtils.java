package co.samepinch.android.app.helpers;

import android.content.Context;
import android.text.format.DateUtils;

import org.apache.commons.lang3.StringUtils;

import java.text.DateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

/**
 * Created by cbenjaram on 10/13/15.
 */
public class TimeUtils {
    public static final List<Long> times = Arrays.asList(
            TimeUnit.DAYS.toMillis(365),
            TimeUnit.DAYS.toMillis(30),
            TimeUnit.DAYS.toMillis(1),
            TimeUnit.HOURS.toMillis(1),
            TimeUnit.MINUTES.toMillis(1),
            TimeUnit.SECONDS.toMillis(1));
    public static final List<String> timesString = Arrays.asList("year", "month", "day", "hour", "minute", "second");

    public static String toHuman(Context context, Date arg0) {
        int gmtOffset = TimeZone.getDefault().getRawOffset();
        long now = System.currentTimeMillis() + gmtOffset;

        CharSequence sequence = DateUtils.getRelativeTimeSpanString(arg0.getTime(), now, DateUtils.FORMAT_ABBREV_RELATIVE);
        return sequence.toString();
    }

    public static String toHuman(long arg0) {
        StringBuffer res = new StringBuffer();
        for (int i = 0; i < times.size(); i++) {
            Long current = times.get(i);
            long temp = arg0 / current;
            if (temp > 0) {
                res.append(temp).append(StringUtils.SPACE).append(times.get(i)).append(temp > 1 ? "s" : "");
                break;
            }
        }

        if ("".equals(res.toString())) {
            res.append("moments");
        }

        res.append(StringUtils.SPACE).append("ago");
        return res.toString();
    }
}