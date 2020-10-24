package com.example.yusuf.smartsmsbox;

import android.icu.text.UnicodeSetSpanner;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;

public class edit_BlackList extends AppCompatActivity implements View.OnClickListener{

    private ListView listView;
    private Button add,delete;
    private EditText number;
    private DatabaseAccess db;
    private ArrayAdapter adapter;
    ArrayList<String> blackListNumber;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit__black_list);
        listView=findViewById(R.id.listview_Blacklist);
        number=(EditText) findViewById(R.id.numberForBlacklist);
        add=findViewById(R.id.addToBlacklist_btn);
        delete=findViewById(R.id.delete_btn);
        db = new DatabaseAccess(getApplicationContext());
        db.createBlackList();
        blackListNumber=new ArrayList<>(db.getBlackListNumber().values());
        adapter=new ArrayAdapter<String>(getApplicationContext(),android.R.layout.simple_list_item_1,blackListNumber);
        listView.setAdapter(adapter);
        add.setOnClickListener(this);
        delete.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if(view.getId()==R.id.addToBlacklist_btn) {
            db.addNumberToBlackList(number.getText().toString());
            blackListNumber=new ArrayList<>(db.getBlackListNumber().values());
            adapter=new ArrayAdapter<String>(getApplicationContext(),android.R.layout.simple_list_item_1,blackListNumber);
            listView.setAdapter(adapter);
            adapter.notifyDataSetChanged();
            Toast.makeText(getApplicationContext(), "Number saved to blacklist", Toast.LENGTH_SHORT).show();
        }
        else if(view.getId()==R.id.delete_btn){
            db.deleteNumberFromBlackList(number.getText().toString());
            blackListNumber=new ArrayList<>(db.getBlackListNumber().values());
            adapter=new ArrayAdapter<String>(getApplicationContext(),android.R.layout.simple_list_item_1,blackListNumber);
            listView.setAdapter(adapter);
            adapter.notifyDataSetChanged();
            Toast.makeText(getApplicationContext(), "Number deleted from blacklist", Toast.LENGTH_SHORT).show();
        }
    }
}
