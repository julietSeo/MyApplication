package com.gdi.myapplication.utils;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.activity.result.ActivityResultLauncher;
import androidx.core.app.ActivityCompat;
import android.bluetooth.BluetoothDevice;


public class BluetoothUtil {




    public static boolean hasPermissions(Activity activity/*, ActivityResultLauncher<String[]> requestPermissionsLauncher*/) {
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            return true;
        }
//        String[] permissions = {Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION};

        boolean missingPermissions = activity.getApplicationContext().checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED
                && activity.getApplicationContext().checkSelfPermission(Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED;

        if(missingPermissions) {
            return true;
        }

//        requestPermissionsLauncher.launch(permissions);

        return false;
    }
}
