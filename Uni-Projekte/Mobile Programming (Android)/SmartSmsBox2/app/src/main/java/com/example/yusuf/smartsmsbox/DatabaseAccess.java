package com.example.yusuf.smartsmsbox;
import android.Manifest;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.ContactsContract;
import android.support.v4.app.ActivityCompat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;


public class DatabaseAccess extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "Database_SMS";
    private static final int DATABASE_VERSION = 1;
    private SQLiteDatabase db;

    public DatabaseAccess(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }

    public void createSmsTable() {
        db = this.getWritableDatabase();
        db.execSQL("CREATE TABLE IF NOT EXISTS SMS (ID INTEGER PRIMARY KEY,NUMBER TEXT,BODY TEXT,TYPE TEXT,LATITUDE TEXT" +
                ",LONGITUDE TEXT,TIME TEXT)");
    }

    public boolean saveSms(String number, String msgBody, String type, Double latitude, Double longitude, String time) {
        db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("NUMBER", number);
        contentValues.put("BODY", msgBody);
        contentValues.put("TYPE", type);
        contentValues.put("LATITUDE", latitude);
        contentValues.put("LONGITUDE", longitude);
        contentValues.put("TIME", time);
        db.insert("SMS", null, contentValues);
        return true;
    }


    public ArrayList<Sms> takeWhiteListSms(HashMap<String, Contact> contact) {
        ArrayList<Sms> smsArrayList = new ArrayList<>();
        HashMap<String,String> blackList=getBlackListNumber();
        String body, type, time;
        double longitude = 0.0, latitude = 0.0;
        db = this.getReadableDatabase();
        String phone;
        for (Map.Entry<String, Contact> c : contact.entrySet()) {
            phone = c.getValue().getPhone();
            phone = phone.replaceAll("\\s", "");
            phone = phone.replaceAll("\\(", "");
            phone = phone.replaceAll("\\)", "");
            phone = phone.replaceAll("\\-", "");
            if(blackList.get(phone)==null) {
                Cursor cursor = db.rawQuery("select * from SMS where NUMBER = '" + phone + "' or NUMBER = '+9" + phone + "'", null);
                cursor.moveToLast();
                if (cursor != null) {
                    if (cursor.getCount() > 0) {
                        body = cursor.getString(cursor.getColumnIndex("BODY"));
                        type = cursor.getString(cursor.getColumnIndex("TYPE"));
                        time = cursor.getString(cursor.getColumnIndex("TIME"));
                        if (cursor.getString(cursor.getColumnIndex("LONGITUDE")) != null) {
                            longitude = Double.parseDouble(cursor.getString(cursor.getColumnIndex("LONGITUDE")));
                            latitude = Double.parseDouble(cursor.getString(cursor.getColumnIndex("LATITUDE")));
                        }
                        Sms s = new Sms(phone, body, type, time, c.getValue().getName(), longitude, latitude);
                        smsArrayList.add(s);
                    }
                }
            }
        }
        return smsArrayList;
    }

    public ArrayList<Sms> getAllSmsViaNumber(String number, String name) {
        ArrayList<Sms> smsArrayList = new ArrayList<>();
        String body, type, time;
        double longitude = 0.0, latitude = 0.0;
        db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("select * from SMS where NUMBER = '" + number + "' or NUMBER = '+9" + number + "'", null);
        cursor.moveToFirst();
        do {
            if (cursor != null) {
                if(cursor.getCount()>0) {
                    body = cursor.getString(cursor.getColumnIndex("BODY"));
                    type = cursor.getString(cursor.getColumnIndex("TYPE"));
                    time = cursor.getString(cursor.getColumnIndex("TIME"));
                    if (cursor.getString(cursor.getColumnIndex("LONGITUDE")) != null) {
                        longitude = Double.parseDouble(cursor.getString(cursor.getColumnIndex("LONGITUDE")));
                        latitude = Double.parseDouble(cursor.getString(cursor.getColumnIndex("LATITUDE")));
                    }
                    Sms s = new Sms(number, body, type, time, name, longitude, latitude);
                    smsArrayList.add(s);
                }
            }
        } while (cursor != null && cursor.moveToNext());
        return smsArrayList;
    }

    public void createBlackList(){
        db=this.getWritableDatabase();
        db.execSQL("CREATE TABLE IF NOT EXISTS BLACKLIST (ID INTEGER PRIMARY KEY,NUMBER TEXT)");
    }

    public HashMap<String,String> getBlackListNumber() {
        HashMap<String,String> blackList = new HashMap<>();
        db = this.getReadableDatabase();
        Cursor cur = db.rawQuery("select * from BLACKLIST", null);
        cur.moveToFirst();
        do{
            if(cur!=null)
                if(cur.getCount()>0)
                    blackList.put(cur.getString(cur.getColumnIndex("NUMBER")),cur.getString(cur.getColumnIndex("NUMBER")));
        }while (cur != null && cur.moveToNext());
        return blackList;
    }

    public void addNumberToBlackList(String number){
        db=this.getWritableDatabase();
        ContentValues contentValues= new ContentValues();
        contentValues.put("NUMBER",number);
        db.insert("BLACKLIST",null,contentValues);
    }

    public void deleteNumberFromBlackList(String number){
        db=this.getWritableDatabase();
        db.delete("BLACKLIST","NUMBER = ?",new String[]{number});
    }

    public ArrayList<Sms> getAllContactSms(HashMap<String, Contact> contact){
        ArrayList<Sms> smsArrayList=new ArrayList<>();
        HashMap<String,String> blackList=getBlackListNumber();
        db=this.getReadableDatabase();
        String body, type, time,phone;
        double longitude = 0.0, latitude = 0.0;
        for (Map.Entry<String, Contact> c : contact.entrySet()) {
            phone = c.getValue().getPhone();
            phone = phone.replaceAll("\\s", "");
            phone = phone.replaceAll("\\(", "");
            phone = phone.replaceAll("\\)", "");
            phone = phone.replaceAll("\\-", "");
            if(blackList.get(phone)==null) {
                Cursor cursor = db.rawQuery("select * from SMS where NUMBER = '" + phone + "' or NUMBER = '+9" + phone + "'", null);
                cursor.moveToFirst();
                do {
                    if (cursor != null) {
                        if (cursor.getCount() > 0) {
                            body = cursor.getString(cursor.getColumnIndex("BODY"));
                            type = cursor.getString(cursor.getColumnIndex("TYPE"));
                            time = cursor.getString(cursor.getColumnIndex("TIME"));
                            if (cursor.getString(cursor.getColumnIndex("LONGITUDE")) != null) {
                                longitude = Double.parseDouble(cursor.getString(cursor.getColumnIndex("LONGITUDE")));
                                latitude = Double.parseDouble(cursor.getString(cursor.getColumnIndex("LATITUDE")));
                            }
                            Sms s = new Sms(phone, body, type, time, c.getValue().getName(), longitude, latitude);
                            smsArrayList.add(s);
                        }
                    }
                }while(cursor!=null&cursor.moveToNext());
            }
        }
        return smsArrayList;
    }

    public ArrayList<Sms> getAllBlackListSms() {
        HashMap<String,String> blackList=getBlackListNumber();
        ArrayList<Sms> smsArrayList = new ArrayList<>();
        String body, type, time;
        double longitude = 0.0, latitude = 0.0;
        db = this.getReadableDatabase();
        Cursor cur;
        for (Map.Entry<String, String> b : blackList.entrySet()){
            cur=db.rawQuery("select * from SMS where NUMBER = '"+b.getValue()+"' or NUMBER = '+9"+b.getValue()+"'",null);
            cur.moveToLast();
            if(cur!=null){
                if(cur.getCount()>0) {
                    body = cur.getString(cur.getColumnIndex("BODY"));
                    type = cur.getString(cur.getColumnIndex("TYPE"));
                    time = cur.getString(cur.getColumnIndex("TIME"));
                    if (cur.getString(cur.getColumnIndex("LONGITUDE")) != null) {
                        longitude = Double.parseDouble(cur.getString(cur.getColumnIndex("LONGITUDE")));
                        latitude = Double.parseDouble(cur.getString(cur.getColumnIndex("LATITUDE")));
                    }
                    Sms s = new Sms(b.getValue(), body, type, time, b.getValue(), longitude, latitude);
                    smsArrayList.add(s);
                }
            }
        }
        return smsArrayList;
    }

    public ArrayList<Sms> getAllOTPSms(){
        ArrayList<Sms> smsArrayList=new ArrayList<>();
        db=this.getReadableDatabase();
        String body, type, time,number;
        double longitude = 0.0, latitude = 0.0;
        Cursor cur=db.rawQuery("select * from SMS",null);
        cur.moveToFirst();
        do{
            body=cur.getString(cur.getColumnIndex("BODY"));
            number=cur.getString(cur.getColumnIndex("NUMBER"));
            if((body.toLowerCase().contains("şifre")||body.toLowerCase().contains("sifre"))
                    ||(body.toLowerCase().contains("paylasmayiniz")&&number.toLowerCase().contains("banka")&&body.toLowerCase().contains("sifre"))
                    &&(body.contains("B001")||body.contains("B002")||body.contains("B003"))){
                body = cur.getString(cur.getColumnIndex("BODY"));
                type = cur.getString(cur.getColumnIndex("TYPE"));
                time = cur.getString(cur.getColumnIndex("TIME"));
                if (cur.getString(cur.getColumnIndex("LONGITUDE")) != null) {
                    longitude = Double.parseDouble(cur.getString(cur.getColumnIndex("LONGITUDE")));
                    latitude = Double.parseDouble(cur.getString(cur.getColumnIndex("LATITUDE")));
                }
                Sms s = new Sms(number, body, type, time, number, longitude, latitude);
                smsArrayList.add(s);

            }
        }while (cur!=null&&cur.moveToNext());
        return smsArrayList;
    }

    public ArrayList<Sms> getAllCommercialSms(){
        ArrayList<Sms> smsArrayList=new ArrayList<>();
        db=this.getReadableDatabase();
        String body, type, time,number;
        double longitude = 0.0, latitude = 0.0;
        Cursor cur=db.rawQuery("select * from SMS",null);
        cur.moveToFirst();
        do{
            body=cur.getString(cur.getColumnIndex("BODY"));
            number=cur.getString(cur.getColumnIndex("NUMBER"));
            Pattern p=Pattern.compile("B.*[0-9].*[0-9].*[0-9]");
            if(p.matcher(body).find()&&(!body.toLowerCase().contains("sifre"))){
                body = cur.getString(cur.getColumnIndex("BODY"));
                type = cur.getString(cur.getColumnIndex("TYPE"));
                time = cur.getString(cur.getColumnIndex("TIME"));
                if (cur.getString(cur.getColumnIndex("LONGITUDE")) != null) {
                    longitude = Double.parseDouble(cur.getString(cur.getColumnIndex("LONGITUDE")));
                    latitude = Double.parseDouble(cur.getString(cur.getColumnIndex("LATITUDE")));
                }
                Sms s = new Sms(number, body, type, time, number, longitude, latitude);
                smsArrayList.add(s);

            }
        }while (cur!=null&&cur.moveToNext());
        return smsArrayList;
    }
}