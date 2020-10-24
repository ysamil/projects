package com.example.yusuf.smartsmsbox;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.provider.Telephony;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.telephony.PhoneNumberUtils;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    private DatabaseAccess db;
    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private MyRecyclerViewAdapter myAdapter;
    private ArrayList<Sms> smsArrayList;
    private IntentFilter mIntentFilter;
    private NavigationView navigationView;
    Dialog dialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog= new Dialog(MainActivity.this);
                ShowPopUp();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        //-------------------------------------------------------------------------------------
        db= new DatabaseAccess(this);
        db.createBlackList();
        if (!checkSmsPermission())
            requestSmsPermission();
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_SMS}, 0);
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS}, 0);
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECEIVE_SMS}, 0);
        SmsListener smsListener= new SmsListener(getApplicationContext());
        Intent intent= new Intent(this,smsListener.getClass());
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startService(intent);

        SharedPreferences preferences=PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        Boolean getSms=preferences.getBoolean("save",false);
        if(!getSms)
            getallSMS();
        recyclerView=(RecyclerView)findViewById(R.id.my_recycler_view);
        layoutManager=new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addItemDecoration(new SimpleDividerItemDecoration(getApplicationContext()));
        smsArrayList=db.takeWhiteListSms(getContact());
        myAdapter=new MyRecyclerViewAdapter(smsArrayList,getApplicationContext(),getContact());
        recyclerView.setAdapter(myAdapter);
        navigationView.getMenu().getItem(0).setChecked(true);
    }

    public void onResume(){
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(messageReceived,new IntentFilter("messageReceived"));
        smsArrayList=db.takeWhiteListSms(getContact());
        myAdapter.setSms(smsArrayList);
        myAdapter.notifyDataSetChanged();
    }

    private BroadcastReceiver messageReceived = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent ıntent) {
            smsArrayList=db.takeWhiteListSms(getContact());
            myAdapter.setSms(smsArrayList);
            myAdapter.notifyDataSetChanged();
            layoutManager.smoothScrollToPosition(recyclerView,null,myAdapter.getItemCount());
        }
    };

    public void ShowPopUp(){
        final AutoCompleteTextView number;
        Button button;
        dialog.setContentView(R.layout.sendsmspopup);
        number=dialog.findViewById(R.id.numberforSending);
        button=dialog.findViewById(R.id.sendNewSms);
        final HashMap<String,Contact> contacts=getContact();
        final ArrayList<String> numbers=new ArrayList<>();
        for(Map.Entry<String, Contact> c : contacts.entrySet()){
            numbers.add(c.getValue().getPhone());
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(MainActivity.this
                , android.R.layout.simple_dropdown_item_1line, numbers);
        number.setAdapter(adapter);
        number.setThreshold(1);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this,ListSms.class);
                intent.putExtra("number",number.getText().toString());
                if(contacts.get(number.getText().toString())!=null)
                    intent.putExtra("name",contacts.get(number.getText().toString()).getName());
                else
                    intent.putExtra("name",number.getText().toString());
                startActivity(intent);
                dialog.dismiss();
            }
        });
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();
    }
    public HashMap getContact(){
        HashMap<String,Contact> contact=new HashMap();
        if(getApplicationContext().checkSelfPermission( Manifest.permission.READ_CONTACTS ) != PackageManager.PERMISSION_GRANTED )
            ActivityCompat.requestPermissions(this , new String[]{Manifest.permission.READ_CONTACTS,Manifest.permission.WRITE_CONTACTS}, 1);
        Cursor phones =getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,null,null, null);
        while (phones.moveToNext())
        {
            contact.put(phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
                    ,new Contact(phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)),
                    phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)),
                    phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_URI))));
        }
        phones.close();
        return  contact;
    }

    public void getallSMS() {
        Boolean saved = true;
        String type;
        SharedPreferences sPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = sPreferences.edit();
        Calendar calendar = Calendar.getInstance();
        DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss");
        long timeinMilis;
        String address, body, date;
        Uri uriSMSURI = Uri.parse("content://sms");
        Cursor cur = getContentResolver().query(uriSMSURI, null, null, null, null);
        db = new DatabaseAccess(getApplicationContext());
        db.createSmsTable();
        cur.moveToLast();

        while (cur != null && cur.moveToPrevious()) {
            address=cur.getString(cur.getColumnIndex("address"));
            address=address.replaceAll("\\s","");
            address=address.replaceAll("\\(","");
            address=address.replaceAll("\\)","");
            address=address.replaceAll("\\-","");
            body = cur.getString(cur.getColumnIndexOrThrow("body"));
            date = cur.getString(cur.getColumnIndex("date"));
            timeinMilis = Long.parseLong(date);
            calendar.setTimeInMillis(timeinMilis);
            date = formatter.format(calendar.getTime());
            if(cur.getString(cur.getColumnIndexOrThrow("type")).contains("1"))
                type="receiving";
            else
                type="sending";
            if (!db.saveSms(address, body, type, null, null, date))
                saved = false;
        }
        if (cur != null) {
            cur.close();
        }
        if(saved){
            editor.putBoolean("save",true);
            editor.commit();
        }
    }

    private void requestSmsPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.READ_SMS)) {
            //Log.d(TAG, "shouldShowRequestPermissionRationale(), no permission requested");
            return;
        }
        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_SMS, Manifest.permission.RECEIVE_SMS},
                0);
    }
    private boolean checkSmsPermission(){
        return ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.READ_SMS) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(MainActivity.this,
                        Manifest.permission.RECEIVE_SMS) == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_blacklist) {
                Intent intent= new Intent(this,edit_BlackList.class);
                startActivity(intent);
            return true;
        }
        else if(id ==R.id.showAllSms){
           if(navigationView.getMenu().findItem(R.id.Personal).isChecked()){
                smsArrayList=db.getAllContactSms(getContact());
            }
            else if(navigationView.getMenu().findItem(R.id.Commercial).isChecked()){
               smsArrayList=db.getAllCommercialSms();
           }
           else if(navigationView.getMenu().findItem(R.id.Spam).isChecked()){
               smsArrayList=db.getAllBlackListSms();
           }
           else if(navigationView.getMenu().findItem(R.id.OTP).isChecked()){
               smsArrayList=db.getAllOTPSms();
           }
           if(smsArrayList!=null) {
               Intent intent = new Intent(this, MapsActivity.class);
               Bundle bundle = new Bundle();
               bundle.putSerializable("allSms", smsArrayList);
               intent.putExtras(bundle);
               startActivity(intent);
           }
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.Personal) {
            ArrayList<Sms> smsArrayList=db.takeWhiteListSms(getContact());
            myAdapter=new MyRecyclerViewAdapter(smsArrayList,getApplicationContext(),getContact());
            recyclerView.setAdapter(myAdapter);
        } else if (id == R.id.Commercial) {
            ArrayList<Sms> smsArrayList=db.getAllCommercialSms();
            myAdapter=new MyRecyclerViewAdapter(smsArrayList,getApplicationContext(),getContact());
            recyclerView.setAdapter(myAdapter);
        } else if (id == R.id.Spam) {
            ArrayList<Sms> smsArrayList=db.getAllBlackListSms();
            myAdapter=new MyRecyclerViewAdapter(smsArrayList,getApplicationContext(),getContact());
            recyclerView.setAdapter(myAdapter);
        } else if (id == R.id.OTP) {
            ArrayList<Sms> smsArrayList=db.getAllOTPSms();
            myAdapter=new MyRecyclerViewAdapter(smsArrayList,getApplicationContext(),getContact());
            recyclerView.setAdapter(myAdapter);
        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
