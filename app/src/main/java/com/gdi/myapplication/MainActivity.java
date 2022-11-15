package com.gdi.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private TextView tv;
    private Boolean testYn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        testYn = true;

        tv = findViewById(R.id.tv_hello);

        if(testYn) {
            tv.setText(getString(R.string.git_test_text));
        } else {
            tv.setText(getString(R.string.test_text));
        }

    }
}