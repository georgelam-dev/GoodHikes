package ca.uwaterloo.magic.goodhikes.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;

import java.util.Map;
import java.util.Set;

public class TestUtilities extends AndroidTestCase {
    static final long TEST_DATE = 1457302006L;

    static void validateCurrentRecord(String error, Cursor valueCursor, ContentValues expectedValues) {
        Set<Map.Entry<String, Object>> valueSet = expectedValues.valueSet();
        for (Map.Entry<String, Object> entry : valueSet) {
            String columnName = entry.getKey();
            int idx = valueCursor.getColumnIndex(columnName);
            assertFalse("Column '" + columnName + "' not found. " + error, idx == -1);
            String expectedValue = entry.getValue().toString();
            assertEquals("Value '" + entry.getValue().toString() +
                    "' did not match the expected value '" +
                    expectedValue + "'. " + error, expectedValue, valueCursor.getString(idx));
        }
    }

    static ContentValues createUserValues() {
        ContentValues userValues = new ContentValues();
        userValues.put(RoutesContract.UserEntry.COLUMN_NAME, "dude");
        return userValues;
    }

    static ContentValues createRouteValues(long userRowId) {
        ContentValues routeValues = new ContentValues();
        routeValues.put(RoutesContract.RouteEntry.COLUMN_DESCRIPTION, "test route");
        routeValues.put(RoutesContract.RouteEntry.COLUMN_USER_KEY, userRowId);
        routeValues.put(RoutesContract.RouteEntry.COLUMN_DATE_START, TEST_DATE);
        routeValues.put(RoutesContract.RouteEntry.COLUMN_DATE_END, TEST_DATE);
        return routeValues;
    }

    static ContentValues createUWaterlooLocationValues(long routeRowId) {
        ContentValues testValues = new ContentValues();
        testValues.put(RoutesContract.LocationEntry.COLUMN_ROUTE_KEY, routeRowId);
        testValues.put(RoutesContract.LocationEntry.COLUMN_DATE, TEST_DATE);
        testValues.put(RoutesContract.LocationEntry.COLUMN_COORD_LAT, 43.4726);
        testValues.put(RoutesContract.LocationEntry.COLUMN_COORD_LONG, -80.5418);
        testValues.put(RoutesContract.LocationEntry.COLUMN_SPEED, 3.5);
        testValues.put(RoutesContract.LocationEntry.COLUMN_BEARING, 158);
        testValues.put(RoutesContract.LocationEntry.COLUMN_ACCURACY, 12.249);
        return testValues;
    }

    static long insertLocationData(Context context) {
        SQLiteDatabase db = RoutesDbHelper.getInstance(context).getWritableDatabase();

        ContentValues userRecord = TestUtilities.createUserValues();
        long userRowId = db.insert(RoutesContract.UserEntry.TABLE_NAME, null, userRecord);
        assertTrue("Error: Failure to insert a test route", userRowId != -1);

        ContentValues routeRecord = TestUtilities.createRouteValues(userRowId);
        long routeRowId = db.insert(RoutesContract.RouteEntry.TABLE_NAME, null, routeRecord);
        assertTrue("Error: Failure to insert a test route", routeRowId != -1);

        ContentValues locationRecord = TestUtilities.createUWaterlooLocationValues(routeRowId);
        long locationRowId = db.insert(RoutesContract.LocationEntry.TABLE_NAME, null, locationRecord);
        assertTrue("Error: Failure to insert a test location", locationRowId != -1);

        db.close();
        return locationRowId;
    }
}
