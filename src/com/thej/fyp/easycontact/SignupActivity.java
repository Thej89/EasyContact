package com.thej.fyp.easycontact;

import java.io.IOException;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.SoapFault;
import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;
import org.xmlpull.v1.XmlPullParserException;

import android.R.integer;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.thej.fyp.easycontact.beans.UserDto;

public class SignupActivity extends Activity {

	private static final String NAMESPACE = "http://sessionbean.thej.fyp/";
	private static final String URL = "http://192.168.1.100:8080/EasyContactWebservice1/EasyContactWebservice1?wsdl";
	private static final String SOAP_ACTION = "http://sessionbean.thej.fyp/signup";
	private static final String METHOD_NAME = "signup";

	private ProgressDialog dialog = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_signup);
		// AsyncTask<String, Integer, String> result = new
		// LoginManager().execute(NAMESPACE , URL , SOAP_ACTION , METHOD_NAME );
		Button button = (Button) findViewById(R.id.btnRegister);
		final TextView username = (TextView) findViewById(R.id.signup_username);
		final TextView password = (TextView) findViewById(R.id.signup_password);
		final TextView fullname = (TextView) findViewById(R.id.signup_fullname);
		final TextView email = (TextView) findViewById(R.id.signup_email);

		button.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				UserDto signupUser = new UserDto();
				signupUser.setUsername(username.getText().toString());
				signupUser.setPassword(password.getText().toString());
				signupUser.setFirst_name(fullname.getText().toString());
				signupUser.setEmail(email.getText().toString());
				
				String[] userStr = new String[4];
				userStr[0] = username.getText().toString();
				userStr[1] = password.getText().toString();
				userStr[2] = fullname.getText().toString();
				userStr[3] = email.getText().toString();
				Log.d("Arrayy", ""+userStr[1]+"/"+userStr[2]);
				
				dialog = ProgressDialog.show(SignupActivity.this, "", "Creating Account..");

				AsyncTask<String[], integer, String> result = new AuthenticateUser().execute(userStr);
				Log.d("webserviceeee", "mesg: " + result);
				
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_signup, menu);
		return true;
	}
	
	private class AuthenticateUser extends AsyncTask<String[], integer, String> {

		@Override
		protected String doInBackground(String[]... params) {
			
//			UserDto user = new UserDto();
//			user = params[0];
			SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME);
			SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(
					SoapEnvelope.VER11);
			PropertyInfo p2 = new PropertyInfo();
			p2.setName("username");
			p2.setValue(params[0][0]);
			p2.setType(String.class); //
			request.addProperty(p2);

			PropertyInfo p3 = new PropertyInfo();
			p3.setName("password");
			p3.setValue(params[0][1]);
			p3.setType(String.class);
			request.addProperty(p3);
			
			PropertyInfo p4 = new PropertyInfo();
			p4.setName("firstname");
			p4.setValue(params[0][2]);
			p4.setType(String.class);
			request.addProperty(p3);
			
			PropertyInfo p5 = new PropertyInfo();
			p5.setName("email");
			p5.setValue(params[0][3]);
			p5.setType(String.class);
			request.addProperty(p5);
			
			Log.d("Arrayy", ""+params[0][0]+"/"+params[0][1]+"/"+params[0][2]+"/"+params[0][3]);
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
				Log.d("responseee: ", ""+envelope.getResponse());
				result = envelope.getResponse().toString();
				Log.d("responseee: ", ""+envelope.getResponse());
			} catch (SoapFault e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			dialog.dismiss();
			System.out.println(" Results : " + result);
			return result;
			
		}
		
		protected void onPostExecute(String result) {
			final TextView errorMessage = (TextView) findViewById(R.id.error_message);
			System.out.println(" Results :  "+ result);
			
			if (result != null && !("").equals(result) && ("success").equals(result)) {
				Intent intent = new Intent(SignupActivity.this, InitialPage.class);
	            startActivity(intent);
			} else {
				errorMessage.setText("Invalid credentials. Please enter correct credentials!!!");
			}
		}

	}
}
