package com.abs.ccv.example;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.abs.ccv.CircularCountdownView;
import com.abs.ccv.CircularCountdownViewListener;

public class MainActivity extends AppCompatActivity implements CircularCountdownViewListener {

    CircularCountdownView ccv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ccv = (CircularCountdownView) findViewById(R.id.ccv);
        ccv.setDuration(30 * 1000);
        ccv.setInitialProgress(15 * 1000);
        ccv.setListener(this);
    }

    @Override
    public void onCountdownFinished() {
        Toast.makeText(this, "onCountdownFinished", Toast.LENGTH_LONG).show();
        ccv.setInitialProgress(0);
    }
}
