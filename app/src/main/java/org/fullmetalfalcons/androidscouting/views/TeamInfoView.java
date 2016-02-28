package org.fullmetalfalcons.androidscouting.views;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.fullmetalfalcons.androidscouting.R;

import java.math.BigDecimal;

/**
 * Created by djher on 2/27/2016.
 */
public class TeamInfoView extends LinearLayout {

    public TeamInfoView(Context context, String columnValue, double raw, double average) {
        super(context);
        LayoutInflater.from(getContext()).inflate(R.layout.team_info_view_layout,this);
        TextView columnView = (TextView) findViewById(R.id.column_value_view);
        TextView rawView = (TextView) findViewById(R.id.raw_value_view);
        TextView averageView = (TextView) findViewById(R.id.average_value_view);

        columnView.setText(columnValue);
        rawView.setText(String.valueOf(round(raw,3)));
        averageView.setText(String.valueOf(round(average,3)));
    }

    public double round(double value, int numberOfDigitsAfterDecimalPoint) {
        BigDecimal bigDecimal = new BigDecimal(value);
        bigDecimal = bigDecimal.setScale(numberOfDigitsAfterDecimalPoint,
                BigDecimal.ROUND_HALF_UP);
        return bigDecimal.doubleValue();
    }


}
