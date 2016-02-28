package org.fullmetalfalcons.androidscouting.activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.fullmetalfalcons.androidscouting.R;
import org.fullmetalfalcons.androidscouting.elements.Element;
import org.fullmetalfalcons.androidscouting.equations.Equation;
import org.fullmetalfalcons.androidscouting.fileio.ConfigManager;
import org.fullmetalfalcons.androidscouting.views.TeamInfoView;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;

public class DisplayDataActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_data);
        Toolbar toolbar = (Toolbar) findViewById(R.id.retrieve_toolbar);
        setSupportActionBar(toolbar);
        //noinspection ConstantConditions
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Bundle bundle = getIntent().getExtras();
        HashMap<String,String> teamData = (HashMap<String, String>) bundle.get("TEAM_DATA");
        int numMatches = Integer.parseInt(teamData.get("num_matches"));

        TextView teamNum = (TextView) findViewById(R.id.team_number_display);
        TextView matchNum = (TextView) findViewById(R.id.match_number_display);

        teamNum.setText(teamData.get("team_num"));
        matchNum.setText(String.valueOf(numMatches));

        LinearLayout teamLayout = (LinearLayout) findViewById(R.id.team_display_scroll);
        TextView label = new TextView(this);

        String value = "";
        double raw = 0.0;
        double average = 0.0;

        for (Element e: ConfigManager.getElements()){
            switch(e.getType()){

                case SEGMENTED_CONTROL:
                    for (String key:e.getKeys()){

                        //Capitalize the first letter of every word
                        String s = makeKeyPretty(key);
                        for (int i = 0; i<e.getArguments().length;i++){
                            String args = e.getArguments()[i];
                            value = s + "-" + args;
                            try{
                                raw = Double.parseDouble(teamData.get(e.getColumnValues()[i]));
                            } catch (NumberFormatException e1){
                                try {
                                    int ii = Integer.parseInt(teamData.get(key));
                                    raw = ii*1.0;
                                } catch (NumberFormatException e2){

                                }
                            }
                            average = 1.0*raw/numMatches;
                            teamLayout.addView(new TeamInfoView(this, value, raw, average));
                        }

                    }
                    break;
                case TEXTFIELD:
                    if (e.getArguments()[0].equalsIgnoreCase("number") || e.getArguments()[0].equalsIgnoreCase("decimal")){
                        String[] keys = e.getKeys();
                        for (int i = 0; i < keys.length; i++) {
                            String key = keys[i];
                            value = makeKeyPretty(key);
                            try {
                                raw = Double.parseDouble(teamData.get(e.getColumnValues()[i]));
                            } catch (NumberFormatException e1) {
                                try {
                                    int ii = Integer.parseInt(teamData.get(e.getColumnValues()[i]));
                                    raw = ii * 1.0;
                                } catch (NumberFormatException e2) {

                                }
                            }
                            average = 1.0 * raw / numMatches;
                            teamLayout.addView(new TeamInfoView(this, value, raw, average));
                        }
                    }



                    break;
                case STEPPER:
                    String[] keys = e.getKeys();
                    for (int i = 0; i < keys.length; i++) {
                        String key = keys[i];
                        value = makeKeyPretty(key);
                        try {
                            raw = Double.parseDouble(teamData.get(e.getColumnValues()[i]));
                        } catch (NumberFormatException e1) {
                            try {
                                int ii = Integer.parseInt(teamData.get(e.getColumnValues()[i]));
                                raw = ii * 1.0;
                            } catch (NumberFormatException e2) {

                            }
                        }
                        average = 1.0 * raw / numMatches;
                        teamLayout.addView(new TeamInfoView(this, value, raw, average));
                    }
                    break;
                case LABEL:
                    if (e.getArguments()[0].trim().equalsIgnoreCase("distinguished")){
                        label = new TextView(this);
                        label.setText(e.getDescriptions()[0]);
                        label.setTextSize(30);
                        label.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                        teamLayout.addView(label);
                    }
                    break;
                case SWITCH:
                    String[] keys1 = e.getKeys();
                    for (int i = 0; i < keys1.length; i++) {
                        String key = keys1[i];
                        //Capitalize the first letter of every word
                        String s = makeKeyPretty(key);
                        value = s + "-Yes";
                        try {
                            raw = Double.parseDouble(teamData.get(e.getColumnValues()[i * 2]));
                        } catch (NumberFormatException e1) {
                            try {
                                int ii = Integer.parseInt(teamData.get(e.getColumnValues()[i*2]));
                                raw = ii * 1.0;
                            } catch (NumberFormatException e2) {

                            }
                        }
                        teamLayout.addView(new TeamInfoView(this, value, raw, average));
                        value = s + "-No";
                        try {
                            raw = Double.parseDouble(teamData.get(e.getColumnValues()[i * 2+1]));
                        } catch (NumberFormatException e1) {
                            try {
                                int ii = Integer.parseInt(teamData.get(e.getColumnValues()[i*2+1]));
                                raw = ii * 1.0;
                            } catch (NumberFormatException e2) {

                            }
                        }
                        teamLayout.addView(new TeamInfoView(this, value, raw, average));
                    }
                    break;
                case SPACE:
                    break;
                case SLIDER:
                    String[] keys2 = e.getKeys();
                    for (int i = 0; i < keys2.length; i++) {
                        String key = keys2[i];
                        value = makeKeyPretty(key);
                        try {
                            raw = Double.parseDouble(teamData.get(e.getColumnValues()[i]));
                        } catch (NumberFormatException e1) {
                            try {
                                int ii = Integer.parseInt(teamData.get(e.getColumnValues()[i]));
                                raw = ii * 1.0;
                            } catch (NumberFormatException e2) {

                            }
                        }
                        average = 1.0 * raw / numMatches;
                        teamLayout.addView(new TeamInfoView(this, value, raw, average));
                    }
                    break;
            }
        }
        label = new TextView(this);
        label.setText("Equations");
        label.setTextSize(30);
        label.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        teamLayout.addView(label);
        for (Equation e: ConfigManager.getEquations()){
            value = e.getName();
            try {
                raw = Double.parseDouble(teamData.get(e.getColumnValue()));
            } catch (NumberFormatException e1) {
                try {
                    int ii = Integer.parseInt(teamData.get(e.getColumnValue()));
                    raw = ii * 1.0;
                } catch (NumberFormatException e2) {

                }
            }
            average = 1.0 * raw / numMatches;
            teamLayout.addView(new TeamInfoView(this, value, raw, average));
        }


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
