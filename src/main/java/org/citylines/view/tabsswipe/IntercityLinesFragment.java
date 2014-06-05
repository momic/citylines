package org.citylines.view.tabsswipe;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.SimpleCursorAdapter;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.FilterQueryProvider;
import android.widget.ImageButton;
import java.text.ParseException;
import java.util.List;
import org.citylines.R;
import org.citylines.db.dao.CarrierLineDAO;
import org.citylines.db.dao.LocationDAO;
import org.citylines.db.dao.factory.DAOFactory;
import org.citylines.db.dao.factory.DAOType;
import org.citylines.model.line.CarrierLine;
import org.citylines.model.line.ExpandableListAdapter;
import org.citylines.model.location.CurrentLocationParams;
import org.citylines.model.location.LocationParams;
import org.citylines.model.location.factory.LocationParamsFactory;
import static org.citylines.model.location.factory.LocationParamsType.LOCATION_PARAMS_CURRENT;
import static org.citylines.model.location.factory.LocationParamsType.LOCATION_PARAMS_SIMILAR;
import org.citylines.view.dialog.date.SelectDateFragment;
import static org.citylines.view.dialog.date.SelectDateFragment.INPUT_DATETIME_FORMATTER;
import org.joda.time.DateTime;
 
public class IntercityLinesFragment extends Fragment {
    
    public static final int DIALOG_FRAGMENT = 1;
    
    private ImageButton changeDate;
    private Context context;
    private LocationDAO locationDAO;
    private CarrierLineDAO carrierLineDAO;
    
    private Long departureId;
    private Long destinationId;
    private DateTime date;
    
    private final OnItemClickListener destinationItemClick = new OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            // get destination
            Cursor destination = (Cursor) parent.getItemAtPosition(position);
            destinationId = destination.getLong(destination.getColumnIndex("_id"));
            
            // show timetable
            if (departureId != null && date != null) {
                showTimetable(departureId, destinationId, date);
            }
        }
    };
    private final OnClickListener changeDateClick;

    public IntercityLinesFragment() {
        this.changeDateClick = new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                View parent = (View)v.getParent();
                if (parent != null) {
                    DialogFragment newFragment = new SelectDateFragment();
                    newFragment.setTargetFragment(IntercityLinesFragment.this, DIALOG_FRAGMENT);
                    
                    final Bundle b = new Bundle();
                    b.putString(SelectDateFragment.DATE_EDIT_ID, "datePickEdit");
                    newFragment.setArguments(b);

                    FragmentActivity fa = (FragmentActivity) context;
                    newFragment.show(fa.getSupportFragmentManager(), "DatePicker");
                }
            }
        };
        
    }
    
    private void showTimetable(Long deparetureId, Long destinationId, DateTime date) {        
        // get the listview
        ExpandableListView expListView = (ExpandableListView) getView().findViewById(R.id.lvExp);
        List<CarrierLine> lines;
        try {
            lines = carrierLineDAO.getAcceptableLines(deparetureId, destinationId, date);
        } catch (ParseException ex) {
            lines = null;
        }        
 
        if (lines != null)
        {
            ExpandableListAdapter listAdapter = new ExpandableListAdapter(getActivity(), lines);

            // setting list adapter
            expListView.setAdapter(listAdapter);            
        }
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.context = getActivity();
        this.carrierLineDAO = (CarrierLineDAO) DAOFactory.build(DAOType.CARRIER_LINE_DAO, getActivity());
        this.locationDAO = (LocationDAO) DAOFactory.build(DAOType.LOCATION_DAO, getActivity());
        
        // get current location
        final CurrentLocationParams clp = (CurrentLocationParams) 
                LocationParamsFactory.build(LOCATION_PARAMS_CURRENT);
        Cursor currentLocation = this.locationDAO.getCurrentLocation(clp);
        this.departureId = currentLocation.getLong(currentLocation.getColumnIndex("id"));
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
 
        View rootView = inflater.inflate(R.layout.fragment_intercity_lines, container, false);
        
        // Initialize auto complete text view
        AutoCompleteTextView editDestination = (AutoCompleteTextView) rootView.findViewById(R.id.editDestination);
        editDestination.setThreshold(2);
        editDestination.setOnItemClickListener(destinationItemClick);
        
        // Initialize auto complete adapter
        SimpleCursorAdapter adapter = new SimpleCursorAdapter(context,
                android.R.layout.simple_list_item_2,
                null,
                new String[] {"name", "municipalityName"},
                new int[] {android.R.id.text1, android.R.id.text2}, 0);
        
        adapter.setFilterQueryProvider(new FilterQueryProvider() {
            
            final LocationParams lp = LocationParamsFactory.build(LOCATION_PARAMS_SIMILAR);

            @Override
            public Cursor runQuery(CharSequence constraint) {
                if (constraint == null) {
                    return null;
                }
                // Create location params
                lp.setConstraint(constraint);
                return locationDAO.getLocationItems(lp);
            }
        });
        
        adapter.setCursorToStringConverter(new SimpleCursorAdapter.CursorToStringConverter() {

            @Override
            public CharSequence convertToString(Cursor cursor) {
                return cursor.getString(cursor.getColumnIndex("name"));
            }
        });
        
        // Set adapter
        editDestination.setAdapter(adapter);
        
        // init date picker
        changeDate = (ImageButton) rootView.findViewById(R.id.datePickButton);
        changeDate.setOnClickListener(changeDateClick);
        
        // init date edit
        EditText editDate = (EditText) rootView.findViewById(R.id.datePickEdit);
        editDate.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0) {
                    //get date
                    date = INPUT_DATETIME_FORMATTER.withZoneUTC().parseDateTime(s.toString());

                    // show timetable
                    if (departureId != null && destinationId != null) {
                        showTimetable(departureId, destinationId, date);                    
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
         
        return rootView;


    }
}