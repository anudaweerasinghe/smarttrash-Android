package com.smarttrash.anuda.garbage;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.location.Location;
import android.os.Vibrator;
import android.provider.Settings;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
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

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

import java.io.IOException;

import Helpers.RestClient;
import models.api_models.RedeemRequest;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class RedeemActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    SurfaceView cameraView;
    TextView barcodeInfo;
    BarcodeDetector barcodeDetector;
    CameraSource cameraSource;
    Editor editor;
    String message;
    String title;
    String phoneLabel;
    Button verifyBtn;
    private String qrValue;
    Boolean verifyStatus = false;
    Double currentlat;
    Double currentlng;

    private FusedLocationProviderClient mFusedLocationClient;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_redeem);

        Toolbar toolbarredeem = (Toolbar) findViewById(R.id.toolbarredeem);
        setSupportActionBar(toolbarredeem);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);


        ImageView qrFram = (ImageView) findViewById(R.id.qrFrame);
        qrFram.setVisibility(View.VISIBLE);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout_redeem);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbarredeem, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        toggle.syncState();

        final SharedPreferences pref = getApplicationContext().getSharedPreferences("IdeaTrash Preferences", 0); // 0 - for private mode
        editor = pref.edit();
        verifyBtn = (Button) findViewById(R.id.verifyQR);
        verifyBtn.setEnabled(true);

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        View hview = navigationView.getHeaderView(0);
        TextView navNameLabel = (TextView) hview.findViewById(R.id.nav_name_text);
        TextView navPhoneLabel = (TextView) hview.findViewById(R.id.nav_mobile_text);
        phoneLabel = pref.getString("Mobile", "");
        String nameLabel = pref.getString("Name", "");
        navPhoneLabel.setText(phoneLabel);
        navNameLabel.setText(nameLabel);


        cameraView = (SurfaceView) findViewById(R.id.camera_view);
        barcodeInfo = (TextView) findViewById(R.id.code_info);

        barcodeDetector =
                new BarcodeDetector.Builder(this)
                        .setBarcodeFormats(Barcode.QR_CODE)
                        .build();

        cameraSource = new CameraSource
                .Builder(this, barcodeDetector)
                .setAutoFocusEnabled(true)
                .setRequestedPreviewSize(1920,1080)
                .build();

        getLastLocation();



        verifyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                verifyBtn.setEnabled(false);

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
                                    Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                                    // Vibrate for 500 milliseconds
                                    v.vibrate(500);

                                    final ProgressDialog progressDialog = new ProgressDialog(RedeemActivity.this,
                                            R.style.AppTheme_Dark_Dialog);
                                    progressDialog.setIndeterminate(true);
                                    progressDialog.setMessage("Verifying...");
                                    progressDialog.show();

                                    byte[] value = qrValue.getBytes();

                                    byte[] encrypted = Base64.encode(value, Base64.NO_WRAP);

                                    String strEncrypted = new String(encrypted);


                                    barcodeDetector.release();

                                    RedeemRequest redeemRequest = new RedeemRequest();

                                    redeemRequest.setAuthCode(strEncrypted);
                                    redeemRequest.setPhone(phoneLabel);
                                    redeemRequest.setRedeemLat(currentlat);
                                    redeemRequest.setRedeemLng(currentlng);

                                    Call<Void> redeemCall = RestClient.garbageBinService.redeem(redeemRequest);
                                    redeemCall.enqueue(new Callback<Void>() {
                                        @Override
                                        public void onResponse(Call<Void> call, Response<Void> response) {
                                            if (response.code() == 200) {
                                                progressDialog.hide();
                                                title = "Successfully Redeemed";
                                                message = "Thank You for your disposal! You will receive your reward shortly.";
                                                redeemMessage(new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        Intent intentNew = new Intent(RedeemActivity.this, Dashboard.class);
                                                        startActivity(intentNew);
                                                    }
                                                });
                                            }else if(response.code()==403){
                                                progressDialog.hide();
                                                title = "Limit Reached";
                                                message = "You have already reached your daily redemption limit. Please try again tomorrow";
                                                redeemMessage(new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        verifyBtn.setEnabled(true);
                                                    }
                                                });
                                            } else if(response.code()==406){
                                                progressDialog.hide();
                                                title = "Redemption Failure";
                                                message = "You are not near an active bin to redeem your reward. Please try again";
                                                redeemMessage(new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        verifyBtn.setEnabled(true);
                                                    }
                                                });
                                            }else if (response.code()==401){
                                                progressDialog.hide();
                                                title = "Invalid QR code";
                                                message = "You are not at an authorized bin";
                                                redeemMessage(new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        verifyBtn.setEnabled(true);
                                                    }
                                                });
                                            }else {
                                                progressDialog.hide();
                                                title = "Redemption Error";
                                                message = "Unfortunately we encountered an error while verifying your disposal. Please try again.";
                                                redeemMessage(new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        verifyBtn.setEnabled(true);
                                                    }
                                                });
                                            }
                                        }

                                        @Override
                                        public void onFailure(Call<Void> call, Throwable t) {
                                            title = "Redemption Error";
                                            message = "Unfortunately we encountered an error while verifying your disposal. Please try again.";
                                            redeemMessage(new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    verifyBtn.setEnabled(true);
                                                }
                                            });
                                        }
                                    });

//                                    verifyStatus = true;
//
//                                    Intent intentRedeem = new Intent(RedeemActivity.this, Dashboard.class);
//                                    intentRedeem.putExtra("verifystatus", verifyStatus);
//                                    intentRedeem.putExtra("encrypted", strEncrypted);
//                                    startActivity(intentRedeem);
//                                    finish();


                                }
                            });
                        }else{
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

    private void redeemMessage(DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(RedeemActivity.this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .create()
                .show();
    }

    private void getLastLocation(){
        mFusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {


                            currentlat=location.getLatitude();
                            currentlng=location.getLongitude();

                        }else{


//
                        }
                    }
                });

    }
}


