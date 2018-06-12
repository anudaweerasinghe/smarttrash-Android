package com.smarttrash.anuda.garbage;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import net.gotev.uploadservice.MultipartUploadRequest;
import net.gotev.uploadservice.UploadNotificationConfig;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import Helpers.Constants;
import Helpers.RestClient;
import models.api_models.PicVerifyRequest;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RedeemPicActivity extends AppCompatActivity {

    private Button btnCapture;
    private TextureView textureView;
    String message;
    String title;

//    Check state orientation of output image
    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();
    static{
        ORIENTATIONS.append(Surface.ROTATION_0,90);
        ORIENTATIONS.append(Surface.ROTATION_90,0);
        ORIENTATIONS.append(Surface.ROTATION_180,270);
        ORIENTATIONS.append(Surface.ROTATION_270,180);
    }

    private String cameraId;
    private CameraDevice cameraDevice;
    private CameraCaptureSession cameraCaptureSessions;
    private CaptureRequest.Builder captureRequestBuilder;
    private Size imageDimension;
    private ImageReader imageReader;

    //Save to FILE
    private File file;
    private static final int REQUEST_CAMERA_PERMISSION = 200;
    private boolean mFlashSupported;
    private Handler mBackgroundHandler;
    private HandlerThread mBackgroundThread;

    CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            cameraDevice = camera;
            createCameraPreview();
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice cameraDevice) {
            cameraDevice.close();
        }

        @Override
        public void onError(@NonNull CameraDevice cameraDevice, int i) {
            cameraDevice.close();
            cameraDevice=null;
        }
    };

    private double lat;
    private double lng;
    private String qrCode;
    private String phone;
    private String imgDir;
    private String loc;

    Bundle extras;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_redeem_pic);

        textureView = (TextureView)findViewById(R.id.textureView);
        //From Java 1.4 , you can use keyword 'assert' to check expression true or false
        assert textureView != null;
        textureView.setSurfaceTextureListener(textureListener);
        btnCapture = (Button)findViewById(R.id.btnCapture);
        btnCapture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                takePicture();
            }
        });

        extras=getIntent().getExtras();

        if(extras!=null){
            lat = extras.getDouble("lat");
            lng = extras.getDouble("lng");
            qrCode = extras.getString("qrCode");
            phone = extras.getString("phone");
            loc = extras.getString("loc");
        }else{
            Intent intentNew = new Intent(RedeemPicActivity.this, RedeemQRActivity.class);
            startActivity(intentNew);
        }
    }

    private void takePicture() {
        if(cameraDevice == null)
            return;
        CameraManager manager = (CameraManager)getSystemService(Context.CAMERA_SERVICE);
        try{
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraDevice.getId());
            Size[] jpegSizes = null;
            if(characteristics != null)
                jpegSizes = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
                        .getOutputSizes(ImageFormat.JPEG);

            //Capture image with custom size
            int width = 640;
            int height = 480;
            if(jpegSizes != null && jpegSizes.length > 0)
            {
                width = jpegSizes[0].getWidth();
                height = jpegSizes[0].getHeight();
            }
            final ImageReader reader = ImageReader.newInstance(width,height,ImageFormat.JPEG,1);
            List<Surface> outputSurface = new ArrayList<>(2);
            outputSurface.add(reader.getSurface());
            outputSurface.add(new Surface(textureView.getSurfaceTexture()));

            final CaptureRequest.Builder captureBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            captureBuilder.addTarget(reader.getSurface());
            captureBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);

            //Check orientation base on device
            int rotation = getWindowManager().getDefaultDisplay().getRotation();
            captureBuilder.set(CaptureRequest.JPEG_ORIENTATION,ORIENTATIONS.get(rotation));

            File dir = new File(Environment.getExternalStorageDirectory()+"/SmartTrash");

            if(!dir.exists()){
                dir.mkdir();
            }else{

            }

            file = new File(dir+"/"+UUID.randomUUID().toString()+".jpg");
            ImageReader.OnImageAvailableListener readerListener = new ImageReader.OnImageAvailableListener() {
                @Override
                public void onImageAvailable(ImageReader imageReader) {
                    Image image = null;
                    try{
                        image = reader.acquireLatestImage();
                        ByteBuffer buffer = image.getPlanes()[0].getBuffer();
                        byte[] bytes = new byte[buffer.capacity()];
                        buffer.get(bytes);
                        save(bytes);

                    }
                    catch (FileNotFoundException e)
                    {
                        e.printStackTrace();
                    }
                    catch (IOException e)
                    {
                        e.printStackTrace();
                    }
                    finally {
                        {
                            if(image != null)
                                image.close();
                        }
                    }
                }
                private void save(byte[] bytes) throws IOException {
                    OutputStream outputStream = null;
                    try{
                        outputStream = new FileOutputStream(file);
                        outputStream.write(bytes);
                    }finally {
                        if(outputStream != null)
                            outputStream.close();
                        uploadPicture(file);
                    }
                }
            };

            reader.setOnImageAvailableListener(readerListener,mBackgroundHandler);
            final CameraCaptureSession.CaptureCallback captureListener = new CameraCaptureSession.CaptureCallback() {
                @Override
                public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
                    super.onCaptureCompleted(session, request, result);
                    Toast.makeText(RedeemPicActivity.this, "Saved "+file, Toast.LENGTH_SHORT).show();
                    createCameraPreview();

                }
            };

            cameraDevice.createCaptureSession(outputSurface, new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                    try{
                        cameraCaptureSession.capture(captureBuilder.build(),captureListener,mBackgroundHandler);
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {

                }
            },mBackgroundHandler);




        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void uploadPicture(File pic) {


        final ProgressDialog progressDialog = new ProgressDialog(RedeemPicActivity.this,
                R.style.AppTheme_Dark_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Uploading...");
        progressDialog.show();

        String uploadId = UUID.randomUUID().toString();




        //Creating a multi part request
        try {
            Toast.makeText(this, "Trying", Toast.LENGTH_SHORT).show();
            new MultipartUploadRequest(this, uploadId, Constants.UPLOAD_URL)
                    .addFileToUpload(pic.toString(), "file") //Adding file
                    .addParameter("location",loc) //Adding text parameter to the request
                    .setNotificationConfig(new UploadNotificationConfig())
                    .setMaxRetries(2)
                    .startUpload(); //St
            Toast.makeText(this, "Finish", Toast.LENGTH_SHORT).show();
        } catch (MalformedURLException | FileNotFoundException e) {
            e.printStackTrace();

            Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show();
        }




//        Call<Void> uploadCall = RestClient.garbageBinService.uploadPic(body,loc);
//
//        uploadCall.enqueue(new Callback<Void>() {
//            @Override
//            public void onResponse(Call<Void> call, Response<Void> response) {
//                progressDialog.dismiss();
//
//                if(response.code()==200){
//                    //codeVerify request
//
//                    imgDir = "http://128.199.178.5"+response.body().toString();
//
//                    final ProgressDialog verifyProgressDialog = new ProgressDialog(RedeemPicActivity.this,
//                            R.style.AppTheme_Dark_Dialog);
//                    verifyProgressDialog.setIndeterminate(true);
//                    verifyProgressDialog.setMessage("Verifying...");
//                    verifyProgressDialog.show();
//
//                    PicVerifyRequest request = new PicVerifyRequest();
//
//                    request.setImage(imgDir);
//                    request.setLat(lat);
//                    request.setLng(lng);
//                    request.setPhone(phone);
//                    request.setQrCode(qrCode);
//
//                    Call<ResponseBody> picVerifyCall = RestClient.garbageBinService.verifyPic(request);
//
//                    picVerifyCall.enqueue(new Callback<ResponseBody>() {
//                        @Override
//                        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
//                            verifyProgressDialog.dismiss();
//                            if(response.code()==200){
//                                title = "Successfully Redeemed";
//                                message = "Thank You for your disposal! You will receive your reward shortly.";
//                                redeemMessage(new DialogInterface.OnClickListener() {
//                                    @Override
//                                    public void onClick(DialogInterface dialog, int which) {
//                                        Intent intentNew = new Intent(RedeemPicActivity.this, Dashboard.class);
//                                        startActivity(intentNew);
//                                    }
//                                });
//                            }else{
//                                title = "Redemption Error";
//                                if(response.body()!=null){
//                                    message = response.body().toString();
//                                }else{
//                                    message = "Unfortunately we encountered an error while verifying your disposal. Please try again.";
//                                }
//                                redeemMessage(new DialogInterface.OnClickListener() {
//                                    @Override
//                                    public void onClick(DialogInterface dialog, int which) {
//                                        Intent intentNew = new Intent(RedeemPicActivity.this, Dashboard.class);
//                                        startActivity(intentNew);
//                                    }
//                                });
//                            }
//                        }
//
//                        @Override
//                        public void onFailure(Call<ResponseBody> call, Throwable t) {
//                            verifyProgressDialog.dismiss();
//                            title = "Redemption Error";
//                            message = "Unfortunately we encountered an error while verifying your disposal. Please try again.";
//                            redeemMessage(new DialogInterface.OnClickListener() {
//                                @Override
//                                public void onClick(DialogInterface dialog, int which) {
//                                    Intent intentNew = new Intent(RedeemPicActivity.this, Dashboard.class);
//                                    startActivity(intentNew);
//                                }
//                            });
//
//
//                        }
//                    });
//
//
//                }else{
//                    progressDialog.dismiss();
//                    title = "Upload Error";
//                    message = "Unfortunately we encountered an error while uploading your picture. Check you internet connection and try again.";
//                    redeemMessage(new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialog, int which) {
//                            Intent intentNew = new Intent(RedeemPicActivity.this, Dashboard.class);
//                            startActivity(intentNew);
//                        }
//                    });
//
//
//                }
//            }
//
//            @Override
//            public void onFailure(Call<Void> call, Throwable t) {
//                progressDialog.dismiss();
//                title = "Upload Error";
//                message = "Unfortunately we encountered an error while uploading your picture. Check you internet connection and try again.";
//                redeemMessage(new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        Intent intentNew = new Intent(RedeemPicActivity.this, Dashboard.class);
//                        startActivity(intentNew);
//                    }
//                });
//            }
//        });

    }

    private void createCameraPreview() {
        try{
            SurfaceTexture texture = textureView.getSurfaceTexture();
            assert  texture != null;
            texture.setDefaultBufferSize(imageDimension.getWidth(),imageDimension.getHeight());
            Surface surface = new Surface(texture);
            captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            captureRequestBuilder.addTarget(surface);
            cameraDevice.createCaptureSession(Arrays.asList(surface), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                    if(cameraDevice == null)
                        return;
                    cameraCaptureSessions = cameraCaptureSession;
                    updatePreview();
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
                    Toast.makeText(RedeemPicActivity.this, "Changed", Toast.LENGTH_SHORT).show();
                }
            },null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void updatePreview() {
        if(cameraDevice == null)
            Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show();
        captureRequestBuilder.set(CaptureRequest.CONTROL_MODE,CaptureRequest.CONTROL_MODE_AUTO);
        try{
            cameraCaptureSessions.setRepeatingRequest(captureRequestBuilder.build(),null,mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }


    private void openCamera() {
        CameraManager manager = (CameraManager)getSystemService(Context.CAMERA_SERVICE);
        try{
            cameraId = manager.getCameraIdList()[0];
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
            StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            assert map != null;
            imageDimension = map.getOutputSizes(SurfaceTexture.class)[0];
            //Check realtime permission if run higher API 23
            if(ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
            {
                ActivityCompat.requestPermissions(this,new String[]{
                        Manifest.permission.CAMERA,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                },REQUEST_CAMERA_PERMISSION);
                return;
            }
            manager.openCamera(cameraId,stateCallback,null);

        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    TextureView.SurfaceTextureListener textureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int i, int i1) {
            openCamera();
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int i, int i1) {

        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {

        }
    };

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == REQUEST_CAMERA_PERMISSION)
        {
            if(grantResults[0] != PackageManager.PERMISSION_GRANTED)
            {
                Toast.makeText(this, "You can't use camera without permission", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        startBackgroundThread();
        if(textureView.isAvailable())
            openCamera();
        else
            textureView.setSurfaceTextureListener(textureListener);
    }

    @Override
    protected void onPause() {
        stopBackgroundThread();
        super.onPause();
    }

    private void stopBackgroundThread() {
        mBackgroundThread.quitSafely();
        try{
            mBackgroundThread.join();
            mBackgroundThread= null;
            mBackgroundHandler = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void startBackgroundThread() {
        mBackgroundThread = new HandlerThread("Camera Background");
        mBackgroundThread.start();
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
    }

    private void redeemMessage(DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(RedeemPicActivity.this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .create()
                .show();
    }
}
