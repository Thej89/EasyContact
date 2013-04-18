package com.thej.fyp.easycontact;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

/**
 * 
 * @author Thejanee To display the fb login screen if the user is not logged in
 * 
 */
public class LoginFragment extends Fragment {

	// set up the view from the layout, login.xml
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.login, container, false);

		// set method to the button for sign up
		final Button btnsignup = (Button) view
				.findViewById(R.id.create_easycontact_login_button);
		btnsignup.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				// Perform action on click 'Sign Up' button
				Intent intent = new Intent();
				intent.setClass(getActivity(), SignupActivity.class);
				startActivity(intent);
			}
		});
		return view;
	}

}
