package com.thej.fyp.easycontact;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

import org.apache.http.util.ByteArrayBuffer;
import org.json.JSONArray;
import org.json.JSONObject;
import org.ksoap2.SoapEnvelope;
import org.ksoap2.SoapFault;
import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;
import org.xmlpull.v1.XmlPullParserException;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.android.Facebook;
import com.facebook.android.Util;
import com.facebook.model.GraphUser;
import com.facebook.widget.ProfilePictureView;
import com.thej.fyp.easycontact.beans.UserDto;
import com.thej.fyp.easycontact.util.SessionManager;

/**
 * 
 * @author Thejanee The class for the auherticated UI after user is logged in
 * 
 */
public class AuthenticatedFragment extends Fragment {
	// webservice information
	private static final String NAMESPACE = "http://sessionbean.thej.fyp/";
	private static final String URL = "http://192.168.1.100:8080/EasyContactWebservice1/EasyContactWebservice1?wsdl";
	private static final String SOAP_ACTION = "http://sessionbean.thej.fyp/saveUserDetails";
	private static final String METHOD_NAME = "saveUserDetails";

	Facebook mFacebook = new Facebook("315257261919737");
	// Container Activity must implement this interface
	private static final String TAG = "AuthenticatedFragment"; // for debugging
																// purposes

	// variable to set the users name and the profile picture
	private ProfilePictureView profilePictureView;
	private TextView userNameView;
	private TextView userId;
	private TextView userfirstName;
	private TextView userlastName;
	private TextView userUsername;
	private ImageButton backButton;
	private ImageButton logoutButton;

	private UiLifecycleHelper uiHelper;
	private Session.StatusCallback callback = new Session.StatusCallback() {
		@Override
		public void call(final Session session, final SessionState state,
				final Exception exception) {
			onSessionStateChange(session, state, exception);
		}
	};

	private static final int REAUTH_ACTIVITY_CODE = 100;
	private Bundle bundle;

	private static final int TAKE_PICTURE_CODE = 100;

	ProgressBar progressBar; // to display the progress bar

	// for image downloading purposes
	private File file;
	private String imgNumber;

	// session managr class
	SessionManager sessionmanager;

	// private ProgressDialog dialog = null;

	// set up the view from the layout, authenticated.xml
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		View view = inflater.inflate(R.layout.authenticated, container, false);
		bundle = savedInstanceState;
		// Find the user's profile picture custom view
		profilePictureView = (ProfilePictureView) view
				.findViewById(R.id.selection_profile_pic);
		profilePictureView.setCropped(true);

		// Find the user's views
		userNameView = (TextView) view.findViewById(R.id.selection_user_name);
		userId = (TextView) view.findViewById(R.id.label_fbId);
		userfirstName = (TextView) view.findViewById(R.id.label_firstname);
		userlastName = (TextView) view.findViewById(R.id.label_lastname);
		userUsername = (TextView) view.findViewById(R.id.label_username);
		
//		backButton = (ImageButton) view.findViewById(R.id.back_button);
//		logoutButton = (ImageButton) view.findViewById(R.id.login_button);

		progressBar = (ProgressBar) view.findViewById(R.id.progressBar);
		progressBar.setVisibility(1);
		
		// Session Manager 
        sessionmanager = new SessionManager(getActivity()); 
        
        //-----------------------------check later---------------------------------
        
//        backButton.setOnClickListener(new View.OnClickListener() {
//			public void onClick(View v) {
//				// Perform action on click 'Sign Up' button
//				Intent intent = new Intent();
//				sessionmanager.checkLogin();
//				intent.setClass(getActivity(), MenuPage.class);
//				startActivity(intent);
//			}
//		});
//        
//        logoutButton.setOnClickListener(new View.OnClickListener() {
//			public void onClick(View v) {
//				// Perform action on click 'Sign Up' button
////				Intent intent = new Intent();
////				intent.setClass(getActivity(), SignupActivity.class);
////				startActivity(intent);
//				sessionmanager.logoutUser();
//			}
//		});
        
		// Check for an open sessions in fb
		Session session = Session.getActiveSession();
		if (session != null && session.isOpened()) {
			// Get the user's data
			makeMeRequest(session);
		}
		return view;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		uiHelper = new UiLifecycleHelper(getActivity(), callback);
		uiHelper.onCreate(savedInstanceState);
	}

	private class getUserDetails extends AsyncTask {

		@Override
		protected Object doInBackground(Object... obj) {
			Bundle params = (Bundle) obj[0];
			JSONArray data;
			StringBuffer imageStringBuffer = new StringBuffer("");
			ArrayList<String> friendIdArray = new ArrayList<String>();
			String loggedinUser;
			String userfbId;

			try {
				progressBar.setVisibility(0);
				progressBar.setProgress(1);
				String f = "{ \"data\": " + mFacebook.request(params) + " }";
				Log.d("fbbbb", f);
				data = Util.parseJson(f).getJSONArray("data");

				progressBar.setMax(data.length());
				Log.d("dataaa", "" + data);
				Log.d("length", "hh: " + data.length());
				progressBar.setProgress(2);

				for (int i = 0; i < 3; i++) {
					JSONObject jObj = data.getJSONObject(i);
					Log.d("object", "" + jObj.toString());
					String friendId = jObj.getString("uid");
					System.out.println(friendId);
					friendIdArray.add(friendId); // adds the extracted friend id
													// into an array.

					// downlaod the profile pictures to the phone 
					long t1 = System.currentTimeMillis();
					DownloadFromUrl("https://graph.facebook.com/"
							+ friendIdArray.get(i)
							+ "/picture?width=200&height=200",
							friendIdArray.get(i) + ".jpg");
					long t2 = System.currentTimeMillis();
					progressBar.setProgress(i + 2);
					Log.d("DownloadComplete(ms)", "" + (t2 - t1));

					// convert it into a string before sending to the server
					Bitmap bitmapOrg = BitmapFactory
							.decodeFile("/mnt/sdcard/profilepictures/"
									+ friendIdArray.get(i) + ".jpg");
					ByteArrayOutputStream bao = new ByteArrayOutputStream();
					bitmapOrg.compress(Bitmap.CompressFormat.JPEG, 90, bao);
					byte[] ba = bao.toByteArray();
					String ba1 = Base64.encodeBytes(ba);

					// add it to the JSONOObject
					jObj.put("imageString", ba1);
					// Toast.makeText(getActivity(),""+(data.length()-1)+" pictures remaining...",Toast.LENGTH_SHORT).show();
				}
				Log.d("done", "---" + data);

				loggedinUser = sessionmanager.getUserDetails().getUsername();
				userfbId = sessionmanager.getUserDetails().getFbId();
				
				Log.d("CHH", ""+userfbId.trim().toString());
//				JSONArray userDetailsJArray = new JSONArray(loggedinUser);
				
				SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME);
				SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(
						SoapEnvelope.VER11);

				PropertyInfo p2 = new PropertyInfo();
				p2.setName("friendDetails");
				p2.setValue(data.toString());
				p2.setType(String.class);
				request.addProperty(p2);

				PropertyInfo p3 = new PropertyInfo();
				p3.setName("userDetails");
				p3.setValue(loggedinUser.trim().toString());
				p3.setType(String.class);
				request.addProperty(p3);
				
				PropertyInfo p4 = new PropertyInfo();
				p4.setName("userFbDetails");
				p4.setValue(userfbId.trim().toString());
				p4.setType(String.class);
				request.addProperty(p4);

				envelope.setOutputSoapObject(request);

				HttpTransportSE androidHttpTransport = new HttpTransportSE(URL);
				envelope.setOutputSoapObject(request);

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

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			return null;
		}

		@Override
		protected void onProgressUpdate(Object... values) {
			// TODO Auto-generated method stub
			super.onProgressUpdate(values[0]);
		}

	}

	// requests user's data
	private void makeMeRequest(final Session session) {
		final UserDto userDto = new UserDto();
		userDto.setUsername(sessionmanager.getUserDetails().getUsername());
		userDto.setEmail(sessionmanager.getUserDetails().getEmail());
		userDto.setFirst_name(sessionmanager.getUserDetails().getFirst_name());
		userDto.setLast_name(sessionmanager.getUserDetails().getLast_name());
		userDto.setId(sessionmanager.getUserDetails().getId());
		
		// Make an API call to get user data and define a
		// new callback to handle the response.
		Log.d("111661", "!!");
		Request request = Request.newMeRequest(session,
				new Request.GraphUserCallback() {
					@Override
					public void onCompleted(GraphUser user, Response response) {

						// If the response is successful
						if (session == Session.getActiveSession()) {
							if (user != null) {
								userDto.setFbId(user.getId());
								sessionmanager.createLoginSession(userDto);
								Log.d("SSSS", ""+sessionmanager.getUserDetails().getFbId());
								Log.d("SSSS", ""+sessionmanager.getUserDetails().getUsername());
								// Set the id for the ProfilePictureView
								// view that in turn displays the profile
								// picture.
								profilePictureView.setProfileId(user.getId());
								Log.d("1111333", "!!");
								// Set the Textview's text to the user's name.
								userNameView.setText(user.getName());
								userId.setText("User Id: "+user.getId());
								userfirstName.setText("User First Name: "+user.getFirstName());
								userlastName.setText("User Last Name: "+user.getLastName());
								userUsername.setText("Easycontact Username: "+sessionmanager.getUserDetails().getUsername());
								Log.d("11113333", "!!3333");

								Log.d("mytest", user.getName());

								Facebook mFacebook = new Facebook(
										"315257261919737");

								mFacebook.setSession(session);
								Log.d("tokane", "fff: "
										+ mFacebook.getSession()
												.getAccessToken());

								try {
									Bundle params = new Bundle();
									params.putString("method", "fql.query");
									Log.d("fbbbb", "hh12");
									params.putString(
											"query",
											"SELECT uid, first_name, last_name, locale, birthday, email, pic_square FROM user where uid IN ( SELECT uid2 FROM friend WHERE uid1 = me())");

									params.putString("access_token", mFacebook
											.getSession().getAccessToken());

									// dialog =
									// ProgressDialog.show(getActivity(), "",
									// "fetching user details....");
									new getUserDetails().execute(params);
									;

								} catch (Exception e) {
									// TODO Auto-generated catch block
									Log.d("mytest", "error: " + e.getMessage());
									e.printStackTrace();
								}
							}
						}
						if (response.getError() != null) {
							// Handle errors, will do so later.
						}
					}
				});
		request.executeAsync();

	}

	// tracks the session state changes
	private void onSessionStateChange(final Session session,
			SessionState state, Exception exception) {
		if (session != null && session.isOpened()) {
			// Get the user's data.
			makeMeRequest(session);
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == REAUTH_ACTIVITY_CODE) {
			uiHelper.onActivityResult(requestCode, resultCode, data);
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		uiHelper.onResume();
	}

	@Override
	public void onSaveInstanceState(Bundle bundle) {
		super.onSaveInstanceState(bundle);
		uiHelper.onSaveInstanceState(bundle);
		setUserVisibleHint(true);
	}

	@Override
	public void onPause() {
		super.onPause();
		uiHelper.onPause();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		uiHelper.onDestroy();
	}

	public void DownloadFromUrl(String DownloadUrl, String fileName) {

		try {
			File root = android.os.Environment.getExternalStorageDirectory();

			File dir = new File("/mnt/sdcard/profilepictures/");
			if (dir.exists() == false) {
				dir.mkdirs();
			}

			URL url = new URL(DownloadUrl); // you can write here any link
			File file = new File(dir, fileName);

			long startTime = System.currentTimeMillis();
			Log.d("DownloadManager", "download begining");
			Log.d("DownloadManager", "download url:" + url);
			Log.d("DownloadManager", "downloaded file name:" + fileName);

			/* Open a connection to that URL. */
			URLConnection ucon = url.openConnection();

			/*
			 * Define InputStreams to read from the URLConnection.
			 */
			InputStream is = ucon.getInputStream();
			BufferedInputStream bis = new BufferedInputStream(is);

			/*
			 * Read bytes to the Buffer until there is nothing more to read(-1).
			 */
			ByteArrayBuffer baf = new ByteArrayBuffer(5000);
			int current = 0;
			while ((current = bis.read()) != -1) {
				baf.append((byte) current);
			}

			/* Convert the Bytes read to a String. */
			FileOutputStream fos = new FileOutputStream(file);
			fos.write(baf.toByteArray());
			fos.flush();
			fos.close();
			Log.d("DownloadManager",
					"download ready in"
							+ ((System.currentTimeMillis() - startTime) / 1000)
							+ " sec");

		} catch (IOException e) {
			Log.d("DownloadManager", "Error: " + e);
		}

	}

}
