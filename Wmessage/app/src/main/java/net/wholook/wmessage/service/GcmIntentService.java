package net.wholook.wmessage.service;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.Context;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import net.wholook.wmessage.api.WholookAPI;
import net.wholook.wmessage.api.WholookPreference;
import net.wholook.wmessage.receiver.GcmBroadcastReceiver;
import net.wholook.wmessage.ui.MyActivity;


public class GcmIntentService extends IntentService {

    public GcmIntentService() {
        super("GcmIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        Log.d(WholookAPI.LOG_TAG, "Receiver -  PUSH " + intent.toString());

        WholookPreference pref = new WholookPreference( getApplicationContext() );
        String user = pref.getValue(pref.PREF_FIELD_USER,"");

        if( !user.equals("")){
            Log.d(WholookAPI.LOG_TAG, "Receiver -  PUSH ---> Service Run");
            Intent i = new Intent(getApplicationContext(), WMessageService.class);
            getApplicationContext().startService(i);
        }else
            Log.d(WholookAPI.LOG_TAG, "Receiver -  PUSH ---> LogOut service not Run");

        /*
        Bundle extras = intent.getExtras();
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);

        String messageType = gcm.getMessageType(intent);
        // 특정 메세지가 들어왔을때만 동작
        if( !extras.isEmpty() ){

            if(GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType)) {

                String app_code = extras.getString(WholookAPI.APP_CODE);
                String app_action = extras.getString(WholookAPI.APP_ACTION);

                if( app_code == null || app_action == null )return;

                if( app_code.equals(WholookAPI.APP_CODE_RESULT) && app_action.equals(WholookAPI.APP_ACTION_RESULT) ){
                    // 앱 코드와 액션 코드로 일단 판단해서 서비스 실행
                    WholookPreference pref = new WholookPreference( getApplicationContext() );
                    String user = pref.getValue(pref.PREF_FIELD_USER,"");

                    if( !user.equals("")){
                        Log.d(WholookAPI.LOG_TAG, "Receiver -  PUSH ---> Service Run");
                        Intent i = new Intent(getApplicationContext(), WMessageService.class);
                        getApplicationContext().startService(i);
                    }else
                        Log.d(WholookAPI.LOG_TAG, "Receiver -  PUSH ---> LogOut service not Run");
                }
            }
        }
        */

        //test code 푸쉬 성공률 체크
        /*
        int idx = Integer.parseInt(extras.getString("idx"));
        long timestamp = Long.parseLong(extras.getString("timestamp"));

        long delaytime = System.currentTimeMillis() - timestamp;

        Log.d(WholookAPI.LOG_TAG, "Message idx = " + idx + " || timestamp = " + timestamp + " || delaytime = " + delaytime );
        */


        // Release the wake lock provided by the WakefulBroadcastReceiver.
        GcmBroadcastReceiver.completeWakefulIntent(intent);
    }
}
