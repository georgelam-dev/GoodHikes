package ca.uwaterloo.magic.goodhikes.data;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;

import java.util.HashSet;

public class TestDb extends AndroidTestCase {

    public static final String LOG_TAG = TestDb.class.getSimpleName();

    public void setUp() {
        deleteTheDatabase();
    }

    void deleteTheDatabase() {
        mContext.deleteDatabase(RoutesDatabaseManager.DATABASE_NAME_TEST);
    }

    public void testCreateDb() throws Throwable {
        // build a HashSet of all of the table names we wish to look for
        // Note that there will be another table in the DB that stores the
        // Android metadata (db version information)
        final HashSet<String> tableNameHashSet = new HashSet<String>();
        tableNameHashSet.add(RoutesContract.RouteEntry.TABLE_NAME);
        tableNameHashSet.add(RoutesContract.LocationEntry.TABLE_NAME);

        mContext.deleteDatabase(RoutesDatabaseManager.DATABASE_NAME_TEST);
        SQLiteDatabase db = RoutesDatabaseManager.getInstance(this.mContext).getWritableDatabase();
        assertEquals(true, db.isOpen());

        // have we created the tables we want?
        Cursor c = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null);

        assertTrue("Error: This means that the database has not been created correctly",
                c.moveToFirst());

        // verify that the tables have been created
        do {
            tableNameHashSet.remove(c.getString(0));
        } while( c.moveToNext() );

        // if this fails, it means that your database doesn't contain both the location entry
        // and weather entry tables
        assertTrue("Error: Your database was created without both the route entry and location entry tables",
                tableNameHashSet.isEmpty());

        // now, do our tables contain the correct columns?
        c = db.rawQuery("PRAGMA table_info(" + RoutesContract.RouteEntry.TABLE_NAME + ")", null);
        assertTrue("Error: This means that we were unable to query the database for table information.", c.moveToFirst());

        // 1. Build a HashSet of all of the column names we want to look for in "routes" table
        final HashSet<String> routeColumnHashSet = new HashSet<String>();
        routeColumnHashSet.add(RoutesContract.RouteEntry._ID);
        routeColumnHashSet.add(RoutesContract.RouteEntry.COLUMN_DESCRIPTION);
        routeColumnHashSet.add(RoutesContract.RouteEntry.COLUMN_DATE_START);
        routeColumnHashSet.add(RoutesContract.RouteEntry.COLUMN_DATE_END);

        int columnNameIndex = c.getColumnIndex("name");
        do {
            String columnName = c.getString(columnNameIndex);
            routeColumnHashSet.remove(columnName);
        } while(c.moveToNext());

        // if this fails, it means that your database doesn't contain all of the required 'route columns
        assertTrue("Error: The database doesn't contain all of the required route entry columns",
                routeColumnHashSet.isEmpty());

        // 2. Build a HashSet of all of the column names we want to look for in "location" table
        final HashSet<String> locationColumnHashSet = new HashSet<String>();
        locationColumnHashSet.add(RoutesContract.LocationEntry._ID);
        locationColumnHashSet.add(RoutesContract.LocationEntry.COLUMN_ROUTE_KEY);
        locationColumnHashSet.add(RoutesContract.LocationEntry.COLUMN_DATE);
        locationColumnHashSet.add(RoutesContract.LocationEntry.COLUMN_COORD_LAT);
        locationColumnHashSet.add(RoutesContract.LocationEntry.COLUMN_COORD_LONG);
        locationColumnHashSet.add(RoutesContract.LocationEntry.COLUMN_SPEED);
        locationColumnHashSet.add(RoutesContract.LocationEntry.COLUMN_BEARING);
        locationColumnHashSet.add(RoutesContract.LocationEntry.COLUMN_ACCURACY);


        // now, do our tables contain the correct columns?
        c = db.rawQuery("PRAGMA table_info(" + RoutesContract.LocationEntry.TABLE_NAME + ")", null);
        assertTrue("Error: This means that we were unable to query the database for table information.", c.moveToFirst());

        do {
            String columnName = c.getString(columnNameIndex);
            locationColumnHashSet.remove(columnName);
        } while(c.moveToNext());

        // if this fails, it means that your database doesn't contain all of the required route columns
        assertTrue("Error: The database doesn't contain all of the required location entry columns",
                routeColumnHashSet.isEmpty());
        db.close();
    }

    public void testInsertData() {
        TestUtilities.insertLocationData(mContext);
    }

    public void testRouteTable() {
        insertRoute();
    }

    public long insertRoute() {
        SQLiteDatabase db = RoutesDatabaseManager.getInstance(this.mContext).getWritableDatabase();

        ContentValues userRecordValues = TestUtilities.createUserValues();
        long userRowId = db.insert(RoutesContract.UserEntry.TABLE_NAME, null, userRecordValues);
        assertTrue(userRowId != -1);

        ContentValues routeRecordValues = TestUtilities.createRouteValues(userRowId);
        long routeRowId = db.insert(RoutesContract.RouteEntry.TABLE_NAME, null, routeRecordValues);
        assertTrue(routeRowId != -1);

        Cursor cursor = db.query(
                RoutesContract.RouteEntry.TABLE_NAME,  // Table to Query
                null, // all columns
                null, // Columns for the "where" clause
                null, // Values for the "where" clause
                null, // columns to group by
                null, // columns to filter by row groups
                null // sort order
        );

        assertTrue("Error: No Records returned from route query", cursor.moveToFirst());
        TestUtilities.validateCurrentRecord("Error: Route Query Validation Failed",
                cursor, routeRecordValues);

        // Move the cursor to demonstrate that there is only one record in the database
        assertFalse( "Error: More than one record returned from location query",
                cursor.moveToNext() );

        cursor.close();
        db.close();
        return routeRowId;
    }

    public void testLocationTable() {
        long routeRowId = insertRoute();
        assertFalse("Error: Route Not Inserted Correctly", routeRowId == -1L);

        SQLiteDatabase db = RoutesDatabaseManager.getInstance(this.mContext).getWritableDatabase();
        ContentValues locationRecordValues = TestUtilities.createUWaterlooLocationValues(routeRowId);
        long locationRowId = db.insert(RoutesContract.LocationEntry.TABLE_NAME, null, locationRecordValues);
        assertTrue(locationRowId != -1);

        Cursor locationCursor = db.query(
                RoutesContract.LocationEntry.TABLE_NAME,  // Table to Query
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null, // columns to group by
                null, // columns to filter by row groups
                null  // sort order
        );

        assertTrue("Error: No Records returned from location query", locationCursor.moveToFirst());
        TestUtilities.validateCurrentRecord("testInsertReadDb locationEntry failed to validate",
                locationCursor, locationRecordValues);

        assertFalse( "Error: More than one record returned from weather query",
                locationCursor.moveToNext() );
        locationCursor.close();
        db.close();
    }
}
