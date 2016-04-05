package datn.bkdn.com.saywithvideo.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import datn.bkdn.com.saywithvideo.R;
import datn.bkdn.com.saywithvideo.adapter.MainPagerAdapter;
import datn.bkdn.com.saywithvideo.utils.PagerSlidingTabStrip;

public class MainActivity extends AppCompatActivity {

    private PagerSlidingTabStrip mTabStrip;
    private ViewPager mPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mPager = (ViewPager) findViewById(R.id.pager);
        mTabStrip = (PagerSlidingTabStrip) findViewById(R.id.tab);
        MainPagerAdapter adapter = new MainPagerAdapter(getSupportFragmentManager());
        mPager.setAdapter(adapter);
        mTabStrip.setViewPager(mPager);
        final FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, SettingActivity.class));
            }
        });

        mTabStrip.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                switch (position) {
                    case 0:
                        fab.setVisibility(View.GONE);
                        break;
                    case 1:
                        fab.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    public void showSounds() {
        mPager.setCurrentItem(0);
    }
}
