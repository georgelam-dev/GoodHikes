package ca.uwaterloo.magic.goodhikes.data;

import android.content.ContentValues;
import android.database.Cursor;
import android.location.Location;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;

import ca.uwaterloo.magic.goodhikes.data.RoutesContract.RouteEntry;
import ca.uwaterloo.magic.goodhikes.data.RoutesContract.UserEntry;

public class Route {
    private long id;
    private ArrayList<LocationPoint> trace;
    private ArrayList<LatLng> pointsCoordinates;
    private User user;
    private String description;

    // Date start/end, stored as long in milliseconds since the epoch
    private long dateStart;
    private long dateEnd;

    public long getId() {return id;}
    public void setId(long id) {this.id = id;}

    public Route(User user) {
        this.user = user;
        this.trace = new ArrayList<LocationPoint>();
        this.pointsCoordinates = new ArrayList<LatLng>();
        this.description = "stub_descr";
        this.dateStart = System.currentTimeMillis();
    }

    public Route() {}

    public void addPoint(Location location){
        LocationPoint locationPoint = new LocationPoint(location);
        trace.add(locationPoint);
        LatLng pointCoordinates = new LatLng(locationPoint.getLatitude(), locationPoint.getLongitude());
        pointsCoordinates.add(pointCoordinates);
    }

    public ArrayList<LocationPoint> getTrace(){
        return trace;
    }

    public void setTrace(ArrayList<LocationPoint> trace){
        this.trace = trace;
        this.pointsCoordinates = new ArrayList<LatLng>();
        for(LocationPoint locationPoint : trace){
            LatLng pointCoordinates = new LatLng(locationPoint.getLatitude(), locationPoint.getLongitude());
            pointsCoordinates.add(pointCoordinates);
        }
    }

    public User getUser(){
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getDescription(){
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }

    public ArrayList<LatLng> getPointsCoordinates(){
        return pointsCoordinates;
    }

    public LatLng getStartCoordinates(){
        return (pointsCoordinates.size()!=0) ? pointsCoordinates.get(0) : null;
    }

    public LatLng getLastCoordinates(){
        return (pointsCoordinates.size()!=0) ? pointsCoordinates.get(pointsCoordinates.size()-1) : null;
    }

    public int size(){
        return trace.size();
    }

    public void clearTrace(){
        trace.clear();
        pointsCoordinates.clear();
    }

    public ContentValues toContentValues(){
        ContentValues values = new ContentValues();
        values.put(RouteEntry.COLUMN_DESCRIPTION, description);
        values.put(RouteEntry.COLUMN_DATE_START, dateStart);
        values.put(RouteEntry.COLUMN_DATE_END, dateEnd);
        return values;
    }

    // stored as long in milliseconds since the epoch
    public void setDateStart(long timeMillis){
        dateStart = timeMillis;
    }

    // stored as long in milliseconds since the epoch
    public void setDateEnd(long timeMillis){
        dateEnd = timeMillis;
    }

    public String getDateStartString(){
        return getFormattedDate(dateStart);
    }

    public String getTimeStart(){
        return getFormattedTime(dateStart);
    }

    public String getTimeEnd(){
        return getFormattedTime(dateEnd);
    }

    private static String getFormattedDate(long dateInMillis) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE MMM dd"); //Mon Jun 3
        String dateString = dateFormat.format(dateInMillis);
        return dateString;
    }

    private static String getFormattedTime(long dateInMillis) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("KK:mm:ss a"); //05:30:20 PM
        String dateString = dateFormat.format(dateInMillis);
        return dateString;
    }

    public String getDurationString(){
        long duration = (dateEnd-dateStart)/1000;
        return String.format("%02d:%02d:%02d", duration/3600, (duration%3600)/60, (duration%60));
    }

    public static Route fromDBCursor(Cursor cursor, boolean withUser){
        Route route = new Route();
        route.setId(cursor.getLong(cursor.getColumnIndex(RouteEntry._ID)));
        route.setDescription(cursor.getString(cursor.getColumnIndex(RouteEntry.COLUMN_DESCRIPTION)));
        route.setDateStart(cursor.getLong(cursor.getColumnIndex(RouteEntry.COLUMN_DATE_START)));
        route.setDateEnd(cursor.getLong(cursor.getColumnIndex(RouteEntry.COLUMN_DATE_END)));
        if(withUser){
            route.user = new User();
            route.user.setId(cursor.getLong(cursor.getColumnIndex(RouteEntry.COLUMN_USER_KEY)));
            route.user.setUsername(cursor.getString(cursor.getColumnIndex(UserEntry.COLUMN_USERNAME_ALIAS)));
        }
        return route;
    }

    //currently filters by username, not user_id, since user object might not have an ID assigned
    //FIX: make sure that passed User object has a user_id assigned
    public static HashMap filterByUser(User user){
        HashMap routesRetrievalOptions = new HashMap();
        routesRetrievalOptions.put(UserEntry.COLUMN_USERNAME_ALIAS, String.valueOf(user.getUsername()));
        return routesRetrievalOptions;
    }

    public LatLngBounds getLatLngBounds(){
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for(LatLng point : pointsCoordinates){
            builder.include(point);
        }
        return builder.build();
    }
}
