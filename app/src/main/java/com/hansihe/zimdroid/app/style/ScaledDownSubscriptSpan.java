package com.hansihe.zimdroid.app.style;

import android.text.TextPaint;
import android.text.style.MetricAffectingSpan;

public class ScaledDownSubscriptSpan extends MetricAffectingSpan {

    public ScaledDownSubscriptSpan() {
    }

    @Override
    public void updateMeasureState(TextPaint tp) {
        tp.baselineShift -= (int) (tp.ascent() / 2);
        tp.setTextSize(tp.getTextSize() / 2);
    }

    @Override
    public void updateDrawState(TextPaint tp) {
        tp.baselineShift -= (int) (tp.ascent() / 2);
        tp.setTextSize(tp.getTextSize() / 2);
    }
}
