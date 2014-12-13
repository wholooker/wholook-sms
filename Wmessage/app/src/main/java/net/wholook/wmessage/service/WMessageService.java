package net.wholook.wmessage.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.app.PendingIntent;
import android.app.AlarmManager;

import org.apache.http.message.BasicNameValuePair;

import android.app.NotificationManager;
import android.widget.Toast;
import android.content.IntentFilter;

import android.content.Context;
import android.telephony.SmsManager;
import android.os.AsyncTask;
import android.util.Log;

import net.wholook.wmessage.R;
import net.wholook.wmessage.api.SMSParer;
import net.wholook.wmessage.api.WholookAPI;
import net.wholook.wmessage.api.WholookError;
import net.wholook.wmessage.api.WholookJSONClient;
import net.wholook.wmessage.api.WholookURLWithParams;
import net.wholook.wmessage.receiver.BootReceiver;
import net.wholook.wmessage.ui.ConfigActivity;
import net.wholook.wmessage.ui.MainActivity;
import net.wholook.wmessage.api.WholookPreference;
import net.wholook.wmessage.receiver.PackageReceiver;
import net.wholook.wmessage.receiver.RestartReceiver;
import net.wholook.wmessage.exception.*;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;

import android.support.v4.app.NotificationCompat;

public class WMessageService extends Service {

    public final static String NOTI_TICKER = "WHOLOOK";
    public final static int NOTI_SERVICE_ID = 777;
    public final static int NOTI_SMS_SEND_STATE = 444;
    private NotificationCompat.Builder builder;
    private NotificationManager notifyManager;

    private final long ALAMER_TIMER = 1000*60*20; //타이머 분 단위
    private PackageReceiver pReceiver;
    private BootReceiver bReceiver;


    public WMessageService() {

    }

    public void onCreate() {
        super.onCreate();

        Log.d(WholookAPI.LOG_TAG,"WMessageService - onCreate");

        WholookPreference pref = new WholookPreference( this );
        String user = pref.getValue(pref.PREF_FIELD_USER,"");
        notifyManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        builder = new NotificationCompat.Builder(this);


        if( user.equals("")){
            Log.d(WholookAPI.LOG_TAG,"WMessageService - onCreate - user == null return");
            pref.put( pref.PREF_FIELD_LOGOUT,true);
            Intent service = new Intent(this, WMessageService.class);
            stopService(service);
            Intent login = new Intent(this, MainActivity.class);
            startActivity(login);
            return;
        }

        pReceiver = new PackageReceiver();
        IntentFilter pFilter = new IntentFilter(Intent.ACTION_PACKAGE_ADDED);
        pFilter.addAction(Intent.ACTION_PACKAGE_REMOVED);
        pFilter.addAction(Intent.ACTION_PACKAGE_REPLACED);
        pFilter.addDataScheme("package");
        registerReceiver(pReceiver, pFilter);

        bReceiver = new BootReceiver();
        IntentFilter bFilter = new IntentFilter();
        bFilter.addAction(Intent.ACTION_BOOT_COMPLETED);
        registerReceiver(bReceiver, bFilter);

        registerRestartAlarm( true );

        String title = user + "님의 Wholook SMS 서비스 실행중";
        String text = user + "님의 후룩 SMS SMS 서비스를 사용하지 않으시려면 클릭하세요.";
        String toast_txt = "Wholook SMS Service Run...";

        PendingIntent intent = PendingIntent.getActivity(
                this, 0,
                new Intent(this, ConfigActivity.class), 0);
        set_APP_Notification( title, text,toast_txt ,intent );
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        //
        Log.d(WholookAPI.LOG_TAG,"WMessageService - onStartCommand START");
        try{
            worker();
        }catch (Exception e){
            e.printStackTrace();
            return START_STICKY;
        }
        //START_STICKY,START_REDELIVER_INTENT
        return START_STICKY;
    }


    public void worker(){
        if( WholookAPI.isNetWorkCheckerNotUI(getApplicationContext(), 3, 5000)){
            try{
                WholookURLWithParams mURLWithParams = new WholookURLWithParams();
                mURLWithParams.url = WholookAPI.server_url + "/api/wsms/";

                mURLWithParams.nameValuePairs.add( new BasicNameValuePair("__sfdata","{}"));
                WholookJSONClient client = new WholookJSONClient(WholookAPI.httpclient);

                client.setResultListner(new WholookJSONClient.ResultListener(){
                    @Override
                    public void responseSuccessfuly(String json) {

                        try{
                            JSONObject result = null;
                            int result_code = -1;

                            try{
                                result = new JSONObject(json);
                                result_code = result.getInt("result");
                            }catch( JSONException j1){
                                String strMsg = "SMS가져오기 실패 ERROR-CODE:(" + WholookError.SERVICE_GETMESSAGELIST_UNKNOWN_RESULT +") json data - " + json;
                                System.out.println( strMsg + " || " + j1.getMessage());
                                j1.printStackTrace();
                                Toast.makeText(getApplicationContext(), strMsg,Toast.LENGTH_LONG).show();
                                return;
                            }

                            if( result_code == 0 ){

                                JSONArray data = null;

                                try{
                                    data = result.getJSONArray("data");
                                    //String timestamp = result.getString("timestamp");

                                }catch( JSONException j1){
                                    Log.d(WholookAPI.LOG_TAG,"메세지 리스트 가져왔는데 할일 없음");
                                    return;
                                }

                                if( data != null || data.length() != 0 ){
                                    SMSParer sms_Data = new SMSParer( data );
                                    ArrayList<SMSParer.Wsms> wsmsList =  sms_Data.getSMSList();

                                    if( wsmsList != null || wsmsList.size() != 0) new SendSMSJob().execute(wsmsList);
                                }
                            }else{
                                //peer not login 또 뭐가 있나?

                                Log.d(WholookAPI.LOG_TAG,"peer not login 로그인 재시도");

                                try{
                                    WholookURLWithParams mURLWithParams = new WholookURLWithParams();
                                    mURLWithParams.url = WholookAPI.server_url+"/api/handshake/2/";
                                    WholookAPI.wtls_passphrase = WholookAPI.get_aes_key();
                                    mURLWithParams.nameValuePairs.add( new BasicNameValuePair("passphrase",WholookAPI.wtls_passphrase));

                                    WholookJSONClient client = new WholookJSONClient( WholookAPI.httpclient);

                                    client.setResultListner( new WholookJSONClient.ResultListener() {
                                        @Override
                                        public void responseSuccessfuly(String json) {
                                            if( json.equals("OK!!")){
                                                try{
                                                    WholookURLWithParams mURLWithParams = new WholookURLWithParams();
                                                    mURLWithParams.url = WholookAPI.server_url + "/api/login/";

                                                    WholookPreference pref = new WholookPreference( getApplicationContext() );

                                                    String id = pref.getValue(pref.PREF_FIELD_ID,"");
                                                    String pass = pref.getValue(pref.PREF_FIELD_PASS,"");
                                                    String reg_id = pref.getValue(pref.PREF_FIELD_GCM_REG_ID,"");
                                                    String phone_number = pref.getValue(pref.PREF_FIELD_PHONE_NUMBER,"");

                                                    JSONObject json_data = new JSONObject();
                                                    json_data.put("email",id);
                                                    json_data.put("password",pass);
                                                    json_data.put("wsms_token",reg_id);
                                                    json_data.put("phone",phone_number);

                                                    mURLWithParams.nameValuePairs.add( new BasicNameValuePair("__sfdata",json_data.toString()));
                                                    WholookJSONClient client = new WholookJSONClient(WholookAPI.httpclient);

                                                    client.setResultListner(new WholookJSONClient.ResultListener(){
                                                        @Override
                                                        public void responseSuccessfuly(String json) {

                                                            try{
                                                                JSONObject result = new JSONObject(json);

                                                                if( result.getInt("result") == 0 ){
                                                                    //로그인 성공
                                                                    String user_name = result.getString("name");
                                                                    WholookPreference pref = new WholookPreference( getApplicationContext() );
                                                                    pref.put( pref.PREF_FIELD_USER,user_name);
                                                                    worker();
                                                                    return;
                                                                }else{

                                                                    Intent intent = new Intent(getApplicationContext(),MainActivity.class);
                                                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                                                    startActivity(intent);
                                                                    Toast.makeText(getApplicationContext(), "아이디 혹은 패스워드가 올바르지 않습니다.",Toast.LENGTH_SHORT).show();
                                                                }

                                                            }catch( JSONException e){
                                                                Intent intent = new Intent(getApplicationContext(),MainActivity.class);
                                                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                                                startActivity(intent);

                                                                String strMsg = "로그인 작업 실패 본사 연락 바랍니다. ERROR-CODE:(" + WholookError.LOGIN_RESULT_JSONPARSING +")";
                                                                System.out.println( strMsg );
                                                                Toast.makeText(getApplicationContext(), strMsg,Toast.LENGTH_LONG).show();
                                                            }
                                                        }

                                                        @Override
                                                        public void responseFaild() {
                                                            Intent intent = new Intent(getApplicationContext(),MainActivity.class);
                                                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                                            startActivity(intent);

                                                            String strMsg = "로그인 작업 실패 본사 연락 바랍니다. ERROR-CODE:(" + WholookError.LOGIN_RESPONSE_FAIL +")";
                                                            System.out.println( strMsg );
                                                            Toast.makeText(getApplicationContext(), strMsg,Toast.LENGTH_LONG).show();
                                                        }
                                                    });
                                                    client.execute( mURLWithParams );
                                                }catch( Exception e){
                                                    Intent intent = new Intent(getApplicationContext(),MainActivity.class);
                                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                                    startActivity(intent);

                                                    String strMsg = "로그인 작업 실패 본사 연락 바랍니다. ERROR-CODE:(" + WholookError.LOGIN_EXCEPTION +")";
                                                    System.out.println( strMsg );
                                                    Toast.makeText(getApplicationContext(), strMsg,Toast.LENGTH_LONG).show();
                                                }
                                            }else{
                                                Intent intent = new Intent(getApplicationContext(),MainActivity.class);
                                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                                startActivity(intent);

                                                String strMsg = "보안 작업 실패 본사 연락 바랍니다. ERROR-CODE:(" + WholookError.HANDSHAKE_UNKNOWN_RESULT +")";
                                                System.out.println( strMsg );
                                                Toast.makeText(getApplicationContext(), strMsg,Toast.LENGTH_LONG).show();
                                            }
                                        }

                                        @Override
                                        public void responseFaild() {
                                            Intent intent = new Intent(getApplicationContext(),MainActivity.class);
                                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                            startActivity(intent);

                                            String strMsg = "보안 작업 실패 본사 연락 바랍니다. ERROR-CODE:(" + WholookError.HANDSHAKE_RESPONSE_FAIL +")";
                                            System.out.println( strMsg );
                                            Toast.makeText(getApplicationContext(), strMsg,Toast.LENGTH_LONG).show();
                                        }
                                    });
                                    client.execute( mURLWithParams );
                                }catch( Exception e){
                                    Intent intent = new Intent(getApplicationContext(),MainActivity.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(intent);

                                    String strMsg = "보안 작업 실패 본사 연락 바랍니다. ERROR-CODE:(" + WholookError.HANDSHAKE_EXCEPTION +")";
                                    System.out.println( strMsg );
                                    Toast.makeText(getApplicationContext(), strMsg,Toast.LENGTH_LONG).show();
                                }

                            }
                        }catch( SMSParsingException e2){
                            String strMsg = "SMS 파싱 실패 ERROR-CODE:(" + WholookError.SERVICE_SMS_JSONPARSING +")";
                            System.out.println( strMsg + " || " + e2.getMessage());
                            e2.printStackTrace();
                            Toast.makeText(getApplicationContext(), strMsg,Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void responseFaild() {
                        String strMsg = "SMS가져오기 실패  ERROR-CODE:(" + WholookError.SERVICE_GETMESSAGELIST_RESPONSE_FAIL +")";
                        System.out.println( strMsg );
                        Toast.makeText(getApplicationContext(), strMsg,Toast.LENGTH_LONG).show();
                    }
                });
                client.execute( mURLWithParams );
            }catch( Exception e){
                String strMsg = "SMS가져오기 실패  ERROR-CODE:(" + WholookError.SERVICE_GETMESSAGELIST_EXCEPTION +")";
                System.out.println( strMsg + " || " + e.getMessage() );
                e.printStackTrace();
                Toast.makeText(getApplicationContext(), strMsg,Toast.LENGTH_LONG).show();
            }
        }else{
            Intent intent = new Intent(getApplicationContext(),MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);

            String strMsg = "네트웍이 연결되지 않았습니다. 폰 상태 확인후 재시도 하십시요.";
            System.out.println( strMsg );
            Toast.makeText(getApplicationContext(), strMsg,Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onDestroy(){
        Log.d(WholookAPI.LOG_TAG,"WMessageService - onDestroy");

        WholookPreference pref = new WholookPreference( this );

        if( pref.getValue(pref.PREF_FIELD_LOGOUT,false)){

            Log.d(WholookAPI.LOG_TAG,"WMessageService - onDestroy - LOGOUT");
            registerRestartAlarm(false);
            notifyManager.cancelAll();
            pref.clearAll();
        }

        if( pReceiver != null)unregisterReceiver(pReceiver);
        if( bReceiver != null)unregisterReceiver(bReceiver);
        //super.onDestroy();
    }

    public void registerRestartAlarm(boolean isOn){
        Intent intent = new Intent(WMessageService.this, RestartReceiver.class);
        intent.setAction(RestartReceiver.ACTION_RESTART_SERVICE);
        PendingIntent sender = PendingIntent.getBroadcast(getApplicationContext(), 0, intent, 0);

        AlarmManager am = (AlarmManager)getSystemService(ALARM_SERVICE);
        if(isOn){
            //ELAPSED_REALTIME,ELAPSED_REALTIME_WAKEUP,setRepeating,setInexactRepeating
            //am.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + 1000, ALAMER_TIMER, sender);
            am.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + ALAMER_TIMER, ALAMER_TIMER, sender);
            Log.d(WholookAPI.LOG_TAG,"registerRestartAlarm - ON");
        }else{
            am.cancel(sender);
            Log.d(WholookAPI.LOG_TAG,"registerRestartAlarm - OFF");
        }
    }

    public void set_APP_Notification( String title, String content, String toast_txt ,PendingIntent indent ){
        // 맘에 드는 스몰 아이콘 찾아서 넣기...ㅠㅠ
        builder.setContentTitle(title)
                .setContentText(content)
                .setTicker(NOTI_TICKER)
                .setAutoCancel( false )
                .setOngoing( true )
                .setSmallIcon(R.drawable.ic_wholook_noti);

        if( indent != null)builder.setContentIntent(indent);

        notifyManager.notify(NOTI_SERVICE_ID, builder.build());

        if( toast_txt != null && !toast_txt.equals("")){
            Toast.makeText(this, toast_txt,Toast.LENGTH_LONG).show();
        }

    }

    private class SendSMSJob extends AsyncTask< ArrayList<SMSParer.Wsms> , Integer, Boolean >{

        private ArrayList<SMSParer.Wsms> wsmsList;

        @Override
        protected void onPreExecute() {
            try{
                builder.setContentTitle(WMessageService.this.getString(R.string.app_name))
                        .setContentText(WMessageService.this.getString(R.string.status_sending))
                        .setTicker("wholook")
                        .setAutoCancel( false )
                        //.setOngoing( true )
                        .setSmallIcon(R.drawable.ic_wholook_noti);
                notifyManager.notify(NOTI_SMS_SEND_STATE, builder.build());

            }catch( Exception e){
                e.printStackTrace();
            }
        }

        protected Boolean doInBackground(ArrayList<SMSParer.Wsms>... params) {

            wsmsList = params[0];
            int send_size = wsmsList.size();
            for( int i = 0; i < send_size;i++){
                SMSParer.Wsms sms =(SMSParer.Wsms)wsmsList.get(i);
                Log.d(WholookAPI.LOG_TAG,"받는사람-" + sms.getRECEIVER() + " 내용-" + sms.getCONTENT() );
                sendSMS( sms );
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                int gage = (int)(((i+1)/(float)send_size)*100);
                publishProgress(gage);
            }

            return true;
        }

        protected void onProgressUpdate(Integer params) {

            builder.setProgress(100, params.intValue(), false);
            notifyManager.notify(NOTI_SMS_SEND_STATE, builder.build());
        }

        protected void onPostExecute(Boolean result) {
            try{

                builder.setProgress(0, 0, true);

                WholookURLWithParams mURLWithParams = new WholookURLWithParams();
                mURLWithParams.url = WholookAPI.server_url + "/api/wsms/update/";
                mURLWithParams.nameValuePairs.add( new BasicNameValuePair("status","2"));
                mURLWithParams.nameValuePairs.add( new BasicNameValuePair("ids",SMSParer.getSuccessList(wsmsList)));
                WholookJSONClient client = new WholookJSONClient(WholookAPI.httpclient);

                client.setResultListner(new WholookJSONClient.ResultListener(){
                    @Override
                    public void responseSuccessfuly(String json) {

                        try{
                            JSONObject result = new JSONObject(json);
                            if( result.getInt("result") == 0 ){
                                builder.setContentTitle(WMessageService.this.getString(R.string.app_name))
                                        .setContentText(WMessageService.this.getString(R.string.status_complete));
                                notifyManager.notify(NOTI_SMS_SEND_STATE, builder.build());

                            }else{
                                notifyManager.cancel(NOTI_SERVICE_ID);

                                Intent intent = new Intent(getApplicationContext(),ConfigActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);

                                String strMsg = "메세지 전송 결과를 전송하다 에러 발생. ERROR-CODE:(" + WholookError.SERVICE_GETMESSAGELIST_UNKNOWN_RESULT +")";
                                System.out.println( strMsg );
                                Toast.makeText(WMessageService.this, strMsg,Toast.LENGTH_LONG).show();
                            }

                        }catch( JSONException e){
                            notifyManager.cancel( NOTI_SERVICE_ID );
                            Intent intent = new Intent(getApplicationContext(),ConfigActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);

                            String strMsg = "메세지 전송 결과를 전송하다 에러 발생. ERROR-CODE:(" + WholookError.SERVICE_MESSAGERETURN_JSONEXCEPTION +")";
                            System.out.println( strMsg );
                            Toast.makeText(WMessageService.this, strMsg,Toast.LENGTH_LONG).show();
                        }finally {

                            try{
                                Thread.sleep(3000);
                            }catch( InterruptedException e){
                                e.printStackTrace();
                            }finally {
                                notifyManager.cancel(NOTI_SMS_SEND_STATE);
                            }
                        }
                    }

                    @Override
                    public void responseFaild() {

                        notifyManager.cancel(NOTI_SMS_SEND_STATE);
                        notifyManager.cancel( NOTI_SERVICE_ID );
                        Intent intent = new Intent(WMessageService.this,ConfigActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);

                        String strMsg = "메세지 전송 결과를 전송하다 에러 발생. ERROR-CODE:(" + WholookError.SERVICE_MESSAGERETURN_RESPONSE_FAIL +")";
                        System.out.println( strMsg );
                        Toast.makeText(WMessageService.this, strMsg,Toast.LENGTH_LONG).show();
                    }
                });
                client.execute( mURLWithParams );

            }catch( Exception e){
                notifyManager.cancel( NOTI_SERVICE_ID );
                Intent intent = new Intent(WMessageService.this,ConfigActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);

                String strMsg = "메세지 전송 결과를 전송하다 에러 발생. ERROR-CODE:(" + WholookError.SERVICE_MESSAGERETURN_EXCEPTION +")";
                System.out.println( strMsg );
                Toast.makeText(WMessageService.this, strMsg,Toast.LENGTH_LONG).show();
            }finally {

                notifyManager.cancel( NOTI_SMS_SEND_STATE );
            }
        }

        protected void onCancelled() {
            super.onCancelled();
        }

        public void sendSMS( SMSParer.Wsms sms )
        {
            String SENT = "SMS_SENT";
            try{
                WholookPreference pref = new WholookPreference(WMessageService.this);

                int count = pref.getValue(pref.PREF_SEND_MESSAGE_TOTAL_COUNT,0);

                String phoneNumber = sms.getRECEIVER();
                String message = sms.getCONTENT();

                PendingIntent pi = PendingIntent.getActivity(WMessageService.this, 0,new Intent(SENT), 0);
                SmsManager smsManager = SmsManager.getDefault();
                //*
                if( message.getBytes().length > 80 ){
                    ArrayList<String> parts = smsManager.divideMessage(message);

                    int numParts = parts.size();
                    //ArrayList<PendingIntent> sentIntents = new ArrayList<PendingIntent>();

                    for(int i = 0; i < numParts;i++){

                        smsManager.sendTextMessage(phoneNumber, null, parts.get( i ) , pi, null);
                        sms.setAction();
                        count+=1;
                        pref.put(pref.PREF_SEND_MESSAGE_TOTAL_COUNT,count);
                        //sentIntents.add(pi);
                    }
                    //smsManager.sendMultipartTextMessage(phoneNumber, null, parts, sentIntents, null);
                }else{
                    smsManager.sendTextMessage(phoneNumber, null, message, pi, null);
                    sms.setAction();
                    count+=1;
                    pref.put(pref.PREF_SEND_MESSAGE_TOTAL_COUNT,count);
                }

            }catch( Exception e){
                Log.d(WholookAPI.LOG_TAG,"SEND-MESSAGE-EXCEPTION" + e.getMessage());
            }
        }
    }
}
