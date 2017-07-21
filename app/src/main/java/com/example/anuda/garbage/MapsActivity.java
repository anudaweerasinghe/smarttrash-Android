package com.example.anuda.garbage;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.location.Location;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;


import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.List;

import Helpers.RestClient;
import models.app_models.Bins;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, NavigationView.OnNavigationItemSelectedListener {

    private GoogleMap mMap;
    private FusedLocationProviderClient mFusedLocationClient;
    Editor editor;
    TextView navPhoneLabel;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        Toolbar toolbarmaps = (Toolbar) findViewById(R.id.toolbarmaps);
        setSupportActionBar(toolbarmaps);
//        navPhoneLabel = (TextView) findViewById(R.id.nav_name_text);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout_maps);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbarmaps, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        toggle.syncState();

        SharedPreferences pref = getApplicationContext().getSharedPreferences("IdeaTrash Preferences", 0); // 0 - for private mode
        editor = pref.edit();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        View hview = navigationView.getHeaderView(0);
        TextView navPhoneLabel = (TextView)hview.findViewById(R.id.nav_mobile_text);
        String phoneLabel = pref.getString("Mobile", "");
        navPhoneLabel.setText(phoneLabel);

                FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.locationfab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getLastLocation();
            }
        });





    }
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout_maps);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.drawer, menu);

        return true;
    }



    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_dashboard) {
            Intent intentNew = new Intent(MapsActivity.this, Dashboard.class);
            startActivity(intentNew);

        } else if (id == R.id.nav_map) {


        }
        else if (id == R.id.nav_redeem) {
            Intent intentNew = new Intent(MapsActivity.this, RedeemActivity.class);
            startActivity(intentNew);
        }else if (id == R.id.nav_share) {
            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_TEXT,"Download IdeaTrash Now!");
            sendIntent.setType("text/plain");
            startActivity(sendIntent);
        } else if (id == R.id.nav_logout) {
            editor.clear();
            editor.commit();
            Intent intentNew = new Intent(MapsActivity.this, LoginActivity.class);
            startActivity(intentNew);
        }else if (id == R.id.nav_collector) {
            Intent intentNew = new Intent(MapsActivity.this,CollectorActivity.class);
            startActivity(intentNew);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout_maps);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        getLastLocation();
        setstyle();

    }

    private void setstyle(){
        MapStyleOptions style;
        style =MapStyleOptions.loadRawResourceStyle(this,R.raw.mapstylegreen);
        mMap.setMapStyle(style);

    }

    private void getLastLocation(){
        mFusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {
                            setMarker(location);


                        }else{

                            showMessageOKCancel("To see Nearby Bins, turn on Location Services",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            Intent onGPS = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                            startActivity(onGPS);
                                        }
                                    });
                        }
                    }
                });
    }

    private void setMarker(Location loc){
        LatLng location = new LatLng(loc.getLatitude(), loc.getLongitude());
        mMap.addMarker(new MarkerOptions().position(location).title("Current Location").icon(BitmapDescriptorFactory.fromResource(R.drawable.marker)));
        CameraPosition cameraPosition = new CameraPosition.Builder().target(location).zoom(15).bearing(0).tilt(0).build();
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
//        mMap.moveCamera(CameraUpdateFactory.zoomIn(newLatLng(location)).);

        final Bins bins = new Bins();

        Call<List<Bins>> BinsCall = RestClient.garbageBinService.bins();

        BinsCall.enqueue(new Callback<List<Bins>>() {
            @Override
            public void onResponse(Call<List<Bins>> call, Response<List<Bins>> response) {


                for (int i = 0; i <response.body().size() ; i++) {

                    double lng = response.body().get(i).getLng();
                    double lat = response.body().get(i).getLat();
                    String name = response.body().get(i).getName();
                    String info = response.body().get(i).getInfo();
                    LatLng binLocations = new LatLng(lat,lng);

                    mMap.addMarker(new MarkerOptions().position(binLocations).title("Dialog @"+name).icon(BitmapDescriptorFactory.fromResource(R.drawable.binmarker)).snippet(info));

                }

            }

            @Override
            public void onFailure(Call<List<Bins>> call, Throwable t) {
                Toast.makeText(getBaseContext(), "There are currently no Active Bins.", Toast.LENGTH_LONG).show();
            }
        });




}

    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(MapsActivity.this)
                .setTitle("Location Services")
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }
}
