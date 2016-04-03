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

import android.provider.BaseColumns;

/**
 * Defines table and column names for the routes database.
 */
public class RoutesContract {
    /*
        Inner class that defines the contents of the location table
     */
    public static final class RouteEntry implements BaseColumns {
        public static final String TABLE_NAME = "routes";

        // Column with the foreign key into the user table.
        public static final String COLUMN_USER_KEY = "user_id";
        public static final String COLUMN_USERNAME = "user_name";
        public static final String COLUMN_DESCRIPTION = "description";

        // Date start/end, stored as long in milliseconds since the epoch
        public static final String COLUMN_DATE_START = "date_start";
        public static final String COLUMN_DATE_END = "date_end";

        public static final String COLUMN_PRIVATE = "private";
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

  /*  public static final class UserEntry implements BaseColumns {
        public static final String TABLE_NAME = "users";
        public static final String COLUMN_USERNAME = "name";

        //stores a 1/0 yes/no flag, if this a user currently logged in on this device
        public static final String COLUMN_CURRENT = "current";

        //used only as alias to column "name" when table "users" is joined with other tables:
        //SELECT routes.*, users.name AS username FROM routes
        public static final String COLUMN_USERNAME_ALIAS = "username";
    }*/

    public static final class MilestoneEntry implements BaseColumns {
        public static final String TABLE_NAME = "milestones";

        // Column with the foreign key into the routes table.
        public static final String COLUMN_ROUTE_KEY = "route_id";

        public static final String COLUMN_COORD_LAT = "coordinates_lat";
        public static final String COLUMN_COORD_LONG = "coordinates_long";

        public static final String COLUMN_NOTE = "note";
        public static final String COLUMN_IMAGE = "image";
    }
}
