package com.sanca.videorecorder;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends Activity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    ButterKnife.bind(this);
  }

  @Override
  protected void onResume() {
    super.onResume();
    Utils.deleteEmptyVideos(this);
  }

  @OnClick(R.id.btn_demo1)
  public void onDemo1Click(View v) {
    Intent demoIntent = new Intent(getApplicationContext(), DemoRecorder1.class);
    startActivity(demoIntent);
  }

  @OnClick(R.id.btn_demo2)
  public void onDemo2Click(View v) {
    Intent demoIntent = new Intent(getApplicationContext(), DemoRecorder2.class);
    startActivity(demoIntent);
  }

  @OnClick(R.id.btn_demo3)
  public void onDemo3Click(View v) {
    Intent demoIntent = new Intent(getApplicationContext(), DemoRecorder3.class);
    startActivity(demoIntent);
  }

}
