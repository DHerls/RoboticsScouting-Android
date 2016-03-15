package org.fullmetalfalcons.androidscouting.activities;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import org.fullmetalfalcons.androidscouting.R;

/**
 * Activity designed only to display about info and credits
 */
public class AboutActivity extends DHActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        Toolbar toolbar = (Toolbar) findViewById(R.id.about_toolbar);
        setSupportActionBar(toolbar);

        //noinspection ConstantConditions
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }
}
