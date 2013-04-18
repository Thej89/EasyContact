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
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.thej.fyp.easycontact.beans.UserDto;
import com.thej.fyp.easycontact.util.SessionManager;

public class EasycontactLogin extends Activity {

	// webservice information
	private static final String NAMESPACE = "http://sessionbean.thej.fyp/";
	private static final String URL = "http://192.168.1.100:8080/EasyContactWebservice1/EasyContactWebservice1?wsdl";
	private static final String SOAP_ACTION = "http://sessionbean.thej.fyp/authenticateUser";
	private static final String METHOD_NAME = "authenticateUser";
	
	private ProgressDialog dialog = null;
	
	// session managr class
	SessionManager session;
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.easycontact_login);
		// AsyncTask<String, Integer, String> result = new
		// LoginManager().execute(NAMESPACE , URL , SOAP_ACTION , METHOD_NAME );
		Button easycontactLoginBtn = (Button) findViewById(R.id.easycontact_login);
		final TextView username = (TextView) findViewById(R.id.username_textbox);
		final TextView password = (TextView) findViewById(R.id.password_textbox);
//		final TextView errorMessage = (TextView) findViewById(R.id.error_message);
		
		// Session Manager
        session = new SessionManager(getApplicationContext()); 
		
		easycontactLoginBtn.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				UserDto user = new UserDto();
				user.setUsername(username.getText().toString());
				user.setPassword(password.getText().toString());
				
				dialog = ProgressDialog.show(EasycontactLogin.this, "", "loading authentication..");

				AsyncTask<UserDto, integer, String> result = new AuthenticateUser().execute(user);
				Log.d("webserviceeee12", "mesg: " + result.toString());
				Log.d("webserviceeee1", "mesg: " + ("true").equalsIgnoreCase(result.toString()));

			}
		});
	}

	private class AuthenticateUser extends AsyncTask<UserDto, integer, String> {

		@Override
		protected String doInBackground(UserDto... params) {
			
			UserDto user = new UserDto();
			user = params[0];
			SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME);
			SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(
					SoapEnvelope.VER11);
			PropertyInfo p2 = new PropertyInfo();
			p2.setName("username");
			p2.setValue(user.getUsername());
			p2.setType(String.class); //
			request.addProperty(p2);

			PropertyInfo p3 = new PropertyInfo();
			p3.setName("password");
			p3.setValue(user.getPassword());
			p3.setType(String.class);
			request.addProperty(p3);

			envelope.setOutputSoapObject(request);

			HttpTransportSE androidHttpTransport = new HttpTransportSE(URL);
			envelope.setOutputSoapObject(request);
			try {
				androidHttpTransport.call(SOAP_ACTION, envelope);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (XmlPullParserException e) {
				// TODO Auto-generated catch block
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

			if(result != null && ("true").equalsIgnoreCase(result.toString())){
				// Creating user login session
                session.createLoginSession(user);
			}
			return result;
			
		}
		
		protected void onPostExecute(String result) {
			final TextView errorMessage = (TextView) findViewById(R.id.error_message);
			System.out.println(" Results :  "+ result);
			
			if (result != null && !("").equals(result) && ("true").equals(result)) {
				Intent intent = new Intent(EasycontactLogin.this, MenuPage.class);
	            startActivity(intent);
			} else {
				errorMessage.setText("Invalid credentials. Please enter correct credentials!!!");
			}
		}

	}

}
