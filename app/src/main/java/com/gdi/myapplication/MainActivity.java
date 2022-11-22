package com.gdi.myapplication;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.FileUtils;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;


@RequiresApi(api = Build.VERSION_CODES.S)
public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private final String[] permissions = {
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
    };

    private static final long SCAN_PERIOD = 10000;

    private BluetoothAdapter bluetoothAdapter;

    private BluetoothLeScanner bluetoothLeScanner;
    private Handler handler;
    private boolean scanning = false;

    private List<String> deviceMap;
    private List<String> bleMap;



    private ProgressDialog progressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initLayout();
        init();
    }

    private void initLayout() {
        TextView tv2;
        Button btnFind;
        Button btnPaired;
        Button btnBle;
        ProgressBar progressBar;

        tv2 = findViewById(R.id.textView2);
        tv2.setVisibility(View.GONE);

        btnFind = findViewById(R.id.btn_find);
        btnPaired = findViewById(R.id.btn_paired);
        btnBle = findViewById(R.id.btn_ble);

        progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.GONE);
        progressBar.setIndeterminate(true);

        btnFind.setOnClickListener(this);
        btnPaired.setOnClickListener(this);
        btnBle.setOnClickListener(this);

    }

    private void init() {
        // 1. check permissions
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_DENIED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_DENIED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_DENIED) {
            requestPermissionLaunchers.launch(permissions);
        } else {
            bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            if (bluetoothAdapter == null) {
                Toast.makeText(this, getString(R.string.BTdisable), Toast.LENGTH_SHORT).show();
            }

            // 3. activate bluetooth if it is deactivated
            if (!bluetoothAdapter.enable()) {
                Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                requestBleLauncher.launch(intent);
            }

        }
        handler = new Handler();
        bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
    }

    private void discover() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        if (bluetoothAdapter.isDiscovering()) {
            bluetoothAdapter.cancelDiscovery();
            Toast.makeText(this, getString(R.string.DisStop), Toast.LENGTH_SHORT).show();
        } else {
            if (bluetoothAdapter.isEnabled()) {
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
                    if (device.getName() != null) {
                        deviceMap.add(device.getName());
                    }
                    break;
                case BluetoothAdapter.ACTION_DISCOVERY_STARTED:

                    break;
                case BluetoothAdapter.ACTION_DISCOVERY_FINISHED:
                    Toast.makeText(context, "블루투스 기기 찾기 완료!", Toast.LENGTH_LONG).show();
                    progressDialog.dismiss();
                    buildDialog(deviceMap);
                    break;
                default:
                    Toast.makeText(context, "블루투스 기기 찾기 실패!", Toast.LENGTH_LONG).show();
                    break;
            }
        }
    };

    private void scan() {
        if (!scanning) {
            // stop scanning
            handler.postDelayed(() -> {

                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }

                scanning = false;
                bluetoothLeScanner.stopScan(leScanCallback);

                progressDialog.dismiss();
                buildDialog(bleMap);
                Toast.makeText(this, "Completed!", Toast.LENGTH_LONG).show();
            }, SCAN_PERIOD);

            scanning = true;
            bluetoothLeScanner.startScan(leScanCallback);
        } else {
            scanning = false;
            bluetoothLeScanner.stopScan(leScanCallback);
        }
    }

    private ScanCallback leScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            if (ActivityCompat.checkSelfPermission(getBaseContext(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {

                return;
            }
            Log.d("[bleScan]", "==>" + result.getDevice().getName());
            if(!bleMap.contains(result.getDevice().getAddress())) {
                bleMap.add(result.getDevice().getAddress());
            }
        }
    };

    /**
     * Request Bluetooth Enabled
     */
    private final ActivityResultLauncher<Intent> requestBleLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getResultCode() == RESULT_OK) {
            Toast.makeText(this, getString(R.string.BTdeactivate), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, getString(R.string.BTdeactivate), Toast.LENGTH_SHORT).show();
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

    public void buildDialog(List<String> items) {
        int size = items.size();

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Bluetooth Device");

        CharSequence[] charSequences = items.toArray(new CharSequence[size]);
        items.toArray(new CharSequence[size]);

        builder.setItems(charSequences, (dialogInterface, i) -> {
            //TODO: Click Event
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
        if(view.getId() == R.id.btn_find) {
            if (ActivityCompat.checkSelfPermission(view.getContext(), Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            deviceMap = new ArrayList<>();
            progressDialog = new ProgressDialog(this);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setMessage(getString(R.string.action_discovery_started));

            discover();

            progressDialog.show();
        } else if(view.getId() == R.id.btn_ble) {
            bleMap = new ArrayList<>();
            progressDialog = new ProgressDialog(this);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setMessage(getString(R.string.action_discovery_ble));

            scan();

            progressDialog.show();
        } else {
            Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();

            if (pairedDevices.size() > 0) {

                List<String> deviceList = new ArrayList<>();

                for (BluetoothDevice device : pairedDevices) {
                    String deviceName = device.getName(); //or device.getAddress();
                    deviceList.add(deviceName);
                    Log.d("[deviceName]", deviceName);
                }

                buildDialog(deviceList);

            }
        }
    }
}