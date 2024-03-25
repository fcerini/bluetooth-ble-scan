package com.example.myapplication

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_main.tView
import org.altbeacon.beacon.BeaconParser


class MainActivity : AppCompatActivity() {
    private lateinit var btAdapter: BluetoothAdapter
    private val requestBT = 986855
    private var btLeScanner = BluetoothAdapter.getDefaultAdapter().bluetoothLeScanner
    private var btScanning = false
    private val btHandlerStopScan = Handler()
    private val scanPeriod: Long = 30000
    private var btNearbyMap = mutableMapOf<String, Int>()
    private val btScanCallback: ScanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            super.onScanResult(callbackType, result)
            if (result.scanRecord == null){
                return
            }

            val scanRecord = result.scanRecord!!

            val beaconParser = BeaconParser()

            beaconParser.setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24") // AltBeacon layout string
            val beacon = beaconParser.fromScanData(
                scanRecord.bytes,
                result.rssi,
                result.device )

            if (beacon != null) {
                // Access beacon data
                val id1 = beacon.id1
                val id2 = beacon.id2
                val id3 = beacon.id3
                val rssi = beacon.rssi
                // ... (Extract other relevant data)
            }
            /*
            var oldRSSI = btNearbyMap.get(deviceImei)
            if (oldRSSI == null) {
                oldRSSI = 999
            }

            btNearbyMap.put(deviceImei, deviceRSSI)

            tView.setText ("")
            for (item in btNearbyMap) {
                tView.setText ("${tView.text}${item.key}: ${item.value}db \n")
            }
*/
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btInit()

    }

    fun btStartScan(v:View){
        try {
            if (!btInit()){
                return
            }
            if (!btScanning) { // Stops scanning after a pre-defined scan period.
                btHandlerStopScan.postDelayed({
                    btScanning = false
                    btLeScanner.stopScan(btScanCallback)
                    tView.setText ("${tView.text}stopScan \n")
                }, scanPeriod)
                btScanning = true
                btLeScanner.startScan(btScanCallback)
                tView.setText ("startScan ${scanPeriod}sec. \n")
            } else {
                btScanning = false
                btLeScanner.stopScan(btScanCallback)
                tView.setText ( "stopScan!")
            }

        } catch (e: Throwable) {
            tView.setText (e.message)
        }

    }

    private fun btInit() :Boolean{
        try {
            if (!packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
                tView.setText ("Bluetooth Low Energy not supported! sorry...")
                return false
            }

            val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
            btAdapter = bluetoothManager.adapter

            if (btAdapter == null || !btAdapter.isEnabled) {
                tView.setText ( "bluetooth problem... \n")
                val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                startActivityForResult(enableBtIntent, requestBT)
                return false
            }

            if (ContextCompat.checkSelfPermission(this@MainActivity,
                    Manifest.permission.ACCESS_FINE_LOCATION) !==
                PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this@MainActivity,
                        Manifest.permission.ACCESS_FINE_LOCATION)) {
                    ActivityCompat.requestPermissions(this@MainActivity,
                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
                } else {
                    ActivityCompat.requestPermissions(this@MainActivity,
                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
                }

                return false
            }

            btLeScanner = BluetoothAdapter.getDefaultAdapter().bluetoothLeScanner

            return true
        } catch (e: Throwable) {
            tView.setText (e.message)
            return true
        }
    }


    override fun onDestroy() {
        super.onDestroy()
    }

}

fun bytesToHex(`in`: ByteArray): String? {
    val builder = StringBuilder()
    for (b in `in`) {
        builder.append(String.format("%02x ", b))
    }
    return builder.toString()
}