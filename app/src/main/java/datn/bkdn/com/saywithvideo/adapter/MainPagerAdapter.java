package datn.bkdn.com.saywithvideo.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.view.View;

import datn.bkdn.com.saywithvideo.fragment.SoundFragment;
import datn.bkdn.com.saywithvideo.fragment.UserProfileFragment;

/**
 * Created by Admin on 2/18/2016.
 */
public class MainPagerAdapter extends FragmentPagerAdapter {
    public static final int TAB_SOUND =0;
    public static final int TAB_DUB =1;
    public static final int NUM_TAB =2;
    public static final String[] TAB_NAME ={"Sounds","My Dubs"};
    public MainPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public int getCount() {
        return NUM_TAB;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position){
            case 0: return SoundFragment.newInstance();
            case 1:return UserProfileFragment.newInstance();
        }
        return null;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return TAB_NAME[position];
    }
}
