package com.thej.fyp.easycontact;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.SoapFault;
import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xmlpull.v1.XmlPullParserException;

import com.thej.fyp.easycontact.beans.UserDto;
import com.thej.fyp.easycontact.util.SessionManager;

import android.R.integer;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

public class MenuPage extends Activity {

	private static final String TAG = "MenuPage"; // for debugging purposes
	static URL u;

	// webservice information
	private static final String NAMESPACE = "http://sessionbean.thej.fyp/";
	private static final String URL = "http://192.168.1.100:8080/EasyContactWebservice1/EasyContactWebservice1?wsdl";
	private static final String SOAP_ACTION = "http://sessionbean.thej.fyp/backupContacts";
	private static final String METHOD_NAME = "backupContacts";

	private ProgressDialog dialogBackup = null;
	
	SessionManager sessionmanager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Session Manager 
        sessionmanager = new SessionManager(getApplicationContext());
		sessionmanager.checkLogin();
		
		setContentView(R.layout.menu_screen);

		ImageButton facebookSyncBtn = (ImageButton) findViewById(R.id.sync_facebook_button);
		ImageButton startRecogBtn = (ImageButton) findViewById(R.id.start_recognition_app);
		ImageButton backupContactsBtn = (ImageButton) findViewById(R.id.Backup_contacts_button);

		facebookSyncBtn.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent(MenuPage.this, MainActivity.class);
				startActivity(intent);
			}
		});

		startRecogBtn.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent(MenuPage.this, CamActivity.class);
				startActivity(intent);
			}
		});

		backupContactsBtn.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				dialogBackup = ProgressDialog
						.show(MenuPage.this,
								"",
								"Saving Contacts to /mnt/sdcard/test.xml and sending backup to EasyContact server... Please wait..");
				createXml(getApplication());
				// String backupStr =
				// getContactNumbers(getApplicationContext());

				new makeBackup().execute();
			}
		});
	}

	public void createXml(Context context) {
		try {

			DocumentBuilderFactory docFactory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

			String contactNumber = null;
			int contactNumberType = Phone.TYPE_MOBILE;
			String nameOfContact = null;

			// if (ApplicationConstants.phoneContacts.size() <= 0) {
			ContentResolver cr = context.getContentResolver();
			Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI, null,
					null, null, null);

			// root elements
			Document doc = docBuilder.newDocument();
			Element rootElement = doc.createElement("contacts");
			doc.appendChild(rootElement);

			if (cur.getCount() > 0) {
				while (cur.moveToNext()) {

					// staff elements
					Element contact = doc.createElement("contact");
					rootElement.appendChild(contact);

					UserDto user = new UserDto();
					String id = cur.getString(cur
							.getColumnIndex(BaseColumns._ID));
					user.setId(Integer.parseInt(id));

					contact.setAttribute("id", id);

					nameOfContact = cur
							.getString(cur
									.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
					user.setFirst_name(nameOfContact);

					// display name elements
					Element displayname = doc.createElement("displayname");
					displayname.appendChild(doc.createTextNode(nameOfContact));
					contact.appendChild(displayname);

					if (Integer
							.parseInt(cur.getString(cur
									.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
						Cursor phones = cr
								.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
										null,
										ContactsContract.CommonDataKinds.Phone.CONTACT_ID
												+ " = ?", new String[] { id },
										null);

						while (phones.moveToNext()) {
							contactNumber = phones.getString(phones
									.getColumnIndex(Phone.NUMBER));

							// display phone number elements
							Element phoneNumber = doc
									.createElement("phonenumber");
							phoneNumber.appendChild(doc
									.createTextNode(contactNumber));
							contact.appendChild(phoneNumber);

							contactNumberType = phones.getInt(phones
									.getColumnIndex(Phone.TYPE));

							// display contact number type elements
							Element phoneNumberType = doc
									.createElement("phonenumbertype");
							phoneNumberType.appendChild(doc
									.createTextNode("TYPE_MOBILE"));
							contact.appendChild(phoneNumberType);

							Log.i(TAG, "...Contact Name ...." + nameOfContact
									+ "...contact Number..." + contactNumber);

						}
						phones.close();
					}

				}
			}// end of contact name cursor
			cur.close();

			// write the content into xml file
			TransformerFactory transformerFactory = TransformerFactory
					.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			DOMSource source = new DOMSource(doc);
			StreamResult result = new StreamResult(new File(
					"/mnt/sdcard/test.xml"));

			transformer.transform(source, result);
			// for debugging purposes Output to console for testing
			StreamResult resultw = new StreamResult(System.out);
			transformer.transform(source, result);

			Toast toast = Toast
					.makeText(
							getApplicationContext(),
							"File Successfully Created in /mnt/sdcard/test.xml. Pleae wait till it makes backup in the server...",
							Toast.LENGTH_LONG);
			toast.show();

			System.out.println("File saved!");

		} catch (ParserConfigurationException pce) {
			pce.printStackTrace();
		} catch (TransformerException tfe) {
			tfe.printStackTrace();
		}
	}

	private class makeBackup extends AsyncTask<String, integer, String> {

		@Override
		protected String doInBackground(String... arg0) {

			// filename is filepath string
			BufferedReader br = null;
			try {
				br = new BufferedReader(new FileReader(new File(
						"/mnt/sdcard/test.xml")));
			} catch (FileNotFoundException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			String line;
			StringBuilder sb = new StringBuilder();

			try {
				while ((line = br.readLine()) != null) {
					sb.append(line.trim());
				}
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			Log.d("XML", "" + sb);

			SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME);
			SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(
					SoapEnvelope.VER11);

			PropertyInfo p2 = new PropertyInfo();
			p2.setName("xmlString");
			p2.setValue(sb.toString());
			p2.setType(String.class);
			request.addProperty(p2);

			PropertyInfo p3 = new PropertyInfo();
			p3.setName("username");
			p3.setValue(sessionmanager.getUserDetails().getUsername());
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
				Log.d("responseee: ", "" + envelope.getResponse());
				result = envelope.getResponse().toString();
				Log.d("responseee: ", "" + envelope.getResponse());
			} catch (SoapFault e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			dialogBackup.dismiss();
			System.out.println(" Results : " + result);
			return result;
		}

	}

	public String getContactNumbers(Context context) {
		String contactNumber = null;
		int contactNumberType = Phone.TYPE_MOBILE;
		String nameOfContact = null;
		StringBuilder sbBackup = new StringBuilder("");
		ArrayList<UserDto> userList = new ArrayList<UserDto>();
		// if (ApplicationConstants.phoneContacts.size() <= 0) {
		ContentResolver cr = context.getContentResolver();
		Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI, null,
				null, null, null);
		Log.d(TAG, "" + cur);
		Log.d(TAG, "" + cur.toString());
		if (cur.getCount() > 0) {
			while (cur.moveToNext()) {
				UserDto user = new UserDto();
				String id = cur.getString(cur.getColumnIndex(BaseColumns._ID));
				user.setId(Integer.parseInt(id));
				sbBackup.append(id).append("#");

				nameOfContact = cur
						.getString(cur
								.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
				user.setFirst_name(nameOfContact);
				sbBackup.append(nameOfContact).append("#");

				if (Integer
						.parseInt(cur.getString(cur
								.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
					Cursor phones = cr.query(
							ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
							null,
							ContactsContract.CommonDataKinds.Phone.CONTACT_ID
									+ " = ?", new String[] { id }, null);

					while (phones.moveToNext()) {
						contactNumber = phones.getString(phones
								.getColumnIndex(Phone.NUMBER));
						sbBackup.append(contactNumber).append("#");
						contactNumberType = phones.getInt(phones
								.getColumnIndex(Phone.TYPE));
						sbBackup.append(contactNumberType).append(",");
						Log.i(TAG, "...Contact Name ...." + nameOfContact
								+ "...contact Number..." + contactNumber);

					}
					phones.close();
				}

			}
		}// end of contact name cursor
		cur.close();
		Log.d("LISTTTT", "" + sbBackup);

		Toast toast = Toast.makeText(getApplicationContext(),
				"Backup Successfully Completed", Toast.LENGTH_LONG);
		toast.show();
		return sbBackup.toString();
		// }
	}

}
