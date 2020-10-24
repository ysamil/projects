package com.example.yusuf.smartsmsbox;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by Yusuf on 26.05.2018.
 */

public class Sms implements Serializable {
    private String number,type,name_from,body,time;
    Double longitude,latitude;

    public Sms(String number, String body,String type,String time,String name_from,double longitude, double latitude){
        this.number=number; this.body=body; this.type=type;
        this.time=time; this.longitude=longitude; this.latitude=latitude;
        this.name_from=name_from;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName_from() {
        return name_from;
    }

    public void setName_from(String name_from) {
        this.name_from = name_from;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }


}
