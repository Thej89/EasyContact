package com.thej.fyp.easycontact;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.SoapFault;
import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;
import org.xmlpull.v1.XmlPullParserException;

import android.R.integer;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.os.AsyncTask;
import android.text.method.Touch;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

public class DrawRect extends View implements View.OnClickListener {

	private Rect r;
	public ArrayList<Rect> faceRecArray = new ArrayList<Rect>();

	private static final String TAG = "DrawRect"; // for debugging purposes

	private static final String NAMESPACE = "http://sessionbean.thej.fyp/";
	private static final String URL = "http://192.168.1.100:8080/EasyContactWebservice1/EasyContactWebservice1?wsdl";
	private static final String SOAP_ACTION = "http://sessionbean.thej.fyp/recognizeFaces";
	private static final String METHOD_NAME = "recognizeFaces";

	public DrawRect(Context context) {
		super(context);
		r = new Rect(0, 0, 1, 1);

	}

	public void update(Rect r) {
		// faceRecArray.add(r);
		this.r = r;
		invalidate();
	}

	@Override
	public void onDraw(Canvas canvas) { System.out.println("onDraw");
		canvas.drawColor(0x00AAAAAA);
		Paint myPaint = new Paint();
		myPaint.setColor(Color.YELLOW);
		myPaint.setStrokeWidth(10);
		myPaint.setStyle(Paint.Style.STROKE);

		Paint textPaint = new Paint();
		textPaint.setFakeBoldText(true);
		textPaint.setTextSize(12);

		for (int i = 0; i < faceRecArray.size(); i++) {
			canvas.drawRect(faceRecArray.get(i), myPaint);
			canvas.drawText("" + CamActivity.resultString, faceRecArray.get(i).centerX(), faceRecArray
					.get(i).centerY(), textPaint);
//			CamActivity.mCamera.setPreviewCallback(previewCallback);
//			CamActivity.mCamera.setOneShotPreviewCallback(previewCallback);			
			//CamActivity.mCamera.stopPreview();
			// super.onDraw(canvas);
		}
		
//		if(CamActivity.takeImgeFlag){
//		CamActivity.mCamera.takePicture(null, null, jpegCallback);
//		System.out.println("picture taken");
//		AsyncTask<String, integer, String> result = new Recognize()
//				.execute("");
//		Log.d("webserviceeee", "mesg: " + result);
//		}
		// while (CamActivity.takeImgeFlag) {
		// System.out.println("takeimage: "+CamActivity.takeImgeFlag);
		// CamActivity.mCamera.setPreviewCallback(previewCallback);
		// // mCamera.takePicture(null, null, jpegCallback);
		// System.out.println("picture taken");
		// AsyncTask<String, integer, String> result = new Recognize()
		// .execute("");
		// Log.d("webserviceeee", "mesg: " + result);
		// } //CamActivity.mCamera.stopPreview();
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
						"/mnt/sdcard/t1.jpg", System.currentTimeMillis()));
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

	private class Recognize extends AsyncTask<String, integer, String> {

		@Override
		protected String doInBackground(String... params) {

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

			// request.addProperty(p3);
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
				Log.d("responseee: ", "" + envelope.getResponse());
			} catch (SoapFault e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			// dialog.dismiss();
			System.out.println(" Results : " + result);
			return result;

		}
	}

	Camera.PreviewCallback previewCallback = new Camera.PreviewCallback() {
		public void onPreviewFrame(byte[] data, Camera camera) {
			FileOutputStream outStream = null;
			try {
				// outStream = new FileOutputStream(String.format(
				// "/mnt/sdcard/t1.jpg", System.currentTimeMillis()));
				// // croppedImage.compress(Bitmap.CompressFormat.JPEG, 100,
				// // outStream);
				// outStream.write(data);
				// outStream.close();
				// Log.d(TAG, "onPictureTaken - wrote bytes: " + data.length);

				BitmapFactory.Options opts = new BitmapFactory.Options();
				Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0,
						data.length);// ,opts);
				outStream = new FileOutputStream("/mnt/sdcard/t1.jpg");
				bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outStream);
			} catch (Exception e) {

			}
		}

	};

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		Log.d(TAG, "ddddd");
		System.out.println(TAG + " innnnn");
		int touchX = (int) event.getX();
		int touchY = (int) event.getY();
		int recFlag = 0;
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			System.out.println("DOWN");
			for (Rect rect : faceRecArray) {
				System.out.println("for");
				if (rect.contains(touchX, touchY)) {
					switch (recFlag) {
					case 0:
						System.out.println("its 0");
						break;
					case 1:
						System.out.println("its 1");
						break;
					default:
						break;
					}
				}
				recFlag++;
			}
			break;
		case MotionEvent.ACTION_UP:
			System.out.println("up");

			break;

		default:
			break;
		}
		return super.onTouchEvent(event);
	}

	@Override
	public void onClick(View v) {
		System.out.println("uuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuu");
		Toast.makeText(getContext(), "heloo!", Toast.LENGTH_SHORT).show();

	}

}