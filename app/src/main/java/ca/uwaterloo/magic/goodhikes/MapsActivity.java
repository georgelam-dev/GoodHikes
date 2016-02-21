package ca.uwaterloo.magic.goodhikes;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.design.widget.FloatingActionButton;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class MapsActivity extends AppCompatActivity
        implements
        OnMapReadyCallback,
        GoogleMap.OnMyLocationButtonClickListener{

    protected GoogleMap mMap;
    protected static final String LOG_TAG = "MapsActivity";
    private GPSUpdatesReceiver mGPSUpdatesReceiver;
    private IntentFilter mFilter;
    private ServiceConnection mConnection;
    private GPSLoggingService mLoggingService;
    private FloatingActionButton mGPSTrackingButton;
    private RouteTrace currentRouteTrace;
    private Polyline visualRouteTrace;
    private Marker initRoutePointMarker, lastRoutePointMarker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mGPSTrackingButton = (FloatingActionButton) findViewById(R.id.gps_tracking_control_button);
        attachUICallbacks();
    }

    /**
     * onCreate() is called only once for Activity, whereas onStart() - each time
     * appActivity is hidden from screen (user uses other apps), and then App is activated again.
     *
     * Using startService() overrides the default service lifetime that is managed by
     * bindService(Intent, ServiceConnection, int): it requires the service to remain running
     * until stopService(Intent) is called, regardless of whether any clients are connected to it.
     */
    @Override
    protected void onStart() {
        super.onStart();
        mFilter = new IntentFilter(GPSLoggingService.locationUpdateCommand);
        mGPSUpdatesReceiver = new GPSUpdatesReceiver();
        mConnection = new GPSLoggingServiceConnection();
        LocalBroadcastManager.getInstance(this).registerReceiver(mGPSUpdatesReceiver, mFilter);
        startService(new Intent(this, GPSLoggingService.class));
        Log.d(LOG_TAG, "Thread: " + Thread.currentThread().getId() + "; MapActivity started");
    }

    @Override
    protected void onStop() {
        super.onStop();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mGPSUpdatesReceiver);
    }

    @Override
    public void onResume() {
        super.onResume();
        Intent intent = new Intent(this, GPSLoggingService.class);
        bindService(intent, mConnection, BIND_AUTO_CREATE);
        Log.d(LOG_TAG, "Thread: " + Thread.currentThread().getId() + "; Binding to service");
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
        mLoggingService.stopLocationTracking();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mGPSUpdatesReceiver);
        unbindService(mConnection);
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
        settings.setZoomControlsEnabled(true);
        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);

        mMap.setOnMyLocationButtonClickListener(this);
        enableMyLocation();
        initVisualTrace();
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

    private void showUWaterlooMarkerOnMap() {
        // UWaterloo: 43.4689° N, 80.5400° W; Davis Centre: 43.472761	-80.542164
        LatLng waterloo = new LatLng(43.472761, -80.542164);
        mMap.addMarker(new MarkerOptions().position(waterloo).title("University of Waterloo"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(waterloo));

    }

    private void updateLocation(Location location){
        LatLng coordinates = new LatLng(location.getLatitude(), location.getLongitude());
        mMap.moveCamera(CameraUpdateFactory.newLatLng(coordinates));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(15.0f));

        Calendar cal = Calendar.getInstance();
        SimpleDateFormat time = new SimpleDateFormat("HH:mm:ss");
        TextView txt = (TextView)findViewById(R.id.textView);
        String output = txt.getText() + "[" + time.format(cal.getTime()) + "] " + String.valueOf(location.getLatitude()) + ", " + String.valueOf(location.getLongitude()) + "\n";
        txt.setText(output);
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
            drawTrace();
            Log.d(LOG_TAG, "Thread: "+Thread.currentThread().getId() + "; Location update received by UI");
        }

    }

    public class GPSLoggingServiceConnection implements ServiceConnection {
        @Override
        public void onServiceConnected(ComponentName className, IBinder binder) {
            mLoggingService = ((GPSLoggingService.LoggingBinder) binder).getService();
            setTrackingButtonIcon();
            currentRouteTrace = mLoggingService.currentRouteTrace;
            mLoggingService.broadcastLastKnownLocation();

        }

        public void onServiceDisconnected(ComponentName className) {
        }
    }

    private void attachUICallbacks(){
        mGPSTrackingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mLoggingService!=null) {
                    if (!mLoggingService.isTrackingActive()) {
                        mLoggingService.startLocationTracking();
                        mGPSTrackingButton.setImageResource(R.drawable.ampelmann_red);
                        clearMap();
                    } else {
                        mLoggingService.stopLocationTracking();
                        mGPSTrackingButton.setImageResource(R.drawable.ampelmann_green);
                    }
                }
            }
        });
    }

    private void setTrackingButtonIcon(){
        if(mLoggingService!=null) {
            if (mLoggingService.isTrackingActive()) {
                mGPSTrackingButton.setImageResource(R.drawable.ampelmann_red);
            } else {
                mGPSTrackingButton.setImageResource(R.drawable.ampelmann_green);
            }
        }
    }

    private void initVisualTrace(){
        PolylineOptions traceOptions = new PolylineOptions();
        traceOptions.color(Color.BLUE);
        visualRouteTrace = mMap.addPolyline(traceOptions);
        visualRouteTrace.setVisible(true);
    }

    private void drawTrace(){
        if(currentRouteTrace==null) return;
        visualRouteTrace.setPoints(currentRouteTrace.getPointsCoordinates());

        if(currentRouteTrace.size()>0 && initRoutePointMarker==null){
            initRoutePointMarker = mMap.addMarker(
                    new MarkerOptions().position(currentRouteTrace.getStartCoordinates()).title("Start"));
        }

        if (currentRouteTrace.size()>1){
            if(lastRoutePointMarker!=null) lastRoutePointMarker.remove();
            lastRoutePointMarker =  mMap.addMarker(
                    new MarkerOptions().position(currentRouteTrace.getLastCoordinates()).title("Current Position"));
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
    }
}
