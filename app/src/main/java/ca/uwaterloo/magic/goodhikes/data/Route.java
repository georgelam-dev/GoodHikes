package ca.uwaterloo.magic.goodhikes.data;

import android.content.ContentValues;
import android.database.Cursor;
import android.location.Location;
import com.google.android.gms.maps.model.LatLng;
import java.util.ArrayList;

import ca.uwaterloo.magic.goodhikes.data.RoutesContract.RouteEntry;

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

    public static Route fromDBCursor(Cursor cursor){
        Route route = new Route();
        route.setId(cursor.getLong(cursor.getColumnIndex(RouteEntry._ID)));
        route.setDescription(cursor.getString(cursor.getColumnIndex(RouteEntry.COLUMN_DESCRIPTION)));
        route.setDateStart(cursor.getLong(cursor.getColumnIndex(RouteEntry.COLUMN_DATE_START)));
        route.setDateEnd(cursor.getLong(cursor.getColumnIndex(RouteEntry.COLUMN_DATE_END)));
        return route;
    }
}
