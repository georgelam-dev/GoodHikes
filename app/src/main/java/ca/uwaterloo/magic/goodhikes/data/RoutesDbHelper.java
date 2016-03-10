package ca.uwaterloo.magic.goodhikes.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;

import ca.uwaterloo.magic.goodhikes.data.RoutesContract.UserEntry;
import ca.uwaterloo.magic.goodhikes.data.RoutesContract.RouteEntry;
import ca.uwaterloo.magic.goodhikes.data.RoutesContract.LocationEntry;

/**
 * Manages device's database for routes data.
 * Use this guide https://guides.codepath.com/android/Local-Databases-with-SQLiteOpenHelper
 * http://developer.android.com/training/basics/data-storage/databases.html
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
                UserEntry.COLUMN_USERNAME     + " TEXT NOT NULL " +
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
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + UserEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }

    public void insertRoute(Route route) {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            long userId = insertOrUpdateUser(route.getUser());
            ContentValues routeCV = route.toContentValues();
            routeCV.put(RouteEntry.COLUMN_USER_KEY, userId);
            long routeId = db.insertOrThrow(RouteEntry.TABLE_NAME, null, routeCV);
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
            for(LocationPoint location : route.getTrace()){
                ContentValues locationCV = location.toContentValues();
                locationCV.put(LocationEntry.COLUMN_ROUTE_KEY, route.getId());
                id = db.insert(LocationEntry.TABLE_NAME, null, locationCV);
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
            ContentValues userCV = user.toContentValues();
            // This assumes usernames are unique
            int rows = db.update(UserEntry.TABLE_NAME, userCV, UserEntry.COLUMN_USERNAME + "= ?", new String[]{user.getUsername()});

            // Check if update succeeded
            if (rows == 1) {
                userId = getUserId(user);
            } else {
                // user with this userName did not already exist, so insert new user
                userId = db.insertOrThrow(UserEntry.TABLE_NAME, null, userCV);
            }
            db.setTransactionSuccessful();
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
                UserEntry._ID, UserEntry.TABLE_NAME, UserEntry.COLUMN_USERNAME);
        Cursor cursor = db.rawQuery(usersSelectQuery, new String[]{user.getUsername()});
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

    public User getUser(long id) {
        User user=new User();
        SQLiteDatabase db = getWritableDatabase();
        String ROUTES_SELECT_QUERY =
                String.format("SELECT * FROM %s WHERE %s = ?",
                        UserEntry.TABLE_NAME, UserEntry._ID);
        Cursor cursor = db.rawQuery(ROUTES_SELECT_QUERY, new String[]{String.valueOf(id)});
        try {
            if (cursor.moveToFirst()) {
                user = User.fromDBCursor(cursor);
            }
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        return user;
    }

    /**
     * SELECT routes._id FROM routes
     * INNER JOIN users ON routes.user_id = users._id
     * WHERE users.name = ?
     * ORDER BY routes._id DESC
     */
    public long getLatestRouteId(User user) {
        SQLiteDatabase db = getWritableDatabase();
        long routeId = -1;
        String routeSelectQuery = String.format("SELECT %s.%s FROM %s INNER JOIN %s ON %s.%s = %s.%s " +
                        "WHERE %s.%s = ? ORDER BY %s.%s DESC",
                RouteEntry.TABLE_NAME, RouteEntry._ID, RouteEntry.TABLE_NAME, UserEntry.TABLE_NAME,
                RouteEntry.TABLE_NAME, RouteEntry.COLUMN_USER_KEY, UserEntry.TABLE_NAME, UserEntry._ID,
                UserEntry.TABLE_NAME, UserEntry.COLUMN_USERNAME, RouteEntry.TABLE_NAME, RouteEntry._ID);

        Cursor cursor = db.rawQuery(routeSelectQuery, new String[]{user.getUsername()});
        try {
            if (cursor.moveToFirst()) {
                routeId = cursor.getInt(0);
            }
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        return routeId;
    }

    public Route getLatestRoute(User user) {
        long latestRouteId = getLatestRouteId(user);
        return getRoute(latestRouteId);
    }

    /**
     * SELECT routes.*, users.name AS username FROM routes
     * INNER JOIN users ON routes.user_id = users._id
     * WHERE routes._id = ?
     */
    public Route getRoute(long routeId) {
        Route route = new Route();
        SQLiteDatabase db = getWritableDatabase();

        String routeSelectQuery = String.format(
                "SELECT %s.*, %s.%s AS %s FROM %s " +
                "INNER JOIN %s ON %s.%s = %s.%s " +
                "WHERE %s.%s = ?",
                RouteEntry.TABLE_NAME, UserEntry.TABLE_NAME, UserEntry.COLUMN_USERNAME, UserEntry.COLUMN_USERNAME_ALIAS, RouteEntry.TABLE_NAME,
                UserEntry.TABLE_NAME, RouteEntry.TABLE_NAME, RouteEntry.COLUMN_USER_KEY, UserEntry.TABLE_NAME, UserEntry._ID,
                RouteEntry.TABLE_NAME, RouteEntry._ID);

        Cursor cursor = db.rawQuery(routeSelectQuery, new String[]{String.valueOf(routeId)});
        try {
            if (cursor.moveToFirst()) {
                route = Route.fromDBCursor(cursor, true);
                route.setTrace(getLocations(route.getId()));
            }
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        return route;
    }

    /**
     * SELECT routes.*, users.name AS username FROM routes
     * INNER JOIN users ON routes.user_id = users._id
     * ORDER BY routes._id DESC
     */
    public ArrayList<Route> getAllRoutes() {
        String routeSelectQuery = String.format(
                "SELECT %s.*, %s.%s AS %s FROM %s " +
                "INNER JOIN %s ON %s.%s = %s.%s " +
                "ORDER BY %s.%s DESC",
                RouteEntry.TABLE_NAME, UserEntry.TABLE_NAME, UserEntry.COLUMN_USERNAME, UserEntry.COLUMN_USERNAME_ALIAS, RouteEntry.TABLE_NAME,
                UserEntry.TABLE_NAME, RouteEntry.TABLE_NAME, RouteEntry.COLUMN_USER_KEY, UserEntry.TABLE_NAME, UserEntry._ID,
                RouteEntry.TABLE_NAME, RouteEntry._ID);

        ArrayList<Route> routes = new ArrayList<>();
        Route route;
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(routeSelectQuery, null);
        try {
            if (cursor.moveToFirst()) {
                do {
                    route = Route.fromDBCursor(cursor, true);
                    route.setTrace(getLocations(route.getId()));
                    routes.add(route);
                } while(cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.d(LOG_TAG, "Error while trying to get routes from database");
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        return routes;
    }

    // SELECT * FROM locations WHERE route_id = ? ORDER BY _id ASC
    public ArrayList<LocationPoint> getLocations(long routeId) {
        ArrayList<LocationPoint> locations = new ArrayList<>();
        SQLiteDatabase db = getWritableDatabase();
        String LOCATIONS_SELECT_QUERY =
                String.format("SELECT * FROM %s WHERE %s = ? ORDER BY %s ASC",
                        LocationEntry.TABLE_NAME, LocationEntry.COLUMN_ROUTE_KEY, LocationEntry._ID);
        Cursor cursor = db.rawQuery(LOCATIONS_SELECT_QUERY, new String[]{String.valueOf(routeId)});
        try {
            if (cursor.moveToFirst()) {
                do {
                    locations.add(LocationPoint.fromDBCursor(cursor));
                } while(cursor.moveToNext());

            }
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        return locations;
    }
}
