package com.example.lbstest2;

import android.content.Intent;
import android.icu.text.LocaleDisplayNames;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

public class SecondActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);
        Intent intent=  getIntent();
        String title=intent.getStringExtra("id1");
        Toast.makeText(getApplicationContext(),title, Toast.LENGTH_SHORT).show();



    }
}
