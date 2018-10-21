package com.embeded.soundrecorder.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.astuetz.PagerSlidingTabStrip;
import com.embeded.soundrecorder.R;
import com.embeded.soundrecorder.RecordingService;
import com.embeded.soundrecorder.adapters.FileViewerAdapter;
import com.embeded.soundrecorder.fragments.FileViewerFragment;
import com.embeded.soundrecorder.fragments.RecordFragment;

import org.acra.ACRA;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

@ReportsCrashes(
        mailTo = "deepak.kumar@vvdntech.com , divyanshi.parashar@vvdntech.in",
        mode = ReportingInteractionMode.TOAST,
        resToastText = R.string.app_name)
public class MainActivity extends ActionBarActivity {
    private static final String LOG_TAG = MainActivity.class.getSimpleName();
    private PagerSlidingTabStrip tabs;
    private ViewPager pager;
    private MyAdapter myAdapter;
    public MainActivity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ACRA.init(getApplication());
        setContentView(R.layout.activity_main);
        pager = (ViewPager) findViewById(R.id.pager);
        myAdapter = new MyAdapter(getSupportFragmentManager());
        pager.setAdapter(myAdapter);
        tabs = (PagerSlidingTabStrip) findViewById(R.id.tabs);
        tabs.setViewPager(pager);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setPopupTheme(R.style.ThemeOverlay_AppCompat_Light);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            //  toolbar.setNavigationIcon(R.drawable.my_icon);
        }

        // pager listener
        pager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                int lPosition = 0;

                if (position == 0){
                    lPosition = 1;
                }
                else{
                    lPosition = 0;
                }
                final Fragment fragment = myAdapter.getFragment(lPosition);
                pager.getAdapter().notifyDataSetChanged();
                if (fragment != null) {
                    // Get a handler that can be used to post to the main thread
                    Handler mainHandler = new Handler(getMainLooper());
                    Runnable myRunnable = new Runnable() {
                        @Override
                        public void run() {
                            fragment.onResume();
                        }
                    };
                    mainHandler.postDelayed(myRunnable, 100);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    @Override
    public void onBackPressed() {
        Fragment fr = getSupportFragmentManager().findFragmentByTag("android:switcher:" + R.id.pager + ":" + pager.getCurrentItem());
        if (fr instanceof FileViewerFragment){
            ((FileViewerFragment) fr).clearList();
        }else{
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        Intent serviceIntent = new Intent(this, RecordingService.class);
        stopService(serviceIntent);
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.action_settings:
                Intent i = new Intent(this, SettingsActivity.class);
                startActivity(i);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public class MyAdapter extends FragmentPagerAdapter {
        private Map<Integer, String> mFragmentTags;
        private FragmentManager mFragmentManager;

        private String[] titles = {getString(R.string.tab_title_record),
                getString(R.string.tab_title_saved_recordings)};

        public MyAdapter(FragmentManager fm) {
            super(fm);
            mFragmentManager = fm;
            mFragmentTags = new HashMap<Integer, String>();
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0: {
                    return RecordFragment.newInstance(position);
                }
                case 1: {
                    return FileViewerFragment.newInstance(position);
                }
            }
            return null;
        }

        @Override
        public int getCount() {
            return titles.length;
        }

        @Override
        public CharSequence getPageTitle(int position) {

            return titles[position];
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            Object object = super.instantiateItem(container, position);
            if (object instanceof Fragment) {
                Fragment fragment = (Fragment) object;
                String tag = fragment.getTag();
                if (mFragmentTags != null) {
                    mFragmentTags.put(position, tag);
                }
            }
            return object;
        }

        // get fragment to recreate it
        public Fragment getFragment(int position) {
            Fragment fragment = null;
            if (mFragmentTags != null) {
                String tag = mFragmentTags.get(position);
                if (tag != null) {
                    fragment = mFragmentManager.findFragmentByTag(tag);
                }
            }
            return fragment;
        }
    }
}