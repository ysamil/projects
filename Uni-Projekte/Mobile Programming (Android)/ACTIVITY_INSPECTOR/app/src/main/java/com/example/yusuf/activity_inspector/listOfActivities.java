package com.example.yusuf.activity_inspector;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

public class listOfActivities extends AppCompatActivity {

    private RecyclerView recyclerView;
    private RecyclerView.Adapter adapter;
    private RecyclerView.LayoutManager layoutManager;
    private MyAdapter myAdapter;
    private ArrayList<activity_info> aInfo;
    private Button delete;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_of_activities);
        recyclerView=(RecyclerView)findViewById(R.id.my_recycler_view);
        Bundle bundle = getIntent().getExtras();
        aInfo=(ArrayList<activity_info>)bundle.getSerializable("list");
        myAdapter=new MyAdapter(aInfo);
        layoutManager=new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addItemDecoration(new SimpleDividerItemDecoration(getApplicationContext()));
        recyclerView.setAdapter(myAdapter);
        delete=(Button)findViewById(R.id.delete_btn);
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try{
                    ArrayList<activity_info> temp= new ArrayList<>();
                    FileOutputStream fOut= openFileOutput("list", Context.MODE_PRIVATE);
                    ObjectOutputStream obj = new ObjectOutputStream(fOut);
                    obj.writeObject(temp);
                    obj.close();
                    fOut.close();
                    Toast.makeText(getApplicationContext(),"Deleted",Toast.LENGTH_SHORT).show();
                }
                catch (IOException e){ e.printStackTrace();}
            }
        });
    }
}