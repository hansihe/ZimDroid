package com.hansihe.zimdroid.app;

import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.widget.EditText;
import android.widget.TextView;
import com.hansihe.zimdroid.app.markup.MarkupParser;
import roboguice.activity.RoboActivity;

import java.io.IOException;
import java.io.StringReader;

public class TestActivity extends RoboActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EditText textView = new EditText(this);
        setContentView(textView);

        try {
            textView.setText(MarkupParser.parse(new StringReader("=== Testing ===\n\nTesting\n\n" + "abc def **ghijk** testing **lmnop** __qrst__ uv **\nabc^{def{} ghi}jkl^ {mno}pqrst\n")));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
