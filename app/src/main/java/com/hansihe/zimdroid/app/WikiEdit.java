package com.hansihe.zimdroid.app;

import android.annotation.TargetApi;
import android.content.*;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.text.*;
import android.text.style.*;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.EditText;

import java.util.*;

public class WikiEdit extends EditText {

    private class ActionTracker {
        public void notify(String text, boolean enter) {
            //Log.d("WikiEdit " + (enter ? "Enter" : "Exit"), text);
        }
    }
    ActionTracker t = new ActionTracker();

    private TextStyleListener styleListener = null;
    private HashSet<SpanUtils.Style> styles = new HashSet<SpanUtils.Style>();

    private HashMap<SpanUtils.Style, Boolean> deferredStyles = new HashMap<SpanUtils.Style, Boolean>();

    public WikiEdit(Context context) {
        super(context);
        init(context);
    }

    public WikiEdit(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public WikiEdit(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public WikiEdit(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    private void init(Context context) {
        setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        setBackground(null);
    }

    SpanWatcher watcher = new SpanWatcher() {
        boolean modifying = false;

        @Override
        public void onSpanAdded(Spannable text, Object what, int start, int end) {
            //if (modifying) return;
            modifying = true;

            if (SpanUtils.isStyleSpan(getText(), what)) {
                t.notify("onSpanAdded (" + what.toString() + ")", true);
                Object[] spans = SpanUtils.styleSpanFilter.getSpans(getText(), Math.max(0, start - 1), Math.min(text.length(), end + 1), CharacterStyle.class, null);
                Log.d("onSpanAdded Styles", SpanUtils.formatSpansForDebugPrint(getText(), spans));
                SpanUtils.mergeSpans(getText(), spans);
                t.notify("onSpanAdded (" + what.toString() + ")", false);
            }

            modifying = false;
        }

        @Override
        public void onSpanRemoved(Spannable text, Object what, int start, int end) {
            //if (modifying) return;
            modifying = true;

            if (SpanUtils.isStyleSpan(getText(), what)) {
                t.notify("onSpanRemoved (" + what.toString() + ")", true);
                //Log.d("WERem", what.toString());
                t.notify("onSpanRemoved (" + what.toString() + ")", false);
            }

            modifying = false;
        }

        @Override
        public void onSpanChanged(Spannable text, Object what, int ostart, int oend, int nstart, int nend) {
            //if (modifying) return;
            modifying = true;

            if (SpanUtils.isStyleSpan(getText(), what)) {
                t.notify("onSpanChanged (" + what.toString() + ", " + Integer.toString(ostart) + "," + Integer.toString(oend) + "," + Integer.toString(nstart) + "," + Integer.toString(nend) + ")", true);
                if (nend - nstart == 0) {
                    text.removeSpan(what);
                    return;
                }

                Object[] spans = SpanUtils.styleSpanFilter.getSpans(getText(), Math.max(0, nstart - 1), Math.min(text.length(), nend + 1), CharacterStyle.class, null);
                SpanUtils.mergeSpans(getText(), spans);
                t.notify("onSpanChanged (" + what.toString() + ")", false);
            }

            modifying = false;
        }
    };

    @Override
    public void setText(CharSequence text, BufferType type) {
        super.setText(text, type);
        getText().setSpan(watcher, 0, getText().length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
    }

    // Copied from TextView
    CharSequence removeSuggestionSpans(CharSequence text) {
        if (text instanceof Spanned) {
            Spannable spannable;
            if (text instanceof Spannable) {
                spannable = (Spannable) text;
            } else {
                spannable = new SpannableString(text);
                text = spannable;
            }

            SuggestionSpan[] spans = spannable.getSpans(0, text.length(), SuggestionSpan.class);
            for (int i = 0; i < spans.length; i++) {
                spannable.removeSpan(spans[i]);
            }
        }
        return text;
    }

    /**
     * IDEs may say this is not used, but it is! See superclass!
     * @param start
     * @param end
     * @return
     */
    CharSequence getTransformedText(int start, int end) {
        // TODO: Serialize output into wiki markup before giving it to the clipboard.
        // TODO: Find way to process pasted content as well.
        return removeSuggestionSpans(getText().subSequence(start, end));
    }

    private boolean cursorInSpace(int cursorStart, int cursorEnd, int spaceStart, int spaceEnd) {
        if (cursorStart == cursorEnd) {
            return spaceStart < cursorStart && spaceEnd >= cursorEnd;
        } else {
            return spaceStart <= cursorStart && spaceEnd >= cursorEnd;
        }
    }

    @Override
    protected void onSelectionChanged(int selStart, int selEnd) {
        super.onSelectionChanged(selStart, selEnd);

        if (getText() != null)
            Log.d("WE", Arrays.toString(SpanUtils.styleSpanFilter.getSpans(getText(), 0, getText().length(), Object.class, null)));

        if (styles != null && styleListener != null) {
            styles.clear();

            Editable editable = getText();

            Object[] styleSpans = SpanUtils.getStyleSpans(editable, selStart, selEnd);
            SpanUtils.Style[] styleTypes = SpanUtils.getTypeArray(styleSpans);

            Log.d("WEEE", SpanUtils.formatSpansForDebugPrint(editable, styleSpans));

            for (int i = 0; i < styleSpans.length; i++) {
                Object span = styleSpans[i];

                if (cursorInSpace(selStart, selEnd, editable.getSpanStart(span), editable.getSpanEnd(span))) {
                    styles.add(styleTypes[i]);
                }
            }

            styleListener.notifyStyle(styles);
        }
    }

    @Override
    protected void onTextChanged(CharSequence text, int start, int lengthBefore, int lengthAfter) {
        super.onTextChanged(text, start, lengthBefore, lengthAfter);

        if (deferredStyles != null) {
            Editable editable = getText();
            for (Map.Entry<SpanUtils.Style, Boolean> entry : deferredStyles.entrySet()) {
                SpanUtils.setRangeStyle(editable, entry.getKey(), start, start + lengthAfter, entry.getValue());
            }
            deferredStyles.clear();
        }
    }

    public void setStyle(SpanUtils.Style style, boolean state) {
        Editable text = getText();
        if (hasSelection()) { // If something is selected, we apply the style to that.
            SpanUtils.setRangeStyle(text, style, getSelectionStart(), getSelectionEnd(), state);
        } else { // If there is nothing selected, do not apply until we start typing.
            deferredStyles.put(style, state);
        }
    }

    public void setTextStyleListener(TextStyleListener listener) {
        styleListener = listener;
    }

    public interface TextStyleListener {

        public void notifyStyle(Set<SpanUtils.Style> styles);
    }

}
