package com.example.q5digitalmarketplace;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import java.util.ArrayList;
import java.util.List;

public class PieChartView extends View {
    private List<Float> dataValues = new ArrayList<>();
    private List<Integer> colors = new ArrayList<>();
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF rectF = new RectF();

    // This constructor is strictly required for layout XML inflation to work
    public PieChartView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    // Exposes the dataset pointer handle to your AnalyticsActivity screen
    public void setData(List<Float> values, List<Integer> customColors) {
        this.dataValues = values;
        this.colors = customColors;
        invalidate(); // Forces the canvas view to refresh and redraw immediately
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (dataValues == null || dataValues.isEmpty()) return;

        float total = 0;
        for (float val : dataValues) {
            total += val;
        }
        if (total == 0) return;

        int width = getWidth();
        int height = getHeight();
        int size = Math.min(width, height) - 60; // Keeps a balanced boundary margin

        int left = (width - size) / 2;
        int top = (height - size) / 2;
        rectF.set(left, top, left + size, top + size);

        float startAngle = 0;
        for (int i = 0; i < dataValues.size(); i++) {
            paint.setColor(colors.get(i % colors.size()));
            float sweepAngle = (dataValues.get(i) / total) * 360f;
            canvas.drawArc(rectF, startAngle, sweepAngle, true, paint);
            startAngle += sweepAngle;
        }
    }
}