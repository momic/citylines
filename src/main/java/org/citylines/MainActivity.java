package org.citylines;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import org.citylines.db.dao.LocationDAO;
import org.citylines.db.dao.factory.DAOFactory;
import org.citylines.db.dao.factory.DAOType;
import org.citylines.model.location.CurrentLocationParams;
import org.citylines.model.location.factory.LocationParamsFactory;
import static org.citylines.model.location.factory.LocationParamsType.LOCATION_PARAMS_CURRENT;
import org.citylines.view.tabsswipe.layout.SlidingTabLayout;
import org.citylines.view.tabsswipe.adapter.TabsPagerAdapter;

public class MainActivity extends FragmentActivity {
     
    // DAO handler
    private LocationDAO locationDAO;
 
    private ViewPager viewPager;
    private TabsPagerAdapter mAdapter;
    private SlidingTabLayout mSlidingTabLayout;

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

        // Tab titles
        String[] tabs = getResources().getStringArray(R.array.tabs_array);
        
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
        mAdapter = new TabsPagerAdapter(getSupportFragmentManager(), tabs);
        viewPager.setAdapter(mAdapter);

        mSlidingTabLayout = (SlidingTabLayout) findViewById(R.id.sliding_tabs);
        mSlidingTabLayout.setViewPager(viewPager);        
    }
}