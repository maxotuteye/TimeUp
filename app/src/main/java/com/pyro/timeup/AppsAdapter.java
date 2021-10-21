package com.pyro.timeup;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.AppOpsManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.IBinder;
import android.os.Process;
import android.provider.Settings;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.util.List;
import java.util.Objects;

class AppsAdapter extends RecyclerView.Adapter<AppsAdapter.ViewHolder> {
    private Context context1;
    private List<String> stringList;
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;
    private String HOURS = "hours";
    private Animation fab_anim;
    private Intent serviceIntent;
    private ActivityManager manager;

    AppsAdapter(Context context, List<String> list) {
        context1 = context;
        stringList = list;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        ImageView imageView;
        TextView textView_App_Name;
        TextView textView_App_Package_Name;
        ImageButton timeFAB;

        ViewHolder(View view) {
            super(view);
            cardView = view.findViewById(R.id.card_view);
            imageView = view.findViewById(R.id.imageview);
            textView_App_Name = view.findViewById(R.id.Apk_Name);
            textView_App_Package_Name = view.findViewById(R.id.Apk_Package_Name);
            timeFAB = view.findViewById(R.id.timeFAB);
        }
    }

    @Override
    public AppsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view2 = LayoutInflater.from(context1).inflate(R.layout.cardview_layout, parent, false);
        return new ViewHolder(view2);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {
        ApkInfoExtractor apkInfoExtractor = new ApkInfoExtractor(context1);
        final String ApplicationPackageName = stringList.get(position);
        final String ApplicationLabelName = apkInfoExtractor.getAppName(ApplicationPackageName);
        Drawable drawable = apkInfoExtractor.getAppIconByPackageName(ApplicationPackageName);

        viewHolder.textView_App_Name.setText(ApplicationLabelName);
        viewHolder.textView_App_Package_Name.setText(ApplicationPackageName);
        viewHolder.imageView.setImageDrawable(drawable);

        viewHolder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                preferences = context1.getSharedPreferences(HOURS, Context.MODE_MULTI_PROCESS);
                float l = preferences.getFloat(ApplicationPackageName, -1);
                if (l < 0) {
                    Toast.makeText(context1, ApplicationLabelName + "'s max hours : Not assigned", Toast.LENGTH_SHORT).show();
                } else {
                    int minutes = (int) ((l / (1000 * 60)) % 60);
                    int seconds = (int) (l / 1000) % 60;
                    int hours = (int) ((l / (1000 * 60 * 60)) % 24);
                    Toast.makeText(context1, ApplicationLabelName + "'s max time : " + hours + "h" + ":" + minutes + "m" + ":" + seconds + "s", Toast.LENGTH_LONG).show();
                }
            }
        });
        viewHolder.timeFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                fab_anim = AnimationUtils.loadAnimation(context1, R.anim.create_todo_anim);
                view.startAnimation(fab_anim);
                final AlertDialog.Builder dialog = new AlertDialog.Builder(context1);
                dialog.setTitle(ApplicationLabelName);
                dialog.setMessage("Enter maximum daily hours for " + ApplicationLabelName);
                EditText input = new EditText(context1);
                input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.MATCH_PARENT);
                input.setLayoutParams(lp);
                dialog.setView(input);
                dialog.setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        manager = (ActivityManager) context1.getSystemService(Context.ACTIVITY_SERVICE);
                        //Store value as a shared pref
                        try {
                            Double noBusinessHere = Double.parseDouble(input.getText().toString().trim());
                            if (noBusinessHere > 24.0) {
                                throw new Exception();
                            } else {
                                preferences = context1.getSharedPreferences(HOURS, Context.MODE_PRIVATE);
                                editor = preferences.edit();
                                float convD = Float.valueOf(input.getText().toString()) * 60 * 60 * 1000;
                                editor.putFloat(ApplicationPackageName, convD);
                                editor.apply();
                                Snackbar.make(view, "Maximum Hours set!", Snackbar.LENGTH_LONG).show();
                                checkForPermission(context1);
                                try {
                                    startOrStopService("false");
                                } catch (Exception eee) {
                                    Log.i("KILL", Objects.requireNonNull(eee.getMessage()));
                                }
                            }
                        } catch (Exception e) {
                            Toast.makeText(context1, "Invalid input!", Toast.LENGTH_LONG).show();
                            e.printStackTrace();
                        }
                    }
                });
                dialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                });
                dialog.show();
            }
        });
    }

    private void checkForPermission(Context context) {
        AppOpsManager appOpsManager = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
        int mode = appOpsManager.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, Process.myUid(), context.getPackageName());
        if (mode != AppOpsManager.MODE_ALLOWED) {
            context.startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS));
        }
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int getItemCount() {
        return stringList.size();
    }

    private void startOrStopService(String stop) {
        Log.i("KILL", "startOrStopService called");
        serviceIntent = new Intent(context1.getApplicationContext(), MonitorService.class);
        serviceIntent.setAction("com.pyro.timeup.MonitorService");
        serviceIntent.putExtra("stop", stop);
        ServiceConnection monitorConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                startOrStopService("true");
            }
        };
        if (isMyServiceRunning(MonitorService.class)) {
            Log.i("KILL", "Stop Service");
            context1.stopService(serviceIntent);
        }
        Log.i("KILL", "Start Service");
        context1.startService(serviceIntent);
    }
}