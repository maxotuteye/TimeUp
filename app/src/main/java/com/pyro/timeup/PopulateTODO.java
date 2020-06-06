package com.pyro.timeup;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class PopulateTODO extends RecyclerView.Adapter<AppsAdapter.ViewHolder> {
    Context _context;
    SharedPreferences preferences;

    public PopulateTODO(Context context, String sharedPrefNAME) {
        _context = context;
        //preferences = _context.getSharedPreferences(sharedPrefNAME, Context.MODE_PRIVATE).getString();
    }


    @NonNull
    @Override
    public AppsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull AppsAdapter.ViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 0;
    }
}
