package com.gdi.myapplication;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Collection;
import java.util.Map;



public class MainActivity extends AppCompatActivity {

    private final String[] PERMISSIONS = {Manifest.permission.BLUETOOTH_CONNECT/*, Manifest.permission.BLUETOOTH_SCAN*/};
    private final int BLE_ENABLE = 100;

    private TextView tv2;
    private Button btnReceive;
    private TextView receivedData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initLayout();
        init();
    }

    private void init() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            checkEssentialPermissions();
        }

    }


    private void checkEssentialPermissions() {
        int checkPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT);
        int checkPermissionB = ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN);

        if(checkPermission < 0) {
            ActivityCompat.requestPermissions(this, PERMISSIONS, BLE_ENABLE);
//            requestPermissionLauncher.launch(Manifest.permission.BLUETOOTH_CONNECT);
        } else {
            Toast.makeText(this, "Ready to start!", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Request Simple Permission
     */
    private final ActivityResultLauncher<String> requestPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {

        if(isGranted) {
            Log.d("[Check granted]", "isGranted = true");
        } else {
            Log.d("[Check granted]", "isGranted = false");
        }
    });

    /**
     * Request Multiple Permissions
     */
//    private final ActivityResultLauncher<String[]> requestPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), isGranted -> {
//        Collection<Boolean> values = isGranted.values();
//        for(Boolean v : values) {
//            if(v) {
//                Log.d("[Check permissions granted]", "isGranted = true");
//            } else {
//                Log.d("[Check permissions granted]", "isGranted = false");
//            }
//        }
//    });

    private void initLayout() {

        tv2 = findViewById(R.id.textView2);

        btnReceive = findViewById(R.id.btn_receive);
        receivedData = findViewById(R.id.tv_rcvData);

        btnReceive.setOnClickListener(v -> getData());
    }


    public void getData() {
        //TODO: get Data from connected device
        Toast.makeText(this, "Receive Button clicked", Toast.LENGTH_SHORT).show();
        Log.d("[Check Button clicked]","Receive Button clicked");

    }
}