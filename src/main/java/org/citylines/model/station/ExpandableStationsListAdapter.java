package org.citylines.model.station;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.citylines.R;
import org.citylines.model.line.CarrierLine;
import org.citylines.model.line.CarrierLineDeparture;

/**
 *
 * @author zlaja
 */
public class ExpandableStationsListAdapter extends BaseExpandableListAdapter {
    
    private final Context context;
    private final List<Station> stations;
    
    private static class GroupViewHolder {
        TextView name;
    }
    private static class ChildViewHolder {
        TextView name;
        TextView departureFrom;
        TextView departureTimes;
        TextView returnFrom;
        TextView returnTimes;
    }

    public ExpandableStationsListAdapter(Context context, List<Station> stations) {
        this.context = context;
        this.stations = stations;
    }

    @Override
    public int getGroupCount() {
        return stations.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return stations.get(groupPosition).carrierLines.size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return stations.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return stations.get(groupPosition).carrierLines.get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return stations.get(groupPosition).id;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return stations.get(groupPosition).carrierLines.get(childPosition).getId();
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        GroupViewHolder groupViewHolder;
        if (convertView == null) {            
            LayoutInflater infalInflater = (LayoutInflater) this.context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.group_station, null);
            
            groupViewHolder = new GroupViewHolder();
            
            groupViewHolder.name = (TextView) convertView.findViewById(R.id.sName);
            groupViewHolder.name.setTypeface(null, Typeface.BOLD);
                        
            convertView.setTag(groupViewHolder);
        } else {
            groupViewHolder = (GroupViewHolder) convertView.getTag();
        }
        
        Station station = (Station) getGroup(groupPosition);
        groupViewHolder.name.setText(station.name);
 
        return convertView;        
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        ChildViewHolder childViewHolder;
        if (convertView == null) {            
            LayoutInflater infalInflater = (LayoutInflater) this.context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.item_station_carrier_line, null);
            
            childViewHolder = new ChildViewHolder();
            
            childViewHolder.name = (TextView) convertView.findViewById(R.id.sclName);
            childViewHolder.name.setTypeface(null, Typeface.BOLD);
            
            childViewHolder.departureFrom = (TextView) convertView.findViewById(R.id.sclDepartureFrom);
            childViewHolder.returnFrom = (TextView) convertView.findViewById(R.id.sclReturnFrom);
            
            childViewHolder.departureTimes = (TextView) convertView.findViewById(R.id.sclDepartureTimes);            
            childViewHolder.returnTimes = (TextView) convertView.findViewById(R.id.sclReturnTimes);
                        
            convertView.setTag(childViewHolder);
        } else {
            childViewHolder = (ChildViewHolder) convertView.getTag();
        }
        
        // Get child
        CarrierLine line = (CarrierLine) getChild(groupPosition, childPosition);
        Iterator<CarrierLineDeparture> departuresIterator = line.getDepartures().iterator();
        
        // Prepare departure and return times
        List<String> departureTimesList = new ArrayList<String>();
        List<String> returnTimesList = new ArrayList<String>();
        while (departuresIterator.hasNext()) {
            CarrierLineDeparture cld = departuresIterator.next();
            departureTimesList.add(cld.getDepartureTime());
            returnTimesList.add(cld.getArrivalTime());
        }
        Collections.sort(departureTimesList);
        Collections.sort(returnTimesList);
        
        // Set textviews
        childViewHolder.name.setText(line.getName());
        
        childViewHolder.departureTimes.setText(departureTimesList.toString().replace("[", "").replace("]", ""));
        childViewHolder.returnTimes.setText(returnTimesList.toString().replace("[", "").replace("]", ""));
        
        childViewHolder.departureFrom.setText(String.format(context.getResources().getString(R.string.departure_from), line.getDepartureStation()));
        childViewHolder.returnFrom.setText(String.format(context.getResources().getString(R.string.return_from), line.getArrivalStation()));
        
        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return false;
    }    
}