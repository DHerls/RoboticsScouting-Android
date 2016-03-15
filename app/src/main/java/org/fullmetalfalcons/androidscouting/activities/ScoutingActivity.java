package org.fullmetalfalcons.androidscouting.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Space;
import android.widget.Switch;

import com.dd.plist.NSDictionary;

import org.fullmetalfalcons.androidscouting.R;
import org.fullmetalfalcons.androidscouting.Utils;
import org.fullmetalfalcons.androidscouting.bluetooth.BluetoothCore;
import org.fullmetalfalcons.androidscouting.elements.Element;
import org.fullmetalfalcons.androidscouting.fileio.FileManager;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 *
 * Collects and sends data about robots to a base
 *
 * Activity is dynamically generated from Elements read from config.txt
 *
 */
public class ScoutingActivity extends DHActivity implements CompoundButton.OnCheckedChangeListener {
    
    /**
     * Called when the activity is created
     *
     * Creates a UI based on activity_scouting.xml and dynamically builds from config.txt
     *
     * @param savedInstanceState if the app had previously stored data, it would be in this bundle
     */
    @Override
    protected void onCreate(Bundle savedInstanceState){
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
                        BluetoothCore.sendScoutingData(results);
                        final ScheduledExecutorService exec = Executors.newScheduledThreadPool(1);

                        exec.schedule(new Runnable() {
                            @Override
                            public void run() {
                                fab.setEnabled(true);
                            }
                        }, 1, TimeUnit.SECONDS);
                        ScoutingActivity.this.clearAll(null);
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
        addViews();

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

        Element.setSwitchColors(false);
    }

    private void restoreData() {
        Bundle bundle = MainActivity.getData();

        //Restore static values
        ((EditText) findViewById(R.id.match_num)).setText(bundle.getString(getString(R.string.key_match_num)));
        ((EditText) findViewById(R.id.team_num)).setText(bundle.getString(getString(R.string.key_team_num)));
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
            case android.R.id.home:
                this.finish();
                break;
            case R.id.action_about:
                intent = new Intent(this,AboutActivity.class);
                startActivity(intent);
                break;
            default:
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * After all the FileManager.getElements() are generated from the config file, they are added in order to the screen
     */
    private void addViews() {
        //Get the main layout of the app
        LinearLayout l = (LinearLayout) findViewById(R.id.mainLinear);
        //For each element created
        try {
            for (Element e : FileManager.getElements()) {
                //Add the Element's view to the app
                View v = e.getView(this);
                l.addView(v, v.getLayoutParams());
            }
        } catch (IllegalStateException e){
            finish();
            return;
        }

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);

        //Add padding at the bottom of the page
        Space s = new Space(this);
        s.setMinimumHeight(fab.getHeight() + fab.getPaddingBottom() + fab.getPaddingTop());
        l.addView(s);
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

        //Put all the static fields into the bundle
        bundle.putString(getString(R.string.key_match_num), ((EditText) findViewById(R.id.match_num)).getText().toString());
        bundle.putString(getString(R.string.key_team_num), ((EditText) findViewById(R.id.team_num)).getText().toString());

    }

    /**
     * Called when the app is restored from a previous state such as when it was minimized
     * Restores all the fields to the state they were before the app was closed
     *
     * @param bundle saved data
     */
    @Override
    protected void onRestoreInstanceState(Bundle bundle){

        ((EditText) findViewById(R.id.match_num)).setText(bundle.getString(getString(R.string.key_match_num)));
        ((EditText) findViewById(R.id.team_num)).setText(bundle.getString(getString(R.string.key_team_num)));

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
        for (Element e: FileManager.getElements()) {
            values.putAll(e.getHash());
        }

        values.put(getString(R.string.key_team_num), Integer.parseInt(((EditText) findViewById(R.id.team_num)).getText().toString()));
        values.put(getString(R.string.key_match_num), Integer.parseInt(((EditText) findViewById(R.id.match_num)).getText().toString()));
        values.put(getString(R.string.key_team_color), ((Switch) findViewById(R.id.team_color)).isChecked() ? "Red" : "Blue");

        return values.toXMLPropertyList();
    }


    @SuppressWarnings({"SameParameterValue", "UnusedParameters"})
    public void clearAll(View v){

        //Prevents the app from defaulting to focus on the Team Number EditText
        LinearLayout l = (LinearLayout) findViewById(R.id.mainLinear);
        l.requestFocus();

        for (int i = 0; i< FileManager.getElements().size();i++){
            FileManager.getElements().get(i).clearViewData();
        }

        //Restore static values
        ((EditText) findViewById(R.id.match_num)).setText("");
        ((EditText) findViewById(R.id.team_num)).setText("");


        MainActivity.clearData();


        Switch s = (Switch) findViewById(R.id.team_color);
        if (s.isChecked()){
            s.setChecked(false);
            Utils.changeToTheme(this, Utils.THEME_BLUE);
        }


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LinearLayout l = (LinearLayout) findViewById(R.id.mainLinear);
        for (Element e: FileManager.getElements()){
            l.removeView(e.getView(this));
        }

        Bundle bundle = new Bundle();
        //Set whether or not the theme should be red
        Switch s = (Switch) findViewById(R.id.team_color);
        bundle.putBoolean("isRed",s.isChecked());

        bundle.putString(getString(R.string.key_match_num), ((EditText) findViewById(R.id.match_num)).getText().toString());
        bundle.putString(getString(R.string.key_team_num), ((EditText) findViewById(R.id.team_num)).getText().toString());
//        //bundle.putString("bluetooth_code", ((EditText) findViewById(R.id.bluetoothCode)).getText().toString());

        MainActivity.saveData(bundle);


    }




}
