package me.lake.servicehosttest;

import android.content.ComponentName;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ToggleButton;

import org.w3c.dom.Text;

public class MainActivity extends AppCompatActivity {
    ToggleButton btn_1, btn_2;
    TextView tv_main;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tv_main = (TextView) findViewById(R.id.tv_main);
        btn_1 = (ToggleButton) findViewById(R.id.btn_1);
        btn_2 = (ToggleButton) findViewById(R.id.btn_2);
        btn_1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (btn_1.isChecked()) {
                    startService(new Intent().setComponent(new ComponentName("me.lake.remoteapp",
                            "me.lake.remoteapp.MyService01")));
                } else {
                    stopService(new Intent().setComponent(new ComponentName("me.lake.remoteapp",
                            "me.lake.remoteapp.MyService01")));
                }
            }
        });
        btn_2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (btn_2.isChecked()) {
                    startService(new Intent().setComponent(new ComponentName("me.lake.remoteapp",
                            "me.lake.remoteapp.MyService02")));
                } else {
                    stopService(new Intent().setComponent(new ComponentName("me.lake.remoteapp",
                            "me.lake.remoteapp.MyService02")));
                }
            }
        });
    }
}
