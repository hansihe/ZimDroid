package com.hansihe.zimdroid.app;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.widget.Button;
import android.widget.Checkable;
import android.widget.ImageButton;

public class CheckableImageButton extends ImageButton implements Checkable {
    private boolean mChecked;
    private boolean mBroadcasting;
    private int mPersonality;
    private OnCheckedChangeListener mOnCheckedChangeListener;

    private static final int[] CHECKED_STATE_SET = { R.attr.is_checked };

    private static final int PERSONALITY_RADIO_BUTTON = 0;
    private static final int PERSONALITY_CHECK_BOX = 1;

    public CheckableImageButton(Context context) {
        this(context, null);
    }

    public CheckableImageButton(Context context, AttributeSet attrs) {
        this(context, attrs, android.R.attr.buttonStyle);
    }

    public CheckableImageButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CheckableImageButton);

        mPersonality = a.getInt(R.styleable.CheckableImageButton_personality, PERSONALITY_RADIO_BUTTON);
        boolean checked = a.getBoolean(R.styleable.CheckableImageButton_is_checked, false);
        setChecked(checked);

        a.recycle();
    }

    public void toggle() {
        toggle(false);
    }

    public void toggle(boolean user) {
        setChecked(!mChecked, user);
    }

    @Override
    public boolean performClick() {
        if (mPersonality == PERSONALITY_RADIO_BUTTON) {
            setChecked(true, true);
        } else if (mPersonality == PERSONALITY_CHECK_BOX) {
            toggle(true);
        }
        return super.performClick();
    }

    public boolean isChecked() {
        return mChecked;
    }

    /**
     * <p>
     * Changes the checked state of this button.
     * </p>
     *
     * @param checked
     *            true to check the button, false to uncheck it
     */
    public void setChecked(boolean checked) {
        setChecked(checked, false);
    }

    public void setChecked(boolean checked, boolean user) {
        if (mChecked != checked) {
            mChecked = checked;
            refreshDrawableState();

            // Avoid infinite recursions if setChecked() is called from a listener
            if (mBroadcasting) {
                return;
            }

            mBroadcasting = true;
            if (mOnCheckedChangeListener != null) {
                mOnCheckedChangeListener.onCheckedChanged(this, mChecked, user);
            }

            mBroadcasting = false;
        }
    }

    /**
     * Register a callback to be invoked when the checked state of this button changes.
     *
     * @param listener
     *            the callback to call on checked state change
     */
    public void setOnCheckedChangeListener(OnCheckedChangeListener listener) {
        mOnCheckedChangeListener = listener;
    }

    /**
     * Interface definition for a callback.
     */
    public static interface OnCheckedChangeListener {
        /**
         * Called when the checked state of a button has changed.
         *
         * @param button
         *            The button view whose state has changed.
         * @param isChecked
         *            The new checked state of button.
         */
        void onCheckedChanged(CheckableImageButton button, boolean isChecked, boolean user);
    }

    @Override
    public int[] onCreateDrawableState(int extraSpace) {
        final int[] drawableState = super.onCreateDrawableState(extraSpace + 1);
        if (isChecked()) {
            mergeDrawableStates(drawableState, CHECKED_STATE_SET);
        }
        return drawableState;
    }

    @Override
    protected void drawableStateChanged() {
        super.drawableStateChanged();
        invalidate();
    }

    static class SavedState extends BaseSavedState {
        boolean checked;

        SavedState(Parcelable superState) {
            super(superState);
        }

        private SavedState(Parcel in) {
            super(in);
            checked = (Boolean) in.readValue(null);
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeValue(checked);
        }

        public static final Parcelable.Creator<SavedState> CREATOR = new Parcelable.Creator<SavedState>() {
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }

    @Override
    public Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        SavedState ss = new SavedState(superState);
        ss.checked = isChecked();
        return ss;
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        SavedState ss = (SavedState) state;

        super.onRestoreInstanceState(ss.getSuperState());
        setChecked(ss.checked);
        requestLayout();
    }
}
