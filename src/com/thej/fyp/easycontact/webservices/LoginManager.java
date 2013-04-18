package com.thej.fyp.easycontact.webservices;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import android.os.AsyncTask;

public class LoginManager extends AsyncTask<String, Integer, String> {

	protected void onProgressUpdate(Integer... progress) {

	}

	protected void onPostExecute(String result) {

		System.out.println(" Results :  "+ result);
	}

	@Override
	protected String doInBackground(String... params) {
		// String _path = "/mnt/sdcard/image/sunset_bike_ride.jpg";  // for send image :) 
		String results = null;
		InputStream is = null;
		String userName = "userName";
		String password = "password"; 
		/*
		 * byte[] bytearray = null; try { is = new FileInputStream(_path); if
		 * (_path != null) try { bytearray = streamToBytes(is); } finally {
		 * is.close(); } } catch (Exception e) { }
		 */
		SoapObject request = new SoapObject(params[0], params[3]);
		SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(
				SoapEnvelope.VER11);
		PropertyInfo p2 = new PropertyInfo();
		p2.setName("arg0");
		p2.setValue(userName);
		p2.setType(String.class); //
		request.addProperty(p2);

		envelope.setOutputSoapObject(request);

		HttpTransportSE ht = new HttpTransportSE(params[1]);

		try {
			ht.call(params[2], envelope);
			SoapPrimitive response = (SoapPrimitive) envelope.getResponse();

			if (response != null) {
				results = response.toString();
			} else {
				results = "Error";
			}


		} catch (Exception e) {

			System.out.println("Error" + e);

			results = e.toString();
		}

		System.out.println(" Results : " + results);
		return results;
	}

	public static byte[] streamToBytes(InputStream is) {
		ByteArrayOutputStream os = new ByteArrayOutputStream(1024);
		byte[] buffer = new byte[1024];
		int len;
		try {
			while ((len = is.read(buffer)) >= 0) {
				os.write(buffer, 0, len);
			}
		} catch (java.io.IOException e) {
		}
		return os.toByteArray();
	}

}
