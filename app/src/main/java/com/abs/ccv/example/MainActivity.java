package com.abs.ccv.example;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.abs.ccv.CircularCountdownView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        CircularCountdownView ccv = (CircularCountdownView) findViewById(R.id.ccv);
        ccv.setDuration(30 * 1000);
        ccv.setInitialProgress(15 * 1000);
    }
}
