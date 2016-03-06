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

    static ContentValues createRouteValues() {
        ContentValues routeValues = new ContentValues();
        routeValues.put(RoutesContract.RouteEntry.COLUMN_DESCRIPTION, "test route");
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
        return testValues;
    }

    static long insertLocationData(Context context) {
        RoutesDbHelper dbHelper = new RoutesDbHelper(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues routeRecord = TestUtilities.createRouteValues();
        long routeRowId = db.insert(RoutesContract.RouteEntry.TABLE_NAME, null, routeRecord);
        assertTrue("Error: Failure to insert a test route", routeRowId != -1);

        ContentValues locationRecord = TestUtilities.createUWaterlooLocationValues(routeRowId);
        long locationRowId = db.insert(RoutesContract.LocationEntry.TABLE_NAME, null, locationRecord);
        assertTrue("Error: Failure to insert a test location", locationRowId != -1);

        return locationRowId;
    }
}
