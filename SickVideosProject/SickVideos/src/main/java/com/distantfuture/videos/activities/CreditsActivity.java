package com.distantfuture.videos.activities;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.distantfuture.videos.R;
import com.distantfuture.videos.misc.Utils;

import java.util.List;

public class CreditsActivity extends Activity {
  ViewGroup mContainer;

  public static void show(Activity activity) {
    // add animation, see finish below for the back transition
    ActivityOptions opts = ActivityOptions.makeCustomAnimation(activity, R.anim.scale_in, R.anim.scale_out);

    Intent intent = new Intent();
    intent.setClass(activity, CreditsActivity.class);
    activity.startActivity(intent, opts.toBundle());
  }

  @Override
  public void finish() {
    super.finish();

    // animate out
    overridePendingTransition(R.anim.scale_out_rev, R.anim.scale_in_rev);
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.activity_credits);

    getActionBar().setDisplayHomeAsUpEnabled(true);

    mContainer = (ViewGroup) findViewById(R.id.credits_container);

    CreditsXMLParser.parseXML(this, new CreditsXMLParser.CreditsXMLParserListener() {
      @Override
      public void parseXMLDone(List<CreditsXMLParser.CreditsPage> newPages) {
        for (CreditsXMLParser.CreditsPage page : newPages) {

          LinearLayout linearLayout = new LinearLayout(CreditsActivity.this);
          linearLayout.setOrientation(LinearLayout.VERTICAL);
          LinearLayout.LayoutParams duhParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
          linearLayout.setLayoutParams(duhParams);

          for (CreditsXMLParser.CreditsPageField field : page.fields)
            linearLayout.addView(createFieldView(field));

          mContainer.addView(linearLayout);
        }
      }
    });
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      // Respond to the action bar's Up/Home button
      case android.R.id.home:
        finish();
        return true;
    }
    return super.onOptionsItemSelected(item);
  }

  private View createFieldView(CreditsXMLParser.CreditsPageField field) {
    final int headerSize = 20;
    final int titleSize = 16;

    TextView textView = new TextView(this);
    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    textView.setLayoutParams(params);
    textView.setTextSize(titleSize);
    textView.setText(field.text);

    if (field.isHeader()) {
      int color = 0xff0000ff; // this.getResources().getColor(R.color.intro_header_color);

      textView.setTextSize(headerSize);
      textView.setTextColor(color);
      textView.setTypeface(Typeface.DEFAULT_BOLD);
      textView.setGravity(Gravity.CENTER);
    }

    LinearLayout linearLayout = new LinearLayout(this);
    LinearLayout.LayoutParams duhParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    int topPaddingPx = (int) Utils.dpToPx(field.topMargin(), this);
    duhParams.setMargins(0, topPaddingPx, 0, 0);
    linearLayout.setLayoutParams(duhParams);

    linearLayout.addView(textView);

    return linearLayout;
  }

}
