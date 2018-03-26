package com.abs.ccv;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * Created by Andrei Benincasa on 23/09/2017.
 */

public class CircularCountdownView extends RelativeLayout implements CircularCountdownViewListener {

    private Paint progressPaint;
    private float progressWidth;
    private int progressColor;
    private ProgressEdge progressEdge;

    private enum ProgressEdge {
        ROUND,
        SQUARE
    }

    private Paint progressStroke;
    private float progressStrokeWidth;
    private int progressStrokeColor;

    private Paint progressBackground;
    private int progressBackgroundColor;

    int minWidth;
    int minHeight;
    private float radius;
    private RectF circleBounds;
    private double progress;

    private CircularCountdownViewListener listener;

    private Handler viewHandler;
    private Runnable updateView;
    private final int FRAME_RATE = 1000 / 60; // 60 frames per second
    private long startTime;
    private long currentTime;
    private long duration;
    private long elapsedTime;
    private long initialElapsedTime;

    // Counter

    private TextView tvCounter;
    private TimeCounterMask timeCounterMask;

    // TODO: Include others masks
    private enum TimeCounterMask {
        ss(R.string.time_counter_ss_mask),
        mm_ss(R.string.time_counter_mm_ss_mask),
        mm_ss_SS(R.string.time_counter_mm_ss_SS_mask),
        HH_mm_ss(R.string.time_counter_HH_mm_ss_mask);

        private int maskResId;

        TimeCounterMask(int maskResId) {
            this.maskResId = maskResId;
        }

        public int getMaskResId() {
            return maskResId;
        }
    }

    public CircularCountdownView(Context context) {
        this(context, null, 0);
    }

    public CircularCountdownView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CircularCountdownView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    void init(Context context, AttributeSet attrs, int defStyleAttr) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CircularCountdownView, defStyleAttr, 0);

        try {
            progressWidth = a.getDimensionPixelSize(R.styleable.CircularCountdownView_progressWidth, getResources().getDimensionPixelSize(R.dimen.default_progress_width));
            progressColor = a.getColor(R.styleable.CircularCountdownView_progressColor, ContextCompat.getColor(context, R.color.default_progress_color));
            progressStrokeWidth = a.getDimensionPixelSize(R.styleable.CircularCountdownView_strokeWidth, getResources().getDimensionPixelSize(R.dimen.default_progress_stroke_width));
            progressStrokeWidth += progressWidth;
            progressStrokeColor = a.getColor(R.styleable.CircularCountdownView_strokeColor, ContextCompat.getColor(context, R.color.default_stroke_color));
            progressBackgroundColor = a.getColor(R.styleable.CircularCountdownView_backgroundColor, ContextCompat.getColor(context, R.color.default_background_color));
            int progressEdgeIndex = a.getInt(R.styleable.CircularCountdownView_progressEdge, getResources().getInteger(R.integer.default_progress_edge));
            progressEdge = ProgressEdge.values()[progressEdgeIndex];
            duration = a.getInt(R.styleable.CircularCountdownView_duration, getResources().getInteger(R.integer.default_duration));
            initialElapsedTime = a.getInt(R.styleable.CircularCountdownView_initialElapsedTime, getResources().getInteger(R.integer.default_initial_elapsed_time));
            int timeCounterMaskIndex = a.getInt(R.styleable.CircularCountdownView_timeCounterMask, getResources().getInteger(R.integer.default_time_counter_mask));
            timeCounterMask = TimeCounterMask.values()[timeCounterMaskIndex];
        } finally {
            a.recycle();
        }

        progressBackground = initPaint();
        progressStroke = initPaint();
        progressPaint = initPaint();

        minWidth = getResources().getDimensionPixelSize(R.dimen.default_progress_min_width);
        minHeight = getResources().getDimensionPixelSize(R.dimen.default_progress_min_height);
        circleBounds = new RectF();

        startTime = System.currentTimeMillis();
        currentTime = startTime;
        elapsedTime = currentTime - startTime + initialElapsedTime;
        progress = (double) elapsedTime / duration;

        if (listener == null) {
            listener = this;
        }

        tvCounter = new TextView(context);
        tvCounter.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimensionPixelSize(R.dimen.default_timer_counter_text_size));
        LayoutParams tvCounterLayoutParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        tvCounterLayoutParams.addRule(CENTER_IN_PARENT, TRUE);
        tvCounter.setText(getTimeRemainingFormatted());
        tvCounter.setLayoutParams(tvCounterLayoutParams);
        addView(tvCounter);

        viewHandler = new Handler();
        updateView = new Runnable() {
            @Override
            public void run() {
                currentTime = System.currentTimeMillis();
                elapsedTime = currentTime - startTime + initialElapsedTime;
                progress = (double) elapsedTime / duration;
                invalidate();

                if (elapsedTime >= duration) {
                    startTime = System.currentTimeMillis();
                    listener.onCountdownFinished();
                }

                tvCounter.setText(getTimeRemainingFormatted());

                viewHandler.postDelayed(updateView, FRAME_RATE);
            }
        };

        viewHandler.post(updateView);
    }

    private String getTimeRemainingFormatted() {
        switch (timeCounterMask) {
            case ss:
                return getContext().getString(timeCounterMask.getMaskResId(), getSeconds());
            case mm_ss:
                return getContext().getString(timeCounterMask.getMaskResId(), getMinutes(), getSeconds());
            case mm_ss_SS:
                return getContext().getString(timeCounterMask.getMaskResId(), getMinutes(), getSeconds(), getMilliseconds() / 10);
            case HH_mm_ss:
                return getContext().getString(timeCounterMask.getMaskResId(), getHours(), getMinutes(), getSeconds());
            default:
                return getContext().getString(timeCounterMask.getMaskResId(), getMinutes(), getSeconds());
        }
    }

    private long getHours() {
        return TimeUnit.MILLISECONDS.toHours(getTimeRemaining()) - TimeUnit.DAYS.toHours(TimeUnit.MILLISECONDS.toDays(getTimeRemaining()));
    }

    private long getMinutes() {
        return TimeUnit.MILLISECONDS.toMinutes(getTimeRemaining()) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(getTimeRemaining()));
    }

    private long getSeconds() {
        return TimeUnit.MILLISECONDS.toSeconds(getTimeRemaining()) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(getTimeRemaining()));
    }

    private long getMilliseconds() {
        return getTimeRemaining() - TimeUnit.SECONDS.toMillis(TimeUnit.MILLISECONDS.toSeconds(getTimeRemaining()));
    }

    Paint initPaint() {
        Paint paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);
        paint.setAntiAlias(true);
        return paint;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        int width;
        int height;

        if (widthMode == MeasureSpec.EXACTLY) {
            width = widthSize;
        } else if (widthMode == MeasureSpec.AT_MOST) {
            width = Math.min(minWidth, widthSize);
        } else {
            width = minWidth;
        }

        if (heightMode == MeasureSpec.EXACTLY) {
            height = heightSize;
        } else if (heightMode == MeasureSpec.AT_MOST) {
            height = Math.min(minHeight, heightSize);
        } else {
            height = minHeight;
        }

        int size;
        if (width > height) {
            size = height;
        } else {
            size = width;
        }

        radius = (size / 2) - (progressStrokeWidth / 2);

        setMeasuredDimension(width, height);
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);

        float centerWidth = getWidth() / 2;
        float centerHeight = getHeight() / 2;

        // set bound of our circle in the middle of the view
        circleBounds.set(
                centerWidth - radius,
                centerHeight - radius,
                centerWidth + radius,
                centerHeight + radius
        );

        setupPaintAttributes(progressPaint, progressWidth, progressColor);
        setupPaintAttributes(progressStroke, progressStrokeWidth, progressStrokeColor);
        setupPaintAttributes(progressBackground, progressWidth, progressBackgroundColor);

        // Draw progressPaint stroke
        canvas.drawCircle(centerWidth, centerHeight, radius, progressStroke);

        // Draw progressPaint background
        canvas.drawCircle(centerWidth, centerHeight, radius, progressBackground);

        // Draw progressPaint from top (-90º)
        canvas.drawArc(circleBounds, -90, (float) (progress * 360), false, progressPaint);
    }

    /*@Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float centerWidth = getWidth() / 2;
        float centerHeight = getHeight() / 2;

        // set bound of our circle in the middle of the view
        circleBounds.set(
                centerWidth - radius,
                centerHeight - radius,
                centerWidth + radius,
                centerHeight + radius
        );

        setupPaintAttributes(progressPaint, progressWidth, progressColor);
        setupPaintAttributes(progressStroke, progressStrokeWidth, progressStrokeColor);
        setupPaintAttributes(progressBackground, progressWidth, progressBackgroundColor);

        // Draw progressPaint stroke
        canvas.drawCircle(centerWidth, centerHeight, radius, progressStroke);

        // Draw progressPaint background
        canvas.drawCircle(centerWidth, centerHeight, radius, progressBackground);

        // Draw progressPaint from top (-90º)
        canvas.drawArc(circleBounds, -90, (float) (progress * 360), false, progressPaint);
    }*/

    // TODO: Check difference when all edges are equal
    void setupPaintAttributes(Paint paint, float width, int color) {
        paint.setStrokeWidth(width);
        switch (progressEdge) {
            case ROUND:
                paint.setStrokeCap(Paint.Cap.ROUND);
                break;
            case SQUARE:
                paint.setStrokeCap(Paint.Cap.SQUARE);
                break;
            default:
                paint.setStrokeCap(Paint.Cap.ROUND);
        }

        paint.setColor(color);
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public long getInitialElapsedTime() {
        return initialElapsedTime;
    }

    public void setInitialElapsedTime(long initialElapsedTime) {
        this.initialElapsedTime = initialElapsedTime;
    }

    public long getTimeRemaining() {
        return duration - elapsedTime;
    }

    public CircularCountdownViewListener getListener() {
        return listener;
    }

    public void setListener(CircularCountdownViewListener listener) {
        this.listener = listener;
    }

    @Override
    public void onCountdownFinished() {

    }

    // TODO: Add new features
    /*public void startCountdown() {
        startTime = System.currentTimeMillis();
        viewHandler.post(updateView);
    }

    public void pauseCountdown() {
        viewHandler.removeCallbacks(updateView);
    }*/
}
