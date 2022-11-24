package com.gdi.myapplication;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.FileUtils;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.gdi.myapplication.utils.RecyclerDecoration;

import java.lang.reflect.Method;
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
    private BluetoothManager bluetoothManager;

    private BluetoothLeScanner bluetoothLeScanner;
    private Handler handler;
    private boolean scanning = false;

    private List<String> deviceMap;
    private List<BluetoothDevice> bleMap;

    private Set<BluetoothDevice> pairedDevices;
    private List<String> pairedDeviceList;

    private ListAdapter listAdapter;
    private RecyclerView recyclerView;


    private ProgressBar progressBar;

    private int selectDevice;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initLayout();
        init();
    }

    @SuppressLint("NotifyDataSetChanged")
    private void initLayout() {
        TextView tv2;
        Button btnFind;
        Button btnPaired;
        Button btnBle;

        tv2 = findViewById(R.id.textView2);

        btnFind = findViewById(R.id.btn_find);
        btnPaired = findViewById(R.id.btn_paired);
        btnBle = findViewById(R.id.btn_ble);

        listAdapter = new ListAdapter();

        recyclerView = findViewById(R.id.rv_devices);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(), new LinearLayoutManager(this).getOrientation());
        recyclerView.addItemDecoration(dividerItemDecoration);
        recyclerView.setHasFixedSize(true);

        listAdapter.notifyDataSetChanged();

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
            bluetoothManager = getSystemService(BluetoothManager.class);
            bluetoothAdapter = bluetoothManager.getAdapter();
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
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
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
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
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
                    progressBar.setVisibility(View.GONE);

//                    buildDialog(deviceMap);
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

                if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }

                scanning = false;
                bluetoothLeScanner.stopScan(leScanCallback);

                progressBar.setVisibility(View.GONE);
//                buildDialog(bleMap);
                for(BluetoothDevice device: bleMap) {
                    Log.d("[device test]", device.toString());
                    listAdapter.setArrayData(device.getAddress());
                }
                listAdapter.setOnItemClickListener((v, pos) -> {
                    pairingDevice(pos);
//                    BluetoothDevice device = bleMap.get(pos);
//                    String address = bleMap.get(pos).getAddress();
//                    Log.d("[address]", address);
                });
                recyclerView.setAdapter(listAdapter);
                Toast.makeText(this, "Completed!", Toast.LENGTH_LONG).show();
            }, SCAN_PERIOD);

            scanning = true;
            bluetoothLeScanner.startScan(leScanCallback);
        } else {
            scanning = false;
            bluetoothLeScanner.stopScan(leScanCallback);
        }
    }

    private final ScanCallback leScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            if (ContextCompat.checkSelfPermission(getBaseContext(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {

                return;
            }
            Log.d("[bleScan]", "==>" + result.getDevice().getName());
            if(!bleMap.contains(result.getDevice())) {
                bleMap.add(result.getDevice());
            }
        }
    };

    private void pairingDevice(int selected) {
        BluetoothDevice device = bleMap.get(selected);

        try {
            Method method = device.getClass().getMethod("createBond", (Class<?>[]) null);
            method.invoke(device, (Object[]) null);
            selectDevice = selected;
            Log.d("selectedDevice", device.getAddress());

//            pairedDeviceList.add(device.getAddress());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

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
            if (ContextCompat.checkSelfPermission(view.getContext(), Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            deviceMap = new ArrayList<>();

            discover();

            progressBar.setVisibility(View.VISIBLE);
        } else if(view.getId() == R.id.btn_ble) {
            bleMap = new ArrayList<>();

            scan();

            progressBar.setVisibility(View.VISIBLE);
        } else {
            pairedDevices = bluetoothAdapter.getBondedDevices();

            if (pairedDevices.size() > 0) {

                pairedDeviceList = new ArrayList<>();

                for (BluetoothDevice device : pairedDevices) {
                    String deviceName = device.getName(); //or device.getAddress();
                    pairedDeviceList.add(deviceName);
                    pairedDeviceList.add(device.getAddress());
                    Log.d("[deviceName]", deviceName);
                }

                buildDialog(pairedDeviceList);

            }
        }
    }
}