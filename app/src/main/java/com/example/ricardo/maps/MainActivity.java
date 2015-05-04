package com.example.ricardo.maps;

import android.app.ProgressDialog;
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
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
import com.parse.Parse;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends ActionBarActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, OnMapReadyCallback, LocationListener{

    MapView mapView;
    GoogleMap googleMap;
    RelativeLayout relativeLayout;
    Button button;
    private boolean mResolvingError = false;
    private boolean mRequestingLocationUpdates = false;
    LocationRequest mLocationRequest;
    Location mCurrentLocation;
    ProgressDialog pDialog;
    List <ParseObject> list;
    ArrayList<LatLng> values = new ArrayList<LatLng>();

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
        Parse.enableLocalDatastore(this);
        Parse.initialize(this, "sy884haPYPxBSHUuiyJIKw0oALDXyZ5380eCR8d3", "onOqSakPVHkfO9AYErwtOaYf0xl40RHm6XFYR5hY");
        relativeLayout = (RelativeLayout) findViewById(R.id.MapLayout);
        button = (Button) findViewById(R.id.getParse);
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

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new GetData().execute();

            }
        });




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
            ParseObject parseObject = new ParseObject("Puntos");
            LatLng lastLatLng= locationToLatLng(mCurrentLocation);
            LatLng thisLatLng= locationToLatLng(location);
            parseObject.put("LatitudUltima",lastLatLng.toString());
            parseObject.put("LatitudEsta",thisLatLng.toString());
            parseObject.saveInBackground();

            googleMap.addPolyline(new PolylineOptions().add(lastLatLng).add(thisLatLng).width(10).color(Color.RED));
            mCurrentLocation = location;
    }

    private class GetData extends AsyncTask<Void,Void,Void>{


        protected void onPreExecute() {
            super.onPreExecute();
            // Create a progressdialog
            pDialog = new ProgressDialog(MainActivity.this);
            // Set progressdialog title
            pDialog.setTitle("Cargando datos de Parse");
            // Set progressdialog message
            pDialog.setMessage("Loading...");
            pDialog.setIndeterminate(false);
            // Show progressdialog
            pDialog.show();
        }

        @Override
        protected Void doInBackground(Void... params) {



            ParseQuery<ParseObject> query = new ParseQuery<ParseObject>("Puntos");
            try {
                list = query.find();
                for(ParseObject dato : list ){
                    values.add((LatLng) dato.get("LatitudUltima"));
                    values.add((LatLng) dato.get("LatitudEsta"));
                }

            } catch (com.parse.ParseException e) {
                e.printStackTrace();
            }


            return null;
        }

        protected void onPostExecute(Void result) {
            // Locate the listview in listview_main.xml
            // Pass the results into ListViewAdapter.java
            for(int i=0; i<=values.size();i++){
                googleMap.addMarker(new MarkerOptions().position(values.get(i)).title("Dato"+i));
            }

            // Close the progressdialog
            pDialog.dismiss();
        }
    }

    public static LatLng locationToLatLng(Location loc) {
        if(loc != null) {
            return new LatLng(loc.getLatitude(), loc.getLongitude());
        }
        return null;
    }

}
