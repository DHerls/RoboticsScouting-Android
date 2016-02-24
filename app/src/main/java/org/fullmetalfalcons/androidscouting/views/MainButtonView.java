package org.fullmetalfalcons.androidscouting.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View;

import org.fullmetalfalcons.androidscouting.R;

/**
 * TODO: document your custom view class.
 */
public class MainButtonView extends View {
    private int mBackgroundColor = Color.RED;
    private int accentColor = Color.BLACK;

    private String text = "Text";
    private Paint mTextPaint;
    private Paint mTextPaintOutline;
    private float mTextWidth;
    private float mTextHeight;

    private int position = 0;

    private float x;
    private float y;

    private Path backgroundPath;
    private Path accentPath;

    private Paint colorPaint;
    private Paint accentPaint;

    public MainButtonView(Context context) {
        super(context);
        init(null, 0);
    }

    public MainButtonView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public MainButtonView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    private void init(AttributeSet attrs, int defStyle) {
        // Load attributes
        final TypedArray a = getContext().obtainStyledAttributes(
                attrs, R.styleable.MainButtonView, defStyle, 0);


        text = a.getString(
                R.styleable.MainButtonView_buttonText);
        mBackgroundColor = a.getColor(
                R.styleable.MainButtonView_backgroundColor,
                mBackgroundColor);

        accentColor = a.getColor(R.styleable.MainButtonView_accentColor,accentColor);

        position = a.getInt(R.styleable.MainButtonView_orientation,position);

        a.recycle();

        // Set up a default TextPaint object
        mTextPaint = new TextPaint();
        mTextPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setTextAlign(Paint.Align.LEFT);


        // Update TextPaint and text measurements from attributes
        invalidateTextPaintAndMeasurements();
    }

    private void invalidateTextPaintAndMeasurements() {

        mTextPaint = new Paint();
        mTextPaint.setAntiAlias(true);
        mTextPaint.setTextSize(100);
        mTextPaint.setColor(Color.WHITE);
        mTextPaint.setStyle(Paint.Style.FILL);

        mTextPaintOutline = new Paint();
        mTextPaintOutline.setAntiAlias(true);
        mTextPaintOutline.setTextSize(100);
        mTextPaintOutline.setColor(Color.BLACK);
        mTextPaintOutline.setStyle(Paint.Style.STROKE);
        mTextPaintOutline.setStrokeWidth(8);

        colorPaint = new Paint();
        colorPaint.setAntiAlias(true);
        colorPaint.setColor(mBackgroundColor);

        accentPaint = new Paint();
        accentPaint.setAntiAlias(true);
        accentPaint.setColor(accentColor);

        mTextWidth = mTextPaint.measureText(text);
        Paint.FontMetrics fontMetrics = mTextPaint.getFontMetrics();
        mTextHeight = fontMetrics.bottom;



    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);



        if (backgroundPath ==null){
            int paddingLeft = getPaddingLeft();
            //int paddingTop = getPaddingTop();
            int paddingRight = getPaddingRight();
            int paddingBottom = getPaddingBottom();

            int width = getWidth() - paddingLeft - paddingRight;
            int height = getHeight() - paddingBottom;

            x = paddingLeft + (width - mTextWidth) / 2;
            y = (height + mTextHeight) / 3;
            backgroundPath = new Path();
            accentPath = new Path();

            if (position==0){
                backgroundPath.moveTo(0f, 0f);
                backgroundPath.lineTo(0f, height);
                backgroundPath.lineTo(width, .5f * height);
                backgroundPath.lineTo(width, 0f);
                backgroundPath.lineTo(0, 0);

                accentPath.moveTo(0f,0f);
                accentPath.lineTo(width, 0f);
                accentPath.lineTo(width, .20f * height);
                accentPath.lineTo(0f,0f);
            } else {
                backgroundPath.moveTo(0f, 0f);
                backgroundPath.lineTo(width, 0f);
                backgroundPath.lineTo(width, height);
                backgroundPath.lineTo(0f, .5f * height);
                backgroundPath.lineTo(0f, 0f);

                accentPath.moveTo(0f,0f);
                accentPath.lineTo(width, 0f);
                accentPath.lineTo(0f, .20f * height);
                accentPath.lineTo(0f,0f);


            }
        }
        colorPaint.setColor(mBackgroundColor);
        colorPaint.setStyle(Paint.Style.FILL);
        canvas.drawPath(backgroundPath, colorPaint);

        canvas.drawPath(accentPath,accentPaint);

        colorPaint.setStyle(Paint.Style.STROKE);
        colorPaint.setStrokeWidth(4);
        colorPaint.setColor(Color.BLACK);
        canvas.drawPath(backgroundPath, colorPaint);

        canvas.drawText(text, x, y, mTextPaintOutline);
        canvas.drawText(text, x, y, mTextPaint);
    }

}
