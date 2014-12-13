package net.wholook.wmessage.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import net.wholook.wmessage.api.WholookAPI;
import net.wholook.wmessage.api.WholookPreference;
import net.wholook.wmessage.service.WMessageService;

public class RestartReceiver extends BroadcastReceiver {

    public static final String ACTION_RESTART_SERVICE = "RestartReceiver.restart";

    public RestartReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving

        Log.d(WholookAPI.LOG_TAG, "RestartReceiver - onReceive");

        if(intent.getAction().equals(ACTION_RESTART_SERVICE)){

            WholookPreference pref = new WholookPreference( context );

            String user = pref.getValue(pref.PREF_FIELD_USER,"");
            //*
            if( user.equals("")){
                Log.d(WholookAPI.LOG_TAG, "RestartReceiver - onReceive - NO Start Service....");
                return;
            }

            if( WholookAPI.isNetWorkCheckerNotUI(context, 3, 5000)){
                Log.d(WholookAPI.LOG_TAG, "RestartReceiver - onReceive - Start Service....");
                Intent i = new Intent(context, WMessageService.class);
                context.startService(i);
            }
        }
    }
}
