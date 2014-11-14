package com.hansihe.zimdroid.app.markup;

import android.util.Log;

public class StringUtils {

    public static int countOccurences(String text, char character) {
        int counter = 0;
        for (int i=0; i < text.length(); i++) {
            if(text.charAt(i) == character) {
                counter++;
            }
        }
        return counter;
    }

    public static int[][] matchBrackets(String text, char openBracket, char closeBracket) {
        int index = 0;
        int currentPos = 0;
        int openPos;

        // Count
        while ((openPos = text.indexOf(openBracket, currentPos)) != -1) {
            int closePos = text.indexOf(closeBracket, openPos);
            if (closePos == -1) {
                break;
            }

            currentPos = closePos + 1;
            index += 1;
        }

        int[][] result = new int[index][2];
        index = 0;
        currentPos = 0;

        while ((openPos = text.indexOf(openBracket, currentPos)) != -1) {
            int closePos = text.indexOf(closeBracket, openPos);
            if (closePos == -1) {
                break;
            }

            result[index][0] = openPos;
            result[index][1] = closePos;


            currentPos = closePos + 1;
            index += 1;
        }

        return result;
    }

    public static int[] matchTokens(String text, String token) {
        int tokenLength = token.length();

        int count = 0;
        int pos = 0;

        while ((pos = text.indexOf(token, pos) + tokenLength) != (-1 + tokenLength)) {
            count += 1;
        }

        int[] result = new int[count];
        count = 0;
        pos = 0;

        while ((pos = text.indexOf(token, pos) + tokenLength) != (-1 + tokenLength)) {
            result[count] = pos - tokenLength;
            count += 1;
        }

        return result;
    }

    public static int[][] matchTokenPairs(String text, String token) {
        // Make sure we have a even number of tokens.
        int[] tokens = matchTokens(text, token);
        int tokenNum = tokens.length  - (tokens.length % 2);
        int[][] tokenPairs = new int[tokenNum/2][2];

        for (int i = 0; i < tokenNum; i++) {
            int num = (int) Math.floor(i/2);
            int element = i % 2;

            tokenPairs[num][element] = tokens[i];
        }

        return tokenPairs;
    }

    public static int countCharsAtStart(String text, char character) {
        int num = 0;
        while (num < text.length()) {
            if (text.charAt(num) != character) {
                break;
            }
            num++;
        }
        return num;
    }

    public static String stripCharacterFromEnds(String text, char character) {
        int start = countCharsAtStart(text, character);
        int end = text.length() - countCharsAtStart(new StringBuilder(text).reverse().toString(), character);
        return text.substring(start, end).trim();
    }

}
