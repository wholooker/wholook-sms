package net.wholook.wmessage.api;

import java.util.ArrayList;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.params.HttpParams;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.json.JSONObject;

import android.app.ProgressDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import net.wholook.wmessage.exception.WholookAPIException;
import net.wholook.wmessage.exception.WholookHttpClientException;

/**
 * Created by wholook on 14. 8. 31..
 */
public class WholookAPI {

    //public static final String server_url = "http://172.17.148.190:8000";
    public static final String server_url = "http://dev.wholook.net";
    public static final String SENDER_ID = "826729090839"; //APP_GCM_PROJECT_NUMBER
    public static final String LOG_TAG = "WMESSAGE";

    public static final String APP_CODE_RESULT = "wholook_sms";
    public static final String APP_ACTION_RESULT = "get_message";
    public static final String APP_CODE = "app_code";
    public static final String APP_ACTION = "app_action";

    public static HttpClient httpclient = getThreadSafeClient();
    public static String wtls_passphrase = null;

    public static DefaultHttpClient getThreadSafeClient() {

        DefaultHttpClient client = new DefaultHttpClient();
        ClientConnectionManager mgr = client.getConnectionManager();
        HttpParams params = client.getParams();
        client = new DefaultHttpClient(new ThreadSafeClientConnManager(params,

                mgr.getSchemeRegistry()), params);
        return client;
    }

    public static String get_aes_key(){
        String chrs="1234567890abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ~!@#$%^&*()_+{}[]-=;:,.<>/?";
        String key = "";
        for( int i =0; i < 32; i++){
            Double idx = Math.floor(Math.random() * chrs.length() );
            key +=chrs.substring(idx.intValue());
        }
        return key;
    }

    public static boolean wholookIsHandshaked()throws WholookAPIException{
        String url = server_url + "/api/handshake/2/";
        String response = null;
        try {
            wtls_passphrase = get_aes_key();
            JSONObject json = new JSONObject();
            json.put("passphrase", wtls_passphrase);
            response = WholookHttpClient.postJsonData(url, json);
            response=response.toString();

        }catch (Exception e) {
            wtls_passphrase = null;
            throw new WholookAPIException("Not wholookIsHandshaked - " + e.getMessage() );
        }

        if( response.equals( "OK!!")){
            return true;
        }else
            return false;
    }

    public static JSONObject wholookSecuredPost( String url, JSONObject args )throws WholookAPIException{
        String response = null;
        JSONObject result = null;
        String json_string = null;
        try{
            url = server_url + url;
            if( wtls_passphrase == null ){
                throw new WholookAPIException("Not wholookIsHandshaked wholookSecuredPost url-" + url);
            }
            ArrayList<NameValuePair> postParameters = new ArrayList<NameValuePair>();
            if( args == null ){
                json_string = "{}";
            }else
            {
                json_string = args.toString();
            }
            postParameters.add(new BasicNameValuePair("__sfdata", json_string ));
            response = WholookHttpClient.executeHttpPost(url, postParameters);
            response=response.toString().replaceAll("\\s+","");
            result = new JSONObject( response );
            return result;

        }catch( WholookHttpClientException e1){
            Log.d(LOG_TAG, "HttpClient-wholookSecuredPost API Fail url-" + url );
            throw new WholookAPIException("HttpClient-wholookSecuredPost API Fail url-" + url );

        }catch( Exception e2){
            Log.d(LOG_TAG, "wholookSecuredPost API Fail url-" + url);
            throw new WholookAPIException("wholookSecuredPost API Fail url-" + url );
        }
    }

    public static JSONObject wholookPost( String url, ArrayList<NameValuePair> postParameters)throws WholookAPIException{
        String response;
        JSONObject result = null;

        try{

            url = server_url + url;
            response = WholookHttpClient.executeHttpPost(url, postParameters);
            response=response.toString().replaceAll("\\s+","");
            result = new JSONObject( response );

        }catch( Exception e){
            throw new WholookAPIException("wholookPost API Fail url-" + url );
        }
        return result;
    }
    // 네트웍 체크
    public static boolean isNetworkAvailable( Context context) {
        boolean isMobile = false, isWifi = false;

        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo[] infoAvailableNetworks = cm.getAllNetworkInfo();

        if (infoAvailableNetworks != null) {
            for (NetworkInfo network : infoAvailableNetworks) {

                if (network.getType() == ConnectivityManager.TYPE_WIFI) {
                    if (network.isConnected() && network.isAvailable())
                        isWifi = true;
                }
                if (network.getType() == ConnectivityManager.TYPE_MOBILE) {
                    if (network.isConnected() && network.isAvailable())
                        isMobile = true;
                }
            }
        }
        return isMobile || isWifi;
    }

    public static boolean isNetWorkChecker( Context context, int retry, int timer){
        ProgressDialog mDlg = null;
        try {
            mDlg = new ProgressDialog(context);
            mDlg.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            mDlg.setMessage("Network checking...");
            mDlg.show();

            int i = 0;
            while( !isNetworkAvailable(context)){
                Thread.sleep(timer);
                i+=1;
                if( i == retry){
                    mDlg.dismiss();
                    return false;
                }
            }
        } catch (InterruptedException e) {
            if( mDlg != null)mDlg.dismiss();
            return false;
        }
        mDlg.dismiss();
        return true;
    }

    public static boolean isNetWorkCheckerNotUI( Context context, int retry, int timer){

        try {
            int i = 0;
            while( !isNetworkAvailable(context)){
                Thread.sleep(timer);
                i+=1;
                if( i == retry){
                    return false;
                }
            }
        } catch (InterruptedException e) {
            return false;
        }
        return true;
    }

    public static void logOut(){
        try{
            wholookSecuredPost("/api/logout/",null);
        }catch( Exception e){
            Log.d(LOG_TAG, "LogOut Fail....");
        }
    }
}
