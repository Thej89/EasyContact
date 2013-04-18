package com.thej.fyp.easycontact.db;

import android.net.Uri;
import android.provider.BaseColumns;


public class User {

	public User() {
	}

	public static final class Users implements BaseColumns {
		private Users() {
		}

		public static final Uri CONTENT_URI = Uri.parse("content://"
				+ EasyContactContentProvider.AUTHORITY + "/users");

		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.jwei512.notes";

		public static final String USER_ID = "_id";

		public static final String USER_NAME = "title";

		public static final String USER_EMAIL = "text";
	}

}

