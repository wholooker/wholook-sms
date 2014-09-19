package net.wholook.wmessage.api;

import android.content.Context;
import android.app.Activity;
import android.content.SharedPreferences;


/**
 * Created by wholook on 14. 9. 3..
 */
public class WholookPreference {

    private final String PREF_NAME = "net.wholook.sms.pref";

    public final String PREF_FIELD_ID = "USER_ID";
    public final String PREF_FIELD_PASS = "USER_PASS";
    public final String PREF_FIELD_USER = "USER_NAME";

    public final String PREF_FIELD_GCM_REG_ID = "GCM_REG_ID";
    public final String PREF_FILED_APP_VERSION = "APP_VERSION";

    public final String PREF_FIELD_LOGOUT = "LOG_OUT";
    public final String PREF_FIELD_TIMESTAMP = "TIMESTAMP";

    public final String PREF_SERVICE_START_TIME = "START_TIME";
    public final String PREF_SEND_MESSAGE_TOTAL_COUNT = "SEND_MESSAGE_TOTAL";

    //public final String PREF_FIELD_LAST_SUCCESSED_IDS = "SUCCESSED_IDS";
    //public final String PREF_FIELD_SESSION_ID = "sessionid";
    //public final String PREF_FIELD_CSRFTOKEN = "csrftoken";


    private SharedPreferences mPref;
    private SharedPreferences.Editor meditor;

    public WholookPreference(Context c) {

        mPref  = c.getSharedPreferences(PREF_NAME,
                Activity.MODE_PRIVATE);
        meditor = mPref.edit();
    }

    public void put(String key, String value) {

        meditor.putString(key, value);
        meditor.commit();
    }

    public void put(String key, boolean value) {

        meditor.putBoolean(key, value);
        meditor.commit();
    }

    public void put(String key, int value) {

        meditor.putInt(key, value);
        meditor.commit();
    }
    public void put(String key, long value){

        meditor.putLong(key,value);
        //meditor.pu
        meditor.commit();
    }

    public String getValue(String key, String dftValue) {

        try {
            return mPref.getString(key, dftValue);
        } catch (Exception e) {
            return dftValue;
        }

    }

    public int getValue(String key, int dftValue) {

        try {
            return mPref.getInt(key, dftValue);
        } catch (Exception e) {
            return dftValue;
        }

    }

    public boolean getValue(String key, boolean dftValue) {

        try {
            return mPref.getBoolean(key, dftValue);
        } catch (Exception e) {
            return dftValue;
        }
    }

    public long getValue( String key, long dftValue){
        try{
            return mPref.getLong(key,dftValue);
        }catch( Exception e){
            return dftValue;
        }
    }

    public void clearAll(){

        meditor.clear();
        meditor.commit();
    }
}
