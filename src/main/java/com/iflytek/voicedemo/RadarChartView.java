package com.iflytek.voicedemo;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

public class RadarChartView extends View {
    private int count = 5;
    private float angle = (float) (Math.PI * 2 / count);
    private float radius;
    private int centerX;
    private int centerY;
    private String[] titles = {"准确分", "流畅分", "发音分", "音律分", "完成分"};
    private float[] data = {0, 0, 0, 0, 0};
    private float maxValue = 100;
    private Paint mainPaint;
    private Paint valuePaint;
    private Paint textPaint;

    public RadarChartView(Context context) {
        this(context, null);
    }

    public RadarChartView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        mainPaint = new Paint();
        mainPaint.setAntiAlias(true);
        mainPaint.setColor(Color.LTGRAY);
        mainPaint.setStyle(Paint.Style.STROKE);
        mainPaint.setStrokeWidth(1f);

        valuePaint = new Paint();
        valuePaint.setAntiAlias(true);
        valuePaint.setColor(Color.parseColor("#4C3B5FF5"));
        valuePaint.setStyle(Paint.Style.FILL_AND_STROKE);

        textPaint = new Paint();
        textPaint.setAntiAlias(true);
        textPaint.setColor(Color.DKGRAY);
        textPaint.setTextSize(30f);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        radius = Math.min(h, w) / 2 * 0.7f;
        centerX = w / 2;
        centerY = h / 2;
        postInvalidate();
        super.onSizeChanged(w, h, oldw, oldh);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        drawPolygon(canvas);
        drawLines(canvas);
        drawText(canvas);
        drawRegion(canvas);
    }

    private void drawPolygon(Canvas canvas) {
        Path path = new Path();
        float r = radius / 5;
        for (int i = 1; i <= 5; i++) {
            float curR = r * i;
            path.reset();
            for (int j = 0; j < count; j++) {
                if (j == 0) {
                    path.moveTo(centerX, centerY - curR);
                } else {
                    float x = (float) (centerX + curR * Math.sin(angle * j));
                    float y = (float) (centerY - curR * Math.cos(angle * j));
                    path.lineTo(x, y);
                }
            }
            path.close();
            canvas.drawPath(path, mainPaint);
        }
    }

    private void drawLines(Canvas canvas) {
        Path path = new Path();
        for (int i = 0; i < count; i++) {
            path.reset();
            path.moveTo(centerX, centerY);
            float x = (float) (centerX + radius * Math.sin(angle * i));
            float y = (float) (centerY - radius * Math.cos(angle * i));
            path.lineTo(x, y);
            canvas.drawPath(path, mainPaint);
        }
    }

    private void drawText(Canvas canvas) {
        Paint.FontMetrics fontMetrics = textPaint.getFontMetrics();
        float fontHeight = fontMetrics.descent - fontMetrics.ascent;
        for (int i = 0; i < count; i++) {
            float x = (float) (centerX + (radius + fontHeight / 2) * Math.sin(angle * i));
            float y = (float) (centerY - (radius + fontHeight / 2) * Math.cos(angle * i));
            float dis = textPaint.measureText(titles[i]);
            canvas.drawText(titles[i], x - dis / 2, y, textPaint);
        }
    }

    private void drawRegion(Canvas canvas) {
        Path path = new Path();
        valuePaint.setAlpha(127);
        for (int i = 0; i < count; i++) {
            float percent = data[i] / maxValue;
            float x = (float) (centerX + radius * percent * Math.sin(angle * i));
            float y = (float) (centerY - radius * percent * Math.cos(angle * i));
            if (i == 0) {
                path.moveTo(x, y);
            } else {
                path.lineTo(x, y);
            }
        }
        path.close();
        canvas.drawPath(path, valuePaint);
    }

    public void setData(float[] data) {
        this.data = data;
        invalidate();
    }
}
