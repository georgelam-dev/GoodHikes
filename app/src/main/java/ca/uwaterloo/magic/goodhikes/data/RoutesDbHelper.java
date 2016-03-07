package ca.uwaterloo.magic.goodhikes.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.location.Location;
import android.util.Log;

import ca.uwaterloo.magic.goodhikes.data.RoutesContract.UserEntry;
import ca.uwaterloo.magic.goodhikes.data.RoutesContract.RouteEntry;
import ca.uwaterloo.magic.goodhikes.data.RoutesContract.LocationEntry;

/**
 * Manages device's database for routes data.
 * Use this guide https://guides.codepath.com/android/Local-Databases-with-SQLiteOpenHelper
 * to add data handling methods
 */
public class RoutesDbHelper extends SQLiteOpenHelper {
    private static RoutesDbHelper sInstance;
    protected static final String LOG_TAG = "RoutesDbHelper";

    // If you change the database schema, you must increment the database version.
    private static final int DATABASE_VERSION = 1;

    static final String DATABASE_NAME = "routes.db";

    public static synchronized RoutesDbHelper getInstance(Context context) {
        // Use the application context, which will ensure that you
        // don't accidentally leak an Activity's context.
        // http://android-developers.blogspot.ca/2009/01/avoiding-memory-leaks.html
        if (sInstance == null) {
            sInstance = new RoutesDbHelper(context.getApplicationContext());
        }
        return sInstance;
    }

    /**
     * Constructor should be private to prevent direct instantiation.
     * Make a call to the static method "getInstance()" instead.
     */
    private RoutesDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        final String SQL_CREATE_USER_TABLE = "CREATE TABLE " + UserEntry.TABLE_NAME + " (" +
                UserEntry._ID                 + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                UserEntry.COLUMN_NAME         + " TEXT NOT NULL " +
                " );";

        final String SQL_CREATE_ROUTE_TABLE = "CREATE TABLE " + RouteEntry.TABLE_NAME + " (" +
                RouteEntry._ID                 + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                // the ID of the user entry associated with this route
                RouteEntry.COLUMN_USER_KEY  + " INTEGER NOT NULL, " +
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
                LocationEntry.COLUMN_SPEED      + " REAL, " +
                LocationEntry.COLUMN_BEARING    + " REAL, " +
                LocationEntry.COLUMN_ACCURACY   + " REAL, " +


                " FOREIGN KEY (" + LocationEntry.COLUMN_ROUTE_KEY + ") REFERENCES " +
                RouteEntry.TABLE_NAME + " (" + RouteEntry._ID + ") " +
                " );";

        sqLiteDatabase.execSQL(SQL_CREATE_USER_TABLE);
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

    public void insertRoute(Route route) {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            long userId = insertOrUpdateUser(route.getUser());
            ContentValues routeKV = route.getContentValues();
            routeKV.put(RouteEntry.COLUMN_USER_KEY, userId);
            long routeId = db.insertOrThrow(RouteEntry.TABLE_NAME, null, routeKV);
            route.setId(routeId);
            bulkInsertLocationPoints(route);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.d(LOG_TAG, "Error while trying to insert route into the db");
        } finally {
            db.endTransaction();
        }
    }

    public long bulkInsertLocationPoints(Route route) {
        SQLiteDatabase db = getWritableDatabase();
        int returnCount = 0;
        long id;

        db.beginTransaction();
        try {
            for(Location location : route.getTrace()){
                ContentValues locationKV = route.getLocationContentValues(location);
                id = db.insert(LocationEntry.TABLE_NAME, null, locationKV);
                if (id != -1) {
                    returnCount++;
                }
            }
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.d(LOG_TAG, "Error while trying to bulk insert location points");
        } finally {
            db.endTransaction();
        }
        return returnCount;
    }

    public long insertOrUpdateUser(User user) {
        SQLiteDatabase db = getWritableDatabase();
        long userId = -1;

        db.beginTransaction();
        try {
            ContentValues userKV = user.getContentValues();
            // This assumes usernames are unique
            int rows = db.update(UserEntry.TABLE_NAME, userKV, UserEntry.COLUMN_NAME + "= ?", new String[]{user.getUsername()});

            // Check if update succeeded
            if (rows == 1) {
                userId = getUserId(user);
            } else {
                // user with this userName did not already exist, so insert new user
                userId = db.insertOrThrow(UserEntry.TABLE_NAME, null, userKV);
                db.setTransactionSuccessful();
            }
        } catch (Exception e) {
            Log.d(LOG_TAG, "Error while trying to insert or update user");
        } finally {
            db.endTransaction();
        }
        user.setId(userId);
        return userId;
    }

    public long getUserId(User user) {
        SQLiteDatabase db = getWritableDatabase();
        long userId = -1;
        String usersSelectQuery = String.format("SELECT %s FROM %s WHERE %s = ?",
                UserEntry._ID, UserEntry.TABLE_NAME, UserEntry.COLUMN_NAME);
        Cursor cursor = db.rawQuery(usersSelectQuery, new String[]{String.valueOf(user.getUsername())});
        try {
            if (cursor.moveToFirst()) {
                userId = cursor.getInt(0);
            }
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        user.setId(userId);
        return userId;
    }
}
