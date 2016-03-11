package org.fullmetalfalcons.androidscouting.views;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.ScrollView;

/**
 * Created by djher on 3/10/2016.
 */
public class InterceptScrollView extends ScrollView {
    public InterceptScrollView(Context context) {
        super(context);
    }

    public InterceptScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public InterceptScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public InterceptScrollView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        getParent().requestDisallowInterceptTouchEvent(true);

    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        final int action = ev.getAction();
        System.out.println(action);
        switch (action)
        {
            case MotionEvent.ACTION_DOWN:
                return false;

            case MotionEvent.ACTION_MOVE:
                return false; // redirect MotionEvents to ourself

            case MotionEvent.ACTION_CANCEL:
                super.onTouchEvent(ev);
                break;

            case MotionEvent.ACTION_UP:
                return false;

            default:  break;
        }

        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        super.onTouchEvent(ev);
        return true;
    }
}
