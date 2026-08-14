package com.example.calculator;

import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

public class  MainActivity extends AppCompatActivity {

    private static final String TAG_NORMAL = "normal_cal";
    private static final String TAG_SCIENTIFIC = "scientific_cal";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 初始化数据库
        LogDatabaseHelper.init(this);

        if (savedInstanceState == null) {
            Fragment fragment = getInitialFragment();
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.fragment_container, fragment, getFragmentTag())
                    .commit();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        FragmentManager fm = getSupportFragmentManager();
        Fragment currentFragment = fm.findFragmentById(R.id.fragment_container);

        Fragment newFragment;
        String newTag;

        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            newFragment = new ScientificCalFragment();
            newTag = TAG_SCIENTIFIC;
        } else {
            newFragment = new NormalCalFragment();
            newTag = TAG_NORMAL;
         }

        if (currentFragment == null || !getFragmentTagForOrientation(newConfig.orientation).equals(
                currentFragment.getTag())) {
            fm.beginTransaction()
                    .replace(R.id.fragment_container, newFragment, newTag)
                    .commit();
        }
    }

    private Fragment getInitialFragment() {
        int orientation = getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            return new ScientificCalFragment();
        } else {
            return new NormalCalFragment();
        }
    }

    private String getFragmentTag() {
        int orientation = getResources().getConfiguration().orientation;
        return getFragmentTagForOrientation(orientation);
    }

    private String getFragmentTagForOrientation(int orientation) {
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            return TAG_SCIENTIFIC;
        } else {
            return TAG_NORMAL;
        }
    }
}