package com.hansihe.zimdroid.app;

import android.content.Context;
import android.util.AttributeSet;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

public class TransparrentSlidingUpPanelLayout extends SlidingUpPanelLayout {
    public TransparrentSlidingUpPanelLayout(Context context) {
        super(context);
        init();
    }

    public TransparrentSlidingUpPanelLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public TransparrentSlidingUpPanelLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        //setBackground(null);
        setCoveredFadeColor(0);
    }



}
