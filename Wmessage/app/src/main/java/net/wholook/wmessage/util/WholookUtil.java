package net.wholook.wmessage.util;

import java.io.IOException;
import java.util.regex.Pattern;

import android.content.Context;
import android.app.Activity;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.text.format.Time;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import net.wholook.wmessage.api.WholookAPI;
import net.wholook.wmessage.api.WholookPreference;

/**
 * Created by wholook on 14. 9. 7..
 */
public class WholookUtil {

    public static boolean IsEmail( String str){
        return Pattern.matches("^[_a-z0-9-]+(.[_a-z0-9-]+)*@(?:\\w+\\.)+\\w+$", str);
    }

    public static boolean IsNumber( String str){
        return Pattern.matches("^[0-9]*$", str);
    }

    public static boolean IsMobilePhoneNumber( String str){
        return Pattern.matches("^\\d{2,3}-\\d{3,4}-\\d{4}$", str);
    }

    public static boolean IsPhoneNumber( String str){
        return Pattern.matches("^\\d{2,3}-\\d{3,4}-\\d{4}$", str);
    }

    public static int CompareTimeStamp( String str1, String str2 ){
        //ISO_OFFSET_DATE_TIME
        Time time1 = new Time(str1);

        Time time2 = new Time(str2);
        return Time.compare( time1,time2);
        //return 1;
    }

    public static int getAppVersion(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            // should never happen
            throw new RuntimeException("Could not get package name: " + e);
        }
    }

    public static String getRegistrationId(Context context) {

        WholookPreference pref = new WholookPreference(context);

        String registrationId = pref.getValue(pref.PREF_FIELD_GCM_REG_ID, "");

        //if (registrationId.isEmpty()) {
        if (registrationId.equals("")) {
            Log.d(WholookAPI.LOG_TAG, "Registration not found.");
            return "";
        }

        int registeredVersion = pref.getValue(pref.PREF_FILED_APP_VERSION, Integer.MIN_VALUE);

        int currentVersion = getAppVersion(context);
        if (registeredVersion != currentVersion) {
            Log.d(WholookAPI.LOG_TAG, "App version changed.");
            return "";
        }
        return registrationId;
    }

    public static boolean checkPlayServices(Activity activity) {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(activity);
        int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, activity,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Log.d(WholookAPI.LOG_TAG, "This device is not supported.");
                activity.finish();
            }
            return false;
        }
        return true;
    }

    public static void storeRegistrationId(Context context, String regId) {

        WholookPreference pref = new WholookPreference(context);

        int appVersion = getAppVersion(context);

        Log.d(WholookAPI.LOG_TAG, "Saving regId on app version " + appVersion);

        pref.put(pref.PREF_FIELD_GCM_REG_ID,regId);
        pref.put(pref.PREF_FILED_APP_VERSION,appVersion);

    }
}
