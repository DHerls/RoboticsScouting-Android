package org.fullmetalfalcons.androidscouting;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Space;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;

public class ScoutingActivity extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener {

    private final ArrayList<Element> ELEMENTS = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Utils.onActivityCreateSetTheme(this);
        setContentView(R.layout.activity_scouting);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ScoutingActivity.this.checkFields()){
                    ScoutingActivity.this.collectResults();
                }
            }
        });
        LinearLayout l = (LinearLayout) findViewById(R.id.mainLinear);
        l.requestFocus();

        createTheApp();

        Switch colorSwitch = (Switch) findViewById(R.id.team_color);
        System.out.println("Restore");

        if (savedInstanceState!=null) {
            if (savedInstanceState.getBoolean("isRed")) {
                colorSwitch.setChecked(true);
            }
        }
        colorSwitch.setOnCheckedChangeListener(this);

        BluetoothCore.startBLE(this);

    }

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

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void createTheApp(){
        loadConfig();
        addViews();
    }

    private void addViews() {
        LinearLayout l = (LinearLayout) findViewById(R.id.mainLinear);
        for (Element e: ELEMENTS){
            l.addView(e.getView());
        }

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);

        Space s = new Space(this);
        s.setMinimumHeight(fab.getHeight() + fab.getPaddingBottom() + fab.getPaddingTop());
        l.addView(s);
    }

    private void loadConfig(){
        AssetManager am = getAssets();
        try {
            BufferedReader config = new BufferedReader(new InputStreamReader(am.open("config.txt")));
            String line;
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
            Toast.makeText(this,"fuck",Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    private void addElement(String line){
        try {
            Element e = new Element(line,this);
            ELEMENTS.add(e);
        } catch (ElementParseException e1) {
            e1.printStackTrace();
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        Log.d("check_changed", "Checked");
        if (isChecked){
            Utils.changeToTheme(this,Utils.THEME_RED);

        } else {
            Utils.changeToTheme(this, Utils.THEME_BLUE);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle bundle) {
        super.onSaveInstanceState(bundle);
        BluetoothCore.stopBLE();
        Switch s = (Switch) findViewById(R.id.team_color);
        bundle.putBoolean("isRed",s.isChecked());

        ParcelableArrayList values = new ParcelableArrayList();
        for (Element e: ELEMENTS){
            values.add(e.getViewData());
        }
        bundle.putParcelable("fieldData",values);

        bundle.putString("match_num", ((EditText) findViewById(R.id.match_num)).getText().toString());
        bundle.putString("team_num", ((EditText) findViewById(R.id.team_num)).getText().toString());

    }

    @Override
    protected void onRestoreInstanceState(Bundle bundle){
        ParcelableArrayList values = (ParcelableArrayList) bundle.getParcelable("fieldData");

        for (int i = 0; i< ELEMENTS.size();i++){
            ELEMENTS.get(i).setViewData(values.get(i));
        }

        ((EditText) findViewById(R.id.match_num)).setText(bundle.getString("match_num"));
        ((EditText) findViewById(R.id.team_num)).setText(bundle.getString("team_num"));

    }

    private boolean checkFields(){
        String s = ((EditText) findViewById(R.id.team_num)).getText().toString();
        if (s.equals("")){
            sendError("Team Number must not be blank",false);
            return false;
        }

        s = ((EditText) findViewById(R.id.match_num)).getText().toString();
        if (s.equals("")){
            sendError("Match Number must not be blank",false);
            return false;
        }




        return true;
    }

    private void collectResults(){
        HashMap<String, Object> values = new HashMap<>();
        for (Element e:ELEMENTS){
            values.putAll(e.getHash());
        }

        values.put("team_num",Integer.parseInt(((EditText) findViewById(R.id.team_num)).getText().toString()));
        values.put("match_num",Integer.parseInt(((EditText) findViewById(R.id.match_num)).getText().toString()));
        values.put("team_color", ((Switch) findViewById(R.id.team_color)).isChecked() ? "Red" : "Blue");
    }

    public void sendError(String message,final boolean fatalError){
        new AlertDialog.Builder(this)
                .setTitle("Something is wrong")
                .setCancelable(!fatalError)
                .setMessage(message)
                .setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        if (fatalError) {
                            System.exit(0);
                        }
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
            //Value of Bluetooth Request Code is 1
            if ((requestCode == 1) && (resultCode == RESULT_OK)) {
                BluetoothCore.enable();
            }
    }

    public void setConnected(final boolean connected){
        final View connectionIndicator = findViewById(R.id.connection_indicator);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                connectionIndicator.setBackgroundColor(connected ? Color.GREEN : Color.RED);
            }
        });
    }

    public void setAdvertising(final boolean advertising){
        final View advertisingIndicator = findViewById(R.id.advertising_indicator);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                advertisingIndicator.setBackgroundColor(advertising ? Color.GREEN : Color.RED);
            }
        });
    }
}
