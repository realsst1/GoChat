package com.example.shreyesh.gochat;

import android.app.Application;
import android.content.Context;

public class GetTimeAgo extends Application {
    private static final int SECOND_MILLIS = 1000;
    private static final int MINUTE_MILLIS = 60 * SECOND_MILLIS;
    private static final int HOURS_MILLIS = 60 * MINUTE_MILLIS;
    private static final int DAYS_MILLIS = 24 * HOURS_MILLIS;

    public static String getTimeAgo(long time, Context context) {
        if (time < 1000000000000L) {
            time *= 1000;
        }

        long now = System.currentTimeMillis();
        if (time > now || time <= 0)
            return null;


        final long diff = now - time;
        if (diff < MINUTE_MILLIS)
            return "just now";
        else if (diff < 2 * MINUTE_MILLIS)
            return "a minute ago";
        else if (diff < 50 * MINUTE_MILLIS)
            return (diff / MINUTE_MILLIS + " minutes ago");
        else if (diff < 90 * MINUTE_MILLIS)
            return "an hour ago";
        else if (diff < 24 * HOURS_MILLIS)
            return (diff / HOURS_MILLIS + " hours ago");
        else if (diff < 48 * HOURS_MILLIS)
            return "yesterday";
        else
            return diff / DAYS_MILLIS + " days ago";
    }
}
