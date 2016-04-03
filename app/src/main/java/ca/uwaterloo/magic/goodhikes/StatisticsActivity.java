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
package ca.uwaterloo.magic.goodhikes;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.TextView;

import org.json.JSONObject;

import ca.uwaterloo.magic.goodhikes.data.UserManager;

public class StatisticsActivity extends AppCompatActivity {

    private TextView user_name;
    private TextView route_name;
    private TextView start_time;
    private TextView end_time;
    private TextView duration;
    private TextView distance;
    private TextView aver_speed;
    private TextView max_speed;
    private TextView num_waypoints;
    private TextView route_private;
    private UserManager userManager;
    protected static final String LOG_TAG = "StatisticsActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        /** Define variables **/
        userManager = new UserManager(getApplicationContext());
        user_name = (TextView) findViewById(R.id.owner_name);
        route_name = (TextView) findViewById(R.id.stat_route_name);
        start_time = (TextView) findViewById(R.id.start_time);
        end_time = (TextView) findViewById(R.id.last_time);
        duration = (TextView) findViewById(R.id.duration);
        distance = (TextView) findViewById(R.id.total_distance);
        aver_speed = (TextView) findViewById(R.id.aver_speed);
        max_speed = (TextView) findViewById(R.id.max_speed);
        num_waypoints = (TextView) findViewById(R.id.num_waypoints);
        route_private = (TextView) findViewById(R.id.route_private);

        try {
            Log.d(LOG_TAG, "in try");
            JSONObject route = new JSONObject(getIntent().getStringExtra("current_route"));
            Log.d(LOG_TAG, "received JSON");
            user_name.setText(route.getString("user_name"));
            route_name.setText(route.getString("route_name"));
            start_time.setText(route.getString("start_time"));
            end_time.setText(route.getString("end_time"));
            duration.setText(route.getString("duration"));
            distance.setText(String.format("%.3f", route.getDouble("distance")));
            aver_speed.setText(String.format("%.2f", route.getDouble("aver_speed")));
            max_speed.setText(String.format("%.2f", route.getDouble("max_speed")));
            num_waypoints.setText(route.getString("num_waypoints"));
            route_private.setText(route.getString("private"));
        } catch (Exception e) {
            Log.e(LOG_TAG, e.getMessage());
        }

    }

}
