package org.fullmetalfalcons.androidscouting.activities;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
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
import org.fullmetalfalcons.androidscouting.fileio.ConfigManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.transform.Result;

public class RetrieveDataActivity extends AppCompatActivity {

    private final HashMap<String, String> prettyColumns = new HashMap<>();
    private static volatile String responseString= null;
    private static RequestType requestType;
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

        Spinner columnSpinner = (Spinner) findViewById(R.id.column_spinner);
        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, getColumnValues());
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        columnSpinner.setAdapter(spinnerArrayAdapter);


        Button teamOkButton = (Button) findViewById(R.id.retrieve_team_ok);

        final EditText teamNumEditText = (EditText) findViewById(R.id.retrieve_team_num);

        teamOkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestTeamNum(teamNumEditText);
            }
        });

        teamNumEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

                if ((actionId == EditorInfo.IME_ACTION_DONE)) {
                    //Toast.makeText(getActivity(), "call",45).show();
                    // hide virtual keyboard
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(teamNumEditText.getWindowToken(), 0);

                    requestTeamNum(teamNumEditText);
                    return true;
                }
                return false;

            }
        });


        Button searchOkButton = (Button) findViewById(R.id.retrieve_team_search_button);
        Spinner typeSpinner = (Spinner) findViewById(R.id.value_spinner);
        Spinner operatorSpinner = (Spinner) findViewById(R.id.operator_spinner);
        final EditText valueText = (EditText) findViewById(R.id.value_edit_text);

        searchOkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO handle search button
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

                    //TODO handle search enter press
                    return true;
                }
                return false;

            }
        });

    }

    private void requestTeamNum(EditText teamNumEditText) {
        if (teamNumEditText.getText().toString().isEmpty()){
            sendError("Team number cannot be blank",false);
        } else {
            if (BluetoothCore.isConnected()){
                requestType = RequestType.TEAM;
                BluetoothCore.requestTeamNum(teamNumEditText.getText().toString());
                waitForResponse();
            } else {
                sendError("Not currently connected to base",false);
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
     * Sends a popup message to the user with a custom message.
     * Also closes the app if the error is fatal
     *
     * @param message message to send to the user
     * @param fatalError whether or not the app should close after user acknowledges
     */
    @SuppressWarnings("SameParameterValue")
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
            Log.wtf(getString(R.string.log_tag), message);
        } else {
            Log.e(getString(R.string.log_tag),message);

        }
    }

    private ArrayList<String> getColumnValues(){
        ArrayList<String> valueList = new ArrayList<>();
        String prelabel = "";
        for (Element e: ConfigManager.getElements()){
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
                    }
                    break;
                case SWITCH:
                    for (String key:e.getKeys()){
                        //Capitalize the first letter of every word
                        String s = makeKeyPretty(key);
                        valueList.add(prelabel + s + "-Yes");
                        valueList.add(prelabel + s + "-No");
                        prettyColumns.put(prelabel + s + "-Yes", e.getColumnValues()[0]);
                        prettyColumns.put(prelabel + s + "-No", e.getColumnValues()[1]);
                    }
                    break;
                case SPACE:
                    break;
                case SLIDER:
                    String[] keys1 = e.getKeys();
                    for (int i = 0; i < keys1.length; i++) {
                        String key = keys1[i];
                        valueList.add(prelabel + makeKeyPretty(key));
                        prettyColumns.put(prelabel + key, e.getColumnValues()[i]);
                    }
                    break;
            }
        }

        for (Equation e: ConfigManager.getEquations()){
            valueList.add("Score: " + makePretty(e.getName()));
        }
        valueList.add("Score: Grand Total");
        return valueList;
    }

    public static void setResponseString(String s) {
        System.out.println("HEY DIPSHIT");
        responseString = s;
    }

    private void waitForResponse() {

        WaitTask waiting = new WaitTask();
        waiting.execute();
    }

    private enum RequestType {
        TEAM,
        SEARCH

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId())
        {
            case android.R.id.home:
                this.finish();
                return (true);
        }
        return super.onOptionsItemSelected(item);
    }


    class WaitTask extends AsyncTask<Object,Void,Void>{
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

            if (!timeout) {
                if (requestType== RetrieveDataActivity.RequestType.TEAM) {
                    switch (responseString) {
                        case "NoReadTable":
                            sendError("No database has been established yet", false);
                            break;
                        case "NoReadTeam":
                            sendError("The team specified cannot be found", false);
                            break;
                        default:
                            Matcher m = p.matcher(responseString);
                            HashMap<String, String> teamInfo = new HashMap<>();
                            while (m.find()) {
                                String[] value = m.group(1).split("=");
                                teamInfo.put(value[0], value[1]);
                                System.out.println(value[0] + "=" + value[1]);
                            }
                            break;
                    }
                    responseString = null;

                } else {
                    //TODO Handle Searches
                }
            } else {
                sendError("Error request timeout", false);
            }

        }

        @Override
        protected Void doInBackground(Object... params) {
            long millis = System.currentTimeMillis();
            timeout = false;
            while (responseString == null) {
                try {
                    Thread.sleep(50);
                    if (System.currentTimeMillis() - millis > 5000) {
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
