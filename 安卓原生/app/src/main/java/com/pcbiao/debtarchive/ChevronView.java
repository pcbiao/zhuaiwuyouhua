package com.pcbiao.debtarchive;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;

final class ChevronView extends View {
    private final boolean expanded;
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

    ChevronView(Context context, boolean expanded) {
        super(context);
        this.expanded = expanded;
        paint.setColor(Color.rgb(109, 119, 116));
        paint.setStrokeWidth(dp(2.2f));
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeJoin(Paint.Join.ROUND);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float cx = getWidth() / 2f;
        float cy = getHeight() / 2f;
        float half = dp(5.5f);
        float rise = dp(4.5f);
        if (expanded) {
            canvas.drawLine(cx - half, cy + rise / 2f, cx, cy - rise / 2f, paint);
            canvas.drawLine(cx, cy - rise / 2f, cx + half, cy + rise / 2f, paint);
        } else {
            canvas.drawLine(cx - half, cy - rise / 2f, cx, cy + rise / 2f, paint);
            canvas.drawLine(cx, cy + rise / 2f, cx + half, cy - rise / 2f, paint);
        }
    }

    private float dp(float value) {
        return value * getResources().getDisplayMetrics().density;
    }
}
