package org.fullmetalfalcons.androidscouting.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.fullmetalfalcons.androidscouting.R;
import org.fullmetalfalcons.androidscouting.Utils;

/**
 * Displays a single like of data for a single team
 *
 * Contains a column name, raw value and average value
 *
 * Created by djher on 2/27/2016.
 */
public class TeamInfoView extends LinearLayout {

    public TeamInfoView(Context context){
        super(context);
    }

    public TeamInfoView(Context context, AttributeSet attributeSet){
        super(context, attributeSet);
    }

    public TeamInfoView(Context context, AttributeSet attributeSet, int i){
        super(context, attributeSet, i);
    }

    public TeamInfoView(Context context, String columnValue, double raw, double average) {
        super(context);
        LayoutInflater.from(getContext()).inflate(R.layout.team_info_view_layout,this);
        TextView columnView = (TextView) findViewById(R.id.column_value_view);
        TextView rawView = (TextView) findViewById(R.id.raw_value_view);
        TextView averageView = (TextView) findViewById(R.id.average_value_view);

        columnView.setText(columnValue);
        if ((raw%1)==0){
            rawView.setText(String.valueOf((int) raw));
        } else {
            rawView.setText(String.valueOf(Utils.round(raw, 3)));
        }

        if ((average%1)==0){
            averageView.setText(String.valueOf((int) average));
        } else {
            averageView.setText(String.valueOf(Utils.round(average, 3)));

        }

    }


}
