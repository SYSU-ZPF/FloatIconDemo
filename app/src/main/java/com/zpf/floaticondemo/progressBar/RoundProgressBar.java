package com.zpf.floaticondemo.progressBar;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.widget.ProgressBar;

import androidx.annotation.ColorInt;

import com.zpf.floaticondemo.R;

import static com.zpf.floaticondemo.util.Util.dp2px;

public class RoundProgressBar extends ProgressBar {

    Paint mPaint;

    @ColorInt
    int roundColor;


    float innerRadius;


    float outRadius;

    int max,progress;
    RectF rectF;


    public RoundProgressBar(Context context) {
        super(context);
    }

    public RoundProgressBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        initAttrs(context, attrs);
    }

    public RoundProgressBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initAttrs(context, attrs);
    }

    public RoundProgressBar(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initAttrs(context, attrs);

    }

    void initAttrs(Context context, AttributeSet attrs){
        mPaint = new Paint();
        mPaint.setAntiAlias(true);

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.RoundProgressBar);
        roundColor = typedArray.getColor(R.styleable.RoundProgressBar_roundColor, Color.GREEN);
        innerRadius = typedArray.getDimension(R.styleable.RoundProgressBar_innerRadius, dp2px(context, 23));
        outRadius = typedArray.getDimension(R.styleable.RoundProgressBar_outRadius,dp2px(context,25));
        max = typedArray.getInteger(R.styleable.RoundProgressBar_max, 100);
        progress = typedArray.getInteger(R.styleable.RoundProgressBar_progress, 20);

        typedArray.recycle();
        float roundWidth = outRadius - innerRadius;
        rectF = new RectF(roundWidth /2 , roundWidth /2, outRadius *2 - roundWidth /2  , outRadius *2 - roundWidth /2 );

    }

    @Override
    protected synchronized void onDraw(Canvas canvas) {
        float cx = outRadius;
        float cy = outRadius;
        float radius = innerRadius;
        float roundWidth = outRadius - innerRadius;
        mPaint.setColor(roundColor);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(outRadius - innerRadius);
//        canvas.drawCircle(cx,cy,radius,mPaint);

        //2.绘制圆弧

        mPaint.setColor(roundColor);//设置画笔颜色
        canvas.drawArc(rectF, 0, progress * 360 / max, false, mPaint);
    }

    @Override
    public void setProgress(int progress) {
        this.progress = progress;
    }
}
