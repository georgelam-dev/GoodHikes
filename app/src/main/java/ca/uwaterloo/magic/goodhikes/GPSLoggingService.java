package ca.uwaterloo.magic.goodhikes;

import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

public class GPSLoggingService extends Service
        implements
        LocationListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    protected static final String LOG_TAG = "GPSLoggingService";
    protected GoogleApiClient mGoogleApiClient;
    private boolean mGoogleApiClientConnected=false;
    protected LocationRequest mLocationRequest;
    public static final String locationUpdateAction = "ca.uwaterloo.magic.goodhikes.location.update";
    private final IBinder mBinder = new LoggingBinder();

    /**
     * Class for clients to access.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    public class LoggingBinder extends Binder {
        GPSLoggingService getService() {
            return GPSLoggingService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onCreate() {
//        android.os.Debug.waitForDebugger();
        super.onCreate();
        createGoogleAPIClient();
        createLocationRequest();
        mGoogleApiClient.connect();
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mGoogleApiClient.isConnected()) {
            stopLocationUpdates();
            mGoogleApiClient.disconnect();
        }
    }

    private void createGoogleAPIClient() {
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
    }

    private void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(3000);
        mLocationRequest.setFastestInterval(3000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    public void startTracking() {
        if (mGoogleApiClient.isConnected()) {
            Location broadcastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            broadcastLocation(broadcastLocation);
            startLocationUpdates();
        }
    }

    public void stopTracking() {
        if (mGoogleApiClient.isConnected()) {
            stopLocationUpdates();
            mGoogleApiClient.disconnect();
        }
    }

    public void resumeTracking() {
    }

    public void pauseTracking() {
        stopLocationUpdates();
    }

    /*
    * GoogleApiClient lifecycle callbacks
    * Interfaces: GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener
    * ------------------------------------------------------------------------------------------
    */
    @Override
    public void onConnected(Bundle connectionHint) {
        mGoogleApiClientConnected=true;
        startTracking();
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(LOG_TAG, "GoogleApiClient connection has been suspended");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.i(LOG_TAG, "GoogleApiClient connection has failed");
    }

    protected void startLocationUpdates() {
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);
    }

    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(
                mGoogleApiClient, this);
    }

    @Override
    public void onLocationChanged(Location location) {
        broadcastLocation(location);
    }

    private void broadcastLocation(Location location){
        Intent intent= new Intent(locationUpdateAction);
        intent.putExtra(locationUpdateAction, location);
        sendBroadcast(intent);
    }
}
