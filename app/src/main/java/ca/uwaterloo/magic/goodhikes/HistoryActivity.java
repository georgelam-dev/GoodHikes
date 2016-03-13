package ca.uwaterloo.magic.goodhikes;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;

import ca.uwaterloo.magic.goodhikes.data.Route;
import ca.uwaterloo.magic.goodhikes.data.RoutesContract.RouteEntry;
import ca.uwaterloo.magic.goodhikes.data.RoutesDatabaseManager;

public class HistoryActivity extends AppCompatActivity {
    private GoodHikesApplication application;
    private RoutesDatabaseManager database;
    private ArrayList<Route> routes;
    private RoutesAdapter routesAdapter;
    private ListView mListView;
    private int mPosition = ListView.INVALID_POSITION;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        database = RoutesDatabaseManager.getInstance(this);
        application = (GoodHikesApplication) getApplicationContext();
        routes = database.getAllRoutes(Route.filterByUser(application.currentUser));
        routesAdapter = new RoutesAdapter(this, routes);
        View rootView = findViewById(android.R.id.content);

        mListView = (ListView) rootView.findViewById(R.id.routes_list);
        mListView.setAdapter(routesAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                Route route = (Route) adapterView.getItemAtPosition(position);
                if (route != null) {
                    Intent intent = new Intent();
                    intent.putExtra(RouteEntry._ID, route.getId());
                    setResult(RESULT_OK, intent);
                    finish();
                }
                mPosition = position;
            }
        });
    }
}
