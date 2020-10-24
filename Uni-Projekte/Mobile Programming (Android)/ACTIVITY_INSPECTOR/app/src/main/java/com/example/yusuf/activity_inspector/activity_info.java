package com.example.yusuf.activity_inspector;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.DateTimeException;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by Yusuf on 20.05.2018.
 */

public class activity_info implements Serializable{
    private float speed;
    private Date start,end;
    private int state;
    private String spendTime;
    public activity_info(float speed, Date start, Date end,int state,String spendTime) {
        this.speed=speed; this.start=start; this.end=end; this.state=state; this.spendTime=spendTime;
    }

    public float getSpeed(){
        return speed;
    }

    public int getState() {
        return state;
    }


    public String getSpendTime() {
        return spendTime;
    }

    public Date getStart() {
        return start;
    }

    public Date getEnd() {
        return end;
    }

}
