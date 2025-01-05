package com.example.myapplication;

import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.Random;
public class MainActivity extends AppCompatActivity {
    private EditText myfirst, mylast, myphone;
    private TextView myresult;
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
        myfirst = (EditText) findViewById(R.id.FirstName);
        mylast = (EditText) findViewById(R.id.LastName);
        myphone = (EditText) findViewById(R.id.Phone);
        myresult = (TextView) findViewById(R.id.Result);
    }
    public void submit(View v){
        Random x=new Random();
        int red=x.nextInt(256);
        int blue=x.nextInt(256);
        int green=x.nextInt(256);
        if (TextUtils.isEmpty(mylast.getText())) {
            myresult.setText("請在姓輸入姓！");
            myresult.setTextColor(Color.RED);
        } else if (TextUtils.isEmpty(myfirst.getText())) {
            myresult.setText("請在名輸入名！");
            myresult.setTextColor(Color.RED);
        } else if (TextUtils.isEmpty(myphone.getText())) {
            myresult.setText("請在電話輸入電話！");
            myresult.setTextColor(Color.RED);
        }else {
            myresult.setText(mylast.getText().toString()+myfirst.getText().toString()+"電話"+myphone.getText().toString());
            myresult.setTextColor(Color.rgb(red,blue,green));
        }

    }
}
