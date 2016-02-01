package org.fullmetalfalcons.androidscouting;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import java.sql.Struct;

/**
 * Created by djher on 1/31/2016.
 */
public class SeekBarWithValues extends RelativeLayout implements SeekBar.OnSeekBarChangeListener {

    private TextView mMinText;
    private TextView mMaxText;
    private TextView mCurrentText;
    private SeekBar mSeek;
    private int max;

    public SeekBarWithValues(Context context, int min, int max) {
        super(context);
        this.max = max;
        LayoutInflater.from(getContext()).inflate(
                R.layout.seek_bar_layout, this);
        // the minimum value is always 0
        mMinText = (TextView) findViewById(R.id.minValue);
        mMinText.setText(String.valueOf(min));
        mMaxText = (TextView) findViewById(R.id.maxValue);
        mCurrentText = (TextView) findViewById(R.id.curentValue);
        mSeek = (SeekBar) findViewById(R.id.seekBar);
        mSeek.setMax(max-min);
        mSeek.setOnSeekBarChangeListener(this);
        mMaxText.setText(String.valueOf(max));
        updateCurrentText(0);
    }

    /**
     * This needs additional work to make the current progress text stay
     * right under the thumb drawable.
     *
     * @param newProgress
     *            the new progress for which to place the text
     */
    public void updateCurrentText(int newProgress) {
        mCurrentText.setText(String.valueOf(newProgress));
        final int padding = mMinText.getWidth() + mSeek.getPaddingLeft();
        final int totalSeekWidth = mSeek.getWidth();
        final RelativeLayout.LayoutParams lp = (LayoutParams) mCurrentText
                .getLayoutParams();
        final int seekLocation = ((mSeek.getWidth()-mMaxText.getWidth()-mSeek.getPaddingRight()
                -mMaxText.getPaddingLeft()-mMaxText.getPaddingRight())/mSeek.getMax())*newProgress;
        lp.leftMargin = seekLocation + padding;
        mCurrentText.setLayoutParams(lp);
    }

    public SeekBar getSeekBar() {
        return mSeek;
    }

    public void updateSeekMaxValue(int newValue) {
        max = newValue;
        mMaxText.setText(max);
        mSeek.setMax(max);
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        updateCurrentText(progress);
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }
}