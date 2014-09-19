package net.wholook.wmessage.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import net.wholook.wmessage.api.WholookAPI;
import net.wholook.wmessage.service.WMessageService;

public class PackageReceiver extends BroadcastReceiver {
    public PackageReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.
        //throw new UnsupportedOperationException("Not yet implemented");
        String action = intent.getAction();

        Log.d(WholookAPI.LOG_TAG, "PackageReceiver - onReceive");

        if (action.equals(Intent.ACTION_PACKAGE_ADDED)) {
            Log.d(WholookAPI.LOG_TAG, "PackageReceiver.......ACTION_PACKAGE_ADDED");

            // 앱이 설치되었을 때
        } else if (action.equals(Intent.ACTION_PACKAGE_REMOVED)) {
            Log.d(WholookAPI.LOG_TAG, "PackageReceiver.......ACTION_PACKAGE_REMOVED");

            // 앱이 삭제되었을 때
        } else if (action.equals(Intent.ACTION_PACKAGE_REPLACED)) {
            // 앱이 업데이트 되었을 때
            Log.d(WholookAPI.LOG_TAG, "PackageReceiver.......ACTION_PACKAGE_REPLACED");
            //Intent i = new Intent(context, WMessageService.class);
            //context.startService(i);
        }
    }
}
