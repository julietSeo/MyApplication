package com.gdi.myapplication;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Collection;


@RequiresApi(api = Build.VERSION_CODES.S)
public class MainActivity extends AppCompatActivity {

    private final String[] PERMISSIONS = {Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN};
    private final int REQUEST_BLE_ENABLE = 100;


    private TextView tv2;
    private Button btnReceive;
    private TextView receivedData;

    private BluetoothManager bluetoothManager;
    private BluetoothAdapter bluetoothAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initLayout();
        init();
    }

    private void init() {
        // 1. check Permissions
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_DENIED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_DENIED) {
//            ActivityCompat.requestPermissions(this, PERMISSIONS, BLE_ENABLE);
            requestPermissionLaunchers.launch(PERMISSIONS);
        }

        // 2. check if device supports Bluetooth
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(bluetoothAdapter == null) {
            Toast.makeText(this, "Device doesn't support Bluetooth", Toast.LENGTH_SHORT).show();
        }

        // 3. activate Bluetooth if it is deactivated
        if(!bluetoothAdapter.enable()) {
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            requestBleLauncher.launch(intent);
        }
    }

    /**
     * Request Bluetooth Enabled
     */
    private final ActivityResultLauncher<Intent> requestBleLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if(result.getResultCode() == RESULT_OK) {
            Toast.makeText(this, "Bluetooth is deactivated", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Bluetooth is deactivated", Toast.LENGTH_SHORT).show();
        }

    });

    /**
     * Request Multiple Permissions
     */
    private final ActivityResultLauncher<String[]> requestPermissionLaunchers = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), isGranted -> {
        Collection<Boolean> values = isGranted.values();
        for (Boolean v : values) {
            if (v) {
                Log.d("[Check granted]", "isGranted = true");
            } else {
                Log.d("[Check granted]", "isGranted = false");
            }
        }
    });

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