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

/**
 * Activity that displays team data in a grid pattern
 */
public class DisplayDataActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_data);
        Toolbar toolbar = (Toolbar) findViewById(R.id.retrieve_toolbar);
        setSupportActionBar(toolbar);
        //noinspection ConstantConditions
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Get team data passed through the intent
        Bundle bundle = getIntent().getExtras();
        HashMap<String,String> teamData = (HashMap<String, String>) bundle.get("TEAM_DATA");
        assert teamData != null;
        int numMatches = Integer.parseInt(teamData.get("num_matches"));

        TextView teamNum = (TextView) findViewById(R.id.team_number_display);
        TextView matchNum = (TextView) findViewById(R.id.match_number_display);

        teamNum.setText(teamData.get("team_num"));
        matchNum.setText(String.valueOf(numMatches));

        LinearLayout teamLayout = (LinearLayout) findViewById(R.id.team_display_scroll);
        TextView label;

        String value = "";
        double raw = 0.0;
        double average = 0.0;

        for (Element e: ConfigManager.getElements()){
            switch(e.getType()){

                case SEGMENTED_CONTROL:
                    for (String key:e.getKeys()){

                        //Capitalize the first letter of every word
                        String s = makeKeyPretty(key);
                        //For each argument
                        for (int i = 0; i<e.getArguments().length;i++){
                            String args = e.getArguments()[i];
                            //Splice argument with name
                            value = s + "-" + args;
                            //Calculate raw value and average
                            try{
                                raw = Double.parseDouble(teamData.get(e.getColumnValues()[i]));
                            } catch (NumberFormatException e1){
                                try {
                                    int ii = Integer.parseInt(teamData.get(key));
                                    raw = ii*1.0;
                                } catch (NumberFormatException e2){
                                    //There really shouldn't be any reason for this to happen unless something has gone seriously wrong
                                }
                            }
                            average = 1.0*raw/numMatches;
                            teamLayout.addView(new TeamInfoView(this, value, raw, average));
                        }

                    }
                    break;
                case TEXTFIELD:
                    //If the textfield returns a number
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
                                    //By definition, this should return no numbers
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
                                //Seriously no numbers
                            }
                        }
                        average = 1.0 * raw / numMatches;
                        teamLayout.addView(new TeamInfoView(this, value, raw, average));
                    }
                    break;
                case LABEL:
                    //If the label is distinguished, add a label to the layout
                    if (e.getArguments()[0].trim().equalsIgnoreCase("distinguished")){
                        label = new TextView(this);
                        label.setText(e.getDescriptions()[0]);
                        label.setTextSize(30);
                        label.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                        teamLayout.addView(label);
                    }
                    break;
                case SWITCH:
                    //Switch needs to be run twice once for "yes" and once for "no"
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
                                //Absolutely no numbers
                            }
                        }
                        average = 1.0 * raw / numMatches;
                        teamLayout.addView(new TeamInfoView(this, value, raw, average));
                        value = s + "-No";
                        try {
                            raw = Double.parseDouble(teamData.get(e.getColumnValues()[i * 2+1]));
                        } catch (NumberFormatException e1) {
                            try {
                                int ii = Integer.parseInt(teamData.get(e.getColumnValues()[i*2+1]));
                                raw = ii * 1.0;
                            } catch (NumberFormatException e2) {
                                //See above
                            }
                        }
                        average = 1.0 * raw / numMatches;
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
                                //Cannot return a nonnumber
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
                    //Equations cannot return non numbers
                }
            }
            average = 1.0 * raw / numMatches;
            teamLayout.addView(new TeamInfoView(this, value, raw, average));
        }


    }

    /**
     * Ensures that the activity returns to previous activity correctly
     *
     * @param item Option selected
     * @return Something
     */
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
