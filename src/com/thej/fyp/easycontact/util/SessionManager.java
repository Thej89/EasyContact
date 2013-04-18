
package com.thej.fyp.easycontact.util;
 
import java.util.HashMap;

import com.thej.fyp.easycontact.EasycontactLogin;
import com.thej.fyp.easycontact.beans.UserDto;
import com.thej.fyp.easycontact.db.User;
 
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
 
public class SessionManager {
    // Shared Preferences
    SharedPreferences pref;
 
    // Editor for Shared preferences
    Editor editor;
 
    // Context
    Context _context;
 
    // Shared pref mode
    int PRIVATE_MODE = 0;
 
    // Sharedpref file name
    private static final String PREF_NAME = "AndroidHivePref";
 
    // All Shared Preferences Keys
    private static final String IS_LOGIN = "IsLoggedIn";
 
    // variable public to access from outside
    public static final String KEY_USERNAME = "username";
    public static final String KEY_EMAIL = "email";
    public static final String KEY_FIRSTNAME = "firstname";
    public static final String KEY_LASTNAME = "lastname";
    public static final String KEY_BIRTHDAY = "birthday";
    public static final String KEY_USERID = "userid";
    public static final String KEY_ROLE = "role";
    public static final String KEY_FRIENDS = "friends";
    public static final String KEY_GENDER = "gender";
    public static final String KEY_FBID = "fbid";
    // Constructor
    public SessionManager(Context context){
        this._context = context;
        pref = _context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = pref.edit();
    }
 
    /**
     * Create login session
     * */
    public void createLoginSession(UserDto user){
        // Storing login value as TRUE
        editor.putBoolean(IS_LOGIN, true);
 
        // Storing details in pref
        editor.putString(KEY_USERNAME, user.getUsername()); 
        editor.putString(KEY_EMAIL, user.getEmail());
        editor.putString(KEY_FIRSTNAME, user.getFirst_name());
        editor.putString(KEY_LASTNAME, user.getLast_name());
        editor.putInt(KEY_USERID, user.getId());
        editor.putString(KEY_FBID, user.getFbId());
//        editor.putString(KEY_FRIENDS, user.get)
 
        // commit changes
        editor.commit();
    }   
 
    /**
     * Check login method will check user login status
     * If false it will redirect user to login page
     * Else won't do anything
     * */
    public void checkLogin(){
        // Check login status
        if(!this.isLoggedIn()){
            // user is not logged in redirect him to Login Activity
            Intent i = new Intent(_context, EasycontactLogin.class);
            // Closing all the Activities
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
 
            // Add new Flag to start new Activity
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
 
            // Staring Login Activity
            _context.startActivity(i);
        }
 
    }
 
    /**
     * Get stored session data
     * */
    public UserDto getUserDetails(){
       UserDto sessionuser = new UserDto();
       sessionuser.setUsername(pref.getString(KEY_USERNAME, null));
       sessionuser.setEmail(pref.getString(KEY_EMAIL, null));
       sessionuser.setFbId(pref.getString(KEY_FBID, null));
 
        // return user
        return sessionuser;
    }
 
    /**
     * Clear session details
     * */
    public void logoutUser(){
        // Clearing all data from Shared Preferences
        editor.clear();
        editor.commit();
 
        // After logout redirect user to Loing Activity
        Intent i = new Intent(_context, EasycontactLogin.class);
        // Closing all the Activities
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
 
        // Add new Flag to start new Activity
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
 
        // Staring Login Activity
        _context.startActivity(i);
    }
 
    /**
     * Quick check for login
     * **/
    // Get Login State
    public boolean isLoggedIn(){
        return pref.getBoolean(IS_LOGIN, false);
    }
}