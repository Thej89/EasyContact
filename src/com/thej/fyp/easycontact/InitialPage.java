package com.thej.fyp.easycontact;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class InitialPage extends Activity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.start_screen);
		// AsyncTask<String, Integer, String> result = new
		// LoginManager().execute(NAMESPACE , URL , SOAP_ACTION , METHOD_NAME );
		Button easycontactLoginBtn = (Button) findViewById(R.id.easycontact_login_btn);
		Button easycontactSignUpBtn = (Button) findViewById(R.id.easycontact_signup_button);

		easycontactLoginBtn.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent(InitialPage.this, EasycontactLogin.class);
	            startActivity(intent);      
			}
		});
		
		easycontactSignUpBtn.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent(InitialPage.this, SignupActivity.class);
	            startActivity(intent);      
			}
		});
	}

}
