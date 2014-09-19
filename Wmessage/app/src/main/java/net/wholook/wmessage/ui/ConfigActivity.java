package net.wholook.wmessage.ui;

import android.content.Intent;
import android.database.DataSetObserver;
import android.os.SystemClock;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
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

}
