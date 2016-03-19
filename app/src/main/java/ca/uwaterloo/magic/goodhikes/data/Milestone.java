package ca.uwaterloo.magic.goodhikes.data;

import android.content.ContentValues;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by GeorgeLam on 3/15/2016.
 */
public class Milestone {
    private long id;
    private long date;
    private LatLng latLng;
    private String note;
    private Bitmap image;

    public long getId() {return id;}
    public void setId(long id) {this.id = id;}

    public Milestone(LatLng latLng, String note, Bitmap image) {
        this.latLng = latLng;
        this.date = System.currentTimeMillis();
        this.note = note;
        this.image = image;
    }

    public LatLng getLatLng() { return latLng; }

    public String getNote() { return note; }
    public void setNote(String note){ this.note = note; }

    public Bitmap getImage() { return image; }
    public void setImage(Bitmap image){
        this.image = image;
    }

    public ContentValues toContentValues(){
        ContentValues values = new ContentValues();
        /*
        values.put(LocationEntry.COLUMN_ROUTE_KEY,  this.routeId);
        values.put(LocationEntry.COLUMN_DATE,       this.getTime());
        values.put(LocationEntry.COLUMN_COORD_LAT,  this.getLatitude());
        values.put(LocationEntry.COLUMN_COORD_LONG, this.getLongitude());
        values.put(LocationEntry.COLUMN_SPEED,      this.getSpeed());
        values.put(LocationEntry.COLUMN_BEARING,    this.getBearing());
        values.put(LocationEntry.COLUMN_ACCURACY,   this.getAccuracy());
        */
        return values;
    }
}
