package com.example.yusuf.smartsmsbox;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;


public class MyRecyclerViewAdapter extends RecyclerView.Adapter<MyRecyclerViewAdapter.MyViewHolder> {
    private ArrayList<Sms> sms;
    private Context context;
    private HashMap<String,Contact> contacts;
    public class MyViewHolder extends RecyclerView.ViewHolder {
        TextView from,body;
        ImageView personImage;
        public MyViewHolder(View view) {
            super(view);
            from=view.findViewById(R.id.from);
            body= view.findViewById(R.id.smsBody);
            //personImage=view.findViewById(R.id.personImage);
        }
    }

    public MyRecyclerViewAdapter(ArrayList<Sms> sms, Context context, HashMap<String,Contact> contacts){
        this.sms=sms;
        this.context=context;
        this.contacts=contacts;
    }
    public void setSms(ArrayList<Sms> sms) {
        this.sms = sms;
    }

    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.smsinfo, parent, false);
        return new MyViewHolder(itemView);
    }

    public void onBindViewHolder(MyViewHolder holder,int position) {
        final Sms s=sms.get(position);
        //Contact c=contacts.get(s.getNumber());
        if(s.getName_from()!=null)
            holder.from.setText(s.getName_from());
        else
            holder.from.setText(s.getNumber());
        holder.body.setText(s.getBody());
        holder.body.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context,ListSms.class);
                intent.putExtra("number",s.getNumber());
                intent.putExtra("name",s.getName_from());
                context.startActivity(intent);
            }
        });
    }

    public int getItemCount() {
        return sms.size();
    }
}
