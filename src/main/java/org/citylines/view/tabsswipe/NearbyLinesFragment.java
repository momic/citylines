package org.citylines.view.tabsswipe;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ProgressBar;
import java.util.List;
import org.citylines.R;
import org.citylines.db.dao.factory.DAOFactory;
import org.citylines.db.dao.factory.DAOType;
import org.citylines.db.dao.StationDAO;
import org.citylines.model.station.ExpandableStationsListAdapter;
import org.citylines.model.station.Station;

public class NearbyLinesFragment extends Fragment implements OnClickListener {
    
    private static final String NEARBY_STATIONS_COUNT = "3";

    private LocationManager locationManager = null;
    private LocationListener locationListener = null;

    private Button btnGetLocation = null;
    private ProgressBar pb = null;
    private Boolean locationAquired = false;
    
    private StationDAO stationDAO;
    private Context context;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        this.context = getActivity();
        stationDAO = (StationDAO) DAOFactory.build(DAOType.STATION_DAO, context);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_nearby_lines, container, false);

        pb = (ProgressBar) rootView.findViewById(R.id.progressBar1);
        pb.setVisibility(View.INVISIBLE);

        btnGetLocation = (Button) rootView.findViewById(R.id.btnLocation);
        btnGetLocation.setOnClickListener(this);

        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        return rootView;
    }
    
    private void showGPSUnavailableAlert() {
        Location lastKnown = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (lastKnown != null) {
            alertbox("Can't get GPS lock.", "Using last known location.", false);
            showNearbyStations(lastKnown);
        } else {
            alertbox("Can't get GPS lock.", "Check GPS and try again.", false);
        }
    }

    @Override
    public void onClick(View v) {
        if (displayGpsStatus()) {
            pb.setVisibility(View.VISIBLE);
            locationListener = new MyLocationListener();
            final Looper myLooper = Looper.myLooper();
            locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, locationListener, myLooper);
            final Handler myHandler = new Handler(myLooper);
            myHandler.postDelayed(new Runnable() {
                 @Override
                 public void run() {
                     pb.setVisibility(View.INVISIBLE);
                     locationManager.removeUpdates(locationListener);
                     if (!locationAquired) {
                        showGPSUnavailableAlert();
                     }
                 }
            }, 20000);            
        } else {
            alertbox("Check GPS Status!", "Your device's GPS is disabled.", true);
        }
    }

    /**
     * Check GPS is enable or disable
     * 
     * @return 
     */
    private Boolean displayGpsStatus() {
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    /**
     * Create an AlertBox
     * 
     * @param title
     * @param mymessage
     * @param actions 
     */
    protected void alertbox(String title, String mymessage, boolean actions) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(mymessage)
                .setCancelable(false)
                .setTitle(title)
                .setNegativeButton(actions ? "Cancel" : "Ok",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                // cancel the dialog box  
                                dialog.cancel();
                            }
                        });
        if (actions) {
            builder.setPositiveButton("Gps On",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            Intent myIntent = new Intent(
                                    Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            startActivity(myIntent);
                            dialog.cancel();
                        }
                    });
        }
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void showNearbyStations(Location loc) {
        // get the listview
        ExpandableListView expListView = (ExpandableListView) getView().findViewById(R.id.elvStation);
        
        List<Station> stations = stationDAO.getNerbyStations(loc.getLatitude(), loc.getLongitude(), NEARBY_STATIONS_COUNT);
        if (stations != null)
        {
            ExpandableStationsListAdapter listAdapter = new ExpandableStationsListAdapter(context, stations);

            // setting list adapter
            expListView.setAdapter(listAdapter);            
        }
    }        
    
    /**
     * LocationListener for getting coordinates
     */
    private class MyLocationListener implements LocationListener {
        
        @Override
        public void onLocationChanged(Location loc) {
            locationAquired = (loc != null);
            pb.setVisibility(View.INVISIBLE);
            if (locationAquired) {
                showNearbyStations(loc);
            } else {
                showGPSUnavailableAlert();
            }
        }

        @Override
        public void onProviderDisabled(String provider) {
        }

        @Override
        public void onProviderEnabled(String provider) {
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }
    }
}
