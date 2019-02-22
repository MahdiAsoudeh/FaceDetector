package com.mahdi20.facedetector;


import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.hardware.Camera;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.OrientationEventListener;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup.LayoutParams;
import android.widget.Toast;

import java.util.List;


public class CameraActivity extends Activity
        implements SurfaceHolder.Callback {

    public static final String TAG = CameraActivity.class.getSimpleName();

    private Camera mCamera;

    // We need the phone orientation to correctly draw the overlay:
    private int mOrientation;
    private int mOrientationCompensation;
    private OrientationEventListener mOrientationEventListener;

    // Let's keep track of the display rotation and orientation also:
    private int mDisplayRotation;
    private int mDisplayOrientation;

    // Holds the Face Detection result:
    private Camera.Face[] mFaces;

    // The surface view for the camera data
    private SurfaceView mView;

    private FaceOverlayView mFaceView;
    private Camera.FaceDetectionListener faceDetectionListener = new Camera.FaceDetectionListener() {
        @Override
        public void onFaceDetection(Camera.Face[] faces, Camera camera) {
            Log.d("onFaceDetection", "Number of Faces:" + faces.length);
            // Update the view now!
            mFaceView.setFaces(faces);
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mView = new SurfaceView(this);


        Toast.makeText(getApplicationContext(), "MahdiAsodeh@gmail.com", Toast.LENGTH_LONG).show();


//        dialog();


        setContentView(mView);
// Now create the OverlayView:
        mFaceView = new FaceOverlayView(this);
        addContentView(mFaceView, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
// Create and Start the OrientationListener:
        mOrientationEventListener = new SimpleOrientationEventListener(this);
        mOrientationEventListener.enable();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        SurfaceHolder holder = mView.getHolder();
        holder.addCallback(this);
    }

    @Override
    protected void onPause() {
        mOrientationEventListener.disable();
        super.onPause();
    }

    @Override
    protected void onResume() {
        mOrientationEventListener.enable();
        super.onResume();


    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        mCamera = Camera.open();
        mCamera.setFaceDetectionListener(faceDetectionListener);
        mCamera.startFaceDetection();
        try {
            mCamera.setPreviewDisplay(surfaceHolder);
        } catch (Exception e) {
            Log.e(TAG, "Could not preview the image.", e);
        }
    }


    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i2, int i3) {
// We have no surface, return immediately:
        if (surfaceHolder.getSurface() == null) {
            return;
        }
// Try to stop the current preview:
        try {
            mCamera.stopPreview();
        } catch (Exception e) {
            // Ignore...
        }
// Get the supported preview sizes:
        Camera.Parameters parameters = mCamera.getParameters();
        List<Camera.Size> previewSizes = parameters.getSupportedPreviewSizes();
        Camera.Size previewSize = previewSizes.get(0);
// And set them:
        parameters.setPreviewSize(previewSize.width, previewSize.height);
        mCamera.setParameters(parameters);
// Now set the display orientation for the camera. Can we do this differently?
        mDisplayRotation = Util.getDisplayRotation(CameraActivity.this);
        mDisplayOrientation = Util.getDisplayOrientation(mDisplayRotation, 0);
        mCamera.setDisplayOrientation(mDisplayOrientation);

        if (mFaceView != null) {
            mFaceView.setDisplayOrientation(mDisplayOrientation);
        }

// Finally start the camera preview again:
        mCamera.startPreview();
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        mCamera.setPreviewCallback(null);
        mCamera.setFaceDetectionListener(null);
        mCamera.setErrorCallback(null);
        mCamera.release();
        mCamera = null;
    }

    /**
     * We need to react on OrientationEvents to rotate the screen and
     * update the views.
     */
    private class SimpleOrientationEventListener extends OrientationEventListener {

        public SimpleOrientationEventListener(Context context) {
            super(context, SensorManager.SENSOR_DELAY_NORMAL);
        }

        @Override
        public void onOrientationChanged(int orientation) {
            // We keep the last known orientation. So if the user first orient
            // the camera then point the camera to floor or sky, we still have
            // the correct orientation.
            if (orientation == ORIENTATION_UNKNOWN) return;
            mOrientation = Util.roundOrientation(orientation, mOrientation);
            // When the screen is unlocked, display rotation may change. Always
            // calculate the up-to-date orientationCompensation.
            int orientationCompensation = mOrientation
                    + Util.getDisplayRotation(CameraActivity.this);
            if (mOrientationCompensation != orientationCompensation) {
                mOrientationCompensation = orientationCompensation;
                mFaceView.setOrientation(mOrientationCompensation);
            }
        }
    }


//    private void dialog() {
//        runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//
//                if (!isFinishing()) {
//                    new AlertDialog.Builder(CameraActivity.this)
//                            .setTitle("کانال ما")
//                            .setMessage("به کانال سر بزنیم و برگردیم")
//                            .setCancelable(true)
//                            .setPositiveButton("بازدید", new DialogInterface.OnClickListener() {
//                                @Override
//                                public void onClick(DialogInterface dialog, int which) {
//
//
//                                    String url = "http://t.me/mahdi20_com";
//                                    Intent i = new Intent(Intent.ACTION_VIEW);
//                                    i.setData(Uri.parse(url));
//                                    startActivity(i);
//
//
//                                }
//                            }).show();
//                }
//            }
//        });
//    }


}