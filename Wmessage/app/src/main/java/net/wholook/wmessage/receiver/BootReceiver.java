package net.wholook.wmessage.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import net.wholook.wmessage.api.WholookAPI;
import net.wholook.wmessage.service.WMessageService;
import net.wholook.wmessage.api.WholookPreference;

public class BootReceiver extends BroadcastReceiver {
    public BootReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.

        Log.d(WholookAPI.LOG_TAG, "BootReceiver - onReceive");

        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            WholookPreference pref = new WholookPreference( context );

            String user = pref.getValue(pref.PREF_FIELD_USER,"");

            if( user.equals("")){
                Log.d(WholookAPI.LOG_TAG, "BootReceiver - onReceive - NO Start Service....");
                return;
            }

            if( WholookAPI.isNetWorkCheckerNotUI(context.getApplicationContext(), 1000, 5000)){
                Log.d(WholookAPI.LOG_TAG, "BootReceiver - onReceive - Start Service....");
                Intent i = new Intent(context, WMessageService.class);
                context.startService(i);
            }
        }

        //throw new UnsupportedOperationException("Not yet implemented");
    }
}
