package com.example.yusuf.smartsmsbox;

import android.app.Activity;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;


public class ListSmsRecycler extends RecyclerView.Adapter<ListSmsRecycler.MyViewHolder> {
    private ArrayList<Sms> sms;
    public class MyViewHolder extends RecyclerView.ViewHolder {
        TextView body,from,date;
        public MyViewHolder(View view) {
            super(view);
            body=view.findViewById(R.id.bodyOfSms);
            from=view.findViewById(R.id.fromWho);
            date=view.findViewById(R.id.date);
        }
    }

    public ListSmsRecycler(ArrayList<Sms> sms){
        this.sms=sms;
    }

    public void setSms(ArrayList<Sms> sms) {
        this.sms = sms;
    }
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.listsms, parent, false);
        return new MyViewHolder(itemView);
    }

    public void onBindViewHolder(MyViewHolder holder,int position) {
        final Sms s=sms.get(position);
        holder.body.setText(s.getBody());
        holder.date.setText(s.getTime());
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        RelativeLayout.LayoutParams lp=new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        if(s.getType().equals("receiving")){
            lp.setMargins(50,0,0,0);
            params.gravity =Gravity.LEFT;
            if(s.getName_from()!=null)
                holder.from.setText(s.getName_from());
            else
                holder.from.setText(s.getNumber());
        }
        else if(s.getType().equals("sending")) {
            lp.setMargins(800, 0, 0, 0);
            holder.from.setText("You");
            params.gravity = Gravity.RIGHT;
        }
        holder.from.setLayoutParams(lp);
        holder.body.setLayoutParams(params);
    }


    public int getItemCount() {
        return sms.size();
    }
}

