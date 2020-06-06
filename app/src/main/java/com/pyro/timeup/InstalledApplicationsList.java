package com.pyro.timeup;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InstalledApplicationsList extends AppCompatActivity {
    public InstalledApplicationsList() {
        runnable.run();
    }

    Map<Drawable, String> AppInfo = new HashMap<>();
    Intent intent = new Intent(Intent.ACTION_MAIN);
    PackageManager pManager = getPackageManager();
    List<ApplicationInfo> apps = pManager.getInstalledApplications(0);
    List<String> appLabels = new ArrayList<>();
    List<Drawable> appIcons = new ArrayList<>();
    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            createAppsList();
        }
    };

    void createAppsList() {
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        for (ApplicationInfo app : apps
        ) {
            appLabels.add((String) pManager.getApplicationLabel(app));
            appIcons.add(pManager.getApplicationIcon(app));
        }
        mapAppInfo(appIcons, appLabels);
    }

    void mapAppInfo(List<Drawable> iconList, List<String> labelList) {
        if (iconList.size() == labelList.size()) {
            for (int c = 0; c < iconList.size(); c++) {
                AppInfo.put(iconList.get(c), labelList.get(c));
            }
        }
    }
}
