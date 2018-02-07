package com.cashzhang.ashley;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.res.Resources;
import android.os.Build;
import android.support.v4.widget.DrawerLayout;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewConfiguration;

/**
 * Created by zhangchi on 2018/2/6.
 */

public class Constants {

    static MainActivity s_activity;
    static Resources s_resources;
    static MainFragment s_fragmentFeeds;
    static FragmentManager s_fragmentManager;
    static DrawerLayout s_drawerLayout;
    static FragmentNavigationDrawer s_fragmentDrawer;


    static void saveInitialConstants(MainActivity activity) {
        s_activity = activity;
        s_resources = activity.getResources();
        s_fragmentManager = activity.getFragmentManager();
        s_fragmentFeeds = (MainFragment) s_fragmentManager.findFragmentById(R.id.main_fragment);
        s_drawerLayout = (DrawerLayout) activity.findViewById(R.id.drawer_layout);
        s_fragmentDrawer = (FragmentNavigationDrawer) s_fragmentManager.findFragmentById(R.id.fragment_navigation_drawer);
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    static
    void setTopOffset(Activity activity)
    {
        setTopOffset(activity, s_activity.findViewById(android.R.id.content));
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    static
    void setTopOffset(Activity activity, View view)
    {
        if(Build.VERSION_CODES.KITKAT <= Build.VERSION.SDK_INT)
        {
            if(!ViewConfiguration.get(activity).hasPermanentMenuKey())
            {
                TypedValue value = new TypedValue();

                Resources.Theme theme = activity.getTheme();
                theme.resolveAttribute(android.R.attr.actionBarSize, value, true);
                int actionBar = s_resources.getDimensionPixelSize(value.resourceId);
                int resourceId = s_resources.getIdentifier("status_bar_height", "dimen", "android");
                int statusBar = s_resources.getDimensionPixelSize(resourceId);

                view.setPadding(0, actionBar + statusBar, 0, 0);
            }
        }
    }

    static void hideFragments(Fragment... fragments) {
        FragmentTransaction transaction = s_fragmentManager.beginTransaction();
        for (Fragment fragment : fragments) {
            transaction.hide(fragment);
        }
        transaction.commit();
    }

    static void showFragments(Fragment... fragments) {
        FragmentTransaction transaction = s_fragmentManager.beginTransaction();
        for (Fragment fragment : fragments) {
            transaction.show(fragment);
        }
        transaction.commit();
    }

}
