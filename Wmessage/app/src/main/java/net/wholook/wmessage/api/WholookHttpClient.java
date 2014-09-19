package net.wholook.wmessage.api;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.ArrayList;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONObject;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import net.wholook.wmessage.api.WholookAPI;
import net.wholook.wmessage.exception.WholookHttpClientException;
/**
 * Created by wholook on 14. 8. 31..
 */
public class WholookHttpClient {

    public static final int HTTP_TIMEOUT = 30 * 1000; // milliseconds
    private static HttpClient mHttpClient;

    private static HttpClient getHttpClient() {
        if (mHttpClient == null) {
            mHttpClient = new DefaultHttpClient();
            final HttpParams params = mHttpClient.getParams();
            HttpConnectionParams.setConnectionTimeout(params, HTTP_TIMEOUT);
            HttpConnectionParams.setSoTimeout(params, HTTP_TIMEOUT);
            ConnManagerParams.setTimeout(params, HTTP_TIMEOUT);
        }
        return mHttpClient;
    }

    public static String executeHttpPost(String url, ArrayList<NameValuePair> postParameters) throws WholookHttpClientException {
        BufferedReader in = null;
        try {
            HttpClient client = getHttpClient();
            HttpPost request = new HttpPost(url);
            //if( )
            UrlEncodedFormEntity formEntity = new UrlEncodedFormEntity(postParameters);
            request.setEntity(formEntity);
            HttpResponse response = client.execute(request);
            in = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));

            StringBuffer sb = new StringBuffer("");
            String line = "";
            String NL = System.getProperty("line.separator");
            while ((line = in.readLine()) != null) {
                sb.append(line + NL);
            }
            in.close();

            String result = sb.toString();
            return result;
        }catch( Exception e1){
            throw new WholookHttpClientException("WholookHttpClient-executeHttpPost" + url + e1.getMessage() );
        }finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e2) {
                    throw new WholookHttpClientException("WholookHttpClient-executeHttpPost" + url + "get not response " );
                    //e.printStackTrace();
                }
            }
        }
    }

    public static String executeHttpGet(String url) throws WholookHttpClientException {
        BufferedReader in = null;
        try {
            HttpClient client = getHttpClient();
            HttpGet request = new HttpGet();
            request.setURI(new URI(url));
            HttpResponse response = client.execute(request);
            in = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));

            StringBuffer sb = new StringBuffer("");
            String line = "";
            String NL = System.getProperty("line.separator");
            while ((line = in.readLine()) != null) {
                sb.append(line + NL);
            }
            in.close();
            String result = sb.toString();
            return result;
        }catch( Exception e1){
            throw new WholookHttpClientException("WholookHttpClient-executeHttpGet" + url + e1.getMessage() );

        }finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e2) {
                    throw new WholookHttpClientException("WholookHttpClient-executeHttpGet" + url + e2.getMessage() );
                }
            }
        }
    }

    public static String postJsonData(String url,JSONObject obj) throws WholookHttpClientException{

        HttpClient httpclient = getHttpClient();
        String json=obj.toString();
        String result=null;

        try {
            HttpPost httppost = new HttpPost(url.toString());
            httppost.setHeader("Content-type", "application/json");
            httppost.setHeader("Accept", "application/json");

            StringEntity se = new StringEntity(json);
            se.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
            httppost.setEntity(se);
            HttpResponse response = httpclient.execute(httppost);
            result = EntityUtils.toString(response.getEntity());

        }catch (Exception e) {
            throw new WholookHttpClientException("WholookHttpClient-postJsonData url(" + url + ") - "+e.getMessage() );
        }
        return result;
    }
}
