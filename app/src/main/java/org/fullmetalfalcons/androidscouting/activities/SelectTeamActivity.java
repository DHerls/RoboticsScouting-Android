package org.fullmetalfalcons.androidscouting.activities;

import android.content.SharedPreferences;
import android.os.ParcelUuid;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.GestureDetector;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import org.fullmetalfalcons.androidscouting.R;
import org.fullmetalfalcons.androidscouting.bluetooth.BluetoothCore;
import org.fullmetalfalcons.androidscouting.sql.SqlManager;
import org.fullmetalfalcons.androidscouting.views.TeamSelectorView;

import java.util.ArrayList;


public class SelectTeamActivity extends RetrieveDataActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_team);
        Toolbar toolbar = (Toolbar) findViewById(R.id.select_toolbar);
        setSupportActionBar(toolbar);
        //noinspection ConstantConditions
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        final LinearLayout mainLayout = (LinearLayout) findViewById(R.id.team_select_layout);
        Bundle bundle = getIntent().getExtras();
        getSupportActionBar().setTitle(bundle.getString("TITLE"));
        ArrayList<ArrayList<String>> data = (ArrayList<ArrayList<String>>) bundle.get("TEAM_DATA");
        for (ArrayList<String> a : data){
            mainLayout.addView(new TeamSelectorView(this,
                    a.get(0),
                    Integer.parseInt(a.get(1)),
                    Double.parseDouble(a.get(2))));
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

    public void requestTeamNum(String teamNum) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        if (teamNum.isEmpty()){
            sendError("Team number cannot be blank",false);
        } else {
            requestType = RequestType.TEAM;
            if (!sharedPref.getBoolean(RetrieveSettingsActivity.REMOTE_RETRIEVE_ENABLED_KEY,false)) {
                if (BluetoothCore.isConnected()) {
                    BluetoothCore.requestTeamNum(teamNum);
                    waitForResponse(5);
                } else {
                    sendError("Not currently connected to base", false);
                }
            } else {
                SqlManager.requestTeamNumber(this, teamNum);
                waitForResponse(5);
            }

        }
    }
}
