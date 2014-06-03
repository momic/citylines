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
 
    public ExpandableListAdapter(Context context, List<CarrierLine> carrierLines) {
        this.context = context;
        this.carrierLines = carrierLines;
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
        
        Object viewHolder;
        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) this.context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            
            int layout;
            if (childPosition == 0) {
                layout = R.layout.item_header_carrier_line_departure;
                viewHolder = new ChildHeaderViewHolder();
            } else {
                layout = R.layout.item_carrier_line_departure;
                viewHolder = new ChildViewHolder();
            }
            convertView = infalInflater.inflate(layout, null);
            
            if (childPosition == 0) {
                ChildHeaderViewHolder headerViewHolder = (ChildHeaderViewHolder) viewHolder;
                headerViewHolder.station = (TextView) convertView.findViewById(R.id.clStation);
                headerViewHolder.gate = (TextView) convertView.findViewById(R.id.clGate);
                headerViewHolder.phone = (TextView) convertView.findViewById(R.id.clPhone);
            } else {
                ChildViewHolder childViewHolder = (ChildViewHolder) viewHolder;
                childViewHolder.departure = (TextView) convertView.findViewById(R.id.cldDepartureTime);
                childViewHolder.arrival = (TextView) convertView.findViewById(R.id.cldArrivalTime);
            }
            
            // Optimize findViewById calls, use Tag if converterView is not null
            convertView.setTag(viewHolder);
        } else {
            viewHolder = convertView.getTag();
        } 

        if (childPosition == 0) {
            final CarrierLine group = (CarrierLine) getGroup(groupPosition);
            ChildHeaderViewHolder headerViewHolder = (ChildHeaderViewHolder) viewHolder;
            headerViewHolder.station.setText(
                    context.getResources().getString(R.string.station) 
                            + " " + group.getDepartureStation());
            headerViewHolder.gate.setText(
                    context.getResources().getString(R.string.gate) 
                            + " " + group.getGate());
            headerViewHolder.phone.setText(
                    context.getResources().getString(R.string.phone) 
                            + " " + group.getPhone());
        } else {
            final CarrierLineDeparture child = (CarrierLineDeparture) 
                    getChild(groupPosition, childPosition);
            
            ChildViewHolder childViewHolder = (ChildViewHolder) viewHolder;
            childViewHolder.departure.setText(child.getDepartureTime());
            childViewHolder.arrival.setText(child.getArrivalTime());            
        }
        
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