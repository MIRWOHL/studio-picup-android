package com.picup.calling.util;

/**
 * Created by frank.truong on 4/13/2017.
 */

public class TimeUtils {


    public static String timeToString(int numOfSeconds) {
        int hours =  numOfSeconds / 3600;
        int minutes = (numOfSeconds % 60);
        int seconds = (numOfSeconds % 3600);

        if (numOfSeconds > 3600) {
            hours = numOfSeconds / 3600;
        }
        return null;
    }
}
