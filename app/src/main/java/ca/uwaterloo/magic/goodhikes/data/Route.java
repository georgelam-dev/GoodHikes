package ca.uwaterloo.magic.goodhikes.data;

import android.content.ContentValues;
import android.location.Location;
import com.google.android.gms.maps.model.LatLng;
import java.util.ArrayList;

import ca.uwaterloo.magic.goodhikes.data.RoutesContract.RouteEntry;
import ca.uwaterloo.magic.goodhikes.data.RoutesContract.LocationEntry;

public class Route {
    private long id;
    private ArrayList<Location> trace;
    private ArrayList<LatLng> pointsCoordinates;
    private User user;
    private String description;

    // Date start/end, stored as long in milliseconds since the epoch
    private int dateStart;
    private int dateEnd;

    public long getId() {return id;}
    public void setId(long id) {this.id = id;}

    public Route(User user) {
        this.user = user;
        this.trace = new ArrayList<Location>();
        this.pointsCoordinates = new ArrayList<LatLng>();
        this.description = "stub_descr";
        this.dateStart = 1457302006; //stub
        this.dateEnd = 1457302006; //stub
    }

    public void addPoint(Location location){
        trace.add(location);
        LatLng pointCoordinates = new LatLng(location.getLatitude(), location.getLongitude());
        pointsCoordinates.add(pointCoordinates);
    }

    public ArrayList<Location> getTrace(){
        return trace;
    }
    public User getUser(){
        return user;
    }
    public String getDescription(){
        return description;
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

    public ContentValues getContentValues(){
        ContentValues values = new ContentValues();
        values.put(RouteEntry.COLUMN_DESCRIPTION, description);
        values.put(RouteEntry.COLUMN_DATE_START, dateStart);
        values.put(RouteEntry.COLUMN_DATE_END, dateEnd);
        return values;
    }

    public ContentValues getLocationContentValues(Location location){
        ContentValues values = new ContentValues();
        values.put(LocationEntry.COLUMN_ROUTE_KEY, this.id);
        values.put(LocationEntry.COLUMN_DATE,      location.getTime());
        values.put(LocationEntry.COLUMN_COORD_LAT, location.getLatitude());
        values.put(LocationEntry.COLUMN_COORD_LONG,location.getLongitude());
        values.put(LocationEntry.COLUMN_SPEED,     location.getSpeed());
        values.put(LocationEntry.COLUMN_BEARING,   location.getBearing());
        values.put(LocationEntry.COLUMN_ACCURACY,  location.getAccuracy());
        return values;
    }
}
