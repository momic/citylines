package org.citylines;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Spinner;
import android.widget.Toast;
import org.citylines.db.DBFactory;
import org.citylines.db.location.CurrentPositionLocationParams;
import org.citylines.db.location.LocationLevel;
import static org.citylines.db.location.LocationLevel.LOCATION_LEVEL_CITY;
import static org.citylines.db.location.LocationLevel.LOCATION_LEVEL_STATE;
import org.citylines.db.location.LocationParams;
import org.citylines.db.location.factory.LocationParamsFactory;
import static org.citylines.db.location.factory.LocationParamsType.LOCATION_PARAMS_CURRENT_POSITION;
import static org.citylines.db.location.factory.LocationParamsType.LOCATION_PARAMS_PARENT;

/**
 *
 * @author zlaja
 */
public class LocationSelectionActivity extends Activity implements OnItemSelectedListener {
    
    // database handler
    private final DBFactory db = DBFactory.getInstance(this);
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Intent intent = getIntent();
        // long value = intent.getLongExtra("key", -1);
        
        // set layout
        setContentView(R.layout.location_selection);
       
        // fill spinners
        this.loadSpinnerData(LOCATION_LEVEL_STATE);
    }
    
    private void loadSpinnerData(LocationLevel locationLevel) {
        this.loadSpinnerData(locationLevel, null);
    }
    
    /**
     * Load spinner from DB
     * 
     */
    private void loadSpinnerData(LocationLevel locationLevel, Long parentKey) {
        
        // create spinner id
        final String spinnerId = locationLevel.getId() + "_spinner";
        
        // Create location params
        final LocationParams lp = LocationParamsFactory.build(LOCATION_PARAMS_PARENT, 
                locationLevel, parentKey);
        
        // Initialize adapter
        SimpleCursorAdapter adapter = new SimpleCursorAdapter(this,
                android.R.layout.simple_spinner_item,
                db.getLocationItems(lp),
                new String[] {"name"},
                new int[] {android.R.id.text1}, 0);        
        
        // Drop down layout style - list view with radio button
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);        
 
        // Find proper spinner
        int resId = getResources().getIdentifier(spinnerId, "id", getPackageName());
        Spinner spinner = (Spinner) findViewById(resId);

        // Set location level as tag
        spinner.setTag(locationLevel);
        
        // Spinner click listener
        spinner.setOnItemSelectedListener(this);
        
        // Attaching data adapter to spinner
        spinner.setAdapter(adapter);
        
        // Set initial selection
        final CurrentPositionLocationParams cplp = (CurrentPositionLocationParams) 
                LocationParamsFactory.build(LOCATION_PARAMS_CURRENT_POSITION, 
                        locationLevel, parentKey);
        final Cursor currentState = db.getCurrentLocation(cplp);
        int currentItemPosition = currentState.getInt(currentState.getColumnCount()-1);        
        spinner.setSelection(currentItemPosition);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        // On selecting a spinner item
        LocationLevel spinnerTag = (LocationLevel) parent.getTag();
        
        if (spinnerTag != LOCATION_LEVEL_CITY) {
            // Load next spinner in hierarchy
            LocationSelectionActivity.this.loadSpinnerData(spinnerTag.getNext(), id);
        } else {
            // Update current city
            if (LocationSelectionActivity.this.db.setCurrentCity(id) > 0) {
                // On rows updated show message
                final Cursor c = (Cursor) parent.getItemAtPosition(position);
                Toast.makeText(getApplicationContext(), 
                        "Current city changed to: " + c.getString(1),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    }
}
