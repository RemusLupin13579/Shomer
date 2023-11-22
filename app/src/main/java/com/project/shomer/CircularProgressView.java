package com.project.shomer;

// CircularProgressView.java
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class CircularProgressView extends View {

    private Paint progressPaint;
    private int progress;

    public CircularProgressView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        progressPaint = new Paint();
        progressPaint.setAntiAlias(true);
        progressPaint.setStyle(Paint.Style.FILL);
        progressPaint.setColor(getResources().getColor(R.color.colorPrimary)); // Change to your desired color
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int width = getWidth();
        int height = getHeight();
        int radius = Math.min(width, height) / 2;

        float startAngle = 0;
        float sweepAngle = (360f * progress) / 100;

        canvas.drawArc(0, 0, width, height, startAngle, sweepAngle, true, progressPaint);
    }

    public void setProgress(int progress) {
        this.progress = progress;
        invalidate();
    }
}
