package org.fullmetalfalcons.androidscouting;

import android.content.res.AssetManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Space;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class ScoutingActivity extends AppCompatActivity {

    private final ArrayList<Element> ELEMENTS = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scouting);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        LinearLayout l = (LinearLayout) findViewById(R.id.mainLinear);
        l.requestFocus();
        createTheApp();

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
        s.setMinimumHeight(fab.getHeight()+fab.getPaddingBottom()+fab.getPaddingTop());
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
}
