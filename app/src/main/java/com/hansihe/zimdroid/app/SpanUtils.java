package com.hansihe.zimdroid.app;

import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.Spannable;
import android.text.Spanned;
import android.text.style.*;
import android.util.Log;
import com.hansihe.zimdroid.app.style.ScaledDownSuperscriptSpan;

import java.util.Arrays;

public class SpanUtils {

    static boolean isStyleSpan(Editable text, Object span) {
        // This WILL produce false positives when the span is already deleted.
        if (span == null) return false;
        if (span instanceof SuggestionSpan) return false;
        if (span.getClass().getName().equals("android.text.style.SuggestionRangeSpan")) return false;
        if (!(span instanceof CharacterStyle ||
                span instanceof ParagraphStyle ||
                span instanceof UpdateAppearance)) return false;
        return (text.getSpanFlags(span) & Spannable.SPAN_COMPOSING) == 0;
    }

    public static enum Style {
        BOLD(StyleSpan.class),
        ITALIC(StyleSpan.class),
        STRIKETHROUGH(StrikethroughSpan.class),
        UNDERLINE(UnderlineSpan.class),
        VERBATIM(TypefaceSpan.class),
        SUBSCRIPT(ScaledDownSuperscriptSpan.class),
        SUPERSCRIPT(SuperscriptSpan.class);

        public final Class<? extends CharacterStyle> classType;

        Style(Class<? extends CharacterStyle> classType) {

            this.classType = classType;
        }
    }

    public static Style getType(Object span) {
        //switch (get)
        for (Style style : Style.values()) {
            if (style.classType.isInstance(span)) {
                return style;
            }
        }
        /*if (span instanceof StyleSpan) {
            if (((StyleSpan) span).getStyle() == Typeface.BOLD) return Style.BOLD;
            if (((StyleSpan) span).getStyle() == Typeface.ITALIC) return Style.ITALIC;
        } else if (span instanceof UnderlineSpan) {
            return Style.UNDERLINE;
        } else if (span instanceof StrikethroughSpan) {
            return Style.STRIKETHROUGH;
        } else if (span instanceof )*/
        return null;
    }

    public static Style[] getTypeArray(Object[] spans) {
        Style[] styles = new Style[spans.length];
        for (int i = 0; i < styles.length; i++) {
            styles[i] = getType(spans[i]);
        }
        return styles;
    }

    public static Object makeSpan(Style type) {
        switch (type) {
            case BOLD: {
                return new StyleSpan(Typeface.BOLD);
            }
            case ITALIC: {
                return new StyleSpan(Typeface.ITALIC);
            }
            case UNDERLINE: {
                return new UnderlineSpan();
            }
            case STRIKETHROUGH: {
                return new StrikethroughSpan();
            }
            case VERBATIM: {
                return new TypefaceSpan("serif");
            }
            case SUBSCRIPT: {
                return new SubscriptSpan();
            }
            case SUPERSCRIPT: {
                return new ScaledDownSuperscriptSpan();
            }
            default: {
                return null;
            }
        }
    }

    public static void setRangeStyle(Editable text, SpanUtils.Style style, int start, int end, boolean state) {
        if (state) {
            text.setSpan(SpanUtils.makeSpan(style), start, end, Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
        } else {
            SpanUtils.subtractStyle(text, style, start, end);
        }
    }

    public static void mergeSpans(Editable text, Object[] spans) {
        Style[] types = getTypeArray(spans);
        boolean modified = false;

        for (int i = 0; i < spans.length; i++) {
            Style type = types[i];
            if (type == null) continue;
            Object span = spans[i];
            int spanStart = text.getSpanStart(span);
            int spanEnd = text.getSpanEnd(span);

            if (spanStart == -1) continue;

            for (int k = 0; k < spans.length; k++) {
                Style innerType = types[k];
                if (innerType == null) continue;
                Object innerSpan = spans[k];
                int innerSpanStart = text.getSpanStart(innerSpan);
                int innerSpanEnd = text.getSpanEnd(innerSpan);

                if (k == i) continue;
                if (!innerType.equals(type)) continue;
                if (innerSpanStart == -1) continue;

                if (innerSpanStart >= spanStart && innerSpanEnd <= spanEnd) {
                    text.removeSpan(innerSpan);
                    modified = true;
                    Log.d("WE", "Contains");
                } else if (innerSpanStart <= spanEnd && innerSpanStart >= spanStart && innerSpanEnd > spanEnd) {
                    text.setSpan(span, spanStart, innerSpanEnd, 0);
                    text.removeSpan(innerSpan);
                    modified = true;
                    Log.d("WE", "Sticking out at the end");
                } else if (spanStart <= innerSpanEnd && spanStart >= innerSpanStart && spanEnd > innerSpanEnd) {
                    text.setSpan(span, innerSpanStart, spanEnd, 0);
                    text.removeSpan(innerSpan);
                    modified = true;
                    Log.d("WE", "Sticking out at the beginning");
                }
            }
        }

        if (modified) mergeSpans(text, spans);
    }

    private static final Bundle sharedBundle = new Bundle();

    public static void subtractStyle(Editable text, Style style, int start, int end) {
        Object[] spans;
        switch (style) {
            case BOLD: {
                synchronized (sharedBundle) {
                    sharedBundle.clear();
                    sharedBundle.putInt("style", Typeface.BOLD);
                    spans = SpanUtils.styleSpanTypeFilter.getSpans(text, start, end, StyleSpan.class, sharedBundle);
                }
                break;
            }
            case ITALIC: {
                synchronized (sharedBundle) {
                    sharedBundle.clear();
                    sharedBundle.putInt("style", Typeface.ITALIC);
                    spans = SpanUtils.styleSpanTypeFilter.getSpans(text, start, end, StyleSpan.class, sharedBundle);
                }
                break;
            }
            case UNDERLINE: {
                spans = SpanUtils.styleSpanFilter.getSpans(text, start, end, UnderlineSpan.class, null);
                break;
            }
            case STRIKETHROUGH: {
                spans = SpanUtils.styleSpanFilter.getSpans(text, start, end, StrikethroughSpan.class, null);
                break;
            }
            default: {
                return;
            }
        }
        Log.d("WE", Arrays.toString(spans));

        for (Object span : spans) {
            int spanStart = text.getSpanStart(span);
            int spanEnd = text.getSpanEnd(span);

            if (spanStart >= start && spanEnd <= end) { // The span is fully contained in the selection
                text.removeSpan(span);
            } else if (spanStart >= start) { // The selection contains the beginning of the span
                text.setSpan(span, end, spanEnd, 0);
            } else if (spanEnd <= end) { // The selection contains the end of the span
                text.setSpan(span, spanStart, start, 0);
            } else { // The selection contains the middle of the span. What a pain!
                text.setSpan(span, spanStart, start, 0);
                Object span2 = SpanUtils.makeSpan(style);
                text.setSpan(span2, end, spanEnd, 0);
            }
        }
    }

    public static Object[] getStyleSpans(Editable text, int start, int end) {
        return SpanUtils.styleSpanFilter.getSpans(text, start, end, CharacterStyle.class, null);
    }

    public static String formatSpansForDebugPrint(Editable text, Object[] spans) {
        String out = "[";
        for (Object span : spans) {
            out += formatSpanForDebugPrint(text, span) + ", ";
        }
        out += "]";
        return out;
    }

    public static String formatSpanForDebugPrint(Editable text, Object span) {
        return span.toString() + "(Start:" + Integer.toString(text.getSpanStart(span)) + ", End:" + Integer.toString(text.getSpanEnd(span)) + ", Flag:" + Integer.toString(text.getSpanFlags(span)) + ")";
    }

    public static interface SpanFilter<T> {
        public T[] getSpans(Editable text, int start, int end, Class<? extends T> classFilter, Bundle data);

        boolean isIdeal(Object span, Editable editable, Bundle data);
        T[] makeArray(int size);
    }

    public static abstract class FilteringAbstractSpanFilter<T> implements SpanFilter<T> {
        @Override
        public T[] getSpans(Editable text, int start, int end, Class<? extends T> classFilter, Bundle data) {
            T[] spans = text.getSpans(start, end, classFilter);
            int spanCount = 0;
            for (int i = 0; i < spans.length; i++) {
                if (!isIdeal(spans[i], text, data)) {
                    spans[i] = null;
                    continue;
                }
                spanCount += 1;
            }
            if (spanCount == 0) {
                return makeArray(0);
            }
            T[] returnArray = makeArray(spanCount);
            int returnArrayPlace = 0;
            for (T span : spans) {
                if (span != null) {
                    returnArray[returnArrayPlace] = span;
                    returnArrayPlace += 1;
                }
            }
            return returnArray;
        }
    }

    public static class StyleSpanFilter extends FilteringAbstractSpanFilter<Object> {
        @Override
        public boolean isIdeal(Object span, Editable editable, Bundle data) {
            return SpanUtils.isStyleSpan(editable, span);
        }

        @Override
        public Object[] makeArray(int size) {
            return new Object[size];
        }
    }
    public static final StyleSpanFilter styleSpanFilter = new StyleSpanFilter();

    public static class StyleSpanTypeFilter extends FilteringAbstractSpanFilter<StyleSpan> {
        @Override
        public boolean isIdeal(Object span, Editable editable, Bundle data) {
            return SpanUtils.isStyleSpan(editable, span) && ((StyleSpan) span).getStyle() == data.getInt("style");
        }

        @Override
        public StyleSpan[] makeArray(int size) {
            return new StyleSpan[size];
        }
    }
    public static final StyleSpanTypeFilter styleSpanTypeFilter = new StyleSpanTypeFilter();

}
