package com.example.myapplication;

import android.content.ContentValues;
import android.content.Context;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener{
    static final String DB_NAME = "ProductDB";
    static final String TB_NAME = "product";
    static final int MAX = 8;
    static final String[] FROM = new String[]{"_id", "id", "name" , "price"};
    SQLiteDatabase db;
    Cursor cur;
    SimpleCursorAdapter adapter;
    EditText etId, etName, etPrice;
    Button btInsert;
    ListView lv;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        etId = (EditText)findViewById(R.id.etId);
        etName = (EditText) findViewById(R.id.etName);
        etPrice = (EditText) findViewById(R.id.etPrice);
        btInsert = (Button) findViewById(R.id.btInsert);
        db = openOrCreateDatabase(DB_NAME, Context.MODE_PRIVATE, null);
        String createTable = "CREATE TABLE IF NOT EXISTS " + TB_NAME +
                "(_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "id VARCHAR(32), " +
                "name VARCHAR(32), " +
                "price VARCHAR(32))";

        db.execSQL(createTable);
        cur = db.rawQuery("SELECT * FROM " + TB_NAME, null);
        adapter = new SimpleCursorAdapter(this,
                R.layout.item, cur,
                new String[]{"id", "name", "price"},
                new int[]{R.id.tvid, R.id.name, R.id.price},
                0);
        lv = (ListView)findViewById(R.id.lv);
        lv.setAdapter(adapter);
        lv.setOnItemClickListener(this);
        requery();
    }
    private void addData(String id, String name, String price){
        ContentValues cv = new ContentValues(3);
        cv.put("id", id);
        cv.put("name", name);
        cv.put("price", price);
        db.insert(TB_NAME, null, cv);
    }
    private void requery(){
        cur = db.rawQuery("SELECT * FROM " + TB_NAME, null);
        adapter.changeCursor(cur);
        if(cur.getCount()==MAX)
            btInsert.setEnabled(false);
        else
            btInsert.setEnabled(true);
    }
    @Override
    public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
        cur.moveToPosition(position);

        try {
            etId.setText(cur.getString(cur.getColumnIndexOrThrow(FROM[1])));
            etName.setText(cur.getString(cur.getColumnIndexOrThrow(FROM[2])));
            etPrice.setText(cur.getString(cur.getColumnIndexOrThrow(FROM[3])));
        } catch (IllegalArgumentException e) {
            // 处理列名不存在的情况
            e.printStackTrace();
        }
    }
    public void onInsertUpdate(View v){
        String idStr = etId.getText().toString().trim();
        String nameStr = etName.getText().toString().trim();
        String priceStr = etPrice.getText().toString().trim();
        if(idStr.length() == 0 || nameStr.length() == 0 || priceStr.length() ==0)return;
        addData(idStr, nameStr, priceStr);
        requery();
    }
}

