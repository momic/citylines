package org.citylines.tabsswipe.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import org.citylines.tabsswipe.CityLinesFragment;
import org.citylines.tabsswipe.ConfigureFragment;
import org.citylines.tabsswipe.IntercityLinesFragment;
import org.citylines.tabsswipe.NearbyLinesFragment;
 
public class TabsPagerAdapter extends FragmentPagerAdapter {
    
    public TabsPagerAdapter(FragmentManager fm) {
        super(fm);
    }
 
    @Override
    public Fragment getItem(int index) {
 
        switch (index) {
        case 0:
            // Nerby lines fragment activity
            return new NearbyLinesFragment();
        case 1:
            // City lines fragment activity
            return new CityLinesFragment();
        case 2:
            // Intercity lines fragment activity
            return new IntercityLinesFragment();
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