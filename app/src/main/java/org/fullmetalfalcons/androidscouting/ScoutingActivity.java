package org.fullmetalfalcons.androidscouting;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.AssetManager;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.Space;
import android.widget.Switch;
import android.widget.Toast;

import com.dd.plist.NSDictionary;

import org.fullmetalfalcons.androidscouting.elements.Element;
import org.fullmetalfalcons.androidscouting.elements.ElementParseException;
import org.fullmetalfalcons.androidscouting.bluetooth.BluetoothCore;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Main Activity for the app
 *
 * Collects and sends data about robots to a base
 *
 */
public class ScoutingActivity extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener {

    private final ArrayList<Element> ELEMENTS = new ArrayList<>();
    private boolean haveBluetoothPermission = true;
    private static boolean isFirstTime = true;

    /**
     * Called when the activity is created
     *
     * Creates a UI based on activity_scouting.xml and dynamically builds from config.txt
     *
     * @param savedInstanceState if the app had previously stored data, it would be in this bundle
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //This method in the Utils class allows the theme to switch between blue and red
        Utils.onActivityCreateSetTheme(this);
        setContentView(R.layout.activity_scouting);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //noinspection ConstantConditions
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Floating Action Button to send data to base
        final FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        //When the button is clicked
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //If all the static fields are filled in correctly
                if (BluetoothCore.isConnected()) {
                    if (ScoutingActivity.this.checkFields()) {
                        fab.setEnabled(false);
                        Snackbar.make(view, "Sending...", Snackbar.LENGTH_LONG).setAction("Action", null).show();
                        String results = ScoutingActivity.this.collectResults();
                        BluetoothCore.sendData(results);
                        final ScheduledExecutorService exec = Executors.newScheduledThreadPool(1);

                        exec.schedule(new Runnable() {
                            @Override
                            public void run() {
                                fab.setEnabled(true);
                            }
                        }, 1, TimeUnit.SECONDS);
                    }
                } else {
                    sendError("Not currently connected to base", false);
                }
            }
        });

        //Prevents the app from defaulting to focus on the Team Number EditText
        LinearLayout l = (LinearLayout) findViewById(R.id.mainLinear);
        l.requestFocus();

//        NumberPicker np = new NumberPicker(this);
//        np.setMinValue(0);
//        np.setMaxValue(100);
//        l.addView(np);


        //Dynamically generate the UI based on config.txt
        createTheApp();

        //Get the Team Color Switch
        Switch colorSwitch = (Switch) findViewById(R.id.team_color);

        //This isn't the first time
        if (MainActivity.getData()!=null&&!MainActivity.getData().isEmpty()) {
            //If the theme should be red
            if (MainActivity.getData().getBoolean("isRed")) {
                //Check the switch
                colorSwitch.setChecked(true);
            }
            restoreData();
        }
        colorSwitch.setOnCheckedChangeListener(this);

    }

    private void restoreData() {
        Bundle bundle = MainActivity.getData();
        ParcelableArrayList values = bundle.getParcelable("fieldData");

        //Restore all dynamic values
        for (int i = 0; i< ELEMENTS.size();i++){
            assert values != null;
            ELEMENTS.get(i).setViewData(values.get(i));
        }

        //Restore static values
        ((EditText) findViewById(R.id.match_num)).setText(bundle.getString("match_num"));
        ((EditText) findViewById(R.id.team_num)).setText(bundle.getString("team_num"));
    }


    /**
     * Create a menu
     *
     * @param menu the menu being created
     * @return Whether the creation of the menu was successful
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_scouting, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        Intent intent;
        switch(id){
            case R.id.action_about:
                intent = new Intent(this,AboutActivity.class);
                startActivity(intent);
                break;
            case R.id.action_settings:

                break;
            default:
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Calls two methods to create the UI dynamically from config.txt
     */
    private void createTheApp(){
        if (ELEMENTS.isEmpty()){
            loadConfig();
        }
        addViews();
    }

    /**
     * After all the Elements are generated from the config file, they are added in order to the screen
     */
    private void addViews() {
        //Get the main layout of the app
        LinearLayout l = (LinearLayout) findViewById(R.id.mainLinear);
        //For each element created
        for (Element e: ELEMENTS){
            //Add the Element's view to the app
            l.addView(e.getView(),e.getView().getLayoutParams());
        }

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);

        //Add padding at the bottom of the page
        Space s = new Space(this);
        s.setMinimumHeight(fab.getHeight() + fab.getPaddingBottom() + fab.getPaddingTop());
        l.addView(s);
    }

    /**
     * Load Element data from the Config file
     */
    private void loadConfig(){
        //Located in the assets folder
        AssetManager am = getAssets();
        try {
            BufferedReader config = new BufferedReader(new InputStreamReader(am.open("config.txt")));
            String line;
            //While there are still lines to read
            while ((line=config.readLine())!=null) {
                line = line.trim();
                if (line.length() < 2) {
                    continue;
                }
                //If the line does not start with ##, which indicates a comment, or @ which indicated an equation
                if (!line.substring(0, 2).equals("##") && line.charAt(0) != '@') {
                    //Attempt to add an Element to the main array
                    addElement(line);
                }
            }
        } catch (IOException e) {
            //This exception signifies the entire app is useless
            sendError("fuck",true);
            e.printStackTrace();
        }
    }

    /**
     * Adds a new element from a string to the main array
     *
     * @param line line from config file
     */
    private void addElement(String line){
        try {
            //Element will throw an exception if it is improperly formed
            Element e = new Element(line,this);
            ELEMENTS.add(e);
        } catch (ElementParseException e1) {
            e1.printStackTrace();
        }
    }

    /**
     * Called when the TeamColor switch is flipped
     *
     * @param buttonView the switch which is flipped
     * @param isChecked Whether or not it is checked
     */
    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (isChecked){
            Utils.changeToTheme(this,Utils.THEME_RED);

        } else {
            Utils.changeToTheme(this, Utils.THEME_BLUE);
        }
    }

    /**
     * When someone leaves the app, or the app is suspended by the phone
     * Saves data from every field for later restore
     *
     * @param bundle Bundle which contains data to be saved between instance states
     */
    @Override
    protected void onSaveInstanceState(Bundle bundle) {
        super.onSaveInstanceState(bundle);
        //Stop advertising
        //BluetoothCore.stopBLE();

        //Set whether or not the theme should be red
        Switch s = (Switch) findViewById(R.id.team_color);
        bundle.putBoolean("isRed",s.isChecked());

        //Put all the values from the views into an arraylist then put it into the bundle
        ParcelableArrayList values = new ParcelableArrayList();
        for (Element e: ELEMENTS){
            values.add(e.getViewData());
        }
        bundle.putParcelable("fieldData", values);

        //Put all the static fields into the bundle
        bundle.putString("match_num", ((EditText) findViewById(R.id.match_num)).getText().toString());
        bundle.putString("team_num", ((EditText) findViewById(R.id.team_num)).getText().toString());
        //bundle.putString("bluetooth_code", ((EditText) findViewById(R.id.bluetoothCode)).getText().toString());

    }

    /**
     * Called when the app is restored from a previous state such as when it was minimized
     * Restores all the fields to the state they were before the app was closed
     *
     * @param bundle saved data
     */
    @Override
    protected void onRestoreInstanceState(Bundle bundle){

        //Get stored values
        ParcelableArrayList values = bundle.getParcelable("fieldData");

        //Restore all dynamic values
        for (int i = 0; i< ELEMENTS.size();i++){
            assert values != null;
            ELEMENTS.get(i).setViewData(values.get(i));
        }

        //Restore static values
        ((EditText) findViewById(R.id.match_num)).setText(bundle.getString("match_num"));
        ((EditText) findViewById(R.id.team_num)).setText(bundle.getString("team_num"));
        //((EditText) findViewById(R.id.bluetoothCode)).setText(bundle.getString("bluetooth_code"));

        //BluetoothCore.startBLE(this);
        //BluetoothCore.setPassphrase(bundle.getString("bluetooth_code"));

    }

    /**
     * Called when the activity is resumed, only used when Bluetooth permission is requested
     */
    @Override
    protected void onResume() {
        super.onResume();
//        if(haveBluetoothPermission &&!isFirstTime){
//            BluetoothCore.startBLE(this);
//        }
//        isFirstTime = false;
    }


    /**
     * Verifies that the static fields are filled in correctly
     *
     * Primarily, checks that the fields are filled in
     *
     * @return whether or not all fields are filled correctly
     */
    private boolean checkFields(){
        String s = ((EditText) findViewById(R.id.team_num)).getText().toString();
        if (s.equals("")){
            sendError("Team Number must not be blank",false);
            return false;
        }

        s = ((EditText) findViewById(R.id.match_num)).getText().toString();
        if (s.equals("")){
            sendError("Match Number must not be blank", false);
            return false;
        }




        return true;
    }

    /**
     * Gathers field values into an NSDictionary in preparation for sending
     */
    private String collectResults(){
        NSDictionary values = new NSDictionary();
        for (Element e:ELEMENTS){
            values.putAll(e.getHash());
        }

        values.put("team_num", Integer.parseInt(((EditText) findViewById(R.id.team_num)).getText().toString()));
        values.put("match_num", Integer.parseInt(((EditText) findViewById(R.id.match_num)).getText().toString()));
        values.put("team_color", ((Switch) findViewById(R.id.team_color)).isChecked() ? "Red" : "Blue");

        return values.toXMLPropertyList();
    }

    /**
     * Sends a popup message to the user with a custom message.
     * Also closes the app if the error is fatal
     *
     * @param message message to send to the user
     * @param fatalError whether or not the app should close after user acknowledges
     */
    public void sendError(String message,final boolean fatalError){
        new AlertDialog.Builder(this)
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
        if(fatalError){
            Log.wtf(getString(R.string.log_tag),message);
        } else {
            Log.e(getString(R.string.log_tag),message);

        }
    }

    public void clearAll(View v){

        for (int i = 0; i< ELEMENTS.size();i++){
            ELEMENTS.get(i).clearViewData();
        }

        //Restore static values
        ((EditText) findViewById(R.id.match_num)).setText("");
        ((EditText) findViewById(R.id.team_num)).setText("");


        MainActivity.clearData();

        ((Switch) findViewById(R.id.team_color)).setChecked(false);
        Utils.changeToTheme(this, Utils.THEME_BLUE);
    }

    public static void log(String message){

    }

    public static void debug(String message){

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Bundle bundle = new Bundle();
        //Set whether or not the theme should be red
        Switch s = (Switch) findViewById(R.id.team_color);
        bundle.putBoolean("isRed",s.isChecked());

        //Put all the values from the views into an arraylist then put it into the bundle
        ParcelableArrayList values = new ParcelableArrayList();
        for (Element e: ELEMENTS){
            values.add(e.getViewData());
        }
        bundle.putParcelable("fieldData", values);

        //Put all the static fields into the bundle
        bundle.putString("match_num", ((EditText) findViewById(R.id.match_num)).getText().toString());
        bundle.putString("team_num", ((EditText) findViewById(R.id.team_num)).getText().toString());
        //bundle.putString("bluetooth_code", ((EditText) findViewById(R.id.bluetoothCode)).getText().toString());

        MainActivity.saveData(bundle);
    }
}
