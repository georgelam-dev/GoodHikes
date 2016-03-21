package ca.uwaterloo.magic.goodhikes.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import ca.uwaterloo.magic.goodhikes.data.RoutesContract.LocationEntry;
import ca.uwaterloo.magic.goodhikes.data.RoutesContract.MilestoneEntry;
import ca.uwaterloo.magic.goodhikes.data.RoutesContract.RouteEntry;

/**
 * Manages device's database for routes data.
 * Use this guide https://guides.codepath.com/android/Local-Databases-with-SQLiteOpenHelper
 * http://developer.android.com/training/basics/data-storage/databases.html
 * to add data handling methods
 */
public class RoutesDatabaseManager extends SQLiteOpenHelper {
    private static RoutesDatabaseManager sInstance;
    protected static final String LOG_TAG = "RoutesDatabaseManager";

    // If you change the database schema, you must increment the database version.
    private static final int DATABASE_VERSION = 5;

    static final String DATABASE_NAME = "routes.db";
    static final String DATABASE_NAME_TEST = "routes_test.db";

    public static synchronized RoutesDatabaseManager getInstance(Context context) {
        // Use the application context, which will ensure that you
        // don't accidentally leak an Activity's context.
        // http://android-developers.blogspot.ca/2009/01/avoiding-memory-leaks.html
        if (sInstance == null) {
            sInstance = new RoutesDatabaseManager(context.getApplicationContext());
        }
        return sInstance;
    }

    /**
     * Constructor should be private to prevent direct instantiation.
     * Make a call to the static method "getInstance()" instead.
     */
    private RoutesDatabaseManager(Context context) {
        super(context, (isJUnitTest() ? DATABASE_NAME_TEST : DATABASE_NAME), null, DATABASE_VERSION);
    }

    private static boolean isJUnitTest() {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        List<StackTraceElement> list = Arrays.asList(stackTrace);
        for (StackTraceElement element : list) {
            if (element.getClassName().startsWith("junit.")) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
/*        final String SQL_CREATE_USER_TABLE = "CREATE TABLE " + UserEntry.TABLE_NAME + " (" +
                UserEntry._ID                 + " TEXT PRIMARY KEY," +
                UserEntry.COLUMN_USERNAME     + " TEXT NOT NULL, " +
                UserEntry.COLUMN_CURRENT      + " INTEGER DEFAULT 0 " +
                " );";
*/
        final String SQL_CREATE_ROUTE_TABLE = "CREATE TABLE " + RouteEntry.TABLE_NAME + " (" +
                RouteEntry._ID                 + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                // the ID of the user entry associated with this route
                RouteEntry.COLUMN_USER_KEY     + " TEXT NOT NULL, " +
                RouteEntry.COLUMN_USERNAME     + " TEXT NOT NULL, " +
                RouteEntry.COLUMN_DESCRIPTION  + " TEXT NOT NULL, " +
                RouteEntry.COLUMN_DATE_START   + " INTEGER NOT NULL, " +
                RouteEntry.COLUMN_DATE_END     + " INTEGER NOT NULL, " +
                RouteEntry.COLUMN_PRIVATE      + " INTEGER NOT NULL DEFAULT 0 " +
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

        final String SQL_CREATE_MILESTONE_TABLE = "CREATE TABLE " + MilestoneEntry.TABLE_NAME + " (" +
                MilestoneEntry._ID               + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                // the ID of the route entry associated with this milestone data
                MilestoneEntry.COLUMN_ROUTE_KEY  + " INTEGER NOT NULL, " +
                MilestoneEntry.COLUMN_COORD_LAT  + " REAL NOT NULL, " +
                MilestoneEntry.COLUMN_COORD_LONG + " REAL NOT NULL, " +
                MilestoneEntry.COLUMN_NOTE      + " TEXT, " +
                MilestoneEntry.COLUMN_IMAGE    + " BLOB, " +


                " FOREIGN KEY (" + MilestoneEntry.COLUMN_ROUTE_KEY + ") REFERENCES " +
                RouteEntry.TABLE_NAME + " (" + RouteEntry._ID + ") " +
                " );";

        //sqLiteDatabase.execSQL(SQL_CREATE_USER_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_ROUTE_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_LOCATION_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_MILESTONE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        // This database upgrade policy is to simply to discard the data and start over.
        // Note that this only fires if you change the version number for your database.
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + RouteEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + LocationEntry.TABLE_NAME);
        //sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + RoutesContract.UserEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + MilestoneEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }

    public void insertRoute(Route route, User user) {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            //String userId = insertOrUpdateUser(route.getUser());
            String userId = user.getId();
            ContentValues routeCV = route.toContentValues();
            routeCV.put(RouteEntry.COLUMN_USER_KEY, userId);
            routeCV.put(RouteEntry.COLUMN_USERNAME, user.getUsername());
            long routeId = db.insertOrThrow(RouteEntry.TABLE_NAME, null, routeCV);
            route.setId(routeId);
            bulkInsertLocationPoints(route);
            bulkInsertMilestones(route);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.d(LOG_TAG, e.getMessage());
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

    public long bulkInsertMilestones(Route route) {
        SQLiteDatabase db = getWritableDatabase();
        int returnCount = 0;
        long id;

        db.beginTransaction();
        try {
            for(Milestone milestone : route.getMilestones()){
                ContentValues milestoneCV = milestone.toContentValues();
                milestoneCV.put(MilestoneEntry.COLUMN_ROUTE_KEY, route.getId());
                id = db.insert(MilestoneEntry.TABLE_NAME, null, milestoneCV);
                if (id != -1) {
                    returnCount++;
                }
            }
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.d(LOG_TAG, "Error while trying to bulk insert milestones");
        } finally {
            db.endTransaction();
        }
        return returnCount;
    }

/*    public String insertOrUpdateUser(User user) {
        SQLiteDatabase db = getWritableDatabase();
        String userId = "0";

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
*/
/*
    public String getUserId(User user) {
        SQLiteDatabase db = getWritableDatabase();
        String userId = "0";
        String usersSelectQuery = String.format("SELECT %s FROM %s WHERE %s = ?",
                UserEntry._ID, UserEntry.TABLE_NAME, UserEntry.COLUMN_USERNAME);
        Cursor cursor = db.rawQuery(usersSelectQuery, new String[]{user.getUsername()});
        try {
            if (cursor.moveToFirst()) {
                userId = cursor.getString(0);
            }
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        user.setId(userId);
        return userId;
    }
*/
/*
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
*/

    /**
     * SELECT routes._id FROM routes
     * INNER JOIN users ON routes.user_id = users._id
     * WHERE users.name = ?
     * ORDER BY routes._id DESC
     */
    public long getLatestRouteId(User user) {
        SQLiteDatabase db = getWritableDatabase();
        long routeId = -1;
        /*String routeSelectQuery = String.format("SELECT %s.%s FROM %s INNER JOIN %s ON %s.%s = %s.%s " +
                        "WHERE %s.%s = ? ORDER BY %s.%s DESC",
                RouteEntry.TABLE_NAME, RouteEntry._ID, RouteEntry.TABLE_NAME, UserEntry.TABLE_NAME,
                RouteEntry.TABLE_NAME, RouteEntry.COLUMN_USER_KEY, UserEntry.TABLE_NAME, UserEntry._ID,
                UserEntry.TABLE_NAME, UserEntry.COLUMN_USERNAME, RouteEntry.TABLE_NAME, RouteEntry._ID);
        */
        String routeSelectQuery = String.format("SELECT %s.%s FROM %s " +
                        "WHERE %s.%s = ? ORDER BY %s.%s DESC",
                RouteEntry.TABLE_NAME, RouteEntry._ID, RouteEntry.TABLE_NAME,
                RouteEntry.TABLE_NAME, RouteEntry.COLUMN_USERNAME, RouteEntry.TABLE_NAME, RouteEntry._ID);
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
        if(routeId==-1){
            route.setId(routeId);
            return route;
        }
        SQLiteDatabase db = getWritableDatabase();

        /*String routeSelectQuery = String.format(
                "SELECT %s.*, %s.%s AS %s FROM %s " +
                "INNER JOIN %s ON %s.%s = %s.%s " +
                "WHERE %s.%s = ?",
                RouteEntry.TABLE_NAME, UserEntry.TABLE_NAME, UserEntry.COLUMN_USERNAME, UserEntry.COLUMN_USERNAME_ALIAS, RouteEntry.TABLE_NAME,
                UserEntry.TABLE_NAME, RouteEntry.TABLE_NAME, RouteEntry.COLUMN_USER_KEY, UserEntry.TABLE_NAME, UserEntry._ID,
                RouteEntry.TABLE_NAME, RouteEntry._ID);
        */
        String routeSelectQuery = String.format(
                "SELECT %s.* FROM %s " +
                        "WHERE %s.%s = ?",
                RouteEntry.TABLE_NAME, RouteEntry.TABLE_NAME,
                RouteEntry.TABLE_NAME, RouteEntry._ID);
        Cursor cursor = db.rawQuery(routeSelectQuery, new String[]{String.valueOf(routeId)});
        try {
            if (cursor.moveToFirst()) {
                route = Route.fromDBCursor(cursor, true);
                route.setTrace(getLocations(route.getId()));
                route.setMilestones(getMilestones(route.getId()));
            }
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        return route;
    }

    public ArrayList<Route> getAllRoutes() {
        return getAllRoutes(new HashMap());
    }

    /**
     * SELECT routes.*, users.name AS username FROM routes
     * INNER JOIN users ON routes.user_id = users._id
     * WHERE username = ?
     * ORDER BY routes._id DESC
     */
    public ArrayList<Route> getAllRoutes(HashMap options) {
        /*String routeSelectQuery = String.format(
                "SELECT %s.*, %s.%s AS %s FROM %s " +
                "INNER JOIN %s ON %s.%s = %s.%s ",
                RouteEntry.TABLE_NAME, UserEntry.TABLE_NAME, UserEntry.COLUMN_USERNAME,
                UserEntry.COLUMN_USERNAME_ALIAS, RouteEntry.TABLE_NAME,
                UserEntry.TABLE_NAME, RouteEntry.TABLE_NAME, RouteEntry.COLUMN_USER_KEY,
                UserEntry.TABLE_NAME, UserEntry._ID);
        */
        String routeSelectQuery = String.format(
                "SELECT %s.* FROM %s ",
                RouteEntry.TABLE_NAME, RouteEntry.TABLE_NAME);

        String[] queryParams = null;
        /*if(options.get(UserEntry.COLUMN_USERNAME_ALIAS)!=null){
            String whereClause = String.format("WHERE %s = ? ", UserEntry.COLUMN_USERNAME_ALIAS);
            routeSelectQuery+=whereClause;
            queryParams = new String[]{String.valueOf(options.get(UserEntry.COLUMN_USERNAME_ALIAS))};
        }*/

        String orderClause = String.format("ORDER BY %s.%s DESC ", RouteEntry.TABLE_NAME, RouteEntry._ID);
        routeSelectQuery+=orderClause;

        ArrayList<Route> routes = new ArrayList<Route>();
        Route route;
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(routeSelectQuery, queryParams);
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

    // SELECT * FROM milestones WHERE route_id = ?
    public ArrayList<Milestone> getMilestones(long routeId) {
        ArrayList<Milestone> milestones = new ArrayList<>();
        SQLiteDatabase db = getWritableDatabase();
        String MILESTONES_SELECT_QUERY =
                String.format("SELECT * FROM %s WHERE %s = ?",
                        MilestoneEntry.TABLE_NAME, MilestoneEntry.COLUMN_ROUTE_KEY);
        Cursor cursor = db.rawQuery(MILESTONES_SELECT_QUERY, new String[]{String.valueOf(routeId)});
        try {
            if (cursor.moveToFirst()) {
                do {
                    milestones.add(Milestone.fromDBCursor(cursor));
                } while(cursor.moveToNext());

            }
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        return milestones;
    }

    public int deleteRoute(long routeId) {
        deleteLocations(routeId);
        deleteMilestones(routeId);
        SQLiteDatabase db = getWritableDatabase();
        String whereQuery = RouteEntry._ID+" = ?";
        return db.delete(RouteEntry.TABLE_NAME, whereQuery, new String[]{String.valueOf(routeId)});
    }

    public int deleteLocations(long routeId) {
        int result=-1;
        SQLiteDatabase db = getWritableDatabase();
        String whereQuery = LocationEntry.COLUMN_ROUTE_KEY+" = ?";
        result = db.delete(LocationEntry.TABLE_NAME, whereQuery, new String[]{String.valueOf(routeId)});
        return result;
    }

    public int deleteMilestones(long routeId) {
        int result=-1;
        SQLiteDatabase db = getWritableDatabase();
        String whereQuery = MilestoneEntry.COLUMN_ROUTE_KEY+" = ?";
        result = db.delete(MilestoneEntry.TABLE_NAME, whereQuery, new String[]{String.valueOf(routeId)});
        return result;
    }
}
