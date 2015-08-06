
package cn.cc.sdk.demo;

import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;

import cn.cc.android.sdk.util.Logger;
import cn.cc.sdk.demo.fragment.DownloadFragment;
import cn.cc.sdk.demo.fragment.UploadFragment;
import cn.cc.sdk.demo.fragment.VideoFragment;
import cn.cc.sdk.demo.util.MyLogger;


public class MainActivity extends FragmentActivity implements ActionBar.TabListener {
    private ViewPager mViewPager;
    private ViewPageAdapter mViewPageAdapter;
    private LocalApplication mApplication;
    private final String LOG_TAG = MainActivity.class.getSimpleName();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try {
            init();
            setActionBar();
            setActionViewPager();
            setTabs();
        } catch (Exception e) {
            Logger.i(LOG_TAG, "onCreate Exception: ", e);
        }
    }

    private void init() {
        mApplication = (LocalApplication) getApplication();
        mApplication.onActivityCreate();
    }

    private void setActionBar() {
        ActionBar actionBar = this.getActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(false);
            actionBar.setDisplayShowHomeEnabled(false);
            actionBar.setDisplayShowTitleEnabled(false);
            actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        }
    }

    private void setActionViewPager() {
        mViewPager = (ViewPager) findViewById(R.id.viewpager);
        mViewPageAdapter = new ViewPageAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(mViewPageAdapter);
        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels);
            }

            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);

                if (getActionBar() != null) {
                    int index = getActionBar().getSelectedNavigationIndex();
                    if (position != index) {
                        getActionBar().setSelectedNavigationItem(position);
                    }

                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                super.onPageScrollStateChanged(state);
            }
        });
    }

    private void setTabs() {
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            for (int index = 0; index < mViewPageAdapter.getCount(); index++) {
                actionBar.addTab(actionBar.newTab().setText(mViewPageAdapter.getPageTitle(index)).
                        setTabListener(this));
            }
        }
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {
        mViewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft) {
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft) {
    }

    private class ViewPageAdapter extends FragmentStatePagerAdapter {
        public ViewPageAdapter(FragmentManager fragmentManager) {
            super(fragmentManager);
        }

        @Override
        public Fragment getItem(int position) {
            Fragment fragment = null;
            switch (position) {
                case 0:
                    fragment = new VideoFragment();
                    break;
                case 1:
                    fragment = new UploadFragment();
                    break;
                case 2:
                    fragment = new DownloadFragment();
                    break;
            }
            return fragment;
        }

        @Override
        public int getCount() {
            return 3;
        }


        @Override
        public CharSequence getPageTitle(int position) {
            String tabLabel = "";
            switch (position) {
                case 0:
                    tabLabel = getString(R.string.label_video);
                    break;
                case 1:
                    tabLabel = getString(R.string.label_upload);
                    break;
                case 2:
                    tabLabel = getString(R.string.label_download);
                    break;
            }
            return tabLabel;
        }


    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        try {
            MyLogger.i(LOG_TAG, "onDestroy");
            mApplication.clear();
        } catch (Exception e) {
            MyLogger.w(LOG_TAG, "onDestroy Exception", e);
        }
    }


}

