package net.wholook.wmessage.ui;

import android.content.Intent;
import android.database.DataSetObserver;
import android.os.SystemClock;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.app.NotificationManager;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import net.wholook.wmessage.R;
import net.wholook.wmessage.api.WholookPreference;
import net.wholook.wmessage.service.WMessageService;
import net.wholook.wmessage.api.WholookAPI;
import net.wholook.wmessage.ui.MyActivity;


import java.util.Date;
import java.text.SimpleDateFormat;

import android.content.ContentResolver;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.provider.ContactsContract.*;
import android.provider.ContactsContract.CommonDataKinds.*;
import android.net.Uri;



public class ConfigActivity extends ActionBarActivity {

    private WholookPreference mpref = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_config);
        mpref = new WholookPreference(getApplicationContext());

        String user = mpref.getValue(mpref.PREF_FIELD_USER,"");
        String id = mpref.getValue(mpref.PREF_FIELD_ID,"");
        long time = mpref.getValue(mpref.PREF_SERVICE_START_TIME,0l);
        System.out.println( time );

        Date date = new Date( time );
        SimpleDateFormat CurDateFormat = new SimpleDateFormat("yyyy년 MM월 dd일 HH시 mm분");
        String strCurDate = CurDateFormat.format(date);

        ListView list = (ListView) findViewById(R.id.lvInfo);

        String[] data = {   "계정 - " + id ,
                "사용자 - " + user,
                "서비스 시작 - " + strCurDate};
        //"사용된 문자 - " + count     };

        ArrayAdapter adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, data);
        list.setAdapter(adapter);

        update();
    }
    public void startService( View view ){

        Intent i = new Intent(getApplicationContext(), WMessageService.class);
        getApplicationContext().startService(i);
    }

    public void refresh( View view ){
        update();
    }

    public void update(){

        int count = mpref.getValue(mpref.PREF_SEND_MESSAGE_TOTAL_COUNT,0);
        TextView tv = (TextView)findViewById(R.id.tv_count);
        tv.setText("사용된 문자 - " + count + " 건" );

    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.config, menu);
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

    public void logOut(View view){
        app_logOut();

    }

    public void app_logOut(){

        try{
            WholookAPI.logOut();

            mpref.put( mpref.PREF_FIELD_LOGOUT,true);

            Intent service = new Intent(getApplicationContext(), WMessageService.class);
            stopService(service);
            Intent login = new Intent(getApplicationContext(), MyActivity.class);
            startActivity(login);
        }catch( Exception e){
            e.printStackTrace();
        }

    }
    public void getContract(View view ){
        String phoneNumber = null;
        String email = null;

        Uri CONTENT_URI = ContactsContract.Contacts.CONTENT_URI;
        String _ID = ContactsContract.Contacts._ID;
        String DISPLAY_NAME = ContactsContract.Contacts.DISPLAY_NAME;
        String HAS_PHONE_NUMBER = ContactsContract.Contacts.HAS_PHONE_NUMBER;

        Uri PhoneCONTENT_URI = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
        String Phone_CONTACT_ID = ContactsContract.CommonDataKinds.Phone.CONTACT_ID;
        String NUMBER = ContactsContract.CommonDataKinds.Phone.NUMBER;

        Uri EmailCONTENT_URI =  ContactsContract.CommonDataKinds.Email.CONTENT_URI;
        String EmailCONTACT_ID = ContactsContract.CommonDataKinds.Email.CONTACT_ID;
        String DATA = ContactsContract.CommonDataKinds.Email.DATA;




        //StringBuffer output = new StringBuffer();
        ContentResolver contentResolver = getContentResolver();
        Cursor cursor = contentResolver.query(CONTENT_URI, null,null, null, null);

        // Loop for every contact in the phone
        int contract_count = 0;
        int cursorTotalCount = cursor.getCount();
        if (cursorTotalCount > 0) {
            System.out.println( "시작------- 총갯수 - " + cursorTotalCount );

            while (cursor.moveToNext()) {
                String contact_id = cursor.getString(cursor.getColumnIndex( _ID ));
                String name = cursor.getString(cursor.getColumnIndex( DISPLAY_NAME ));
                int hasPhoneNumber = Integer.parseInt(cursor.getString(cursor.getColumnIndex( HAS_PHONE_NUMBER )));
                if (hasPhoneNumber > 0) {

                    /*
                    System.out.println("all data:" + name);
                    Cursor c = contentResolver.query(Data.CONTENT_URI,
                            new String[] {Data._ID, Phone.NUMBER, Phone.TYPE, Phone.LABEL},
                            Data.CONTACT_ID + "=?" + " AND "
                                    + Data.MIMETYPE + "='" + Phone.CONTENT_ITEM_TYPE + "'",
                            new String[] {String.valueOf(contact_id)}, null);

                    while( c.moveToNext()){

                        for( int cl = 0; cl < c.getColumnCount(); cl++){
                            String col = c.getColumnName( cl );
                            System.out.println( col );
                        }
                    }
                    c.close();
                    */

                    System.out.println("First Name:" + name);
                    // Query and loop for every phone number of the contact
                    Cursor phoneCursor = contentResolver.query(PhoneCONTENT_URI, null, Phone_CONTACT_ID + " = ?", new String[] { contact_id }, null);

                    while (phoneCursor.moveToNext()) {
                        phoneNumber = phoneCursor.getString(phoneCursor.getColumnIndex(NUMBER));
                        System.out.println("Phone number:" + phoneNumber);
                    }
                    phoneCursor.close();
                    // Query and loop for every email of the contact
                    Cursor emailCursor = contentResolver.query(EmailCONTENT_URI,    null, EmailCONTACT_ID+ " = ?", new String[] { contact_id }, null);

                    while (emailCursor.moveToNext()) {
                        email = emailCursor.getString(emailCursor.getColumnIndex(DATA));
                        System.out.println("Email:" + email);
                    }
                    emailCursor.close();
                }
                contract_count+=1;
                System.out.println("---------" + contract_count + "번째");
            }
            System.out.println( "완료------- 총갯수 - " + contract_count);
        }
        cursor.close();
    }

    protected void onDestroy(){
        Log.d(WholookAPI.LOG_TAG, "-------------ConfigActivity - onDestroy()-------------");
        super.onDestroy();
    }

    protected void onPause(){
        Log.d(WholookAPI.LOG_TAG, "----------------ConfigActivity - onPause()----------------");
        super.onPause();
    }
}
