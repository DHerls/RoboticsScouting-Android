package org.fullmetalfalcons.androidscouting.views;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.fullmetalfalcons.androidscouting.R;
import org.fullmetalfalcons.androidscouting.Utils;
import org.fullmetalfalcons.androidscouting.activities.SelectTeamActivity;

/**
 * Displays data for a single team in search results
 *
 * Contains team number, number of matches, raw value, and average value
 */
public class TeamSelectorView extends LinearLayout {

    private SelectTeamActivity a;

    public TeamSelectorView(Context context){
        super(context);
    }

    public TeamSelectorView(Context context, AttributeSet attributeSet){
        super(context, attributeSet);
    }

    public TeamSelectorView(Context context, AttributeSet attributeSet, int i){
        super(context, attributeSet, i);
    }

    public TeamSelectorView(SelectTeamActivity context, final String teamNum, int numMatches, double raw) {
        super(context);
        a = context;
        LayoutInflater.from(getContext()).inflate(R.layout.team_selector_view_layout, this);
        Log.d("Hey",teamNum + ":" + numMatches + ":" + raw);
        TextView teamNumView = (TextView) findViewById(R.id.selector_team_num);
        TextView numMatchesView = (TextView) findViewById(R.id.selector_num_matches);
        TextView rawView = (TextView) findViewById(R.id.selector_raw_value);
        TextView averageView = (TextView) findViewById(R.id.selector_average);

        teamNumView.setText(teamNum);
        numMatchesView.setText(String.valueOf(numMatches));

        if (raw%1==0){
            rawView.setText(String.valueOf((int)raw));
        } else {
            rawView.setText(String.valueOf(Utils.round(raw, 3)));
        }

        double average = raw/numMatches;

        if (average%1==0){
            averageView.setText(String.valueOf((int)average));
        } else {
            averageView.setText(String.valueOf(Utils.round(average, 3)));
        }

        teamNumView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                a.requestTeamNum(teamNum);
            }
        });


    }




}
