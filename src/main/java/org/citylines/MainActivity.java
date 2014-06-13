package org.citylines;

import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import java.util.Locale;
import org.citylines.db.dao.LocationDAO;
import org.citylines.db.dao.factory.DAOFactory;
import org.citylines.db.dao.factory.DAOType;
import org.citylines.model.location.CurrentLocationParams;
import org.citylines.model.location.factory.LocationParamsFactory;
import static org.citylines.model.location.factory.LocationParamsType.LOCATION_PARAMS_CURRENT;
import org.citylines.view.tabsswipe.adapter.TabsPagerAdapter;

public class MainActivity extends FragmentActivity implements
        ActionBar.TabListener {
     
    // DAO handler
    private LocationDAO locationDAO;
 
    private ViewPager viewPager;
    private TabsPagerAdapter mAdapter;
    private ActionBar actionBar;
    // Tab titles
    private String[] tabs;
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_activity_actions, menu);
        return super.onCreateOptionsMenu(menu);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.action_location:
                Intent myIntent = new Intent(this, LocationSelectionActivity.class);
                startActivity(myIntent);                
                return true;
            case R.id.action_update:
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }    
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        tabs = getResources().getStringArray(R.array.tabs_array);
        
        setContentView(R.layout.main);
              
        // init dao handlers
        this.locationDAO = (LocationDAO) DAOFactory.build(DAOType.LOCATION_DAO, this);
        
        // set title to current location
        final CurrentLocationParams clp = (CurrentLocationParams) 
                LocationParamsFactory.build(LOCATION_PARAMS_CURRENT);
        Cursor c = locationDAO.getCurrentLocation(clp);
        setTitle(getResources().getString(R.string.app_name) 
                + ": " + c.getString(c.getColumnIndex("name")));
        
        // Initilization
        viewPager = (ViewPager) findViewById(R.id.pager);
        actionBar = getActionBar();
        mAdapter = new TabsPagerAdapter(getSupportFragmentManager(), tabs.length);
 
        viewPager.setAdapter(mAdapter);
        actionBar.setHomeButtonEnabled(false);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);       
 
        // Adding Tabs
        for (String tab_name : tabs) {
            actionBar.addTab(actionBar.newTab().setText(tab_name.toUpperCase(Locale.US))
                    .setTabListener(this));
        }    
        
        /**
         * on swiping the viewpager make respective tab selected
         */
        viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageSelected(int position) {
                // on changing the page
                // make respected tab selected
                actionBar.setSelectedNavigationItem(position);
            }

            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {
            }

            @Override
            public void onPageScrollStateChanged(int arg0) {
            }
        });        
    }
    
    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {
        // on tab selected
        // show respected fragment view
        viewPager.setCurrentItem(tab.getPosition());  
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft) {
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft) {
    }
}