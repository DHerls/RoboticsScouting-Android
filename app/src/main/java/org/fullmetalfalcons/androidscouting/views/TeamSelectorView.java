package org.fullmetalfalcons.androidscouting.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.fullmetalfalcons.androidscouting.R;
import org.fullmetalfalcons.androidscouting.activities.SelectTeamActivity;

import java.math.BigDecimal;

public class TeamSelectorView extends LinearLayout {

    private SelectTeamActivity a;
    private String teamNum;

    public TeamSelectorView(SelectTeamActivity context, String teamNum, int numMatches, double raw) {
        super(context);
        a = context;
        this.teamNum = teamNum;
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
            rawView.setText(String.valueOf(round(raw,3)));
        }

        double average = raw/numMatches;

        if (average%1==0){
            averageView.setText(String.valueOf((int)average));
        } else {
            averageView.setText(String.valueOf(round(average,3)));
        }


    }

    public double round(double value, int numberOfDigitsAfterDecimalPoint) {
        BigDecimal bigDecimal = new BigDecimal(value);
        bigDecimal = bigDecimal.setScale(numberOfDigitsAfterDecimalPoint,
                BigDecimal.ROUND_HALF_UP);
        return bigDecimal.doubleValue();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        System.out.println(MotionEvent.actionToString(event.getAction()));
        //a.onTouchEvent(event);

//        if (event.getAction() == MotionEvent.ACTION_DOWN){
//            a.requestTeamNum(teamNum);
//        }
        return super.onTouchEvent(event);
    }



}
