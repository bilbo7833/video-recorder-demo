package com.stefan.anca.demo.videorecorder;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class MainActivity extends Activity implements OnClickListener
{

    Button demo1;
    Button demo2;
    Button demo3;
    Button demo4;

    @Override
    protected void onCreate( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_main );

        demo1 = ( Button ) findViewById( R.id.btn_demo1 );
        demo2 = ( Button ) findViewById( R.id.btn_demo2 );
        demo3 = ( Button ) findViewById( R.id.btn_demo3 );
//        demo4 = ( Button ) findViewById( R.id.btn_demo4 );

        demo1.setOnClickListener( this );
        demo2.setOnClickListener( this );
        demo3.setOnClickListener( this );
//        demo4.setOnClickListener( this );
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        Utils.deleteEmptyVideos( this );
    }

    @Override
    public boolean onCreateOptionsMenu( Menu menu )
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate( R.menu.main, menu );
        return true;
    }

    @Override
    public void onClick( View v )
    {
        Intent demoIntent;
        switch (v.getId())
        {
            case R.id.btn_demo1:
            {
                demoIntent = new Intent(getApplicationContext(), DemoRecorder1.class );
                break;
            }
            case R.id.btn_demo2:
            {
                demoIntent = new Intent(getApplicationContext(), DemoRecorder2.class );
                break;
            }
            case R.id.btn_demo3:
            {
                demoIntent = new Intent(getApplicationContext(), DemoRecorder3.class );
                break;
            }
//            case R.id.btn_demo4:
//            {
//                demoIntent = new Intent(getApplicationContext(), DemoRecorder4.class );
//                break;
//            }
            default:
            {
                demoIntent = new Intent(getApplicationContext(), DemoRecorder1.class );
                break;
            }
        }
        startActivity( demoIntent );

    }



}
