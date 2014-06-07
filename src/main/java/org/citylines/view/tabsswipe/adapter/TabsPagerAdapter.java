package org.citylines.view.tabsswipe.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import org.citylines.view.tabsswipe.MapFragment;
import org.citylines.view.tabsswipe.ConfigureFragment;
import org.citylines.view.tabsswipe.IntercityLinesFragment;
import org.citylines.view.tabsswipe.CityLinesFragment;
 
public class TabsPagerAdapter extends FragmentPagerAdapter {
    
    public TabsPagerAdapter(FragmentManager fm) {
        super(fm);
    }
 
    @Override
    public Fragment getItem(int index) {
 
        switch (index) {
        case 0:
            // City lines fragment activity
            return new CityLinesFragment();
        case 1:
            // Intercity lines fragment activity
            return new IntercityLinesFragment();
        case 2:
            // Map fragment activity
            return new MapFragment();
        case 3:
            // Configure fragment activity
            return new ConfigureFragment();
        }
 
        return null;
    }
 
    @Override
    public int getCount() {
        // get item count - equal to number of tabs
        return 4;
    }
 
}