package com.gdi.myapplication;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;


@RequiresApi(api = Build.VERSION_CODES.S)
public class MainActivity extends AppCompatActivity implements View.OnClickListener  {

    private final String[] PERMISSIONS = {Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION};
    private final int REQUEST_BLE_ENABLE = 100;


    private TextView tv2;
    private Button btnFind;
    private Button btnPaired;

    private BluetoothManager bluetoothManager;
    private BluetoothAdapter bluetoothAdapter;

    private BluetoothDevice bluetoothDevice;
    private Set<BluetoothDevice> pairedDevices;

    private List<String> deviceMap;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initLayout();
        init();

//        btnFind.setOnClickListener(view -> {

//            IntentFilter filter = new IntentFilter();
//            filter.addAction(BluetoothDevice.ACTION_FOUND);
//            filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
//            filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
//
//            registerReceiver(receiver, filter);
//
//            bluetoothAdapter.startDiscovery();
//        });
    }

    private void initLayout() {
        tv2 = findViewById(R.id.textView2);

        btnFind = findViewById(R.id.btn_find);
        btnPaired = findViewById(R.id.btn_paired);

        btnFind.setOnClickListener(this);
        btnPaired.setOnClickListener(this);

    }

    private void init() {
        // 1. check permissions
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_DENIED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_DENIED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_DENIED) {
//            ActivityCompat.requestPermissions(this, PERMISSIONS, BLE_ENABLE);
            requestPermissionLaunchers.launch(PERMISSIONS);
        } else {
            bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            if (bluetoothAdapter == null) {
                Toast.makeText(this, "Device doesn't support Bluetooth", Toast.LENGTH_SHORT).show();
            }

            // 3. activate bluetooth if it is deactivated
            if (!bluetoothAdapter.enable()) {
                Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                requestBleLauncher.launch(intent);
            }

            // 4. get paired devices
//            pairedDevices = bluetoothAdapter.getBondedDevices();
//
//            if (pairedDevices.size() > 0) {
//
//                List<String> deviceList = new ArrayList<>();
//
//                for (BluetoothDevice device : pairedDevices) {
//                    String deviceName = device.getName(); //or device.getAddress();
//                    deviceList.add(deviceName);
//                    Log.d("[deviceName]", deviceName);
//                }
//
//                buildDialog(deviceList);
//
//            }

            // 5. find bluetooth devices

        }
    }

    private void discover() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        if (bluetoothAdapter.isDiscovering()) {
            bluetoothAdapter.cancelDiscovery();
            Toast.makeText(this, getString(R.string.DisStop),Toast.LENGTH_SHORT).show();
        } else {
            if(bluetoothAdapter.isEnabled()) {
                IntentFilter filter = new IntentFilter();
                filter.addAction(BluetoothDevice.ACTION_FOUND);
                filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
                filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);

                registerReceiver(receiver, filter);

                bluetoothAdapter.startDiscovery();
            } else {
                Toast.makeText(this, getString(R.string.BTnotOn), Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void connectDevice(String deviceName, int divide) {
        if(divide == 1) {
            for (BluetoothDevice device : pairedDevices) {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {

                    return;
                }
                if (deviceName.equals(device.getName())) {
                    bluetoothDevice = device;
                    Toast.makeText(this, bluetoothDevice.getName(), Toast.LENGTH_SHORT).show();
                    break;
                }
            }
        } else {
            Toast.makeText(this, deviceName, Toast.LENGTH_SHORT).show();
        }

    }


    /**
     * Request Bluetooth Enabled
     */
    private final ActivityResultLauncher<Intent> requestBleLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getResultCode() == RESULT_OK) {
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


    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                return;
            }

            String action = intent.getAction();
            switch (action) {
                case BluetoothDevice.ACTION_FOUND:
                    if(device.getName() != null) {
                        deviceMap.add(device.getName());
                    }
                    break;
                case BluetoothAdapter.ACTION_DISCOVERY_STARTED:
                    Toast.makeText(context, "주변 블루투스 기기 찾는중...", Toast.LENGTH_LONG).show();
                    break;
                case BluetoothAdapter.ACTION_DISCOVERY_FINISHED:
                    Toast.makeText(context, "블루투스 기기 찾기 완료!", Toast.LENGTH_LONG).show();
                    buildDialogB(deviceMap);
                    break;
                default:
                    Toast.makeText(context, "블루투스 기기 찾기 실패!", Toast.LENGTH_LONG).show();
                    break;
            }
        }
    };


    public void buildDialog(List<String> items) {
        int size = items.size();

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Paired Bluetooth Device");

        CharSequence[] charSequences = items.toArray(new CharSequence[size]);
        items.toArray(new CharSequence[size]);

        builder.setItems(charSequences, (dialogInterface, i) -> {
            connectDevice(charSequences[i].toString(), 1);
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();

    }

    public void buildDialogB(List<String> items) {
        int size = items.size();

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Nearby Bluetooth Device");

        CharSequence[] charSequences = items.toArray(new CharSequence[size]);
        items.toArray(new CharSequence[size]);

        builder.setItems(charSequences, (dialogInterface, i) -> {
            connectDevice(charSequences[i].toString(), 2);
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        unregisterReceiver(receiver);
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()) {
            case R.id.btn_find:
                if (ActivityCompat.checkSelfPermission(view.getContext(), Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                deviceMap = new ArrayList<>();
                discover();
                break;

            case R.id.btn_paired:
                pairedDevices = bluetoothAdapter.getBondedDevices();

                if (pairedDevices.size() > 0) {

                    List<String> deviceList = new ArrayList<>();

                    for (BluetoothDevice device : pairedDevices) {
                        String deviceName = device.getName(); //or device.getAddress();
                        deviceList.add(deviceName);
                        Log.d("[deviceName]", deviceName);
                    }

                    buildDialog(deviceList);

                }
                break;
        }
    }
}