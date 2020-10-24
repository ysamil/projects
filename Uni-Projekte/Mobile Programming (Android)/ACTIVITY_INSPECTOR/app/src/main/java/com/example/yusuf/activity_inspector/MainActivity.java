package com.example.yusuf.activity_inspector;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Environment;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;


public class MainActivity extends AppCompatActivity implements SensorEventListener,LocationListener {
    private TextView status;
    private ImageView ımageView;
    private String currentTime;
    private Button list;
    private SensorManager sm;
    private LocationManager lm;
    private Sensor acl;
    private float values[],sum=0;
    private ArrayList<Double> threshold;
    private float start,end,speed=0;
    private ArrayList<Float> speeds;
    private int oldActivity=3,newActivity=3;
    private ArrayList<activity_info> activityList;
    private Chronometer chronometer;
    private Date startDate,endDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        status=(TextView)findViewById(R.id.status);
        chronometer=(Chronometer)findViewById(R.id.chronometer2);
        list=(Button)findViewById(R.id.list_act);
        ımageView=(ImageView)findViewById(R.id.imageView);
        startNewActivity();
        list.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopActivity();
                try{
                    FileOutputStream fOut= openFileOutput("list", Context.MODE_PRIVATE);
                    ObjectOutputStream obj = new ObjectOutputStream(fOut);
                    obj.writeObject(activityList);
                    obj.close();
                    fOut.close();
                }
                catch (IOException e){ e.printStackTrace();}
                Intent intent = new Intent(MainActivity.this,listOfActivities.class);
                Bundle bundle =new Bundle();
                bundle.putSerializable("list",activityList);
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });
        sm=(SensorManager)getSystemService(Context.SENSOR_SERVICE);
        acl=sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        lm=(LocationManager)getSystemService(Context.LOCATION_SERVICE);

        try {
            FileInputStream fis = openFileInput("list");
            ObjectInputStream is = new ObjectInputStream(fis);
            activityList= (ArrayList<activity_info>)is.readObject();
            is.close();
            fis.close();
        }
        catch (ClassNotFoundException | IOException e ){e.printStackTrace();}
        try {
            lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 0, this);
            lm.requestLocationUpdates(LocationManager.GPS_PROVIDER,1000,0,this);
        }
        catch (SecurityException e){
            e.printStackTrace();
        }

        activityList= new ArrayList<>();
        threshold= new ArrayList<>();
        speeds=new ArrayList<>();
    }
    public void setSpeed(float speed){
        this.speed=speed;
    }
    public float getSpeed() {
        return speed;
    }

    @Override
    public void onLocationChanged(Location location) {
        setSpeed(location.getSpeed());
        speeds.add(location.getSpeed());
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {
        Log.d("TAG", "onProviderEnabled: ");
    }

    @Override
    public void onProviderDisabled(String s) {
        Log.d("TAG", "dis: ");
    }

    public  final  void  onAccuracyChanged(Sensor s,int accuracy){

    }
    double max,min;
    public final void onSensorChanged(SensorEvent event){
        values=event.values;
        if(end-start>1000000000) {
            min = Collections.min(threshold);
            max = Collections.max(threshold);
            threshold.clear();
            if (max - min > 2.0 && getSpeed()> 0.1 && getSpeed() < 1.5) {
                status.setText("Walking\nSpeed: " + getSpeed()+" m/s");
                ımageView.setImageResource(R.drawable.walk);
                newActivity = 1;
            }
            else if (max - min >= 15.0 && getSpeed() >= 1.5) {
                status.setText("Running\nSpeed: " + getSpeed()+" m/s");
                ımageView.setImageResource(R.drawable.run);
                newActivity = 2;
            }
            else if (max - min <= 2.0 && getSpeed() < 0.1) {
                status.setText("Stationary\nSpeed: " + getSpeed()+" m/s");
                ımageView.setImageResource(R.drawable.stand);
                newActivity = 3;
            }
            if(oldActivity!=newActivity){
                sum=0;
                for (float f : speeds)
                    sum += f;
                sum = sum / speeds.size();
                speeds.clear();
                stopActivity();
                oldActivity=newActivity;
                startNewActivity();
            }
            start = event.timestamp;
        }
        threshold.add(Math.sqrt(Math.pow(values[0],2)+Math.pow(values[1],2)+Math.pow(values[2],2)));
        end= event.timestamp;
    }

    public void startNewActivity(){
        chronometer.setBase(SystemClock.elapsedRealtime());
        chronometer.start();
        startDate=Calendar.getInstance().getTime();
    }

    public void stopActivity(){
        int time=(int) (SystemClock.elapsedRealtime() - chronometer.getBase());
        int ss=(time/1000);
        int mm=ss/60;
        ss=ss%60;
        endDate=Calendar.getInstance().getTime();
        activity_info aInfo= new activity_info(sum,startDate,endDate,oldActivity,mm+" min: "+ss+" sec");
        activityList.add(aInfo);
    }

    public void onResume(){
        super.onResume();
        try {
            FileInputStream fis = openFileInput("list");
            ObjectInputStream is = new ObjectInputStream(fis);
            activityList= (ArrayList<activity_info>)is.readObject();
            is.close();
            fis.close();
        }
        catch (ClassNotFoundException | IOException e ){e.printStackTrace();}
        sm.registerListener(this,acl,sm.SENSOR_DELAY_NORMAL );
        chronometer.setBase(SystemClock.elapsedRealtime());
        chronometer.start();
    }

    public void onPause(){
        super.onPause();
        sm.unregisterListener(this);
    }

}
