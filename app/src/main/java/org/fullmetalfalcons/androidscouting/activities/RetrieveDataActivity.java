package org.fullmetalfalcons.androidscouting.activities;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.ParcelUuid;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import org.fullmetalfalcons.androidscouting.R;
import org.fullmetalfalcons.androidscouting.bluetooth.BluetoothCore;
import org.fullmetalfalcons.androidscouting.elements.Element;
import org.fullmetalfalcons.androidscouting.equations.Equation;
import org.fullmetalfalcons.androidscouting.fileio.FileManager;
import org.fullmetalfalcons.androidscouting.sql.SqlManager;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Allows the user to select criteria to retrieve team data
 */
public class RetrieveDataActivity extends DHActivity {

    private final HashMap<String, String> prettyColumns = new HashMap<>();

    private static volatile String responseString= null;
    private static volatile ResultSet resultSet = null;

    public static RequestType requestType;

    private final Pattern p = Pattern.compile("\\[(.*?)\\]");
    private ProgressDialog progress;
    private boolean timeout = false;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_retrieve_data);
        Toolbar toolbar = (Toolbar) findViewById(R.id.retrieve_toolbar);
        setSupportActionBar(toolbar);
        //noinspection ConstantConditions
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Set column values in the column spinner
        final Spinner columnSpinner = (Spinner) findViewById(R.id.column_spinner);
        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, getColumnValues());
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        columnSpinner.setAdapter(spinnerArrayAdapter);

        Button teamOkButton = (Button) findViewById(R.id.retrieve_team_ok);

        final EditText teamNumEditText = (EditText) findViewById(R.id.retrieve_team_num);

        //When the user presses the ok button next to the team number box
        teamOkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Close the keyboard
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(teamNumEditText.getWindowToken(), 0);
                //Request the specified team
                requestTeamNum(teamNumEditText);
            }
        });

        //When the user hits the uner button in the team number box
        teamNumEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

                if ((actionId == EditorInfo.IME_ACTION_DONE)) {
                    //Toast.makeText(getActivity(), "call",45).show();
                    // hide virtual keyboard
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(teamNumEditText.getWindowToken(), 0);

                    //Request team number
                    requestTeamNum(teamNumEditText);
                    return true;
                }
                return false;

            }
        });


        Button searchOkButton = (Button) findViewById(R.id.retrieve_team_search_button);
        final Spinner typeSpinner = (Spinner) findViewById(R.id.value_spinner);
        final Spinner operatorSpinner = (Spinner) findViewById(R.id.operator_spinner);
        final EditText valueText = (EditText) findViewById(R.id.value_edit_text);

        searchOkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(valueText.getWindowToken(), 0);
                searchForTeams(typeSpinner,columnSpinner,operatorSpinner,valueText);
            }
        });

        valueText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

                if ((actionId == EditorInfo.IME_ACTION_DONE)) {
                    //Toast.makeText(getActivity(), "call",45).show();
                    // hide virtual keyboard
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(valueText.getWindowToken(), 0);

                    searchForTeams(typeSpinner, columnSpinner, operatorSpinner, valueText);
                    return true;
                }
                return false;

            }
        });
    }

    private void searchForTeams(Spinner typeSpinner, Spinner columnSpinner, Spinner operatorSpinner, EditText valueText) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        String value = valueText.getText().toString();


        if (value.isEmpty()){
            sendError("Value cannot be blank",false);
        } else {
            String type = typeSpinner.getSelectedItem().toString();
            String column = prettyColumns.get(columnSpinner.getSelectedItem().toString());
            String operator = operatorSpinner.getSelectedItem().toString();

            requestType = RequestType.SEARCH;
            if (!sharedPref.getBoolean(RetrieveSettingsActivity.REMOTE_RETRIEVE_ENABLED_KEY,false)) {
                if (BluetoothCore.isConnected()) {
                    //TODO Bluetooth team search
                    //BluetoothCore.requestTeamNum(teamNumEditText.getText().toString());
                    waitForResponse(5);
                } else {
                    sendError("Not currently connected to base", false);
                }
            } else {
                SqlManager.searchForTeams(this, type, column, operator, value);
                waitForResponse(10);
            }

        }
    }

    private void requestTeamNum(EditText teamNumEditText) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        if (teamNumEditText.getText().toString().isEmpty()){
            sendError("Team number cannot be blank",false);
        } else {
            requestType = RequestType.TEAM;
            if (!sharedPref.getBoolean(RetrieveSettingsActivity.REMOTE_RETRIEVE_ENABLED_KEY,false)) {
                if (BluetoothCore.isConnected()) {
                    BluetoothCore.requestTeamNum(teamNumEditText.getText().toString());
                    waitForResponse(5);
                } else {
                    sendError("Not currently connected to base", false);
                }
            } else {
                SqlManager.requestTeamNumber(this, teamNumEditText.getText().toString());
                waitForResponse(5);
            }

        }
    }

    private static String capitalize(String input){
        return input.substring(0, 1).toUpperCase() + input.substring(1);
    }

    private static String makePretty(String input){
        String[] split = input.split("(\\s)|(_)");
        StringBuilder output = new StringBuilder();
        for (String s : split) {
            output.append(s.substring(0, 1).toUpperCase()).append(s.substring(1)).append(" ");
        }

        return output.toString().trim();
    }

    private static String makeKeyPretty(String input){
        String[] split = input.split("(\\s)|(_)");
        StringBuilder output = new StringBuilder();
        for (int i = 1; i<split.length; i++){
            String s = split[i];
            output.append(s.substring(0, 1).toUpperCase()).append(s.substring(1)).append(" ");
        }

        return output.toString().trim();
    }

    /**
     * Gathers results and displays them
     * Called by WaitTask after response string is set
     *
     * @param timeout Whether or not the request had timed out
     */
    public void displayResults(boolean timeout){
        if (!timeout){
            if (responseString!=null) {
                switch (responseString) {
                    case "NoReadTable":
                        sendError("No database has been established yet", false);
                        break;
                    case "NoReadTeam":
                        sendError("The team specified cannot be found", false);
                        break;
                    case "cancel":
                        break;
                    case "NoSearchResult":
                        sendError("Your search returned 0 results",false);
                        break;
                    default:
                        if (requestType == RequestType.TEAM) {
                            Matcher m = p.matcher(responseString);
                            HashMap<String, String> teamInfo = new HashMap<>();
                            while (m.find()) {
                                String[] value = m.group(1).split("=");
                                teamInfo.put(value[0], value[1]);
                                //                            System.out.println(value[0] + "=" + value[1]);
                            }
                            Intent displayIntent = new Intent(this, DisplayDataActivity.class);
                            displayIntent.putExtra("TEAM_DATA", teamInfo);
                            displayIntent.putExtra("COLUMN_DATA", prettyColumns);
                            startActivity(displayIntent);
                        } else {
                            //TODO Handle Searches
                        }
                        break;
                }
            } else {
                try {
                    if (requestType==RequestType.TEAM) {
                        HashMap<String, String> teamInfo = new HashMap<>();
                        ResultSetMetaData rsmd = resultSet.getMetaData();
                        int columnCount = rsmd.getColumnCount();
                        while (resultSet.next()) {
                            for (int i = 1; i <= columnCount; i++) {
                                teamInfo.put(rsmd.getColumnName(i), resultSet.getString(i));
                            }
                        }
                        Intent displayIntent = new Intent(this, DisplayDataActivity.class);
                        displayIntent.putExtra("TEAM_DATA", teamInfo);
                        displayIntent.putExtra("COLUMN_DATA", prettyColumns);
                        startActivity(displayIntent);
                    } else {
                        ArrayList<ArrayList<String>> data = new ArrayList<>();
                        ResultSetMetaData rsmd = resultSet.getMetaData();
                        int columnCount = rsmd.getColumnCount();
                        ArrayList<String> row;
                        while (resultSet.next()) {
                            row = new ArrayList<>();
                            for (int i = 1; i <= columnCount; i++) {
                                row.add(resultSet.getString(i));
                            }
                            data.add(row);
                        }
                        Intent displayIntent = new Intent(this, SelectTeamActivity.class);
                        displayIntent.putExtra("TEAM_DATA", data);
                        startActivity(displayIntent);
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                resultSet = null;
            }
        } else {
            sendError("Error request timeout", false);
        }
        responseString = null;
    }

    /**
     * Returns a list of columns formatted to be readable by the user
     *
     * @return List of formatted columns
     */
    private ArrayList<String> getColumnValues(){
        ArrayList<String> valueList = new ArrayList<>();
        String prelabel = "";
        for (Element e: FileManager.getElements()){
            switch(e.getType()){

                case SEGMENTED_CONTROL:
                    for (String key:e.getKeys()){

                        //Capitalize the first letter of every word
                        String s = makeKeyPretty(key);
                        for (int i = 0; i<e.getArguments().length;i++){
                            String args = e.getArguments()[i];
                            valueList.add(prelabel + s + "-" + args);
                            prettyColumns.put(prelabel + s + "-" + args, e.getColumnValues()[i]);
                        }
                    }
                    break;
                case TEXTFIELD:
                    if (e.getArguments()[0].equalsIgnoreCase("number") || e.getArguments()[0].equalsIgnoreCase("decimal")){
                        String[] keys = e.getKeys();
                        for (int i = 0; i < keys.length; i++) {
                            String key = keys[i];
                            valueList.add(prelabel + makeKeyPretty(key));
                            prettyColumns.put(prelabel + makeKeyPretty(key), e.getColumnValues()[i]);
                        }
                    }
                    break;
                case STEPPER:
                    String[] keys = e.getKeys();
                    for (int i = 0; i < keys.length; i++) {
                        String key = keys[i];
                        valueList.add(prelabel + makeKeyPretty(key));
                        prettyColumns.put(prelabel + makePretty(key), e.getColumnValues()[i]);
                    }
                    break;
                case LABEL:
                    if (e.getArguments()[0].trim().equalsIgnoreCase("distinguished")){
                        prelabel = e.getDescriptions()[0] + ": ";
                        //prelabel is placed before all the columns
                    }
                    break;
                case SWITCH:
                    String[] keys1 = e.getKeys();
                    for (int i = 0; i < keys1.length; i++) {
                        String key = keys1[i];
                        //Capitalize the first letter of every word
                        String s = makeKeyPretty(key);
                        valueList.add(prelabel + s + "-Yes");
                        valueList.add(prelabel + s + "-No");
                        prettyColumns.put(prelabel + s + "-Yes", e.getColumnValues()[i*2]);
                        prettyColumns.put(prelabel + s + "-No", e.getColumnValues()[i*2+1]);
                    }
                    break;
                case SPACE:
                    break;
                case SLIDER:
                    String[] keys2 = e.getKeys();
                    for (int i = 0; i < keys2.length; i++) {
                        String key = keys2[i];
                        valueList.add(prelabel + makeKeyPretty(key));
                        prettyColumns.put(prelabel + key, e.getColumnValues()[i]);
                    }
                    break;
            }
        }

        for (Equation e: FileManager.getEquations()){
            valueList.add("Score: " + makePretty(e.getName()));
        }
        valueList.add("Score: Grand Total");
        return valueList;
    }

    public static void setResponseString(String s) {
        responseString = s;
    }

    public static void setResultSet(ResultSet resultSet) {
        RetrieveDataActivity.resultSet = resultSet;
    }

    public void waitForResponse(int seconds) {
        //responseString = "[team_num=442][team_color=Blue][num_matches=1][match_nums={64}][aut_reaches_defenses_yes=0][aut_reaches_defenses_no=1][aut_portcullis_yes=0][aut_portcullis_no=1][aut_chevaldefrise_yes=0][aut_chevaldefrise_no=1][aut_moat_yes=1][aut_moat_no=0][aut_ramparts_yes=0][aut_ramparts_no=1][aut_drawbridge_yes=0][aut_drawbridge_no=1][aut_sallyport_yes=0][aut_sallyport_no=1][aut_rockwall_yes=1][aut_rockwall_no=0][aut_rough_terrain_yes=0][aut_rough_terrain_no=1][aut_shoots_high_tower=0][aut_shoots_low_tower=1][aut_shoots_try_fail=0][aut_shoots_none=0][aut_underlowbar_yes=0][aut_underlowbar_no=1][aut_underlowbar_try_fail=0][aut_shot_accuracy=4.65748][teleop_starting_position_neutral_zone=0][teleop_starting_position_spy=1][teleop_portcullis_yes=0][teleop_portcullis_no=1][teleop_chevaldefrise_yes=0][teleop_chevaldefrise_no=1][teleop_moat_yes=0][teleop_moat_no=1][teleop_ramparts_yes=1][teleop_ramparts_no=0][teleop_drawbridge_yes=0][teleop_drawbridge_no=1][teleop_sallyport_yes=1][teleop_sallyport_no=0][teleop_rockwall_yes=0][teleop_rockwall_no=1][teleop_rough_terrain_yes=0][teleop_rough_terrain_no=1][teleop_underlowbar_yes=0][teleop_underlowbar_no=1][teleop_underlowbar_try_fail=0][teleop_climbing_yes=0][teleop_climbing_no=0][teleop_climbing_try_fail=1][teleop_defender_bot_yes=1][teleop_defender_bot_no=0][teleop_shots_highgoal=2][teleop_shots_lowgoal=4][teleop_shot_accuracy=9.70342][teleop_technical_fouls=3][teleop_normal_fouls=4][teleop_total_points=88][human_uses_gestures_yes=0][human_uses_gestures_no=1][human_effective=2.78613][autonomous_score=0.222][teleop_score=0.25][human_score=0][grand_total=0.472]";
        WaitTask waiting = new WaitTask();
        waiting.execute(seconds * 1000);
    }

    public enum RequestType {
        TEAM,
        SEARCH
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId())
        {
            case android.R.id.home:
                this.finish();
                break;
            case R.id.action_retrieve_settings:
                Intent intent = new Intent(this,RetrieveSettingsActivity.class);
                startActivity(intent);
                break;
        }
        return (true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_retrieve, menu);
        return true;
    }



    /**
     * Displays a waiting icon until data is received by the app
     */
    class WaitTask extends AsyncTask<Integer,Void,Void>{
        @Override
        protected void onPreExecute() {
            progress = new ProgressDialog(RetrieveDataActivity.this);
            progress.setTitle("Waiting");
            progress.setMessage("Waiting for response...");
            progress.setCancelable(false);
            progress.show();

        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            if (progress.isShowing()) {
                progress.dismiss();
            }

            displayResults(timeout);

        }

        @Override
        protected Void doInBackground(Integer... params) {
            long millis = System.currentTimeMillis();
            timeout = false;
            while (responseString == null && resultSet == null) {
                try {
                    Thread.sleep(50);
                    if (System.currentTimeMillis() - millis > params[0]) {
                        timeout = true;
                        break;
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }

            return null;
        }
    }

}
