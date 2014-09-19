package net.wholook.wmessage.api;

import android.util.Log;

import org.json.JSONObject;
import org.json.JSONArray;

import java.util.ArrayList;

import net.wholook.wmessage.exception.SMSParsingException;

/**
 * Created by wholook on 14. 9. 6..
 */
public class SMSParer {

    private JSONArray jsonData = null;
    private ArrayList<Wsms> list;


    public SMSParer(JSONArray jsons){
        this.jsonData = jsons;
        list = new ArrayList<Wsms>();
    }

    public ArrayList<Wsms> getSMSList()throws SMSParsingException{

        if( this.jsonData == null )throw new SMSParsingException("Json Data is null");

        try{
            for( int i=0;i < this.jsonData.length();i++){
                JSONObject wmsg = (JSONObject)this.jsonData.get(i);
                int id = wmsg.getInt("id");
                JSONObject contentObj = (JSONObject)wmsg.getJSONObject("content");
                String timestamp = wmsg.getString("timestamp");
                JSONObject senderObj = (JSONObject)wmsg.getJSONObject("sender");
                JSONObject receiverObj = (JSONObject)wmsg.getJSONObject("receiver");
                JSONObject msgObj = (JSONObject)wmsg.getJSONObject("msg");
                int status =  wmsg.getInt("status");

                Wsms sms = new Wsms(id,
                                    contentObj.getString("title"),
                                    contentObj.getString("message"),
                                    receiverObj.getString("phone"),
                                    senderObj.getString("name"),
                                    timestamp,status);
                list.add(sms);
            }
        }catch( Exception e ){
            throw new SMSParsingException("Json Data is not parsing");
        }
        return list;
    }

    public static String getSuccessList( ArrayList<Wsms> list ){
        String result = "";

        for( int i=0; i < list.size(); i++){
            SMSParer.Wsms sms = list.get(i);
            if( sms.getAction() == 1){
                if( result.equals(""))
                    result += sms.getIDX()+"";
                else
                    result += "," + sms.getIDX();
            }
        }
        Log.d(WholookAPI.LOG_TAG, "getSuccessList result = " + result );
        return result;
    }

    public class Wsms{

        private int IDX;
        private String TITLE = null;
        private String CONTENT = null;
        private String RECEIVER = null;
        private String SENDER = null;
        private String TIMESTAMP = null;
        private int STATUS;
        private int is;//0 = 안보냄 1 = 보냄

        public Wsms( int idx,String title, String content,String receiver,String sender,String timestamp, int status){
            IDX = idx;
            TITLE = title;
            CONTENT = content;
            RECEIVER = receiver;
            SENDER = sender;
            TIMESTAMP = timestamp;
            STATUS = status;
            is = 0;
        }

        public int getIDX(){
            return IDX;
        }
        public String getTITLE(){
            return TITLE;
        }
        public String getCONTENT(){
            return CONTENT;
        }
        public String getRECEIVER(){
            return RECEIVER;
        }
        public String getSENDER(){
            return SENDER;
        }
        public String getTIMESTAMP(){
            return TIMESTAMP;
        }

        public void setAction(){
            is = 1;
        }
        public int getAction(){
            return is;
        }
    }
}
