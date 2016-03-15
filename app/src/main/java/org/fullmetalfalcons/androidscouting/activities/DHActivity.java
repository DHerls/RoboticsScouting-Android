package org.fullmetalfalcons.androidscouting.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import org.fullmetalfalcons.androidscouting.R;

/**
 * Base activity that includes the sendError method in all class
 *
 * Created by Dan on 3/5/2016.
 */
public abstract class DHActivity extends AppCompatActivity {

    /**
     * Sends a popup message to the user with a custom message.
     * Also closes the app if the error is fatal
     *
     * @param message message to send to the user
     * @param fatalError whether or not the app should close after user acknowledges
     */
    @SuppressWarnings("SameParameterValue")
    public void sendError(final String message,final boolean fatalError){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                new AlertDialog.Builder(DHActivity.this)
                        .setTitle("Something is wrong")
                                //Can ignore if not fatal
                        .setCancelable(!fatalError)
                        .setMessage(message)
                        .setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                //Close the app
                                if (fatalError) {
                                    System.exit(0);
                                }
                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
                if (fatalError) {
                    Log.wtf(getString(R.string.log_tag), message);
                } else {
                    Log.e(getString(R.string.log_tag), message);

                }
            }
        });

    }

    /**
     * Sends a popup message to the user with a custom message.
     * Also closes the app if the error is fatal
     *
     * @param message message to send to the user
     * @param fatalError whether or not the app should close after user acknowledges
     */
    @SuppressWarnings("SameParameterValue")
    public void sendError(final String message,final boolean fatalError, Exception e){
        e.printStackTrace();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                new AlertDialog.Builder(DHActivity.this)
                        .setTitle("Something is wrong")
                                //Can ignore if not fatal
                        .setCancelable(!fatalError)
                        .setMessage(message)
                        .setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                //Close the app
                                if (fatalError) {
                                    System.exit(0);
                                }
                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
                if (fatalError) {
                    Log.wtf(getString(R.string.log_tag), message);
                } else {
                    Log.e(getString(R.string.log_tag), message);

                }
            }
        });

    }
}
