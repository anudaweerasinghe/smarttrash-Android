package com.smarttrash.anuda.garbage;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.provider.Settings;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;


import java.util.List;

import Helpers.RestClient;
import models.api_models.RewardsStatusRequest;
import models.app_models.Bins;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class Dashboard extends AppCompatActivity implements View.OnClickListener,OnMapReadyCallback, NavigationView.OnNavigationItemSelectedListener
//        ,
//        DrawerLayout.DrawerListener
{

    TextView typeLabel;
    Button Dispose;
    TextView dateLabel;
    Double currentLat;
    Double currentLng;
    final Context context = this;
    private GoogleMap Map;
    private FusedLocationProviderClient mFusedLocationClient;
    private int numberOfRedemptions;
    SharedPreferences.Editor editor;
    String message;
    String title;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        typeLabel = (TextView) findViewById(R.id.typeLabel);
        Dispose = (Button) findViewById(R.id.btnDispose);
        dateLabel = (TextView) findViewById(R.id.dateLabel);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Dispose.setOnClickListener(this);




        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout_dashboard);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        toggle.syncState();

        SharedPreferences pref = getApplicationContext().getSharedPreferences("IdeaTrash Preferences", 0); // 0 - for private mode
        editor = pref.edit();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        View hview = navigationView.getHeaderView(0);
        TextView navNameLabel = (TextView)hview.findViewById(R.id.nav_name_text);
        TextView navPhoneLabel = (TextView)hview.findViewById(R.id.nav_mobile_text);
        String phoneLabel = pref.getString("Mobile", "");
        String nameLabel = pref.getString("Name","");
        navPhoneLabel.setText(phoneLabel);
        navNameLabel.setText(nameLabel);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.dashmap);
        mapFragment.getMapAsync(this);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        RewardsStatusRequest rewardsStatusRequest = new RewardsStatusRequest();

        rewardsStatusRequest.setPhone(phoneLabel);

        Call<Integer> rewardsStatusCall = RestClient.garbageBinService.redeemStatus(rewardsStatusRequest);

        rewardsStatusCall.enqueue(new Callback<Integer>() {
            @Override
            public void onResponse(Call<Integer> call, Response<Integer> response) {

                dateLabel.setText("Recent Earnings\n\n"+response.body()+" MB");

            }

            @Override
            public void onFailure(Call<Integer> call, Throwable t) {

                dateLabel.setText("Error");
            }
        });

        lastTransaction();

//        




    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout_dashboard);
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


        } else if (id == R.id.nav_map) {
            Intent intentNew = new Intent(Dashboard.this, MapsActivity.class);
            startActivity(intentNew);

        } else if (id == R.id.nav_redeem) {
            Intent intentNew = new Intent(Dashboard.this, RedeemQRActivity.class);
            startActivity(intentNew);

        }
        else if (id == R.id.nav_share) {
            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_TEXT,"Download SmartTrash Now!");
            sendIntent.setType("text/plain");
            startActivity(sendIntent);

        } else if (id == R.id.nav_logout) {
            editor.clear();
            editor.commit();
            Intent intentNew = new Intent(Dashboard.this, LoginActivity.class);
            startActivity(intentNew);
        }else if (id == R.id.nav_collector) {
            Intent intentNew = new Intent(Dashboard.this,CollectorActivity.class);
            startActivity(intentNew);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout_dashboard);
        drawer.closeDrawer(GravityCompat.START);
        return true;

    }


    @Override
    public void onClick(View v){
        Intent intentNew = new Intent(Dashboard.this, DisposeActivity.class);
        startActivity(intentNew);
    }

    public void lastTransaction() {
        typeLabel.setText("Waste Type\n\ne-Waste");

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Map = googleMap;
        getLastLocation();
        setstyle();

    }

    private void setstyle(){
        MapStyleOptions style;
        style =MapStyleOptions.loadRawResourceStyle(this,R.raw.mapstyledash);
        Map.setMapStyle(style);

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
//
                        }
                    }
                });

    }

    private void setMarker(Location loc){
        LatLng location = new LatLng(loc.getLatitude(), loc.getLongitude());
        Map.addMarker(new MarkerOptions().position(location).title("Current Location").icon(BitmapDescriptorFactory.fromResource(R.drawable.marker)));
        CameraPosition cameraPosition = new CameraPosition.Builder().target(location).zoom(12).bearing(0).tilt(0).build();
        Map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
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

                    Map.addMarker(new MarkerOptions().position(binLocations).title("Dialog @"+name).icon(BitmapDescriptorFactory.fromResource(R.drawable.binmarker)).snippet(info));

                }

            }

            @Override
            public void onFailure(Call<List<Bins>> call, Throwable t) {
                Toast.makeText(getBaseContext(), "There are currently no Active Bins.", Toast.LENGTH_LONG).show();
            }
        });

    }

    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(Dashboard.this)
                .setTitle("Location Services")
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }
    private void redeemMessage(DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(Dashboard.this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .create()
                .show();
    }



}

