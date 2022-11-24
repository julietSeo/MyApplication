package com.gdi.myapplication

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import java.util.*

@RequiresApi(Build.VERSION_CODES.S)
class SubActivity : AppCompatActivity(), View.OnClickListener {

    private val permissions = arrayOf(
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
    )

    private val SCANPERIOD: Long = 10000

    private lateinit var bluetoothAdapter: BluetoothAdapter
    private lateinit var bluetoothManager: BluetoothManager

    private lateinit var bluetoothLeScanner: BluetoothLeScanner
    private lateinit var handler: Handler
    private var scanning: Boolean = false

    private lateinit var deviceMap: MutableList<String>
    private lateinit var bleMap: MutableList<String>

    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sub)

        initLayout();
        init();
        

    }

    private fun initLayout() {
        val btnFind = findViewById<Button>(R.id.btn_find)
        val btnBLE = findViewById<Button>(R.id.btn_ble);
        val btnPaired = findViewById<Button>(R.id.btn_paired);

        btnFind.setOnClickListener(this)
        btnBLE.setOnClickListener(this)
        btnPaired.setOnClickListener(this)

        progressBar = findViewById(R.id.progressBar)
        progressBar.visibility = View.GONE
        progressBar.isIndeterminate = true

        handler = Handler(Looper.getMainLooper())

        val txtView = findViewById<TextView>(R.id.textView2);
    }

    private fun init() {
        // 1. check permissions
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_DENIED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_DENIED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_DENIED) {
            requestPermissionResultLauncher.launch(permissions)
        } else {
            bluetoothManager = getSystemService(BluetoothManager::class.java) as BluetoothManager
            bluetoothAdapter = bluetoothManager.adapter

            if(!bluetoothAdapter.enable()) {
                var intent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                requestBleResultLauncher.launch(intent)
            }
        }

        bluetoothLeScanner = bluetoothAdapter.bluetoothLeScanner

    }

    private fun discover() {
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        if(bluetoothAdapter.isDiscovering){
            bluetoothAdapter.cancelDiscovery()
            Toast.makeText(this, getString(R.string.DisStop), Toast.LENGTH_SHORT).show()
        } else {
            if(bluetoothAdapter.isEnabled) {
                val filter = IntentFilter()
                filter.addAction(BluetoothDevice.ACTION_FOUND)
                filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED)
                filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)

                registerReceiver(receiver, filter)

                bluetoothAdapter.startDiscovery()
            }
        }
    }

    private fun scan() {
        try {
            if(!scanning) {
                handler.postDelayed({
                    scanning = false
                    bluetoothLeScanner.stopScan(leScanCallback)

                    progressBar.visibility = View.GONE
                    buildDialog(bleMap)
                }, SCANPERIOD)

                scanning = true
                bluetoothLeScanner.startScan(leScanCallback)
            } else {
                scanning = false
                bluetoothLeScanner.stopScan(leScanCallback)
            }
        } catch(e: SecurityException) {
            e.printStackTrace()
        }
    }

    private val leScanCallback: ScanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            super.onScanResult(callbackType, result)

            if(ContextCompat.checkSelfPermission(baseContext, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                return
            }

            if(!bleMap.contains(result.device.address)) {
                if(result.device.name != null && !bleMap.contains(result.device.name)) {
                    bleMap.add(result.device.name)
                    return
                }
                bleMap.add(result.device.address)
            }
        }
    }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val device: BluetoothDevice? = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
            if(ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                return
            }

            val action = intent.action
            when(action) {
                BluetoothDevice.ACTION_FOUND -> {
                    if(device?.name != null) {
                        deviceMap.add(device.name)
                    }
                }
                BluetoothAdapter.ACTION_DISCOVERY_STARTED -> {}
                BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                    Toast.makeText(context, "블루투스 기기 찾기 완료!", Toast.LENGTH_SHORT).show()
                    progressBar.visibility = View.GONE
                    buildDialog(deviceMap)
                }
                else -> {
                    Toast.makeText(context, "블루투스 기기 찾기 실패!", Toast.LENGTH_SHORT).show()
                }
            }
        }

    }

    private val requestPermissionResultLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { isGranted ->
        isGranted.forEach {
            if(!it.value) {
                Toast.makeText(this, "${it.key} 권한 허용 필요", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }


    private val requestBleResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if(result.resultCode != RESULT_OK) {
            Toast.makeText(this, getString(R.string.BTdeactivate), Toast.LENGTH_SHORT).show()
        }
    }

    fun buildDialog(items: MutableList<String>) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Bluetooth Device")
            .setItems(items.toTypedArray()) { dialogInterface, which ->
                Toast.makeText(this, items[which], Toast.LENGTH_SHORT).show()
            }

        builder.show()
    }

    override fun onDestroy() {
        super.onDestroy()

        unregisterReceiver(receiver)
    }

    override fun onClick(v: View?) {
        if(v?.id == R.id.btn_find) { //Bluetooth
            try {
                deviceMap = mutableListOf()
                discover()

                progressBar.visibility = View.VISIBLE

            } catch (e: SecurityException) {
                e.printStackTrace()
            }

        } else if(v?.id == R.id.btn_ble) { //Bluetooth Low Energy
            bleMap = mutableListOf()
            scan()

            progressBar.visibility = View.VISIBLE

        } else {
            try {
                val pairedDevices: Set<BluetoothDevice>? = bluetoothAdapter.bondedDevices
                val deviceList = mutableListOf<String>()

                pairedDevices?.forEach {
                    val deviceName = it.name

                    deviceList.add(deviceName)
                }

                buildDialog(deviceList)

            } catch (e: SecurityException) {
                e.printStackTrace()
            }

        }

    }
}