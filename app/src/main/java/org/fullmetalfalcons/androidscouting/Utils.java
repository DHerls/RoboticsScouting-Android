package org.fullmetalfalcons.androidscouting;

/**
 * Created by Dan on 2/1/2016.
 */
import android.app.Activity;
import android.content.Intent;

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
}