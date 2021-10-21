package com.pyro.timeup;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import com.pyro.timeup.dummy.DummyContent;
import com.pyro.timeup.ui.main.SectionsPagerAdapter;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements UsageFragment.OnListFragmentInteractionListener, AppListFragment.OnListFragmentInteractionListener, FragmentToDo.OnFragmentInteractionListener {

    final ApkInfoExtractor extractor = new ApkInfoExtractor(this);
    List<String> apkInfoList;
    ArrayList<String> appLabels;
    List<Drawable> appIcons;
    SharedPreferences prefs;
    SharedPreferences.Editor editor;
    Drawable moon_light;
    Drawable moon_dark;
    boolean lightTheme = true;
    boolean darkTheme = false;
    String theme = "theme";
    FloatingActionButton themeSwitch;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        moon_dark = getDrawable(R.drawable.moon_dark);
        moon_light = getDrawable(R.drawable.moon_light);
//        checkTheme();
        //this.setTheme(R.style.LightTheme);
        setContentView(R.layout.activity_main);
        themeSwitch = findViewById(R.id.themeSwitch);
        SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(this, getSupportFragmentManager());
        ViewPager viewPager = findViewById(R.id.view_pager);
        viewPager.setAdapter(sectionsPagerAdapter);
        TabLayout tabs = findViewById(R.id.tabs);
        tabs.setupWithViewPager(viewPager);
        prefs = getApplicationContext().getSharedPreferences("ItemArray", MODE_PRIVATE);
        apkInfoList = extractor.getAllInstalledApkInfo();
        appLabels = new ArrayList<>();
        appIcons = new ArrayList<>();
        getAppData();
       // startOrStopService("true");
        themeSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isLightTheme()) {
                    setTheme(lightTheme);
                } else {
                    setTheme(darkTheme);
                }
            }
        });
    }

    @Override
    public void onListFragmentInteraction(DummyContent.DummyItem item) {
        Toast.makeText(getApplicationContext(), appLabels.toString(), Toast.LENGTH_SHORT).show();
    }


    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    private void startOrStopService(String stop) {
        Intent serviceIntent;
        Context context1 = getApplicationContext();
        serviceIntent = new Intent(context1.getApplicationContext(), MonitorService.class);
        serviceIntent.putExtra("stop", stop);
        serviceIntent.setAction("com.pyro.timeup.MonitorService");
        ServiceConnection monitorConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                startOrStopService("true");
            }
        };
        if (!isMyServiceRunning(MonitorService.class)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context1.startService(serviceIntent);
                context1.bindService(serviceIntent, monitorConnection, Context.BIND_EXTERNAL_SERVICE);
            } else {
                context1.startService(serviceIntent);
            }
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                context1.unbindService(monitorConnection);
                context1.bindService(serviceIntent, monitorConnection, Context.BIND_EXTERNAL_SERVICE);
            } else {
                context1.stopService(serviceIntent);
                context1.startService(serviceIntent);
            }
        }
    }

    private void getAppData() {
        for (String app : apkInfoList) {
            appLabels.add(extractor.getAppName(app));
            appIcons.add(extractor.getAppIconByPackageName(app));
        }
    }

    private void setTheme(boolean theme) {
        prefs = getSharedPreferences(this.theme, MODE_PRIVATE);
        editor = prefs.edit();
        editor.putBoolean("light", theme);
        editor.apply();
        checkTheme();
    }

    private void checkTheme() {
        prefs = getSharedPreferences(this.theme, MODE_PRIVATE);
        if (isLightTheme()) {
            setTheme(R.style.LightTheme);
            themeSwitch.setImageDrawable(moon_dark);
        } else {
            setTheme(R.style.AppTheme);
            themeSwitch.setImageDrawable(moon_light);
        }
    }

    private boolean isLightTheme() {
        prefs = getSharedPreferences(this.theme, MODE_PRIVATE);
        return prefs.getBoolean("light", false);
    }
}