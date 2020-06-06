package com.pyro.timeup;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

public class AutoStart extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent intent1 = new Intent(context,MonitorService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            context.startForegroundService(intent1);
        } else {
            context.startService(intent1);
        }
    }
}
