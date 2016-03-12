package ca.uwaterloo.magic.goodhikes;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import ca.uwaterloo.magic.goodhikes.data.Route;
import ca.uwaterloo.magic.goodhikes.data.RoutesDatabaseManager;

public class MapsActivity extends AppCompatActivity
        implements
        OnMapReadyCallback,
        GoogleMap.OnMyLocationButtonClickListener{

    protected GoogleMap mMap;
    private RoutesDatabaseManager database;
    private GoodHikesApplication application;
    protected static final String LOG_TAG = "MapsActivity";
    private GPSUpdatesReceiver mGPSUpdatesReceiver;
    private IntentFilter mFilter;
    private ServiceConnection mConnection;
    private GPSLoggingService mLoggingService;
    private ImageButton mGPSTrackingButton, mSettingsButton, mHistoryButton;
    private Route latestRoute;
    private Polyline visualRouteTrace;
    private Marker initRoutePointMarker, lastRoutePointMarker;

    private final BroadcastReceiver logoutReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            stopGPSLoggingService();
            finish();
        }
    };

    /**
     * onCreate() is called only once for Activity, whereas onStart() - each time
     * appActivity is hidden from screen (user uses other apps), and then App is activated again.
     *
     * Using startService() overrides the default service lifetime that is managed by
     * bindService(Intent, ServiceConnection, int): it requires the service to remain running
     * until stopService(Intent) is called, regardless of whether any clients are connected to it.
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        database = RoutesDatabaseManager.getInstance(this);
        application = (GoodHikesApplication) getApplicationContext();

        //Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //Create and attach listeners to buttons
        mSettingsButton = (ImageButton) findViewById(R.id.settings_button);
        mHistoryButton = (ImageButton) findViewById(R.id.history_button);
        mGPSTrackingButton = (ImageButton) findViewById(R.id.gps_tracking_control_button);
        attachUICallbacks();
    }

    //updateMapType changes the map type when the setting is changed.

    public void updateMapType() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        int map_type = Integer.parseInt(prefs.getString
                (getString(R.string.map_pref),
                        getString(R.string.map_type_default)));
        mMap.setMapType(map_type);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mFilter = new IntentFilter(GPSLoggingService.locationUpdateCommand);
        mGPSUpdatesReceiver = new GPSUpdatesReceiver();
        mConnection = new GPSLoggingServiceConnection();
        latestRoute = database.getLatestRoute(application.currentUser);
        clearMap();
        LocalBroadcastManager.getInstance(this).registerReceiver(mGPSUpdatesReceiver, mFilter);
        registerReceiver(logoutReceiver, new IntentFilter("logout"));
        startService(new Intent(this, GPSLoggingService.class));
        Log.d(LOG_TAG, "Thread: " + Thread.currentThread().getId() + "; MapActivity started");
    }

    @Override
    protected void onStop() {
        super.onStop();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mGPSUpdatesReceiver);
        unregisterReceiver(logoutReceiver);
    }

    @Override
    public void onResume() {
        super.onResume();
        Intent intent = new Intent(this, GPSLoggingService.class);
        bindService(intent, mConnection, BIND_AUTO_CREATE);
        Log.d(LOG_TAG, "Thread: " + Thread.currentThread().getId() + "; Binding to service");
        if (mMap != null) {
            updateMapType();
        }
    }

    /*
    * Unbinding GPS service in onPause, so that there are always matching pairs of bind/unbind calls.
    * */

    @Override
    protected void onPause() {
        super.onPause();
        unbindService(mConnection);
        Log.d(LOG_TAG, "Thread: " + Thread.currentThread().getId() + "; Unbinding from GPS service");
    }

    /*
    * When GPSLoggingService starts it has it's own lifecycle independent of the Activity that started it.
    * This means that GPSLoggingService will be running even if user switches to another app for a bit,
    * or even simply rotates the screen.
    * GPS tracking logging runs in a separate LoopingThread within GPSLoggingService,
    * and tracking should continue running in the background, even if the app is put in the background.
    * */

    public void stopGPSLoggingService(){
        stopService(new Intent(this, GPSLoggingService.class));
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        UiSettings settings = mMap.getUiSettings();
        settings.setCompassEnabled(true);
        settings.setMapToolbarEnabled(true);
        settings.setScrollGesturesEnabled(true);
        updateMapType();
        enableMyLocation();
        Log.d(LOG_TAG, "Thread: " + Thread.currentThread().getId() + "; onMapReady()");
    }

    @Override
    public boolean onMyLocationButtonClick() {
        Toast.makeText(this, "MyLocation button clicked", Toast.LENGTH_SHORT).show();
        // Return false so that we don't consume the event and the default behavior still occurs
        // (the camera animates to the user's current position).
        return false;
    }

    private void enableMyLocation() {
        if (mMap != null) {
            mMap.setMyLocationEnabled(true);
        }
    }

    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();
    }

    private void updateLocation(Location location){
        mLoggingService.updateGPSfrequency();
        LatLng coordinates = new LatLng(location.getLatitude(), location.getLongitude());
        mMap.moveCamera(CameraUpdateFactory.newLatLng(coordinates));
//        mMap.animateCamera(CameraUpdateFactory.zoomTo(15.0f));
    }

    public class GPSUpdatesReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Location location;
            Bundle extras = intent.getExtras();
            if(extras != null) {
                location = (Location) intent.getExtras().get(GPSLoggingService.locationUpdateCommand);
                updateLocation(location);
            }
            drawTrace(mLoggingService.currentRoute);
            Log.d(LOG_TAG, "Thread: "+Thread.currentThread().getId() + "; Location update received by UI");
        }

    }

    public class GPSLoggingServiceConnection implements ServiceConnection {
        @Override
        public void onServiceConnected(ComponentName className, IBinder binder) {
            mLoggingService = ((GPSLoggingService.LoggingBinder) binder).getService();
            setTrackingButtonIcon();
//            mLoggingService.broadcastLastKnownLocation();

            Log.d(LOG_TAG, "Thread: " + Thread.currentThread().getId() + "; GPS Logging service connected");
            if (!mLoggingService.isTrackingActive()) {
                clearMap();
                drawTrace(latestRoute);
                moveMapCameraToRoute(latestRoute);
            }
        }
        public void onServiceDisconnected(ComponentName className) {}
    }

    private void attachUICallbacks(){
        mGPSTrackingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mLoggingService!=null) {
                    if (!mLoggingService.isTrackingActive()) {
                        mLoggingService.startLocationTracking();
                        mGPSTrackingButton.setImageResource(R.drawable.ic_pause_white_18dp);
                        clearMap();
                    } else {
                        mLoggingService.stopLocationTracking();
                        mGPSTrackingButton.setImageResource(R.drawable.ic_directions_run_white_18dp);
                    }
                }
            }
        });
        mSettingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), SettingsActivity.class));
            }
        });
        mHistoryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), HistoryActivity.class));
            }
        });
    }

    private void setTrackingButtonIcon(){
        if(mLoggingService!=null) {
            if (mLoggingService.isTrackingActive()) {
                mGPSTrackingButton.setImageResource(R.drawable.ic_pause_white_18dp);
            } else {
                mGPSTrackingButton.setImageResource(R.drawable.ic_directions_run_white_18dp);
            }
        }
    }

    private void initVisualTrace(){
        PolylineOptions traceOptions = new PolylineOptions();
        traceOptions.color(Color.BLUE);
        visualRouteTrace = mMap.addPolyline(traceOptions);
        visualRouteTrace.setVisible(true);
    }

    private void drawTrace(Route route){
        if(route ==null) return;
        if(visualRouteTrace==null) initVisualTrace();
        visualRouteTrace.setPoints(route.getPointsCoordinates());

        if(route.size()>0 && initRoutePointMarker==null){
            initRoutePointMarker = mMap.addMarker(
                    new MarkerOptions().position(route.getStartCoordinates()).title("Start"));
        }

        if (route.size()>1){
            if(lastRoutePointMarker!=null) lastRoutePointMarker.remove();
            lastRoutePointMarker =  mMap.addMarker(new MarkerOptions()
                    .position(route.getLastCoordinates())
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
        }
    }

    private void clearMap(){
        if(initRoutePointMarker!=null){
            initRoutePointMarker.remove();
            initRoutePointMarker=null;
        }
        if(lastRoutePointMarker!=null){
            lastRoutePointMarker.remove();
            lastRoutePointMarker=null;
        }
        if(visualRouteTrace!=null){
            visualRouteTrace.remove();
            visualRouteTrace=null;
        }
    }

    private void moveMapCameraToRoute(Route route){
        int padding = 100; // offset from edges of the map in pixels
        int animationDuration = 2000;
        int width = getResources().getDisplayMetrics().widthPixels;
        int height = getResources().getDisplayMetrics().heightPixels;
        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(route.getLatLngBounds(), width, height, padding);
        mMap.animateCamera(cu, animationDuration, new GoogleMap.CancelableCallback() {
            @Override
            public void onFinish() {}
            @Override
            public void onCancel() {}
        });
    }
}
