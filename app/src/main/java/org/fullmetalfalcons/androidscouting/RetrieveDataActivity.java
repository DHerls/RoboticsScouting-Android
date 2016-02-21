package org.fullmetalfalcons.androidscouting;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import org.fullmetalfalcons.androidscouting.elements.Element;
import org.fullmetalfalcons.androidscouting.fileio.ConfigManager;

import java.util.ArrayList;
import java.util.Arrays;

public class RetrieveDataActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_retrieve_data);
        Toolbar toolbar = (Toolbar) findViewById(R.id.retrieve_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        ArrayList<String> columnList = new ArrayList<>();
        for (Element e: ConfigManager.getElements()){
            columnList.addAll(Arrays.asList(e.getColumnValues()));
        }

        Spinner s = (Spinner) findViewById(R.id.column_spinner);
        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, columnList);
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        s.setAdapter(spinnerArrayAdapter);
    }
}
