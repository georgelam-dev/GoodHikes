package ca.uwaterloo.magic.goodhikes;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import java.util.ArrayList;

import ca.uwaterloo.magic.goodhikes.data.Route;
import ca.uwaterloo.magic.goodhikes.data.RoutesDatabaseManager;

public class HistoryActivity extends AppCompatActivity {
    private RoutesDatabaseManager database;
    private ArrayList<Route> routes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        database = RoutesDatabaseManager.getInstance(this);
        routes = database.getAllRoutes();
    }
}
