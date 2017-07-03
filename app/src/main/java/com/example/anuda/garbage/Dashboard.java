package com.example.anuda.garbage;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.Location;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.ShareActionProvider;
import android.support.v7.widget.Toolbar;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.text.SpannableString;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
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
import com.google.android.gms.vision.text.Text;


import java.util.ArrayList;
import java.util.List;


public class Dashboard extends AppCompatActivity implements View.OnClickListener,OnMapReadyCallback, NavigationView.OnNavigationItemSelectedListener
//        ,
//        DrawerLayout.DrawerListener
{

    int WasteType=0;
    String date="26.06.2017";
    TextView typeLabel;
    Button Dispose;
    TextView dateLabel;
    final Context context= this;
    private GoogleMap Map;
    private FusedLocationProviderClient mFusedLocationClient;





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


        lastTransaction();

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout_dashboard);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.dashmap);
        mapFragment.getMapAsync(this);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);



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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
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
            Intent intentNew = new Intent(Dashboard.this, RedeemActivity.class);
            startActivity(intentNew);

        }
        else if (id == R.id.nav_share) {
            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_TEXT,"Download IdeaTrash Now!");
            sendIntent.setType("text/plain");
            startActivity(sendIntent);

        } else if (id == R.id.nav_logout) {
            Intent intentNew = new Intent(Dashboard.this, LoginActivity.class);
            startActivity(intentNew);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout_dashboard);
        drawer.closeDrawer(GravityCompat.START);
        return true;

    }


    @Override
    public void onClick(View v){
        WasteType++;
        lastTransaction();
    }

    public void lastTransaction() {
        if (WasteType == 0) {
            typeLabel.setText("\nStart Recycling Now!");
        } else if (WasteType == 1) {
            typeLabel.setText("\nE-Waste");
        } else if (WasteType == 2) {
            typeLabel.setText("\nPaper");
        } else if (WasteType == 3) {
            typeLabel.setText("\nCardboard");
        } else {
            typeLabel.setText("\nStart Recycling Now!");
        }
        dateLabel.setText("\n"+date);
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

                            LatLng Colombo = new LatLng(6, 80);
                            Map.addMarker(new MarkerOptions().position(Colombo).title("Colombo"));
                            Map.moveCamera(CameraUpdateFactory.newLatLng(Colombo));
                        }
                    }
                });
        
    }

    private void setMarker(Location loc){
        LatLng location = new LatLng(loc.getLatitude(), loc.getLongitude());
        Map.addMarker(new MarkerOptions().position(location).title("Current Location").icon(BitmapDescriptorFactory.fromResource(R.drawable.marker)));
        CameraPosition cameraPosition = new CameraPosition.Builder().target(location).zoom(17).bearing(0).tilt(0).build();
        Map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
//        Map.moveCamera(CameraUpdateFactory.zoomIn(newLatLng(location)).);

        LatLng bins[]={new LatLng(7.177, 79.89)
                ,new LatLng(6.1,80.478)
                ,new LatLng(6.433,79.998)
                ,new LatLng(6.292,80.164)
                ,new LatLng(7.328,80.121)
                ,new LatLng(8.029,79.833)};

        Map.addMarker(new MarkerOptions().position(bins[0]).title("Bin 1").icon(BitmapDescriptorFactory.fromResource(R.drawable.binmarker)));
        Map.addMarker(new MarkerOptions().position(bins[1]).title("Bin 2").icon(BitmapDescriptorFactory.fromResource(R.drawable.binmarker)));
        Map.addMarker(new MarkerOptions().position(bins[2]).title("Bin 3").icon(BitmapDescriptorFactory.fromResource(R.drawable.binmarker)));
        Map.addMarker(new MarkerOptions().position(bins[3]).title("Bin 4").icon(BitmapDescriptorFactory.fromResource(R.drawable.binmarker)));
        Map.addMarker(new MarkerOptions().position(bins[4]).title("Bin 5").icon(BitmapDescriptorFactory.fromResource(R.drawable.binmarker)));
        Map.addMarker(new MarkerOptions().position(bins[5]).title("Bin 6").icon(BitmapDescriptorFactory.fromResource(R.drawable.binmarker)));


    }


}

