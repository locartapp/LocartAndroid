package com.locart.Activity;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import com.locart.Fragments.CardFragment;
import com.locart.Fragments.MatchesFragment;
import com.locart.Fragments.UserFragment;

import org.jetbrains.annotations.NotNull;

public class MainAdapter extends FragmentStatePagerAdapter {
    int mNumOfTabs;

    public MainAdapter(FragmentManager fm, int NumOfTabs) {
        super(fm);
        this.mNumOfTabs = NumOfTabs;
    }

    @NotNull
    @Override
    public Fragment getItem(int position) {

        switch (position) {
            case 0:

                return new UserFragment();
            case 2:

                return new MatchesFragment();
            case 1:

            default:

              return  new CardFragment();

        }
    }

    @Override
    public int getCount() {
        return mNumOfTabs;
    }


}
