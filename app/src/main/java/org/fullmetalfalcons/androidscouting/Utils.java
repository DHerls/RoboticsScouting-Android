package org.fullmetalfalcons.androidscouting;

/**
 * Utility class whose methods are used throughout the app
 *
 * Created by Dan on 2/1/2016.
 */

import android.app.Activity;
import android.os.Build;

import org.fullmetalfalcons.androidscouting.elements.Element;

import java.math.BigDecimal;

public class Utils
{
    private static int sTheme = 2;
    public final static int THEME_RED = 1;
    public final static int THEME_BLUE = 2;
    /**
     * Set the theme of the Activity, and restart it by creating a new Activity of the same type.
     */
    public static void changeToTheme(Activity activity, int theme)
    {
        sTheme = theme;
        Element.setSwitchColors(true);
        activity.recreate();
    }

    /** Set the theme of the activity, according to the configuration. */
    public static void onActivityCreateSetTheme(Activity activity)
    {
        switch (sTheme)
        {
            default:
            case THEME_BLUE:
                activity.setTheme(R.style.BlueTheme);
                break;
            case THEME_RED:
                activity.setTheme(R.style.RedTheme);
                break;
        }
    }

    public static String getDeviceName() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.startsWith(manufacturer)) {
            return capitalize(model);
        } else {
            return capitalize(manufacturer) + " " + model;
        }
    }


    private static String capitalize(String s) {
        if (s == null || s.length() == 0) {
            return "";
        }
        char first = s.charAt(0);
        if (Character.isUpperCase(first)) {
            return s;
        } else {
            return Character.toUpperCase(first) + s.substring(1);
        }
    }

    @SuppressWarnings("SameParameterValue")
    public static double round(double number, int numPlaces){
        BigDecimal bigDecimal = new BigDecimal(number);
        bigDecimal = bigDecimal.setScale(numPlaces,
                BigDecimal.ROUND_HALF_UP);
        return bigDecimal.doubleValue();
    }
}