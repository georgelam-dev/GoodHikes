package ca.uwaterloo.magic.goodhikes;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import ca.uwaterloo.magic.goodhikes.data.Route;

public class RoutesAdapter extends ArrayAdapter<Route> {

     // Cache of the children views for a route list item
    public static class ViewHolder {
        public final TextView descriptionView, durationView, dateOnView,
                dateFromView, dateToView, pointsCountView;
        public final ImageView iconDeleteItemView;

        public ViewHolder(View view) {
            descriptionView = (TextView) view.findViewById(R.id.list_item_descr_textview);
            dateOnView = (TextView) view.findViewById(R.id.list_item_date_on);
            dateFromView = (TextView) view.findViewById(R.id.list_item_date_from);
            dateToView = (TextView) view.findViewById(R.id.list_item_date_to);
            durationView = (TextView) view.findViewById(R.id.list_item_duration_textview);
            pointsCountView = (TextView) view.findViewById(R.id.list_item_points_count_textview);
            iconDeleteItemView = (ImageView) view.findViewById(R.id.list_item_delete_icon);
        }
    }

    public RoutesAdapter(Context context, ArrayList<Route> routes) {
        super(context, 0, routes);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder; // to reference the child views for later actions
        Route route = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_route, parent, false);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        }
        viewHolder = (ViewHolder) convertView.getTag();

        viewHolder.descriptionView.setText(route.getDescription());
        viewHolder.dateOnView.setText(route.getDateStartString());
        viewHolder.durationView.setText(route.getDurationString());
        viewHolder.dateFromView.setText(route.getTimeStart());
        viewHolder.dateToView.setText(route.getTimeEnd());
        viewHolder.pointsCountView.setText(String.valueOf(route.size()));
        return convertView;
    }
}
