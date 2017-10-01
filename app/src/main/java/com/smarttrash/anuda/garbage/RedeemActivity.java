package com.smarttrash.anuda.garbage;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

import java.io.IOException;


public class RedeemActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    SurfaceView cameraView;
    TextView barcodeInfo;
    BarcodeDetector barcodeDetector;
    CameraSource cameraSource;
    Editor editor;
    String message;
    String title;
    Button verifyBtn;
    private String qrValue;
    Boolean verifyStatus=false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_redeem);

        Toolbar toolbarredeem = (Toolbar) findViewById(R.id.toolbarredeem);
        setSupportActionBar(toolbarredeem);

        ImageView qrFram = (ImageView) findViewById(R.id.qrFrame);
        qrFram.setVisibility(View.VISIBLE);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout_redeem);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbarredeem, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        toggle.syncState();

        final SharedPreferences pref = getApplicationContext().getSharedPreferences("IdeaTrash Preferences", 0); // 0 - for private mode
        editor = pref.edit();
         verifyBtn = (Button) findViewById(R.id.verifyQR);

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        View hview = navigationView.getHeaderView(0);
        TextView navPhoneLabel = (TextView) hview.findViewById(R.id.nav_mobile_text);
        final String phoneLabel = pref.getString("Mobile", "");
        navPhoneLabel.setText(phoneLabel);


        cameraView = (SurfaceView) findViewById(R.id.camera_view);
        barcodeInfo = (TextView) findViewById(R.id.code_info);

        barcodeDetector =
                new BarcodeDetector.Builder(this)
                        .setBarcodeFormats(Barcode.QR_CODE)
                        .build();

        cameraSource = new CameraSource
                .Builder(this, barcodeDetector)
                .setAutoFocusEnabled(true)
                .setRequestedPreviewSize(800, 800)
                .build();

        verifyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                barcodeDetector.setProcessor(new Detector.Processor<Barcode>() {
                    @Override
                    public void release() {
                    }

                    @Override
                    public void receiveDetections(Detector.Detections<Barcode> detections) {
                        final SparseArray<Barcode> barcodes = detections.getDetectedItems();

                        if (barcodes.size() != 0) {
                            barcodeInfo.post(new Runnable() {    // Use the post method of the TextView
                                public void run() {
                                    qrValue = barcodes.valueAt(0).displayValue;

                                    if (qrValue.equals("Verified")) {

                                        verifyStatus = true;
                                        Integer numberOfRedemptions = (pref.getInt("numberOfRedemptions",0))+1;

                                        editor.putInt("numberOfRedemptions",numberOfRedemptions);
                                        editor.commit();
                                        Intent intentRedeem = new Intent (RedeemActivity.this, Dashboard.class);
                                        intentRedeem.putExtra("verifystatus",verifyStatus);
                                        startActivity(intentRedeem);
                                        finish();

                                    }else{
                                        barcodeDetector.release();
                                        title = "Redemption Error";
                                        message = "Unfortunately we encountered an error while verifying your disposal. Please Try again.";
                                        showMessageOKCancel(
                                                new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        Intent intentNew = new Intent(RedeemActivity.this, RedeemActivity.class);
                                                        startActivity(intentNew);
                                                    }
                                                });
                                    }




                                }
                            });
                        }
                    }
                });
            }
        });


        cameraView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                try {
                    cameraSource.start(cameraView.getHolder());
                } catch (IOException ie) {
                    Log.e("CAMERA SOURCE", ie.getMessage());
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                cameraSource.stop();
            }
        });




    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout_redeem);
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
            Intent intentNew = new Intent(RedeemActivity.this, Dashboard.class);
            startActivity(intentNew);

        } else if (id == R.id.nav_map) {
            Intent intentNew = new Intent(RedeemActivity.this, MapsActivity.class);
            startActivity(intentNew);

        } else if (id == R.id.nav_redeem) {

        } else if (id == R.id.nav_share) {
            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_TEXT, "Download SmartTrash Now!");
            sendIntent.setType("text/plain");
            startActivity(sendIntent);
        } else if (id == R.id.nav_logout) {
            editor.clear();
            editor.commit();
            Intent intentNew = new Intent(RedeemActivity.this, LoginActivity.class);
            startActivity(intentNew);
        } else if (id == R.id.nav_collector) {
            Intent intentNew = new Intent(RedeemActivity.this, CollectorActivity.class);
            startActivity(intentNew);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout_redeem);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void showMessageOKCancel(DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(RedeemActivity.this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .create()
                .show();
    }
}


