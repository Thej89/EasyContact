package com.thej.fyp.easycontact;

import com.thej.fyp.easycontact.util.SessionManager;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.Camera;
import android.hardware.Camera.Face;
import android.hardware.Camera.FaceDetectionListener;
import android.hardware.Camera.Size;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;

@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
public class CamActivity extends Activity implements FaceDetectionListener,
		SensorEventListener {

	private DrawRect drawView;
	private Camera mCamera;
	private CameraPreview mPreview;
	private SensorManager mSensorManager;
	private Sensor mAccelerometer;
	
	SessionManager sessionmanager;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// Session Manager 
        sessionmanager = new SessionManager(getApplicationContext());
		sessionmanager.checkLogin();
		
		setContentView(R.layout.activity_cam);

		mCamera = getCameraInstance();

		mPreview = new CameraPreview(this, mCamera);
		drawView = new DrawRect(this);
		addContentView(drawView, new LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT));
		FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
		preview.addView(mPreview);
		mCamera.setFaceDetectionListener(this);
		Camera.Parameters params = mCamera.getParameters();
		setCameraDisplayOrientation(this, 0, mCamera); // Corrects the orientation issues

		if (params.getMaxNumDetectedFaces() > 0) {
			mCamera.startFaceDetection();
		}

		mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		mAccelerometer = mSensorManager
				.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		mSensorManager.registerListener(this, mAccelerometer,
				SensorManager.SENSOR_DELAY_NORMAL);

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}
	
    @Override
    public void onBackPressed() {
    	super.onBackPressed();
    }

	public static Camera getCameraInstance() {
		Camera c = null;
		try {
			c = Camera.open();
		} catch (Exception e) {

		}
		return c;
	}

	@Override
	public void onFaceDetection(Face[] faces, Camera camera) {
		Log.d("mulaaa", "size: " + drawView.faceRecArray.size());
		drawView.faceRecArray.clear();
		if (faces.length > 0) {
			Log.d("FaceDetection", "face detected: " + faces.length
					+ " Face 1 Location X: " + faces[0].rect.centerX() + " Y: "
					+ faces[0].rect.centerY());
			for (int i = 0; i < faces.length; i++) {
				// gets the screen resolutions
				Display display = getWindowManager().getDefaultDisplay();
				Point point = new Point();
				display.getSize(point);
				double screenHeight = point.y;
				double screenWidth = point.x;
				// calculates the coordinates for the rectangle
				double x1 = ((faces[i].rect.centerX() + 950.0) / 2200.0)
						* screenHeight;
				double y1 = ((faces[i].rect.centerY() + 1000.0) / 2000.0)
						* screenWidth;
				int x = 480 - (int) y1;
				int y = (int) x1;
				int width = faces[i].rect.height() / 5;
				Rect r = new Rect(x - width, y - width, x + width, y + width);
				Log.d("final REC: ", "face " + i + ": " + r);

				drawView.faceRecArray.add(r);
				drawView.update(r);

			}
		} else {
			Rect r = new Rect(0,0,0,0);
			drawView.update(r);
		}
	}

	@Override
	public void onAccuracyChanged(Sensor arg0, int arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onSensorChanged(SensorEvent arg0) {
		// Log.d("onSensorChanged",
		// "innnn: "+mCamera.getParameters().getMaxNumDetectedFaces());
		Log.d("in if", ""+mCamera.getParameters().getMaxNumDetectedFaces());
		if (mCamera.getParameters().getMaxNumDetectedFaces() <= 0) {
			Log.d("in if", "yesss");
			Rect r = new Rect(0, 0, 0, 0);
			drawView.update(r);
		}

	}

	public static void setCameraDisplayOrientation(Activity activity,
			int cameraId, android.hardware.Camera camera) {
		android.hardware.Camera.CameraInfo info = new android.hardware.Camera.CameraInfo();
		android.hardware.Camera.getCameraInfo(cameraId, info);
		int rotation = activity.getWindowManager().getDefaultDisplay()
				.getRotation();
		int degrees = 0;
		switch (rotation) {
		case Surface.ROTATION_0:
			degrees = 0;
			Log.d("Rotationnnnn:", "0");
			break;
		case Surface.ROTATION_90:
			degrees = 90;
			Log.d("Rotationn:", "90");
			break;
		case Surface.ROTATION_180:
			degrees = 180;
			Log.d("Rotationn:", "180");
			break;
		case Surface.ROTATION_270:
			degrees = 270;
			Log.d("Rotationn:", "270");
			break;
		}

		int result;
		if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
			result = (info.orientation + degrees) % 360;
			result = (360 - result) % 360; // compensate the mirror
		} else { // back-facing
			result = (info.orientation - degrees + 360) % 360;
		}
		camera.setDisplayOrientation(result);
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		Log.d("onConfigurationChanged", "jjjjjjjjjjjjjjjjjjjjjjjjjj");
		super.onConfigurationChanged(newConfig);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
	}

}
