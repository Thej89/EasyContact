package com.thej.fyp.easycontact.db;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.thej.fyp.easycontact.beans.UserDto;

public class DatabaseHandler extends SQLiteOpenHelper {
	private static final int DATABASE_VERSION = 1;

	private static final String DATABASE_NAME = "contactsManager";

	private static final String TABLE_PERSON = "person";

	private static final String KEY_ID = "id";
	private static final String KEY_NAME = "name";
	private static final String KEY_PH_NO = "phone_number";
	private static final String KEY_EMAIL = "email";
	private static final String KEY_IMAGE = "image";

	public DatabaseHandler(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		String CREATE_CONTACTS_TABLE = "CREATE TABLE " + TABLE_PERSON + "("
				+ KEY_ID + " INTEGER PRIMARY KEY," + KEY_NAME + " TEXT,"
				+ KEY_PH_NO + " TEXT" + "," + KEY_EMAIL + " TEXT" + ","
				+ KEY_IMAGE + " BLOB)";
		db.execSQL(CREATE_CONTACTS_TABLE);

	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int arg1, int arg2) {
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_PERSON);
		onCreate(db);

	}

	public void addPerson(UserDto person) {
		SQLiteDatabase db = this.getWritableDatabase();

		ContentValues values = new ContentValues();
//		values.put(KEY_NAME, person.getName());
		values.put(KEY_PH_NO, person.getMobileNo());
		values.put(KEY_EMAIL, person.getEmail());
		values.put(KEY_IMAGE, person.getImage());

		db.insert(TABLE_PERSON, null, values);
		db.close();
	}

	UserDto getPerson(int id) {
		SQLiteDatabase db = this.getReadableDatabase();

		Cursor cursor = db.query(TABLE_PERSON, new String[] { KEY_ID, KEY_NAME,
				KEY_PH_NO, KEY_EMAIL, KEY_IMAGE }, KEY_ID + "=?",
				new String[] { String.valueOf(id) }, null, null, null, null);
		if (cursor != null)
			cursor.moveToFirst();
		UserDto person = new UserDto();
		person.setEmail(cursor.getString(4));
		person.setId(cursor.getInt(1));
		person.setImage(cursor.getBlob(5));
//		person.setName(cursor.getString(2));
		person.setMobileNo(cursor.getString(3));

		return person;
	}

	public List getAllPeople() {
		List persontList = new ArrayList();
		String selectQuery = "SELECT " + KEY_ID + " , " + KEY_NAME + " , "
				+ KEY_PH_NO + " , " + KEY_EMAIL + " , " + KEY_IMAGE
				+ " , FROM " + TABLE_PERSON;

		SQLiteDatabase db = this.getWritableDatabase();
		Cursor cursor = db.rawQuery(selectQuery, null);
		if (cursor.moveToFirst()) {
			do {
				UserDto person = new UserDto();
				person.setEmail(cursor.getString(4));
				person.setId(cursor.getInt(1));
				person.setImage(cursor.getBlob(5));
//				person.setName(cursor.getString(2));
				person.setMobileNo(cursor.getString(3));
				persontList.add(person);
			} while (cursor.moveToNext());
		}
		return persontList;
	}

	public void deletePerson(int id) {
		SQLiteDatabase db = this.getWritableDatabase();
		db.delete(TABLE_PERSON, KEY_ID + " = ?",
				new String[] { String.valueOf(id) });
		db.close();
	}
}
