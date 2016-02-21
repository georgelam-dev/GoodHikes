package ca.uwaterloo.magic.goodhikes;

import android.location.Location;
import com.google.android.gms.maps.model.LatLng;
import java.util.ArrayList;

public class RouteTrace {
    private ArrayList<Location> points;
    private ArrayList<LatLng> pointsCoordinates;

    public RouteTrace() {
        this.points = new ArrayList<Location>();
        this.pointsCoordinates = new ArrayList<LatLng>();
    }

    public void addPoint(Location location){
        points.add(location);
        LatLng pointCoordinates = new LatLng(location.getLatitude(), location.getLongitude());
        pointsCoordinates.add(pointCoordinates);
    }

    public ArrayList<Location> getTrace(){
        return points;
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
        return points.size();
    }

    public void clearTrace(){
        points.clear();
        pointsCoordinates.clear();
    }
}
