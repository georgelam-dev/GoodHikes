package ca.uwaterloo.magic.goodhikes.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import ca.uwaterloo.magic.goodhikes.data.RoutesContract.RouteEntry;
import ca.uwaterloo.magic.goodhikes.data.RoutesContract.LocationEntry;

/**
 * Manages device's database for routes data.
 */
public class RoutesDbHelper extends SQLiteOpenHelper {

    // If you change the database schema, you must increment the database version.
    private static final int DATABASE_VERSION = 1;

    static final String DATABASE_NAME = "routes.db";

    public RoutesDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        final String SQL_CREATE_ROUTE_TABLE = "CREATE TABLE " + RouteEntry.TABLE_NAME + " (" +
                RouteEntry._ID                 + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                RouteEntry.COLUMN_DESCRIPTION  + " TEXT NOT NULL, " +
                RouteEntry.COLUMN_DATE_START   + " INTEGER NOT NULL, " +
                RouteEntry.COLUMN_DATE_END     + " INTEGER NOT NULL " +
                " );";

        final String SQL_CREATE_LOCATION_TABLE = "CREATE TABLE " + LocationEntry.TABLE_NAME + " (" +
                LocationEntry._ID               + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                // the ID of the route entry associated with this location point data
                LocationEntry.COLUMN_ROUTE_KEY  + " INTEGER NOT NULL, " +
                LocationEntry.COLUMN_DATE       + " INTEGER NOT NULL, " +
                LocationEntry.COLUMN_COORD_LAT  + " REAL NOT NULL, " +
                LocationEntry.COLUMN_COORD_LONG + " REAL NOT NULL, " +

                " FOREIGN KEY (" + LocationEntry.COLUMN_ROUTE_KEY + ") REFERENCES " +
                RouteEntry.TABLE_NAME + " (" + RouteEntry._ID + ") " +
                " );";

        sqLiteDatabase.execSQL(SQL_CREATE_ROUTE_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_LOCATION_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        // This database upgrade policy is to simply to discard the data and start over.
        // Note that this only fires if you change the version number for your database.
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + RouteEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + LocationEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
}
