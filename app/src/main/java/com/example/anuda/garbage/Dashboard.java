package com.example.anuda.garbage;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
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


import java.util.ArrayList;
import java.util.List;


public class Dashboard extends AppCompatActivity implements View.OnClickListener, NavigationView.OnNavigationItemSelectedListener
//        ,
//        DrawerLayout.DrawerListener
{

    int WasteType=0;
    String date="26.06.2017";
    TextView typeLabel;
    Button Dispose;
    TextView dateLabel;
    private PieChart binGraph;
    final Context context= this;
    private int data1=75;
    private int data2= 100-data1;





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
        binGraph = (PieChart) findViewById(R.id.chart1);
        binGraph.setUsePercentValues(true);
        binGraph.getDescription().setEnabled(false);
        binGraph.setExtraOffsets(5, 10, 5, 5);

        binGraph.setDragDecelerationFrictionCoef(0.95f);

        binGraph.setDrawHoleEnabled(true);
        binGraph.setHoleColor(Color.WHITE);

        binGraph.setTransparentCircleColor(Color.WHITE);
        binGraph.setTransparentCircleAlpha(110);

        binGraph.setHoleRadius(58f);
        binGraph.setTransparentCircleRadius(61f);

        binGraph.setDrawCenterText(true);

        binGraph.setRotationAngle(0);
        binGraph.setRotationEnabled(false);
        binGraph.setHighlightPerTapEnabled(false);
        fillData();
        binGraph.setCenterText(centerText());
        toolbar.setTitle("Dashboard");
        centerText();
        Legend legend = binGraph.getLegend();
        legend.setEnabled(false);
        lastTransaction();

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout_dashboard);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);



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



    public void fillData(){
        List<PieEntry> entries=new ArrayList<>();
        entries.add(new PieEntry(data1, ""));
        entries.add(new PieEntry(data2, ""));

        PieDataSet set= new PieDataSet(entries,"Colors");
        PieData data =new PieData(set);
        int colorCodeOne=Color.parseColor("#468500");
        int colorCodeTwo=Color.parseColor("#F44336");
        set.setColors(new int[]{colorCodeTwo, colorCodeOne});

        binGraph.setData(data);
        binGraph.invalidate();
        data.setHighlightEnabled(false);
        data.setDrawValues(false);


    }

    private SpannableString centerText(){
        SpannableString s = new SpannableString(data1+"% \n FULL");



        return s;
    }


}

