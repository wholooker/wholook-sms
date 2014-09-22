package net.wholook.wmessage.api;

import android.content.Context;
import android.os.AsyncTask;
import android.app.ProgressDialog;
import android.util.Log;

//import java.net.CookieStore;
import net.wholook.wmessage.ui.ErrorDialog;

import java.util.List;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.Header;
import org.apache.http.client.CookieStore;
import org.apache.http.util.EntityUtils;

/**
 * Created by wholook on 14. 9. 17..
 */
public class WholookJSONClient extends AsyncTask<WholookURLWithParams,Void,String>{
    private final static String TAG = "WholookJSONClient";

    private ProgressDialog mDlg = null;
    private ResultListener mResultListner = null;
    private Context mContext = null;
    private String mtxt = null;
    private final String mDefault = "Loading...";

    private static HttpClient httpclient = null;
    //*
    public WholookJSONClient(HttpClient client){
        httpclient = client;
    }
    //*/

    public WholookJSONClient(HttpClient client,Context context){
        httpclient = client;
        mContext = context;
    }

    public WholookJSONClient(HttpClient client,Context context, String load_txt ){
        httpclient = client;
        mContext = context;
        mtxt = load_txt;
    }

    public void setResultListner( ResultListener resultListner){
        mResultListner = resultListner;
    }

    protected void onPreExecute(){
        //*
        if( mContext != null ){
            mDlg = new ProgressDialog(mContext);
            mDlg.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            if( mtxt != null){
                mDlg.setMessage(mtxt);
            }else{
                mDlg.setMessage(mDefault);
            }
            mDlg.show();
        }
        //*/
        //super.onPreExecute();
    }
    @Override
    protected String doInBackground(WholookURLWithParams... urls) {
        return connect(urls[0].url, urls[0].nameValuePairs);
    }

    public static String connect(String url, List<NameValuePair> pairs)
    {

        if(url == null)
            Log.d(TAG, "want to connect, but url is null");
        else
            Log.d(TAG, "starting connect with url " + url);

        if(pairs == null)
            Log.d(TAG, "want to connect, though pairs is null");
        else
        {
            Log.d(TAG, "starting connect with this many pairs: " + pairs.size());
            for(NameValuePair dog : pairs)
            {
                Log.d(TAG, "request: " + dog.toString());
            }
        }

        HttpResponse response = null;
        try {

            HttpPost httpPost = new HttpPost(url);
            httpPost.setEntity(new UrlEncodedFormEntity(pairs));
            response = httpclient.execute(httpPost);

            BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "UTF-8"));
            String json = reader.readLine();
            return json;

        } catch (ClientProtocolException e) {
            System.out.println( "ClientProtocolException=============");
            e.printStackTrace();
        } catch (IOException e2) {

            //ErrorDialog error = new ErrorDialog( strMsg );
            //error.show();
            System.out.println( "IOException=============");
            e2.printStackTrace();
        }catch( Exception e3){
            System.out.println( "Exception=============");
            e3.getStackTrace();
        }/*finally {
            try{
                if( response != null){
                    HttpEntity enty = response.getEntity();
                    if (enty != null)
                        enty.consumeContent();
                }
            }catch(Exception e){

            }

        }*/
        return null;
    }
    //사용 안함
    private  void updateCookie( HttpClient client){
        try{
            CookieStore cookieStore = ((DefaultHttpClient)client).getCookieStore();
            List<Cookie> cookieList = cookieStore.getCookies();
            Log.i(TAG, "updateCookie - cookie size: "+cookieList.size());
            for( int i = 0; i < cookieList.size();i++){
                Cookie cookie = cookieList.get(i);

                Log.i(TAG, "Cookie -------------------: "+ i );

                Log.i(TAG, "Cookie - getComment: "+ cookie.getComment());
                Log.i(TAG, "Cookie - getCommentURL: "+ cookie.getCommentURL());
                Log.i(TAG, "Cookie - getDomain: "+ cookie.getDomain());
                Log.i(TAG, "Cookie - getName: "+ cookie.getName());
                Log.i(TAG, "Cookie - getPath: "+ cookie.getPath());
                Log.i(TAG, "Cookie - getValue: "+ cookie.getValue());
                Log.i(TAG, "Cookie - getExpiryDate: "+ cookie.getExpiryDate());
                Log.i(TAG, "Cookie - getVersion: "+ cookie.getVersion());
                Log.i(TAG, "Cookie -------------------: -------------------");
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }
    @Override
    protected void onPostExecute(String json ) {

        if( mDlg != null)mDlg.dismiss();

        if( json == null || json.equals("")){
            mResultListner.responseFaild();
        }else{
            mResultListner.responseSuccessfuly(json);
        }
    }

    // interface call back function
    public static interface ResultListener{
        void responseSuccessfuly(String json);
        void responseFaild();
    }
}
