package camerachecker.example.com.camerachecker;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

public class MainActivity extends AppCompatActivity {

    Handler mHandler;
    HandlerThread mThread;
    CameraManager cameraManager;
    NotificationCompat.Builder mBuilder;
    NotificationManager notificationManager;
    private static final String CHANNEL_ID="Camera_Channel";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.v("cameracheckstatus","oncreate");
        checkPermission();

    }

    CameraManager.AvailabilityCallback availabilityCallback=new CameraManager.AvailabilityCallback() {
        @Override
        public void onCameraAvailable(@NonNull String cameraId) {
            super.onCameraAvailable(cameraId);
            Log.v("cameracheckstatus","Camera available "+cameraId);
            removeNotification();
        }

        @Override
        public void onCameraUnavailable(@NonNull String cameraId) {
            super.onCameraUnavailable(cameraId);
            Log.v("cameracheckstatus","Camera unavailable "+cameraId);
            createNotification(cameraId);
        }
    };

    Runnable runnable=new Runnable() {
        @Override
        public void run() {
            Log.v("cameracheckstatus","Camera handler");
        }
    };

    private void checkCamera() {

        mThread=new HandlerThread("camera handler");
        mThread.start();
        mHandler=new Handler(mThread.getLooper());

        cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            for (String cameraId:cameraManager.getCameraIdList()
                 ) {
                Log.v("cameracheckstatus","cameraid- "+cameraId);
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        if (cameraManager != null) {
            Log.v("cameracheckstatus","registering callback");
            cameraManager.registerAvailabilityCallback(availabilityCallback,mHandler);
        }else {
            Log.v("cameracheckstatus","camera manager null");
        }
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "CameraChecker";
            String description = "Camera checker";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
             notificationManager= getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }


    private void createNotification(String cameraId){
        createNotificationChannel();
        String text="Camera is in use";
        if(cameraId.equals("0"))
            text="Back Camera is in use";
        else
            text="Front Camera is in use";

         mBuilder= new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.sym_def_app_icon)
                .setContentTitle("Camera Checker")
                .setContentText(text)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
         notificationManager.notify(1,mBuilder.build());

    }

    private void removeNotification(){
        if(notificationManager!=null)
            notificationManager.cancel(1);
    }

    private void checkPermission(){
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {

            Log.v("cameracheckstatus","requesting permission");
            // No explanation needed; request the permission
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    99);


        }else {
            checkCamera();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 99: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    checkCamera();
                    Log.v("cameracheckstatus","permission granted");
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                } else {
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.CAMERA},
                            99);
                    Log.v("cameracheckstatus","permission denied");
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
            }

            // other 'case' lines to check for other
            // permissions this app might request.
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(cameraManager!=null)
            cameraManager.unregisterAvailabilityCallback(availabilityCallback);
        Log.v("cameracheckstatus","on destroy");
    }
}

