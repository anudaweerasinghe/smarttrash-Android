package com.example.anuda.garbage;

import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
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

public class DisposeActivity extends AppCompatActivity implements OnMapReadyCallback, NavigationView.OnNavigationItemSelectedListener {

    private GoogleMap disposeMap;
    TextView disposeText;
    private FusedLocationProviderClient mFusedLocationClient;
    String nearestBin= "Dialog Iconic Center at Union Place";
    double nearestBinLocationLat = 6.9183201;
    double nearestBinLocationLng= 79.8626769;
    FloatingActionButton navigationFab;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dispose);

        disposeText = (TextView) findViewById(R.id.disposeText);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.disposemap);
        mapFragment.getMapAsync(this);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        setNearestBin();

        navigationFab = (FloatingActionButton)findViewById(R.id.navigationFab);
        navigationFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                navigationButton();
            }
        });

        Toolbar toolbardispose = (Toolbar) findViewById(R.id.toolbardispose);
        setSupportActionBar(toolbardispose);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout_dispose);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbardispose, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        disposeMap = googleMap;
        getLastLocation();
        setstyle();


    }

    private void setstyle() {
        MapStyleOptions style;
        style = MapStyleOptions.loadRawResourceStyle(this, R.raw.mapstyledash);
        disposeMap.setMapStyle(style);

    }

    private void getLastLocation() {
        mFusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {
                            setMarker(location);


                        } else {

                            LatLng Colombo = new LatLng(6, 80);
                            disposeMap.addMarker(new MarkerOptions().position(Colombo).title("Colombo"));
                            disposeMap.moveCamera(CameraUpdateFactory.newLatLng(Colombo));
                        }
                    }
                });

    }

    private void setMarker(Location loc) {
        LatLng location = new LatLng(loc.getLatitude(), loc.getLongitude());
        disposeMap.addMarker(new MarkerOptions().position(location).title("Current Location").icon(BitmapDescriptorFactory.fromResource(R.drawable.marker)));
        CameraPosition cameraPosition = new CameraPosition.Builder().target(location).zoom(14).bearing(0).tilt(0).build();
        disposeMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

        LatLng nearestBinLatLng = new LatLng(nearestBinLocationLat,nearestBinLocationLng);

        disposeMap.addMarker(new MarkerOptions().position(nearestBinLatLng).title(nearestBin).icon(BitmapDescriptorFactory.fromResource(R.drawable.binmarker)));


//        Map.moveCamera(CameraUpdateFactory.zoomIn(newLatLng(location)).);


    }

    private void setNearestBin(){

        disposeText.setText("The Nearest Bin is At \n\n"+nearestBin);
    }

    private void navigationButton(){
        String url = "https://www.google.lk/maps/dir//"+nearestBinLocationLat+","+nearestBinLocationLng+"/@"+nearestBinLocationLat+","+nearestBinLocationLng+"17.95z/data=!4m8!1m7!3m6!1s0x0:0x0!2zNsKwNTUnMDYuMCJOIDc5wrA1MSc0NS42IkU!3b1!8m2!3d6.9183201!4d79.8626769?hl=en";
        try {
            Intent i = new Intent("android.intent.action.MAIN");
            i.setComponent(ComponentName.unflattenFromString("com.android.chrome/com.android.chrome.Main"));
            i.addCategory("android.intent.category.LAUNCHER");
            i.setData(Uri.parse(url));
            startActivity(i);
        }
        catch(ActivityNotFoundException e) {
            // Chrome is not installed
            Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(i);
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout_dispose);
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
            Intent intentNew = new Intent(DisposeActivity.this, Dashboard.class);
            startActivity(intentNew);

        } else if (id == R.id.nav_map) {
            Intent intentNew = new Intent(DisposeActivity.this, MapsActivity.class);
            startActivity(intentNew);

        } else if (id == R.id.nav_redeem) {
            Intent intentNew = new Intent(DisposeActivity.this, RedeemActivity.class);
            startActivity(intentNew);
        }else if (id == R.id.nav_share) {
            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_TEXT,"Download IdeaTrash Now!");
            sendIntent.setType("text/plain");
            startActivity(sendIntent);
        } else if (id == R.id.nav_logout) {
            Intent intentNew = new Intent(DisposeActivity.this, LoginActivity.class);
            startActivity(intentNew);
        }else if (id == R.id.nav_collector) {
            Intent intentNew = new Intent(DisposeActivity.this,CollectorActivity.class);
            startActivity(intentNew);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout_dispose);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }




}
