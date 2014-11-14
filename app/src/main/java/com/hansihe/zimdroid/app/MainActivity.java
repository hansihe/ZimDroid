package com.hansihe.zimdroid.app;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import com.hansihe.zimdroid.app.markup.MarkupParser;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;
import roboguice.activity.RoboActionBarActivity;
import roboguice.inject.ContentView;
import roboguice.inject.InjectView;

import java.io.IOException;
import java.io.StringReader;
import java.util.Set;

@ContentView(R.layout.activity_main)
public class MainActivity extends RoboActionBarActivity {

    @InjectView(R.id.toolbar)
    Toolbar toolbar;

    @InjectView(R.id.text_edit)
    WikiEdit wikiEdit;
    @InjectView(R.id.sliding_card)
    CardView slidingCard;
    @InjectView(R.id.fold_bar)
    LinearLayout slidingCardFoldBar;

    @InjectView(R.id.bold_toggle)
    CheckableImageButton boldButton;
    @InjectView(R.id.italic_toggle)
    CheckableImageButton italicButton;
    @InjectView(R.id.underline_toggle)
    CheckableImageButton underlineButton;
    @InjectView(R.id.strikethrough_toggle)
    CheckableImageButton strikethroughButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d("MA", String.valueOf(boldButton));

        // Toolbar and drawer
        setSupportActionBar(toolbar);

        //wikiEdit.setText(new SpannableStringBuilder("Testy test test testitty \ntesting wat who when testing \nmuch.\n\nakjsdjhafsdakdjhahjfsa"));
        //wikiEdit.getText().setSpan(new HeaderSpan(HeaderSpan.HeaderSize.H1), 0, 10, Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
        //wikiEdit.getText().setSpan(new StyleSpan(Typeface.BOLD), 63, 70, Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
        //wikiEdit.getText().setSpan(new StyleSpan(Typeface.BOLD), 80, 85, Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
        try {
            wikiEdit.setText(MarkupParser.parse(new StringReader("=== Testing ===\n\nTesting\n\n" + "abc def **ghijk** testing **lmnop** __qrst__ uv **\nabc^{def{} ghi}jkl^ {mno}pqrst\n")));
        } catch (IOException e) {
            e.printStackTrace();
        }

        final TransparrentSlidingUpPanelLayout panelLayout = (TransparrentSlidingUpPanelLayout) findViewById(R.id.sliding_panel);
        panelLayout.setPanelSlideListener(new SlidingUpPanelLayout.PanelSlideListener() {
            @Override
            public void onPanelSlide(View view, float v) {
                int cardHeight = slidingCard.getHeight();
                int cardMinimizedHeight = panelLayout.getPanelHeight();

                int currentHeight = (int) ((cardHeight - cardMinimizedHeight) * v) + cardMinimizedHeight;
            }

            @Override
            public void onPanelCollapsed(View view) {
                wikiEdit.setPadding(wikiEdit.getPaddingLeft(), wikiEdit.getPaddingTop(), wikiEdit.getPaddingRight(), panelLayout.getPanelHeight() - (slidingCard.getPaddingTop() - slidingCard.getContentPaddingTop()));
                Log.d("MA", String.valueOf(slidingCard.getPaddingTop() - slidingCard.getContentPaddingTop()));
            }

            @Override
            public void onPanelExpanded(View view) {
                wikiEdit.setPadding(wikiEdit.getPaddingLeft(), wikiEdit.getPaddingTop(), wikiEdit.getPaddingRight(), slidingCard.getHeight() - (slidingCard.getPaddingTop() - slidingCard.getContentPaddingTop()));
            }

            @Override
            public void onPanelAnchored(View view) {}

            @Override
            public void onPanelHidden(View view) {}
        });
        wikiEdit.setPadding(wikiEdit.getPaddingLeft(), wikiEdit.getPaddingTop(), wikiEdit.getPaddingRight(), panelLayout.getPanelHeight() - (slidingCard.getPaddingTop() - slidingCard.getContentPaddingTop()));

        ViewGroup.LayoutParams atfLayoutParams = slidingCardFoldBar.getLayoutParams();
        atfLayoutParams.height = panelLayout.getPanelHeight() - slidingCard.getPaddingTop();
        slidingCardFoldBar.setLayoutParams(atfLayoutParams);

        wikiEdit.setTextStyleListener(new WikiEdit.TextStyleListener() {
            @Override
            public void notifyStyle(Set<SpanUtils.Style> styles) {
                boldButton.setChecked(styles.contains(SpanUtils.Style.BOLD));
                italicButton.setChecked(styles.contains(SpanUtils.Style.ITALIC));
                underlineButton.setChecked(styles.contains(SpanUtils.Style.UNDERLINE));
                strikethroughButton.setChecked(styles.contains(SpanUtils.Style.STRIKETHROUGH));
            }
        });

        boldButton.setOnCheckedChangeListener(new CheckableImageButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CheckableImageButton button, boolean isChecked, boolean user) {
                // If the interaction from the user, apply it, else, ignore it.
                if (user) {
                    wikiEdit.setStyle(SpanUtils.Style.BOLD, isChecked);
                }
            }
        });
        italicButton.setOnCheckedChangeListener(new CheckableImageButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CheckableImageButton button, boolean isChecked, boolean user) {
                // If the interaction from the user, apply it, else, ignore it.
                if (user) {
                    wikiEdit.setStyle(SpanUtils.Style.ITALIC, isChecked);
                }
            }
        });
        underlineButton.setOnCheckedChangeListener(new CheckableImageButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CheckableImageButton button, boolean isChecked, boolean user) {
                // If the interaction from the user, apply it, else, ignore it.
                if (user) {
                    wikiEdit.setStyle(SpanUtils.Style.UNDERLINE, isChecked);
                }
            }
        });
        strikethroughButton.setOnCheckedChangeListener(new CheckableImageButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CheckableImageButton button, boolean isChecked, boolean user) {
                // If the interaction from the user, apply it, else, ignore it.
                if (user) {
                    wikiEdit.setStyle(SpanUtils.Style.STRIKETHROUGH, isChecked);
                }
            }
        });



        //DrawerLayout mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer);
        //mDrawerToggle = new ActionBarDrawerToggle(
        //        this, mDrawerLayout, mToolbar,
        //        R.string.navigation_drawer_open, R.string.navigation_drawer_close
        //);
        //mDrawerLayout.setDrawerListener(mDrawerToggle);

        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //getSupportActionBar().setHomeButtonEnabled(true);
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent si = new Intent();
            si.setClass(this, TestActivity.class);
            startActivity(si);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
