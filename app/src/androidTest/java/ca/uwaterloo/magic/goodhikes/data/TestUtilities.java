/*------------------------------------------------------------------------------
 *   Authors: Slavik, George, Thao, Chelsea
 *   Copyright: (c) 2016 Team Magic
 *
 *   This file is part of GoodHikes.
 *
 *   GoodHikes is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   GoodHikes is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with GoodHikes.  If not, see <http://www.gnu.org/licenses/>.
 */
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
            assertEquals("Value '" + valueCursor.getString(idx) +
                    "' did not match the expected value '" +
                    expectedValue + "'. " + error, expectedValue, valueCursor.getString(idx));
        }
    }

    static ContentValues createUserValues() {
        ContentValues userValues = new ContentValues();
        userValues.put(RoutesContract.UserEntry.COLUMN_USERNAME, "dude");
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
        SQLiteDatabase db = RoutesDatabaseManager.getInstance(context).getWritableDatabase();

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
