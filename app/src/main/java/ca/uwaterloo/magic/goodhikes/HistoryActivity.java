package ca.uwaterloo.magic.goodhikes;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import java.util.ArrayList;
import java.util.HashMap;

import ca.uwaterloo.magic.goodhikes.data.Route;
import ca.uwaterloo.magic.goodhikes.data.RoutesDatabaseManager;

public class HistoryActivity extends AppCompatActivity {
    private GoodHikesApplication application;
    private RoutesDatabaseManager database;
    private ArrayList<Route> routes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        database = RoutesDatabaseManager.getInstance(this);
        application = (GoodHikesApplication) getApplicationContext();
        routes = database.getAllRoutes(Route.filterByUser(application.currentUser));
    }
}
