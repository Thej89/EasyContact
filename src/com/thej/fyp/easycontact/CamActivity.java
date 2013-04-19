package com.thej.fyp.easycontact;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import org.json.JSONArray;
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
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PointF;
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
import android.media.FaceDetector;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;
import android.widget.ImageView;

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
	private int facenumber;

	SessionManager sessionmanager;

	private static final String TAG = "CamActivity"; // for debugging purposes

	private static final String NAMESPACE = "http://sessionbean.thej.fyp/";
	private static final String URL = "http://192.168.1.100:8080/EasyContactWebservice1/EasyContactWebservice1?wsdl";
	private static final String SOAP_ACTION = "http://sessionbean.thej.fyp/recognizeFaces";
	private static final String METHOD_NAME = "recognizeFaces";

	private static final int NUM_FACES = 5; // max is 64
	
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
		
		params.setRotation(90);

		List<Size> sl = params.getSupportedPictureSizes(); //checks for supported sizes

		int w = 0, h = 0; // for best resolution
		for (Size s : sl) {
			w = s.width;
			h = s.height;
			break;
		}

		params.setPictureSize(w, h);

		mCamera.setParameters(params);

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
			AsyncTask<String, integer, String> result = new Recognize()
					.execute("");

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
//				Rect r = new Rect(10,10, 10, 10);
				Log.d(TAG + " : final REC: ", "face " + i + ": " + r);
				Log.d(TAG, "" + r.left + " , " + r.top + " , " + r.right
						+ " , " + r.bottom);

				drawView.faceRecArray.add(r);
				drawView.update(r);
			}

		} else {
			Log.d("else", "eeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeee");
			takeImgeFlag = false;
			Log.d(TAG, "else");
			Rect r = new Rect(0, 0, 0, 0);
			drawView.update(r);
		}
	}

	private class Recognize extends AsyncTask<String, integer, String> {

		@Override
		protected String doInBackground(String... params) {
			System.out.println("asyncccc");
			
			JSONArray data;
			
			if (takeImgeFlag) {
				mCamera.takePicture(null, null, jpegCallback);
				takeImgeFlag = false;
			}
			// convert it into a string before sending to the server
			Bitmap bitmapOrg = BitmapFactory.decodeFile("/mnt/sdcard/t.jpg");
			ByteArrayOutputStream bao = new ByteArrayOutputStream();
//			bitmapOrg.compress(Bitmap.CompressFormat.JPEG, 90, bao);
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
			Bitmap sourceImage;
			FaceDetector arrayFaces;
			FaceDetector.Face getAllFaces[] = new FaceDetector.Face[NUM_FACES];
			FaceDetector.Face getFace = null;
			int picWidth, picHeight;
			PointF eyesMidPts[] = new PointF[NUM_FACES];
			float eyesDistance[] = new float[NUM_FACES];

			Matrix matrix = new Matrix();
			// rotate the Bitmap
	        matrix.postRotate(90);

	        BitmapFactory.Options options=new BitmapFactory.Options();
            options.inSampleSize = 5;
            options.inPreferredConfig = Bitmap.Config.RGB_565;

            Bitmap bmp=BitmapFactory.decodeByteArray(data,0,data.length,options);

			try {
				sourceImage = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), matrix, false);
				
				picWidth = sourceImage.getWidth();
				picHeight = sourceImage.getHeight();
				System.out.println(picWidth + "x" + picHeight);

				arrayFaces = new FaceDetector(picWidth, picHeight, NUM_FACES);
				facenumber = arrayFaces.findFaces(sourceImage, getAllFaces);

//				sourceImage = Bitmap.createScaledBitmap(sourceImage,
//						picWidth / 2, picHeight / 2, false);
				
				Log.d("facee", ""+getAllFaces.length);
				Log.d("facee2", ""+facenumber);
				for (int i = 0; i < facenumber; i++) { 
					getFace = getAllFaces[i]; Log.d("facee", ""+getFace);
					try {
						PointF eyesMP = new PointF();
						getFace.getMidPoint(eyesMP);
						eyesDistance[i] = getFace.eyesDistance(); Log.d("distance", ""+eyesDistance[i]);
						eyesMidPts[i] = eyesMP; Log.d("MP", ""+eyesMidPts[i] );

						Log.i("Face", i + " " + getFace.confidence() + " "
								+ getFace.eyesDistance() + " " + "Pose: ("
								+ getFace.pose(FaceDetector.Face.EULER_X) + ","
								+ getFace.pose(FaceDetector.Face.EULER_Y) + ","
								+ getFace.pose(FaceDetector.Face.EULER_Z) + ")"
								+ "Eyes Midpoint: (" + eyesMidPts[i].x + ","
								+ eyesMidPts[i].y + ")");
					} catch (Exception e) {
						Log.d("catch", ""+e);
					}
				
					int width = (int) eyesDistance[i]*4; Log.d("width", ""+width);
					int height = (int) eyesDistance[i]*4; Log.d("height", ""+height);
					int x = (int) (eyesMidPts[i].x - (eyesDistance[i] * 2)); Log.d("x", ""+x);
					int y = (int) (eyesMidPts[i].y - (eyesDistance[i] * 2)); Log.d("y", ""+y);
//
					Bitmap croppedImage = Bitmap.createBitmap(sourceImage, x, y, width,
							height);  // crops the image captured
					FileOutputStream outStream2 = null;
					try {
						outStream2 = new FileOutputStream("/mnt/sdcard/cropped_"+i+".jpg");
					    croppedImage.compress(Bitmap.CompressFormat.JPEG, 100, outStream2);
					} catch (Exception e) {
					       e.printStackTrace();
					}
				}

				// canvas.drawRect((int) (myMidPoint.x - myEyesDistance * 2),
				// (int) (myMidPoint.y - myEyesDistance * 2),
				// (int) (myMidPoint.x + myEyesDistance * 2),
				// (int) (myMidPoint.y + myEyesDistance * 2), myPaint);

				Log.d(TAG, "onPictureTaken - wrote bytes: " + data.length);
			} finally {
			}
			Log.d(TAG, "onPictureTaken - jpeg");
		}
	};

	private Uri getTempUri() {
		return Uri.fromFile(getTempFile());
	}

	private File getTempFile() {
		if (isSDCARDMounted()) {

			File f = new File(Environment.getExternalStorageDirectory(),
					"/temporary_holder.jpg");

			try {
				f.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return f;
		} else {
			return null;
		}
	}

	private boolean isSDCARDMounted() {
		String status = Environment.getExternalStorageState();
		// Log.i("Main", "status "+status);
		if (status.equals(Environment.MEDIA_MOUNTED))
			return true;
		return false;
	}

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
