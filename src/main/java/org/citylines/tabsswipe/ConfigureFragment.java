package org.citylines.tabsswipe;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListAdapter;
import android.widget.ListView;
import org.citylines.LocationSelectionActivity;
import org.citylines.R;
import org.citylines.db.DBFactory;
import org.citylines.db.location.CurrentLocationParams;
import org.citylines.db.location.factory.LocationParamsFactory;
import static org.citylines.db.location.factory.LocationParamsType.LOCATION_PARAMS_CURRENT;
 
public class ConfigureFragment extends Fragment {

    private Context context = null;
    
    public OnItemClickListener itemClickListener = new OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if (id == 1L) {
                Intent myIntent = new Intent(ConfigureFragment.this.context, LocationSelectionActivity.class);
                // myIntent.putExtra("key", id); //Optional parameters
                ConfigureFragment.this.startActivity(myIntent);
            } else {
                /* TODO: Update action */
            }
        }
    };    
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.context = this.getActivity();
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        
        View rootView = inflater.inflate(R.layout.fragment_configure, container, false);
        
        final CurrentLocationParams clp = (CurrentLocationParams) 
                LocationParamsFactory.build(LOCATION_PARAMS_CURRENT);
        Cursor c = DBFactory.getInstance(context).getCurrentLocation(clp);
        String currentCity = c.getString(c.getColumnIndex("name"));
        
        String[] columns = new String[] { "_id", "config", "value"};
        MatrixCursor matrixCursor = new MatrixCursor(columns);
        matrixCursor.addRow(new Object[] { 1, "Change location", "Current city: " + currentCity });
        matrixCursor.addRow(new Object[] { 2, "Update", "Refresh local database" });
                
        ListAdapter adapter = new SimpleCursorAdapter(context,
                android.R.layout.simple_list_item_2,
                matrixCursor,
                new String[] {"config", "value"},
                new int[] {android.R.id.text1, android.R.id.text2}, 0);

        ListView listView = (ListView) rootView.findViewById(R.id.list);
        listView.setClickable(true);
        listView.setOnItemClickListener(itemClickListener);
        
        listView.setAdapter(adapter);         
         
        return rootView;
    }
}