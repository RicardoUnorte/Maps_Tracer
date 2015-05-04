package com.example.ricardo.maps;

import android.graphics.Color;
import android.location.Location;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.RelativeLayout;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;


public class MainActivity extends ActionBarActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, OnMapReadyCallback, LocationListener{

    MapView mapView;
    GoogleMap googleMap;
    RelativeLayout relativeLayout;
    private boolean mResolvingError = false;
    private boolean mRequestingLocationUpdates = false;
    LocationRequest mLocationRequest;
    Location mCurrentLocation;

    GoogleApiClient mGoogleApiClient;

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
        if (mGoogleApiClient.isConnected() && !mRequestingLocationUpdates) {
            startLocationUpdates();
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        relativeLayout = (RelativeLayout) findViewById(R.id.Relative);
        mapView = new MapView (this);
        relativeLayout.addView(mapView, new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT));
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        MapsInitializer.initialize(this);
        createLocationRequest();


    }

    protected void createLocationRequest(){
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(!mResolvingError){
            mGoogleApiClient.connect();
        }
    }

    @Override
    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
        stopLocationUpdates();

    }

    protected  void stopLocationUpdates(){
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        mRequestingLocationUpdates = false;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    public void onConnected(Bundle bundle) {
        if (!mRequestingLocationUpdates) {
            startLocationUpdates();
        }


    }
   private void startLocationUpdates() {
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest,  this);
        mRequestingLocationUpdates = true;

    }

    @Override
    public void onConnectionSuspended(int i) {


    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
      this.googleMap =  googleMap;

    }

    @Override
    public void onLocationChanged(Location location) {


        Double lat =location.getLatitude();
        Double lon =location.getLongitude();
        LatLng latLng = new LatLng(lat,lon);
        CameraPosition cameraUpdate = CameraPosition.builder().target(latLng).zoom(17).build();
        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraUpdate));
        //googleMap.addPolyline(polylineOptions);
        if(mCurrentLocation == null){
            mCurrentLocation = location;

        }
            LatLng lastLatLng= locationToLatLng(mCurrentLocation);
            LatLng thisLatLng= locationToLatLng(location);
            googleMap.addPolyline(new PolylineOptions().add(lastLatLng).add(thisLatLng).width(10).color(Color.RED));
            mCurrentLocation = location;
    }
    public static LatLng locationToLatLng(Location loc) {
        if(loc != null) {
            return new LatLng(loc.getLatitude(), loc.getLongitude());
        }
        return null;
    }

}
