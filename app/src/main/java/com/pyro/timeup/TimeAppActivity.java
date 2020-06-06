package com.pyro.timeup;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;

public class TimeAppActivity extends AppCompatActivity {
    private String appName;
    private String appPkgName;
    Intent getAppData;
    TextView TV_appName;
    TextView TV_appPkg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_time_app);

        getAppData = getIntent();
        appName = getAppData.getStringExtra("name");
        appPkgName = getAppData.getStringExtra("pkg");

        TV_appName = findViewById(R.id.name);
        TV_appPkg =findViewById(R.id.pkg);
        TV_appName.setText(appName);
        TV_appPkg.setText(appPkgName);
    }
}
