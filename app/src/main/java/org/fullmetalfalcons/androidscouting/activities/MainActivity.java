package org.fullmetalfalcons.androidscouting.activities;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import org.fullmetalfalcons.androidscouting.R;
import org.fullmetalfalcons.androidscouting.bluetooth.BluetoothCore;
import org.fullmetalfalcons.androidscouting.fileio.FileManager;
import org.fullmetalfalcons.androidscouting.views.MainButtonView;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends DHActivity {

    private final Pattern bluetoothCodePattern = Pattern.compile("([a-fA-F]|\\d){4}");
    private BroadcastReceiver mReceiver;
    private static Bundle viewData;
    private boolean doLaunch = true;

    private MainButtonView retrieveButton;
    private MainButtonView scoutButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //If the elements haven't been loaded from the config file, load them
        if (FileManager.getElements().isEmpty()){
            FileManager.loadConfig(this);
            FileManager.loadTeamNames(this);
        }

        //Change the color of the status bar to black, because it looks better
        Window window = this.getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(Color.BLACK);

        final EditText bluetoothCodeView = (EditText) findViewById(R.id.bluetoothCode);

        //Refresh button next to the bluetooth code EditText
        final ImageButton refreshBtn = (ImageButton) findViewById(R.id.detail_refresh_btn);
        refreshBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setPassphrase(bluetoothCodeView);

            }
        });

        //When the user presses the enter button in the Bluetooth Code box
        bluetoothCodeView.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

                if ((actionId== EditorInfo.IME_ACTION_DONE )   )
                {
                    setPassphrase(bluetoothCodeView);
                    return true;
                }
                return false;

            }
        });


        if (!isEmulator()) {
            //Start advertising
            BluetoothCore.startBLE(this);

            //Register broadcast receiver to detect changes to bluetooth adapter
            registerBluetoothReceiver();
        } else {
            sendError("Device is running in an emulator, bluetooth will be useless",false);
        }

    }

    private void setPassphrase(EditText bluetoothCodeView){
        final Animation ranim = AnimationUtils.loadAnimation(this, R.anim.rotate);
        final ImageButton refreshBtn = (ImageButton) findViewById(R.id.detail_refresh_btn);

        // hide virtual keyboard
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(bluetoothCodeView.getWindowToken(), 0);

        //Create a matcher based on the pattern above and the text in the code EditText
        Matcher bluetoothCodeMatcher = bluetoothCodePattern.matcher(bluetoothCodeView.getText());
        //If the text matches the pattern
        if (bluetoothCodeMatcher.matches()) {
            //Animate the refresh button
            refreshBtn.startAnimation(ranim);
            if (!isEmulator()){
                //Set a new passphrase
                BluetoothCore.setPassphrase(bluetoothCodeView.getText().toString());
            }
        } else {
            //Send error message to user
            sendError("Bluetooth Code must be in the format (# or (A-F))x4", false);
            //Clear the code box
            bluetoothCodeView.setText("");
        }

        retrieveButton = (MainButtonView) findViewById(R.id.retrieve_button);
        scoutButton = (MainButtonView) findViewById(R.id.scout_button);
    }

    /**
     * Changes the color of connected notification icon based on connection state
     *
     * @param connected whether or not a bluetooth device is connected
     */
    public void setConnected(final boolean connected){
        //Updates to UI must be run on the UI thread
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //Get connection indicator
                View v = findViewById(R.id.connection_indicator);

                //Get the shape itself
                LayerDrawable bgDrawable = (LayerDrawable) v.getBackground();
                //Set inner circle color
                GradientDrawable shape = (GradientDrawable) bgDrawable.getDrawable(0);
                shape.setColor(ContextCompat.getColor(MainActivity.this, connected ? R.color.colorGreenIndicator : R.color.colorRedIndicator));

                //Set outer circle color
                shape = (GradientDrawable) bgDrawable.getDrawable(1);
                shape.setStroke(8, ContextCompat.getColor(MainActivity.this, connected ? R.color.colorGreenIndicator : R.color.colorRedIndicator));

                final ImageButton refreshBtn = (ImageButton) findViewById(R.id.detail_refresh_btn);
                refreshBtn.setEnabled(!connected);
                Log.d("MERF", "Set Connected" + connected);

            }
        });

    }

    /**
     * Changes the color of connected notification icon based on connection state
     *
     * @param advertising whether or not a bluetooth device is connected
     */
    public void setAdvertising(final boolean advertising) {
        //Updates to UI must be run on the UI thread
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //Get connection indicator
                View v = findViewById(R.id.advertising_indicator);

                //Get the shape itself
                LayerDrawable bgDrawable = (LayerDrawable) v.getBackground();
                //Set inner circle color
                GradientDrawable shape = (GradientDrawable) bgDrawable.getDrawable(0);
                shape.setColor(ContextCompat.getColor(MainActivity.this, advertising ? R.color.colorGreenIndicator : R.color.colorRedIndicator));

                //Set outer circle color
                shape = (GradientDrawable) bgDrawable.getDrawable(1);
                shape.setStroke(8, ContextCompat.getColor(MainActivity.this, advertising ? R.color.colorGreenIndicator : R.color.colorRedIndicator));
            }
        });

    }

    /**
     * Called when request for bluetooth permissions returns
     *
     * @param requestCode Number of activity request
     * @param resultCode Result of the activity
     * @param data data passed by the activity
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //Value of Bluetooth Request Code is 1
        if ((requestCode == 1) && (resultCode == RESULT_OK)) {
            BluetoothCore.enable();
        }

        if ((requestCode == 1) && (resultCode == RESULT_CANCELED)) {
            //sendError("This app will not be useful until bluetooth is enabled", false);
        }
        if (requestCode==2){
            doLaunch = true;
        }
    }


    /**
     * Creates and registers a BroadcastReceiver to track changes to BluetoothAdapter
     */
    private void registerBluetoothReceiver() {
        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                final String action = intent.getAction();

                if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                    final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,
                            BluetoothAdapter.ERROR);
                    switch (state) {
                        case BluetoothAdapter.STATE_OFF:
                            //sendError("Bluetooth is required for this app, don't turn it off",true);
                            setConnected(false);
                            setAdvertising(false);
                            BluetoothCore.stopBLE();
                            //sendError("This app will not be useful until bluetooth is enabled",false);
                            break;
                        case BluetoothAdapter.STATE_TURNING_OFF:
                            break;
                        case BluetoothAdapter.STATE_ON:
                            BluetoothCore.startBLE(MainActivity.this);
                            break;
                        case BluetoothAdapter.STATE_TURNING_ON:
                            break;
                    }
                }
            }
        };

        IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(mReceiver, filter);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
    }


    public static void saveData(Bundle bundle) {
        MainActivity.viewData = bundle;
    }

    public static Bundle getData(){
        return viewData;
    }

    public static void clearData() {
        if (viewData!=null){
            viewData.clear();
        }
    }

    public void launchActivity(View v){
        if (doLaunch){
            Intent intent = null;
            switch (v.getId()){
                case R.id.scout_button:
                    doLaunch = false;
                    intent = new Intent(this,ScoutingActivity.class);
                    break;
                case R.id.retrieve_button:
                    doLaunch = false;
                    intent = new Intent(this,RetrieveDataActivity.class);
                    break;
            }

            startActivityForResult(intent,2);

        }

    }


    private static boolean isEmulator() {
        return Build.FINGERPRINT.startsWith("unknown")
                || Build.MODEL.contains("google_sdk")
                || Build.MODEL.contains("Emulator")
                || Build.MODEL.contains("Android SDK built for x86")
                || Build.MANUFACTURER.contains("Genymotion")
                || (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic"))
                || "google_sdk".equals(Build.PRODUCT);
    }

}
