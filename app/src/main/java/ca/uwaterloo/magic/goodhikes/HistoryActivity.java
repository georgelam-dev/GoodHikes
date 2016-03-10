package ca.uwaterloo.magic.goodhikes;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import java.util.ArrayList;

import ca.uwaterloo.magic.goodhikes.data.Route;
import ca.uwaterloo.magic.goodhikes.data.RoutesDbHelper;

public class HistoryActivity extends AppCompatActivity {
    private RoutesDbHelper database;
    private ArrayList<Route> routes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        database = RoutesDbHelper.getInstance(this);
        routes = database.getAllRoutes();
    }
}
