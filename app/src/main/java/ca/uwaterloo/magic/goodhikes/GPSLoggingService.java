package ca.uwaterloo.magic.goodhikes;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
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
    protected LocationRequest mLocationRequest;
    public static final String locationUpdateCommand = "ca.uwaterloo.magic.goodhikes.location.update";
    private final IBinder mBinder = new LoggingBinder();
    private LooperThread internalLooperThread;
    private boolean mTrackingIsActive=false;
    public RouteTrace currentRouteTrace = new RouteTrace();

    private static class GPSTrackingCommands {
        public static final String START = "ca.uwaterloo.magic.goodhikes.location.update.start";
        public static final String STOP = "ca.uwaterloo.magic.goodhikes.location.update.stop";
    }

    public void startLocationTracking() {
        sendCommandToLooperThread(new Intent(GPSTrackingCommands.START));
    }

    public void stopLocationTracking() {
        sendCommandToLooperThread(new Intent(GPSTrackingCommands.STOP));
    }

    private void sendCommandToLooperThread(Intent command){
        if(internalLooperThread!=null) {
            Message msg = internalLooperThread.mLooperThreadHandler.obtainMessage();
            msg.obj = command;
            Log.d(LOG_TAG, "Thread: " + Thread.currentThread().getId() +
                    "; Sending "+command.getAction()+" command to looper thread");
            internalLooperThread.mLooperThreadHandler.sendMessage(msg);
        }
    }

    public boolean isTrackingActive() {
        return mTrackingIsActive;
    }

    public void setTrackingStatus(boolean mTrackingIsActive) {
        this.mTrackingIsActive = mTrackingIsActive;
    }

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
        Log.d(LOG_TAG, "Thread: " + Thread.currentThread().getId() + "; Started GPS tracking service");
    }

    public void stopService() {
        if(mGoogleApiClient.isConnected()) {
            stopLocationTracking();
            mGoogleApiClient.disconnect();
        }
        if(internalLooperThread.mLooper!=null){
            internalLooperThread.mLooper.quit();
            internalLooperThread.interrupt();
        }
            Log.d(LOG_TAG, "Thread: " + Thread.currentThread().getId() + "; Stopped GPS tracking service");
    }

    @Override
    public void onDestroy() {
        stopService();
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
        updateGPSfrequency();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    //Settings: Change GPS Update Frequency
    public void updateGPSfrequency() {
        if (mLocationRequest != null) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            int gps_frequency = Integer.parseInt(prefs.getString
                    (getString(R.string.interval_pref),
                            getString(R.string.interval_pref_default)));
            System.out.println("update freq:" + gps_frequency);
            mLocationRequest.setInterval(gps_frequency);
            mLocationRequest.setFastestInterval(gps_frequency);
        }
    }

    private void startLocationTrackingInLooperThread() {
        if (mGoogleApiClient.isConnected() && internalLooperThread.mLooper!=null) {
            LocationServices.FusedLocationApi.requestLocationUpdates(
                    mGoogleApiClient, mLocationRequest, this, internalLooperThread.mLooper);
            mTrackingIsActive=true;
            currentRouteTrace.clearTrace();
            Log.d(LOG_TAG, "Thread: " + Thread.currentThread().getId() + "; Starting location tracking in looper thread");
        }
    }

    private void stopLocationTrackingInLooperThread() {
        LocationServices.FusedLocationApi.removeLocationUpdates(
                mGoogleApiClient, this);
        mTrackingIsActive=false;
        Log.d(LOG_TAG, "Thread: " + Thread.currentThread().getId() + "; Stopped location tracking in looper thread");
    }

    /*
    * GoogleApiClient lifecycle callbacks
    * Interfaces: GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener
    * ------------------------------------------------------------------------------------------
    */
    @Override
    public void onConnected(Bundle connectionHint) {
        broadcastLastKnownLocation();
        internalLooperThread = new LooperThread();
        internalLooperThread.start();
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(LOG_TAG, "GoogleApiClient connection has been suspended");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.i(LOG_TAG, "GoogleApiClient connection has failed");
    }

    @Override
    public void onLocationChanged(Location location) {
        currentRouteTrace.addPoint(location);
        broadcastLocation(location);
    }

    private void broadcastLocation(Location location){
        Log.d(LOG_TAG, "Thread: "+Thread.currentThread().getId() + "; Sending location update");
        Intent intent= new Intent(locationUpdateCommand);
        intent.putExtra(locationUpdateCommand, location);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    public void broadcastLastKnownLocation(){
        if(mGoogleApiClient.isConnected()) {
            Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            broadcastLocation(location);
        }
    }

    class LooperThread extends Thread {
        private Looper mLooper;
        private Handler mLooperThreadHandler;
        public void run() {
            Looper.prepare();
            mLooper = Looper.myLooper();

            mLooperThreadHandler = new Handler() {
                public void handleMessage(Message msg) {
                    if (msg.obj != null && msg.obj instanceof Intent) {
                        onHandleIntent((Intent) msg.obj);
                    }
                }
            };
            Log.d(LOG_TAG, "Thread: " + Thread.currentThread().getId() +
                    "; Thread looper" + mLooper.toString() + "; Created a runner thread");
            Looper.loop();
        }
    }

    private void onHandleIntent(Intent intent) {
        if(intent.getAction()==GPSTrackingCommands.START) {
            startLocationTrackingInLooperThread();
        }else if(intent.getAction()==GPSTrackingCommands.STOP){
            stopLocationTrackingInLooperThread();
        }

    }
}
