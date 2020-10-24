package com.example.yusuf.activity_inspector;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {
    private ArrayList<activity_info> aInfo;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        TextView spendTime,speed,state,start,end;
        ImageView img;
        public MyViewHolder(View view) {
            super(view);
            spendTime=(TextView)view.findViewById(R.id.spend);
            speed=(TextView)view.findViewById(R.id.speed);
            state=(TextView)view.findViewById(R.id.state);
            start=(TextView)view.findViewById(R.id.start);
            end=(TextView)view.findViewById(R.id.end);
            img=(ImageView)view.findViewById(R.id.img);
        }
    }

    public MyAdapter(ArrayList<activity_info> aInfo ){
        this.aInfo=aInfo;
    }

    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.ainfo, parent, false);
        return new MyViewHolder(itemView);
    }

    public void onBindViewHolder(MyViewHolder holder,int position) {
        final activity_info activity=aInfo.get(position);
        String state="";
        int s=activity.getState();
        if(s==1) {
            state = "Walking";
            holder.img.setImageResource(R.drawable.walk);
        }
        else if(s==2) {
            state = "Running";
            holder.img.setImageResource(R.drawable.run);
        }
        else if(s==3) {
            state = "Stationary";
            holder.img.setImageResource(R.drawable.stand);
        }
        holder.img.getLayoutParams().width=150;
        holder.img.getLayoutParams().height=150;
        holder.state.setText("Activity :"+ state+"\n");
        if(!Float.toString(activity.getSpeed()).equals("NaN"))
            holder.speed.setText("Average speed: "+Float.toString(activity.getSpeed())+"\n");
        else
            holder.speed.setText("Average speed: No Gps Info\n");
        holder.spendTime.setText("Activity time: "+activity.getSpendTime()+"\n");
        SimpleDateFormat format = new SimpleDateFormat("d MMMM, yyyy  h:mm:ss a");
        holder.start.setText("Activity start time:\n"+"   "+format.format(activity.getStart())+"\n");
        holder.end.setText("Activity end time:\n"+"   "+format.format(activity.getEnd())+"\n");

    }

    public int getItemCount() {
        return aInfo.size();
    }
}