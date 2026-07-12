package com.pcbiao.debtarchive;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.view.View;

final class TypeIconView extends View {
    private String type;
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    TypeIconView(Context context, String type) {
        super(context);
        this.type = type == null ? "" : type;
        paint.setColor(Color.rgb(156, 166, 163));
        paint.setStrokeWidth(dp(1.35f));
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeJoin(Paint.Join.ROUND);
        textPaint.setColor(Color.rgb(156, 166, 163));
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTypeface(Typeface.DEFAULT_BOLD);
        textPaint.setTextSize(dp(8.5f));
    }

    void setType(String type) {
        this.type = type == null ? "" : type;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float cx = getWidth() / 2f;
        float cy = getHeight() / 2f;
        if ("信用卡".equals(type)) drawCard(canvas, cx, cy);
        else if ("银行贷款".equals(type)) drawBank(canvas, cx, cy);
        else drawPhone(canvas, cx, cy);
    }

    private void drawPhone(Canvas canvas, float cx, float cy) {
        float left = cx - dp(9);
        float top = cy - dp(10);
        float right = cx + dp(3);
        float bottom = cy + dp(8);
        float radius = dp(2);
        canvas.drawArc(new RectF(left, top, left + radius * 2, top + radius * 2), 180, 90, false, paint);
        canvas.drawArc(new RectF(right - radius * 2, top, right, top + radius * 2), 270, 90, false, paint);
        canvas.drawArc(new RectF(left, bottom - radius * 2, left + radius * 2, bottom), 90, 90, false, paint);
        canvas.drawLine(left + radius, top, right - radius, top, paint);
        canvas.drawLine(left, top + radius, left, bottom - radius, paint);
        canvas.drawLine(right, top + radius, right, cy - dp(2.2f), paint);
        canvas.drawLine(left + radius, bottom, cx - dp(1.4f), bottom, paint);
        float coinX = cx + dp(5.2f);
        float coinY = cy + dp(4.8f);
        canvas.drawCircle(coinX, coinY, dp(5.4f), paint);
        Paint.FontMetrics metrics = textPaint.getFontMetrics();
        canvas.drawText("¥", coinX, coinY - (metrics.ascent + metrics.descent) / 2f, textPaint);
    }

    private void drawCard(Canvas canvas, float cx, float cy) {
        RectF card = new RectF(cx - dp(9), cy - dp(6.5f), cx + dp(9), cy + dp(6.5f));
        canvas.drawRoundRect(card, dp(2), dp(2), paint);
        canvas.drawLine(cx - dp(6.5f), cy - dp(1.5f), cx + dp(6.5f), cy - dp(1.5f), paint);
        canvas.drawLine(cx - dp(5.5f), cy + dp(3), cx - dp(1.5f), cy + dp(3), paint);
    }

    private void drawBank(Canvas canvas, float cx, float cy) {
        canvas.drawLine(cx - dp(9), cy - dp(4.5f), cx, cy - dp(9), paint);
        canvas.drawLine(cx, cy - dp(9), cx + dp(9), cy - dp(4.5f), paint);
        canvas.drawLine(cx - dp(7.5f), cy - dp(3), cx + dp(7.5f), cy - dp(3), paint);
        for (float x : new float[]{cx - dp(5), cx, cx + dp(5)}) canvas.drawLine(x, cy - dp(1), x, cy + dp(6), paint);
        canvas.drawLine(cx - dp(8.5f), cy + dp(8), cx + dp(8.5f), cy + dp(8), paint);
    }

    private float dp(float value) {
        return value * getResources().getDisplayMetrics().density;
    }
}
