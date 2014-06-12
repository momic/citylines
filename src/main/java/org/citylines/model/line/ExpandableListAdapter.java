package org.citylines.model.line;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;
import java.util.List;
import org.citylines.R;

public class ExpandableListAdapter extends BaseExpandableListAdapter {
    
    private static class GroupViewHolder {
        TextView carrier;
        TextView line;    
    }    
    private static class ChildViewHolder {
        TextView departure;
        TextView arrival;
    }    
    private static class ChildHeaderViewHolder {
        TextView station;
        TextView gate;
        TextView phone;   
    }    
 
    private final Context context;
    private final List<CarrierLine> carrierLines; // header titles
    private final LayoutInflater inflater;
 
    public ExpandableListAdapter(Context context, List<CarrierLine> carrierLines) {
        this.context = context;
        this.carrierLines = carrierLines;
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }
    
    @Override
    public Object getChild(int groupPosition, int childPosition) {
        if (childPosition == 0)
            return null;
        
        return this.carrierLines.get(groupPosition).getDepartures().get(childPosition-1);
    }
 
    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }
 
    @Override
    public View getChildView(int groupPosition, final int childPosition,
            boolean isLastChild, View convertView, ViewGroup parent) {
        
        if (childPosition == 0) {

            ChildHeaderViewHolder viewHolder;
            if (convertView == null || !(convertView.getTag() instanceof ChildHeaderViewHolder)) {
                viewHolder = new ChildHeaderViewHolder();
                convertView = inflater.inflate(R.layout.item_header_carrier_line_departure, null);

                viewHolder.station = (TextView) convertView.findViewById(R.id.clStation);
                viewHolder.gate = (TextView) convertView.findViewById(R.id.clGate);
                viewHolder.phone = (TextView) convertView.findViewById(R.id.clPhone);

                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ChildHeaderViewHolder) convertView.getTag();
            }
            
            final CarrierLine group = (CarrierLine) getGroup(groupPosition);
            viewHolder.station.setText(context.getResources().getString(R.string.station) 
                        + " " + group.getDepartureStation());
            viewHolder.gate.setText(context.getResources().getString(R.string.gate) 
                        + " " + group.getGate());
            viewHolder.phone.setText(context.getResources().getString(R.string.phone)
                    + " " + group.getPhone());   
            
            return convertView;
        }
        
        ChildViewHolder viewHolder;
        if (convertView == null || !(convertView.getTag() instanceof ChildViewHolder)) {
            viewHolder = new ChildViewHolder();
            convertView = inflater.inflate(R.layout.item_carrier_line_departure, null);

            viewHolder.departure = (TextView) convertView.findViewById(R.id.cldDepartureTime);
            viewHolder.arrival = (TextView) convertView.findViewById(R.id.cldArrivalTime);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ChildViewHolder) convertView.getTag();
        }        
        
        final CarrierLineDeparture child = (CarrierLineDeparture) 
            getChild(groupPosition, childPosition);                

        viewHolder.departure.setText(child.getDepartureTime());
        viewHolder.arrival.setText(child.getArrivalTime());            
            
        return convertView;
    }
 
    @Override
    public int getChildrenCount(int groupPosition) {
        return this.carrierLines.get(groupPosition).getDepartures().size() + 1;
    }
 
    @Override
    public Object getGroup(int groupPosition) {
        return this.carrierLines.get(groupPosition);
    }
 
    @Override
    public int getGroupCount() {
        return this.carrierLines.size();
    }
 
    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }
 
    @Override
    public View getGroupView(int groupPosition, boolean isExpanded,
            View convertView, ViewGroup parent) {

        GroupViewHolder groupViewHolder;
        if (convertView == null) {            
            LayoutInflater infalInflater = (LayoutInflater) this.context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.group_carrier_line, null);
            
            groupViewHolder = new GroupViewHolder();
            
            groupViewHolder.carrier = (TextView) convertView.findViewById(R.id.clCarrier);
            groupViewHolder.carrier.setTypeface(null, Typeface.BOLD);
            
            groupViewHolder.line = (TextView) convertView.findViewById(R.id.clLine);
            groupViewHolder.line.setTypeface(null, Typeface.BOLD);
                        
            convertView.setTag(groupViewHolder);
        } else {
            groupViewHolder = (GroupViewHolder) convertView.getTag();
        }
        
        CarrierLine carrierLine = (CarrierLine) getGroup(groupPosition);
        groupViewHolder.carrier.setText(carrierLine.getCarrier());
        groupViewHolder.line.setText(context.getResources().getString(R.string.line) + " " + carrierLine.getName());
 
        return convertView;
    }
 
    @Override
    public boolean hasStableIds() {
        return false;
    }
 
    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return false;
    }
}