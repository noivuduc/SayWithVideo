package datn.bkdn.com.saywithvideo.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import datn.bkdn.com.saywithvideo.fragment.GroupFragment;
import datn.bkdn.com.saywithvideo.fragment.UserProfileFragment;

public class MainPagerAdapter extends FragmentPagerAdapter {
    public static final int NUM_TAB = 2;
    public static final String[] TAB_NAME = {"Âm thanh", "Video của tôi"};

    public MainPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public int getCount() {
        return NUM_TAB;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return GroupFragment.newInstance();
            case 1:
                return UserProfileFragment.newInstance();
        }
        return null;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return TAB_NAME[position];
    }
}
