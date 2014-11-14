package com.hansihe.zimdroid.app.markup;

import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.util.Log;
import com.hansihe.zimdroid.app.SpanUtils;
import com.hansihe.zimdroid.app.style.HeaderSpan;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;

public class MarkupParser {

    private static class SpanContainer {
        private final Object span;
        private final int start;
        private final int end;

        public SpanContainer(Object span, int start, int end) {
            this.span = span;
            this.start = start;
            this.end = end;
        }

        public Object getSpan() {
            return span;
        }

        public int getStart() {
            return start;
        }

        public int getEnd() {
            return end;
        }
    }

    private static class LineState {
        ArrayList<SpanContainer> spanBuffer = new ArrayList<SpanContainer>();
        String textBuffer = "";

        public ArrayList<SpanContainer> getSpanBuffer() {
            return spanBuffer;
        }

        public void setSpanBuffer(ArrayList<SpanContainer> spanBuffer) {
            this.spanBuffer = spanBuffer;
        }

        public String getTextBuffer() {
            return textBuffer;
        }

        public void setTextBuffer(String textBuffer) {
            this.textBuffer = textBuffer;
        }
    }

    public static SpannableStringBuilder parse(Reader input) throws IOException {
        BufferedReader reader = new BufferedReader(input);
        SpannableStringBuilder output = new SpannableStringBuilder();

        String line;

        boolean first = true;
        while ((line = reader.readLine()) != null) {
            if (first) {
                first = false;
            } else {
                output.append('\n');
            }
            int begin = output.length();

            // Headers
            if (line.startsWith("=")) {
                int num = StringUtils.countCharsAtStart(line, '=');
                String stripped = StringUtils.stripCharacterFromEnds(line, '=');
                output.append(stripped);
                int end = begin + stripped.length();

                HeaderSpan span;
                switch (num) {
                    case 1: {
                        span = new HeaderSpan(HeaderSpan.HeaderSize.H5);
                        break;
                    }
                    case 2: {
                        span = new HeaderSpan(HeaderSpan.HeaderSize.H4);
                        break;
                    }
                    case 3: {
                        span = new HeaderSpan(HeaderSpan.HeaderSize.H3);
                        break;
                    }
                    case 4: {
                        span = new HeaderSpan(HeaderSpan.HeaderSize.H2);
                        break;
                    }
                    default: { // >= 5
                        span = new HeaderSpan(HeaderSpan.HeaderSize.H1);
                        break;
                    }
                }
                output.setSpan(span, begin, end, 0);

                // Zim does not parse styles inside headers
                continue;
            }

            LineState lineState = new LineState();
            lineState.setTextBuffer(line);

            processStyle(lineState, MarkupTokens.BOLD_TOKEN, SpanUtils.Style.BOLD);
            processStyle(lineState, MarkupTokens.ITALIC_TOKEN, SpanUtils.Style.ITALIC);
            processStyle(lineState, MarkupTokens.UNDERLINE_TOKEN, SpanUtils.Style.UNDERLINE);
            processStyle(lineState, MarkupTokens.STRIKETHROUGH_TOKEN, SpanUtils.Style.STRIKETHROUGH);
            processBracketStyle(lineState, "^", '{', '}', SpanUtils.Style.SUPERSCRIPT);
            processBracketStyle(lineState, "_", '{', '}', SpanUtils.Style.SUBSCRIPT);

            int lastOutputLength = output.length();
            output.append(lineState.getTextBuffer());
            for (SpanContainer span : lineState.getSpanBuffer()) {
                output.setSpan(span.getSpan(), lastOutputLength + span.getStart(), lastOutputLength + span.getEnd(), Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
            }

        }

        // We already have a method for merging overlaying/adjacent spans. Use that!
        SpanUtils.mergeSpans(output, output.getSpans(0, output.length(), Object.class));
        return output;
    }

    private static void processStyle(LineState lineState, String token, SpanUtils.Style style) {
        String textBuffer = "";

        int[][] boldTokenPairs = StringUtils.matchTokenPairs(lineState.getTextBuffer(), token);
        if (boldTokenPairs.length == 0) {
            textBuffer += lineState.getTextBuffer();
        } else {
            textBuffer += lineState.getTextBuffer().substring(0, boldTokenPairs[0][0]);
            for (int i = 0; i < boldTokenPairs.length; i++) {
                int startToken = boldTokenPairs[i][0] - ((2 * i) * token.length());
                int endToken = boldTokenPairs[i][1] - (((2 * i) + 1) * token.length());

                lineState.getSpanBuffer().add(new SpanContainer(SpanUtils.makeSpan(style), startToken, endToken));
                textBuffer += lineState.getTextBuffer().substring(
                        boldTokenPairs[i][0] + token.length(),
                        boldTokenPairs[i][1]);

                if (i != boldTokenPairs.length-1) {
                    textBuffer += lineState.getTextBuffer().substring(boldTokenPairs[i][1] + token.length(), boldTokenPairs[i+1][0]);
                }
            }
            textBuffer += lineState.getTextBuffer().substring(boldTokenPairs[boldTokenPairs.length-1][1] + token.length(), lineState.getTextBuffer().length());
        }

        lineState.setTextBuffer(textBuffer);
    }

    private static void processBracketStyle(LineState lineState, String token, char openBracket, char closeBracket, SpanUtils.Style style) {
        int[][] brackets = StringUtils.matchBrackets(lineState.getTextBuffer(), openBracket, closeBracket);
        String textBuffer = "";

        if (brackets.length == 0) {
            textBuffer += lineState.getTextBuffer();
        } else {
            boolean first = true;
            int removedBracketPairs = 0;
            for (int i = 0; i < brackets.length; i++) {
                int[] bracketPair = brackets[i];

                if (bracketPair[0] >= token.length() && lineState.getTextBuffer().substring(bracketPair[0] - token.length(), bracketPair[0]).equals(token)) {
                    if (first) {
                        textBuffer += lineState.getTextBuffer().substring(0, bracketPair[0] - token.length());
                    } else {
                        textBuffer += lineState.getTextBuffer().substring(brackets[i-1][1] + 1, bracketPair[0] - 1);
                    }

                    textBuffer += lineState.getTextBuffer().substring(bracketPair[0] + 1, bracketPair[1]);

                    removedBracketPairs += 1;

                    lineState.getSpanBuffer().add(new SpanContainer(SpanUtils.makeSpan(style), (bracketPair[0] + 1) - (removedBracketPairs * 2), bracketPair[1] - (removedBracketPairs * 2)));
                } else {
                    if (first) {
                        textBuffer += lineState.getTextBuffer().substring(0, bracketPair[1] + 1);
                    } else {
                        textBuffer += lineState.getTextBuffer().substring(brackets[i-1][1] + 1, bracketPair[1] + 1);
                    }
                }
                first = false;
            }
            textBuffer += lineState.getTextBuffer().substring(brackets[brackets.length - 1][1] + 1, lineState.getTextBuffer().length());
        }

        lineState.setTextBuffer(textBuffer);
    }

}
