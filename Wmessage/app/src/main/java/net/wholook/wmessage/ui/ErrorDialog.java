package net.wholook.wmessage.ui;

import android.app.Dialog;
import android.content.Context;
//import android.content.DialogInterface;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.app.ActivityManager;


import android.os.Bundle;

import net.wholook.wmessage.R;

/**
 * Created by wholook on 14. 9. 23..
 */
public class ErrorDialog extends Dialog {

    String mMessage;
    //Context context = getContext();

    //Button btn;


    public ErrorDialog( Context context, String message,View.OnClickListener clickListener){
        super(context);
        mMessage = message;
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.error_dialog);

        TextView tv = (TextView)findViewById(R.id.tv_error_msg);
        tv.setText( message );

        Button btn = (Button)findViewById(R.id.btn_error_ok);

        btn.setOnClickListener( clickListener );
    }
    /*
    public void error_ok(View view){
        this.dismiss();
        String packageName = getPackageName();
        String className = getClass().getName();

        //kill background activities
        ActivityManager am = (ActivityManager)getSystemService(ACTIVITY_SERVICE);
        am.killBackgroundProcesses(packageName);

        //kill foreground activity
        System.exit(0);
    }
    */
}
