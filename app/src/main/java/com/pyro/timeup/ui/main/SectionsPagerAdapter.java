package com.pyro.timeup.ui.main;

import android.content.Context;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.google.android.material.snackbar.Snackbar;
import com.pyro.timeup.AppListFragment;
import com.pyro.timeup.FragmentToDo;
import com.pyro.timeup.MainActivity;
import com.pyro.timeup.R;
import com.pyro.timeup.UsageFragment;
import com.pyro.timeup.dummy.DummyContent;

/**
 * A [FragmentPagerAdapter] that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */
public class SectionsPagerAdapter extends FragmentPagerAdapter implements AppListFragment.OnListFragmentInteractionListener {

    @StringRes
    private static final int[] TAB_TITLES = new int[]{R.string.tab_text_2, R.string.tab_text_3};
    private final Context mContext;

    public SectionsPagerAdapter(Context context, FragmentManager fm) {
        super(fm);
        mContext = context;
    }

    @Override
    public Fragment getItem(int position) {
        // getItem is called to instantiate the fragment for the given page.
        // Return a PlaceholderFragment (defined as a static inner class below).
        switch (position) {
            case 0:
                return AppListFragment.newInstance(1);
            case 1:
                return UsageFragment.newInstance(1);
        }
        return PlaceholderFragment.newInstance(position + 1);
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return mContext.getResources().getString(TAB_TITLES[position]);
    }

    @Override
    public int getCount() {
        // Show 3 total pages.
        return 2;
    }

    @Override
    public void onListFragmentInteraction(DummyContent.DummyItem item) {
        Toast.makeText(mContext, "I work", Toast.LENGTH_SHORT).show();
    }
}