package com.example.yusuf.smartsmsbox;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.telephony.SmsManager;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class ListSms extends AppCompatActivity{
    private DatabaseAccess db;
    private RecyclerView recyclerView;
    private RecyclerView.Adapter adapter;
    private RecyclerView.LayoutManager layoutManager;
    private ListSmsRecycler myAdapter;
    private EditText message;
    private ImageButton send;
    String number,name;
    private static ArrayList<Sms> smsArrayList;
    private int pos;
    private BroadcastReceiver smsListener=new SmsListener();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_sms);
        number=getIntent().getExtras().getString("number");
        name=getIntent().getExtras().getString("name");
        message=findViewById(R.id.messageText);
        send=findViewById(R.id.sendSms);
        recyclerView=(RecyclerView)findViewById(R.id.list_sms_recycler);
        layoutManager=new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        db= new DatabaseAccess(getApplicationContext());
        smsArrayList=db.getAllSmsViaNumber(number,name);
        myAdapter=new ListSmsRecycler(smsArrayList);
        recyclerView.setAdapter(myAdapter);
        ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.SEND_SMS},1);
        layoutManager.smoothScrollToPosition(recyclerView,null,myAdapter.getItemCount());
        recyclerView.addOnItemTouchListener(
                new RecyclerItemClickListener(this, new RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View v, int position) {
                        registerForContextMenu(v);
                        pos=position;
                    }
                })
        );

        send.setOnClickListener(new View.OnClickListener() {
                    Location gpsLocation;
                    LocationManager locationManager;
                    Double latitude = null, longitude = null;

                    @Override
                    public void onClick(View view) {
                        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                        if (!message.getText().toString().matches("")) {
                            SmsManager smsManager = SmsManager.getDefault();
                            smsManager.sendTextMessage(number, null, message.getText().toString(), null, null);
                            try {
                                locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, new LocationListener() {
                                    @Override
                                    public void onLocationChanged(Location location) {
                                    }

                                    @Override
                                    public void onStatusChanged(String s, int i, Bundle bundle) {
                                    }

                                    @Override
                                    public void onProviderEnabled(String s) {
                                    }

                                    @Override
                                    public void onProviderDisabled(String s) {
                                    }
                                }, null);
                                gpsLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                            } catch (SecurityException e) {
                                e.printStackTrace();
                            }
                            DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss");
                            String time = formatter.format(Calendar.getInstance().getTime());
                            if (gpsLocation != null) {
                                latitude = gpsLocation.getLatitude();
                                longitude = gpsLocation.getLongitude();
                            }
                            db.saveSms(number, message.getText().toString(), "sending", latitude, longitude, time);
                            smsArrayList = db.getAllSmsViaNumber(number, name);
                            myAdapter.setSms(smsArrayList);
                            myAdapter.notifyDataSetChanged();
                            message.setText("");
                            layoutManager.smoothScrollToPosition(recyclerView, null, myAdapter.getItemCount());
                            Toast.makeText(getApplicationContext(), "Sms sent", Toast.LENGTH_LONG).show();
                        }
                    }
                });

    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.recyclermenu, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_map:
                if(smsArrayList.get(pos).getLatitude()==0.0&&smsArrayList.get(pos).getLongitude()==0.0)
                    Toast.makeText(getApplicationContext(),"No location info for this sms.",Toast.LENGTH_LONG).show();
                else {
                    ArrayList<Sms> temp = new ArrayList<>();
                    temp.add(smsArrayList.get(pos));
                    Intent intent = new Intent(this, MapsActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("allSms", temp);
                    intent.putExtras(bundle);
                    startActivity(intent);
                }
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    public void onResume(){
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(messageReceived,new IntentFilter("messageReceived"));
    }

    private BroadcastReceiver messageReceived = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent ıntent) {
            smsArrayList=db.getAllSmsViaNumber(number,name);
            myAdapter.setSms(smsArrayList);
            myAdapter.notifyDataSetChanged();
            layoutManager.smoothScrollToPosition(recyclerView,null,myAdapter.getItemCount());
        }
    };
}
