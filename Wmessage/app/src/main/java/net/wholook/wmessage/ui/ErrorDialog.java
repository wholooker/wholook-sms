package net.wholook.wmessage.ui;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import net.wholook.wmessage.R;

/**
 * Created by wholook on 14. 9. 23..
 */
public class ErrorDialog extends Dialog {

    public ErrorDialog( Context context, String message,View.OnClickListener clickListener){
        super(context);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.error_dialog);

        TextView tv = (TextView)findViewById(R.id.tv_error_msg);
        tv.setText( message );
        Button btn = (Button)findViewById(R.id.btn_error_ok);
        btn.setOnClickListener( clickListener );
    }
}
