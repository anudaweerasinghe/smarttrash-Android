package com.smarttrash.anuda.garbage;

import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class CollectorActivity extends AppCompatActivity implements View.OnClickListener,NavigationView.OnNavigationItemSelectedListener {

    TextView info;
    Button forms;
    Editor editor;
    TextView navPhoneLabel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collector);
        info =(TextView)findViewById(R.id.collectorinfo);
        forms = (Button)findViewById(R.id.formbutton);
        forms.setOnClickListener(this);
        info.setText("The next stage of this project is to expand beyond only e-waste disposal to a solution for all recyclable waste disposal. In order to do this, we plan to crowdsource the problem to the recycling community. We plan to connect the recycler to the consumer. As a recycler, this will ensure you with an efficient way to bring in customers. If you are interested, please click the button below and provide us with some information about you and your business.");


        Toolbar toolbarcollector = (Toolbar) findViewById(R.id.toolbarcollector);
        setSupportActionBar(toolbarcollector);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout_collector);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbarcollector, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
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

    }

    @Override
    public void onClick(View v){
        String url = "https://goo.gl/j3SmKL";
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
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout_collector);
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
            Intent intentNew = new Intent(CollectorActivity.this, Dashboard.class);
            startActivity(intentNew);

        } else if (id == R.id.nav_map) {
            Intent intentNew = new Intent(CollectorActivity.this, MapsActivity.class);
            startActivity(intentNew);

        } else if (id == R.id.nav_redeem) {
            Intent intentNew = new Intent(CollectorActivity.this,RedeemActivity.class);
            startActivity(intentNew);
        }else if (id == R.id.nav_share) {
            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_TEXT,"Download SmartTrash Now!");
            sendIntent.setType("text/plain");
            startActivity(sendIntent);
        } else if (id == R.id.nav_logout) {
            editor.clear();
            editor.commit();
            Intent intentNew = new Intent(CollectorActivity.this, LoginActivity.class);
            startActivity(intentNew);
        }else if (id == R.id.nav_collector) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout_collector);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

}
