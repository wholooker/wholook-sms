package net.wholook.wmessage.ui;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.view.View;
import android.telephony.TelephonyManager;
import android.widget.Toast;
import android.os.SystemClock;

import net.wholook.wmessage.R;
import net.wholook.wmessage.api.WholookAPI;
import net.wholook.wmessage.api.WholookError;
import net.wholook.wmessage.service.WMessageService;
import net.wholook.wmessage.api.WholookURLWithParams;
import net.wholook.wmessage.api.WholookJSONClient;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import net.wholook.wmessage.api.WholookPreference;
import net.wholook.wmessage.util.WholookUtil;

import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;
import org.json.JSONException;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

public class MyActivity extends ActionBarActivity {


    GoogleCloudMessaging gcm;
    AtomicInteger msgId = new AtomicInteger();
    Context context;
    EditText mDisplay;
    String regid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getApplicationContext();

        if (WholookUtil.checkPlayServices(this)) {
            gcm = GoogleCloudMessaging.getInstance(this);
            regid = WholookUtil.getRegistrationId(context);

            //if (regid.isEmpty()) {
            if( regid.equals("")){
                registerInBackground();
            }
        } else {
            Log.d(WholookAPI.LOG_TAG, "No valid Google Play Services APK found.");
        }

        WholookPreference pref = new WholookPreference( getApplicationContext() );

        if( pref.getValue(pref.PREF_FIELD_USER,"").equals("")){
            setContentView(R.layout.activity_my);
            startActivity(new Intent(this,Splash.class));

        }else{
            String id =null;
            String pass = null;
            String reg_id = null;

            id = pref.getValue(pref.PREF_FIELD_ID,"");
            pass = pref.getValue(pref.PREF_FIELD_PASS,"");
            reg_id = pref.getValue(pref.PREF_FIELD_GCM_REG_ID,"");

            LogIn(id,pass,reg_id);
        }
        /*
        mDisplay = (EditText) findViewById(R.id.et_info);

        TelephonyManager systemService = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);

        String deviceID = systemService.getDeviceId();
        String simSerialNumber = systemService.getSimSerialNumber();
        String PhoneNumber = systemService.getLine1Number();
        PhoneNumber = PhoneNumber.substring(PhoneNumber.length()-10,PhoneNumber.length());
        PhoneNumber="0"+PhoneNumber;

        mDisplay.setText("deviceID - " + deviceID + " ,simSerialNumber - " + simSerialNumber +
                " ,PhoneNumber - " + PhoneNumber + " ,android version - " + android.os.Build.VERSION.SDK_INT );
        */

    }

    @Override
    protected void onResume() {
        super.onResume();
        // Check device for Play Services APK.
        WholookUtil.checkPlayServices( this );
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.my, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void sendMessage( View view ){

        EditText et_id = (EditText) findViewById(R.id.id);
        EditText et_pass = (EditText) findViewById(R.id.pass);

        WholookPreference pref = new WholookPreference( getApplicationContext() );

        String id = et_id.getText().toString();
        String pass = et_pass.getText().toString();
        String reg_id = pref.getValue(pref.PREF_FIELD_GCM_REG_ID,"");

        LogIn(id,pass,reg_id);

    }

    public void LogIn(final String id,final String pass, final String reg_id ){

        if( WholookAPI.isNetWorkChecker(this, 3, 5000)){

            try{
                WholookURLWithParams mURLWithParams = new WholookURLWithParams();
                mURLWithParams.url = WholookAPI.server_url+"/api/handshake/2/";
                WholookAPI.wtls_passphrase = WholookAPI.get_aes_key();
                mURLWithParams.nameValuePairs.add( new BasicNameValuePair("passphrase",WholookAPI.wtls_passphrase));

                WholookJSONClient client = new WholookJSONClient( WholookAPI.httpclient,MyActivity.this,"보안 작업중...");

                client.setResultListner( new WholookJSONClient.ResultListener() {
                    @Override
                    public void responseSuccessfuly(String json) {
                        if( json.equals("OK!!")){
                            try{
                                WholookURLWithParams mURLWithParams = new WholookURLWithParams();
                                mURLWithParams.url = WholookAPI.server_url + "/api/login/";

                                JSONObject json_data = new JSONObject();
                                json_data.put("email",id);
                                json_data.put("password",pass);
                                json_data.put("wsms_token",reg_id);

                                mURLWithParams.nameValuePairs.add( new BasicNameValuePair("__sfdata",json_data.toString()));
                                WholookJSONClient client = new WholookJSONClient(WholookAPI.httpclient,MyActivity.this, "로그인 중...");

                                client.setResultListner(new WholookJSONClient.ResultListener(){
                                    @Override
                                    public void responseSuccessfuly(String json) {
                                        try{
                                            JSONObject result = new JSONObject(json);
                                            if( result.getInt("result") == 0 ){
                                                //로그인 성공
                                                String user_name = result.getString("name");

                                                WholookPreference pref = new WholookPreference( getApplicationContext() );
                                                pref.put(pref.PREF_FIELD_ID, id );
                                                pref.put(pref.PREF_FIELD_PASS, pass );
                                                pref.put(pref.PREF_FIELD_GCM_REG_ID, reg_id );
                                                pref.put( pref.PREF_FIELD_USER,user_name);
                                                pref.put( pref.PREF_SERVICE_START_TIME,System.currentTimeMillis());
                                                pref.put( pref.PREF_SEND_MESSAGE_TOTAL_COUNT,0);

                                                Intent service = new Intent(getApplicationContext(), WMessageService.class);

                                                moveTaskToBack( true );
                                                finish();
                                                startService(service);

                                            }else{

                                                setContentView(R.layout.activity_my);
                                                startActivity(new Intent(getApplicationContext(),Splash.class));
                                                Toast.makeText(context, "아이디 혹은 패스워드가 올바르지 않습니다.",Toast.LENGTH_SHORT).show();
                                            }

                                        }catch( JSONException e){
                                            String strMsg = "로그인 작업 실패 본사 연락 바랍니다. ERROR-CODE:(" + WholookError.LOGIN_RESULT_JSONPARSING +")";
                                            System.out.println( strMsg );
                                            Toast.makeText(context, strMsg,Toast.LENGTH_LONG).show();
                                        }
                                    }

                                    @Override
                                    public void responseFaild() {
                                        String strMsg = "로그인 작업 실패 본사 연락 바랍니다. ERROR-CODE:(" + WholookError.LOGIN_RESPONSE_FAIL +")";
                                        System.out.println( strMsg );
                                        Toast.makeText(context, strMsg,Toast.LENGTH_LONG).show();
                                    }
                                });
                                client.execute( mURLWithParams );
                            }catch( Exception e){
                                String strMsg = "로그인 작업 실패 본사 연락 바랍니다. ERROR-CODE:(" + WholookError.LOGIN_EXCEPTION +")";
                                System.out.println( strMsg );
                                Toast.makeText(context, strMsg,Toast.LENGTH_LONG).show();
                            }
                        }else{
                            String strMsg = "보안 작업 실패 본사 연락 바랍니다. ERROR-CODE:(" + WholookError.HANDSHAKE_UNKNOWN_RESULT +")";
                            System.out.println( strMsg );
                            Toast.makeText(context, strMsg,Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void responseFaild() {
                        String strMsg = "보안 작업 실패 본사 연락 바랍니다. ERROR-CODE:(" + WholookError.HANDSHAKE_RESPONSE_FAIL +")";
                        System.out.println( strMsg );
                        Toast.makeText(context, strMsg,Toast.LENGTH_LONG).show();
                    }
                });
                client.execute( mURLWithParams );
            }catch( Exception e){
                String strMsg = "보안 작업 실패 본사 연락 바랍니다. ERROR-CODE:(" + WholookError.HANDSHAKE_EXCEPTION +")";
                System.out.println( strMsg );
                Toast.makeText(context, strMsg,Toast.LENGTH_LONG).show();
            }
        }else{
            String strMsg = "네트웍이 연결되지 않았습니다. 폰 상태 확인후 재시도 하십시요.";
            System.out.println( strMsg );
            Toast.makeText(context, strMsg,Toast.LENGTH_LONG).show();
        }
    }

    public void checkClick( View view ){
        CheckBox et_id = (CheckBox) findViewById(R.id.checkbox);
        Button btn = (Button) findViewById(R.id.button);

        if( et_id.isChecked() ){
            btn.setEnabled( true );
        }else{
            btn.setEnabled( false );
        }
    }

    private void registerInBackground() {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                String msg = "";
                try {
                    if (gcm == null) {
                        gcm = GoogleCloudMessaging.getInstance(context);
                    }
                    regid = gcm.register(WholookAPI.SENDER_ID);
                    msg = "Device registered, registration ID=" + regid;


                    Log.d(WholookAPI.LOG_TAG, msg);


                    WholookUtil.storeRegistrationId(context, regid);
                } catch (IOException ex) {
                    msg = "Error :" + ex.getMessage();
                    // If there is an error, don't just keep trying to register.
                    // Require the user to click a button again, or perform
                    // exponential back-off.
                }
                return msg;
            }

            @Override
            protected void onPostExecute(String msg) {

                //mDisplay.append(msg + "\n");

            }
        }.execute(null, null, null);
    }
}