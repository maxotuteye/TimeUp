package com.pyro.timeup;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import java.sql.Time;

public class ToDoItem {
    // Data decs for the necessary information
    SharedPreferences prefs;
    SharedPreferences.Editor editor;
    String title;
    String subtitle;
    Time startTime;
    Time endTime;
    int category;
    String ItemARRAY = "ITEMS";

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }

    public Time getStartTime() {
        return startTime;
    }

    public void setStartTime(Time startTime) {
        this.startTime = startTime;
    }

    public Time getEndTime() {
        return endTime;
    }

    public void setEndTime(Time endTime) {
        this.endTime = endTime;
    }

    public int getCategory() {
        return category;
    }

    public void setCategory(int category) {
        this.category = category;
    }

    public void makeItem(Context context) {
        prefs = context.getSharedPreferences(ItemARRAY, Context.MODE_PRIVATE);
        editor = prefs.edit();
        editor.putString(title, subtitle);
        editor.apply();
        Toast.makeText(context, "made item successfully", Toast.LENGTH_SHORT).show();
        Log.println(Log.WARN, "TTTTTTTTTTTTTTTTTTTTTTT", title + subtitle);
    }
}
