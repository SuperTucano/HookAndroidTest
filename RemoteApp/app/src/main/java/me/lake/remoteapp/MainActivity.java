package me.lake.remoteapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends Activity {
    TextView tv_info;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tv_info = (TextView) findViewById(R.id.tv_info);
        findViewById(R.id.tv_main).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ("stop".equals(tv_info.getText())) {
                    tv_info.setText("start");
                    MainActivity.this.startService(new Intent(MainActivity.this, MyService01.class));
                } else {
                    tv_info.setText("stop");
                    MainActivity.this.stopService(new Intent(MainActivity.this, MyService01.class));
                }
            }
        });
    }
}
