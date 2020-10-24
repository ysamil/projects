package com.example.yusuf.smartsmsbox;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.icu.text.UnicodeSetSpanner;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Telephony;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.telephony.SmsMessage;
import android.widget.Toast;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by Yusuf on 25.05.2018.
 */

public class SmsListener extends BroadcastReceiver implements LocationListener{

    private LocationManager locationManager;
    private Context context;
    private DatabaseAccess db;
    public SmsListener(){}
    public SmsListener(Context context){
        this.context=context;
    }

    public void onReceive(Context context, Intent intent){
        if(intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED")){
            locationManager=(LocationManager)context.getSystemService(Context.LOCATION_SERVICE);
            db=new DatabaseAccess(context);
            DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss");
            String messageBody="",sms_from="";
            Location gpslocation=null;
            Location networkLocation=null;
            for (SmsMessage smsMessage : Telephony.Sms.Intents.getMessagesFromIntent(intent)) {
                messageBody = smsMessage.getMessageBody();
                sms_from=smsMessage.getOriginatingAddress();
            }
            try {
                locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER,this,null);
                gpslocation= locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                locationManager.requestSingleUpdate(LocationManager.NETWORK_PROVIDER,this,null);
                networkLocation=locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            }
            catch (SecurityException e){
                e.printStackTrace();
            }
            String time= formatter.format(Calendar.getInstance().getTime());
            if(networkLocation==null&&gpslocation==null){
                db.saveSms(sms_from,messageBody,"receiving",null,
                        null,time);
            }
            else if(networkLocation!=null){
                db.saveSms(sms_from,messageBody,"receiving",networkLocation.getLatitude(),
                        networkLocation.getLongitude(),time);
            }
            else if(gpslocation!=null){
                db.saveSms(sms_from,messageBody,"receiving",gpslocation.getLatitude(),
                        gpslocation.getLongitude(),time);
            }
            Intent msgReceived = new Intent("messageReceived");
            LocalBroadcastManager.getInstance(this.context).sendBroadcast(msgReceived);
        }
    }




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
}
