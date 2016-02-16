package org.fullmetalfalcons.androidscouting.bluetooth;

import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.*;
import android.bluetooth.le.*;
import android.content.Context;
import android.content.Intent;
import android.os.ParcelUuid;
import android.util.Log;
import android.widget.Toast;

import org.fullmetalfalcons.androidscouting.MainActivity;
import org.fullmetalfalcons.androidscouting.R;
import org.fullmetalfalcons.androidscouting.ScoutingActivity;
import org.fullmetalfalcons.androidscouting.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Bluetooth LE advertising and scanning utilities.
 *
 * Created by micah on 7/16/14.
 */
public class BluetoothUtility {

    /**
     * Contants
     */
    private static final int REQUEST_ENABLE_BT = 1;

    /**
     * String Constants
     */
    private static final String TAG = "MyActivity";
    private static final String BLUETOOTH_ADAPTER_NAME = "Scouting";

    /**
     * Advertising Constants
     */
    private static boolean advertising;
    private static AdvertiseCallback advertiseCallback; //Must implement and set
    private static BluetoothGattServerCallback gattServerCallback; //Must implement and set
    private static BluetoothGattService gattService;
    private static BluetoothGattCharacteristic gattCharacteristic;

    /**
     * Bluetooth Objects
     */
    private static MainActivity activity;
    private static BluetoothManager bluetoothManager;
    private static BluetoothAdapter bluetoothAdapter;
    private static BluetoothLeAdvertiser bluetoothLeAdvertiser;
    private static BluetoothGattServer gattServer;


    protected static boolean setupBluetooth(Activity a){
        activity = (MainActivity) a;
        bluetoothManager = (BluetoothManager) activity.getApplicationContext().getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();

        if (bluetoothAdapter == null){
            activity.sendError("Bluetooth is not supported on this device, this app is useless",true);
            return false;
        } else {
            bluetoothAdapter.setName(Utils.getDeviceName() + " " + BLUETOOTH_ADAPTER_NAME);
            System.out.println();
        }

        if(!bluetoothAdapter.isEnabled()) {
            Log.d(a.getString(R.string.log_tag),"Requesting permission to turn on Bluetooth");
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            activity.startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);

            return false;

        } else {
            bluetoothLeAdvertiser = bluetoothAdapter.getBluetoothLeAdvertiser();
            return true;
        }


    }

    protected static void stopAll() {
        if(getAdvertising()) stopAdvertise();
        if(gattServer != null) gattServer.close();

    }

    /*-------------------------------------------------------------------------------*/

    protected static boolean getAdvertising() {
        return advertising;
    }

    protected static void setAdvertiseCallback(AdvertiseCallback callback) {
        advertiseCallback = callback;
    }
    protected static void setGattServerCallback(BluetoothGattServerCallback callback) {
        gattServerCallback = callback;
    }

    /**
     * BLE Advertising
     */
    //Public method to begin advertising services
    protected static void startAdvertise() {
        if(getAdvertising()) return;

        startGattServer();

        AdvertiseData.Builder dataBuilder = new AdvertiseData.Builder();
        AdvertiseSettings.Builder settingsBuilder = new AdvertiseSettings.Builder();

        dataBuilder.setIncludeTxPowerLevel(false);
        //LITERALLY THE MOST IMPORTANT LINE
        dataBuilder.addServiceUuid(new ParcelUuid(gattService.getUuid()));

        settingsBuilder.setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_BALANCED);
        settingsBuilder.setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH);
        settingsBuilder.setConnectable(true);

        bluetoothLeAdvertiser.startAdvertising(settingsBuilder.build(), dataBuilder.build(), advertiseCallback);
        advertising = true;
        Log.d(TAG, "Start Advertising");

    }

    //Stop ble advertising and clean up
    protected static void stopAdvertise() {
        if(!getAdvertising()) return;
        bluetoothLeAdvertiser.stopAdvertising(advertiseCallback);
        advertising = false;
        activity.setAdvertising(false);
        Log.d(TAG,"Stop Advertising");

    }

    protected void stopGattServer(){
        gattServer.clearServices();
        gattServer.close();
        activity.setConnected(false);
    }

    protected static BluetoothGattServer getGattServer() {
        return gattServer;
    }

    private static void startGattServer() {
        Log.d(activity.getString(R.string.log_tag),"Starting GATT Server");
        gattServer = bluetoothManager.openGattServer(activity, gattServerCallback);

        gattServer.addService(gattService);

    }

    protected static void enable() {
        boolean isEnabling = bluetoothAdapter.enable();
        if (!isEnabling)
        {
            // an immediate error occurred - perhaps the bluetooth is already on?
        }
        else if (bluetoothAdapter.getState() == BluetoothAdapter.STATE_TURNING_ON)
        {
            ProgressDialog progress = new ProgressDialog(activity);
            progress.setTitle("Bluetooth");
            progress.setMessage("Turning on Bluetooth...");
            progress.setCancelable(false);
            progress.show();
            while (bluetoothAdapter.getState() ==BluetoothAdapter.STATE_TURNING_ON){
                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
            progress.dismiss();
        }
        bluetoothLeAdvertiser = bluetoothAdapter.getBluetoothLeAdvertiser();
    }

    protected static void sendResponse(BluetoothDevice device, int requestId, int gattSuccess, int offset, byte[] value) {
        gattServer.sendResponse(device, requestId, gattSuccess, offset, value);

    }

    protected static void sendNotification(BluetoothDevice device, String value){
        gattCharacteristic.setValue(value);
        gattServer.notifyCharacteristicChanged(device, gattCharacteristic, false);

    }

    protected static void createNotificationService(String serviceUUID, String characteristicUUID) {
        gattService = new BluetoothGattService(UUID.fromString(serviceUUID),BluetoothGattService.SERVICE_TYPE_PRIMARY);
        gattCharacteristic = new BluetoothGattCharacteristic(
                UUID.fromString(characteristicUUID),
                BluetoothGattCharacteristic.PROPERTY_NOTIFY,
                BluetoothGattCharacteristic.PERMISSION_READ);
        BluetoothGattDescriptor gD = new BluetoothGattDescriptor(
                UUID.fromString("00002902-0000-1000-8000-00805F9B34FB"),
                BluetoothGattDescriptor.PERMISSION_WRITE | BluetoothGattDescriptor.PERMISSION_READ);
        gattCharacteristic.addDescriptor(gD);
        gattService.addCharacteristic(gattCharacteristic);


    }

}