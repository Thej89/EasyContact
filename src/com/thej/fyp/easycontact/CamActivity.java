package com.thej.fyp.easycontact;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.SoapFault;
import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;
import org.xmlpull.v1.XmlPullParserException;

import com.thej.fyp.easycontact.util.SessionManager;

import android.R.integer;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.Camera;
import android.hardware.Camera.Face;
import android.hardware.Camera.FaceDetectionListener;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.Size;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
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
	public static boolean takeImgeFlag = false;
	public static String resultString = "";

	SessionManager sessionmanager;

	private static final String TAG = "CamActivity"; // for debugging purposes

	private static final String NAMESPACE = "http://sessionbean.thej.fyp/";
	private static final String URL = "http://192.168.1.100:8080/EasyContactWebservice1/EasyContactWebservice1?wsdl";
	private static final String SOAP_ACTION = "http://sessionbean.thej.fyp/recognizeFaces";
	private static final String METHOD_NAME = "recognizeFaces";

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
		setCameraDisplayOrientation(this, 0, mCamera); // Corrects the
														// orientation issues

		if (params.getMaxNumDetectedFaces() > 0) {
			mCamera.startFaceDetection();
		}

		// System.out.println(params.getSupportedPictureSizes().toArray()
		// .toString());
		// params.setPictureSize(320, 240);
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
		if (mCamera != null) {
			/*
			 * Call stopPreview() to stop updating the preview surface.
			 */
			mCamera.stopPreview();

			/*
			 * Important: Call release() to release the camera for use by other
			 * applications. Applications should release the camera immediately
			 * in onPause() (and re-open() it in onResume()).
			 */
			mCamera.release();

			mCamera = null;
		}
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
		Log.d(TAG, "size: " + drawView.faceRecArray.size());
		drawView.faceRecArray.clear();
		takeImgeFlag = false;
		if (faces.length > 0) {
			System.out.println("detected");
			takeImgeFlag = true;
			AsyncTask<String, integer, String> result = new Recognize().execute("");
			
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
				Log.d(TAG + " : final REC: ", "face " + i + ": " + r);
				Log.d(TAG, "" + r.left + " , " + r.top + " , " + r.right
						+ " , " + r.bottom);

				drawView.faceRecArray.add(r);
				drawView.update(r);
				
				// Log.d("webserviceeee", "mesg: " + result);
			}
			
		} else { Log.d("else", "eeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeee");
			takeImgeFlag = false;
			Log.d(TAG, "else");
			Rect r = new Rect(0, 0, 0, 0);
			drawView.update(r);
		}
	}

	
	private class Recognize extends AsyncTask<String, integer, String> {

		@Override
		protected String doInBackground(String... params) { System.out.println("asyncccc");
			if(takeImgeFlag){
			 mCamera.takePicture(null, null, jpegCallback);
			 takeImgeFlag = false;
			}
			// convert it into a string before sending to the server
			Bitmap bitmapOrg = BitmapFactory.decodeFile("/mnt/sdcard/t.jpg");
			ByteArrayOutputStream bao = new ByteArrayOutputStream();
			bitmapOrg.compress(Bitmap.CompressFormat.JPEG, 90, bao);
			byte[] ba = bao.toByteArray();
			String ba1 = Base64.encodeBytes(ba);

			SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME);
			SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(
					SoapEnvelope.VER11);
			PropertyInfo p2 = new PropertyInfo();
			p2.setName("faces");
			p2.setValue(ba1);
			p2.setType(String.class); //
			request.addProperty(p2);

			// PropertyInfo p3 = new PropertyInfo();
			// p3.setName("password");
			// p3.setValue(params[0][1]);
			// p3.setType(String.class);
			// request.addProperty(p3);

			// PropertyInfo p4 = new PropertyInfo();
			// p4.setName("firstname");
			// p4.setValue(params[0][2]);
			// p4.setType(String.class);
			// request.addProperty(p3);
			//
			// PropertyInfo p5 = new PropertyInfo();
			// p5.setName("email");
			// p5.setValue(params[0][3]);
			// p5.setType(String.class);

			// request.addProperty(p5);
			Log.d("Arrayy", "" + params.toString());
			envelope.setOutputSoapObject(request);

			HttpTransportSE androidHttpTransport = new HttpTransportSE(URL);
			envelope.setOutputSoapObject(request);
			Log.d("Signup", "aaaaaaaaa");
			try {
				androidHttpTransport.call(SOAP_ACTION, envelope);

			} catch (IOException e) {

				e.printStackTrace();
			} catch (XmlPullParserException e) {

				e.printStackTrace();
			}
			String result = null;
			try {
				Log.d("responseee: ", "" + envelope.getResponse());
				result = envelope.getResponse().toString();
			} catch (SoapFault e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			// dialog.dismiss();
			resultString = result.toString();
			System.out.println(resultString);
			System.out.println(" Results : " + result);
			return result;

		}
	}

	/** Handles data for jpeg picture */
	PictureCallback jpegCallback = new PictureCallback() {
		public void onPictureTaken(byte[] data, Camera camera) {
			System.out.println("jpgcall");
			FileOutputStream outStream = null;
			try {
				// Bitmap bitmap = BitmapFactory.decodeByteArray(data , 0,
				// data.length);
				// Bitmap croppedImage = Bitmap
				// .createBitmap(
				// bitmap,
				// drawView.faceRecArray.get(0).left,
				// drawView.faceRecArray.get(0).top,
				// drawView.faceRecArray.get(0).width(),
				// drawView.faceRecArray.get(0).height());
				// Bitmap croppedImage = Bitmap.createBitmap(bitmap, 30, 30,
				// drawView.faceRecArray.get(0).width(),
				// drawView.faceRecArray.get(0).height());

				// File sdcard = Environment.getExternalStorageDirectory();
				// File f = new File (sdcard, "filename.png");
				// FileOutputStream out = new FileOutputStream(f);

				// write to local sandbox file system
				// outStream =
				// CameraDemo.this.openFileOutput(String.format("%d.jpg",
				// System.currentTimeMillis()), 0);
				// Or write to sdcard
				outStream = new FileOutputStream(String.format(
						"/mnt/sdcard/t.jpg", System.currentTimeMillis()));
				// croppedImage.compress(Bitmap.CompressFormat.JPEG, 100,
				// outStream);
				outStream.write(data);
				outStream.close();
				Log.d(TAG, "onPictureTaken - wrote bytes: " + data.length);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
			}
			Log.d(TAG, "onPictureTaken - jpeg");
		}
	};

	@Override
	public void onAccuracyChanged(Sensor arg0, int arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onSensorChanged(SensorEvent arg0) {
		// Log.d("onSensorChanged",
		// "innnn: "+mCamera.getParameters().getMaxNumDetectedFaces());
		if (mCamera.getParameters().getMaxNumDetectedFaces() <= 0) {
			Log.d(TAG, "yesss");
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
		Log.d(TAG + " : onConfigurationChanged", "jjjjjjjjjjjjjjjjjjjjjjjjjj");
		super.onConfigurationChanged(newConfig);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
	}

}
