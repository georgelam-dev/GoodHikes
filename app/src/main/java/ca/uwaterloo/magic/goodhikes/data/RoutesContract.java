package ca.uwaterloo.magic.goodhikes.data;

import android.provider.BaseColumns;
import android.text.format.Time;

/**
 * Defines table and column names for the routes database.
 */
public class RoutesContract {

    // To make it easy to query for the exact date, we normalize all dates that go into
    // the database to the start of the the Julian day at UTC.
    public static long normalizeDate(long startDate) {
        // normalize the start date to the beginning of the (UTC) day
        Time time = new Time();
        time.set(startDate);
        int julianDay = Time.getJulianDay(startDate, time.gmtoff);
        return time.setJulianDay(julianDay);
    }

    /*
        Inner class that defines the contents of the location table
     */
    public static final class RouteEntry implements BaseColumns {
        public static final String TABLE_NAME = "routes";

        // Column with the foreign key into the user table.
        public static final String COLUMN_USER_KEY = "user_id";
        public static final String COLUMN_DESCRIPTION = "description";

        // Date start/end, stored as long in milliseconds since the epoch
        public static final String COLUMN_DATE_START = "date_start";
        public static final String COLUMN_DATE_END = "date_end";
    }

    /* Stores the following entities:
     * http://developer.android.com/reference/android/location/Location.html
     */
    public static final class LocationEntry implements BaseColumns {
        public static final String TABLE_NAME = "locations";
        // Column with the foreign key into the routes table.
        public static final String COLUMN_ROUTE_KEY = "route_id";

        // UTC datetime, stored as long in milliseconds since the epoch (Jan 1, 1970)
        public static final String COLUMN_DATE = "datetime";

        public static final String COLUMN_COORD_LAT = "coordinates_lat";
        public static final String COLUMN_COORD_LONG = "coordinates_long";

        // meters/second over ground (float)
        public static final String COLUMN_SPEED = "speed";

        //direction in degrees, in the range (0.0, 360.0] (float)
        public static final String COLUMN_BEARING = "bearing";

        //estimated accuracy of this location in meters (float)
        public static final String COLUMN_ACCURACY = "accuracy";
    }

    public static final class UserEntry implements BaseColumns {
        public static final String TABLE_NAME = "users";
        public static final String COLUMN_NAME = "name";
    }
}
