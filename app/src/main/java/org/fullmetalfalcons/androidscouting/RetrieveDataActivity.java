package org.fullmetalfalcons.androidscouting;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import org.fullmetalfalcons.androidscouting.elements.Element;
import org.fullmetalfalcons.androidscouting.equations.Equation;
import org.fullmetalfalcons.androidscouting.fileio.ConfigManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class RetrieveDataActivity extends AppCompatActivity {

    private HashMap<String, String> prettyColumns = new HashMap<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_retrieve_data);
        Toolbar toolbar = (Toolbar) findViewById(R.id.retrieve_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

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
        Spinner s = (Spinner) findViewById(R.id.column_spinner);
        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, valueList);
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        s.setAdapter(spinnerArrayAdapter);
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
}
