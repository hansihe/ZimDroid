package com.hansihe.zimdroid.app.style;

import android.graphics.Color;
import android.text.TextPaint;
import android.text.style.MetricAffectingSpan;
import android.text.style.UpdateAppearance;

public class HeaderSpan extends MetricAffectingSpan implements UpdateAppearance {

    public static enum HeaderSize {
        H1(1.7f),
        H2(1.5f),
        H3(1.3f),
        H4(1.1f),
        H5(0.9f);

        final float scale;

        HeaderSize(float scale) {
            this.scale = scale;
        }
    }

    public static final int color = Color.parseColor("#4e9a06");
    public final HeaderSize size;

    public HeaderSpan(HeaderSize size) {
        this.size = size;
    }

    @Override
    public void updateMeasureState(TextPaint p) {
        p.setTextSize(p.getTextSize() * size.scale);
    }

    @Override
    public void updateDrawState(TextPaint p) {
        p.setTextSize(p.getTextSize() * size.scale);
        p.setColor(color);
        if (size == HeaderSize.H1) {
            p.setUnderlineText(true);
        }
    }
}
