package org.fullmetalfalcons.androidscouting.bluetooth;

import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.*;
import android.bluetooth.le.*;
import android.content.Context;
import android.content.Intent;
import android.os.ParcelUuid;
import android.util.Log;

import org.fullmetalfalcons.androidscouting.activities.MainActivity;
import org.fullmetalfalcons.androidscouting.R;
import org.fullmetalfalcons.androidscouting.Utils;

import java.util.ArrayList;
import java.util.UUID;

/**
 * Handles low level BLE
 *
 * Adapted by Dan from the code created by micah on 7/16/14.
 */
class BluetoothUtility {

    /**
     * Constants
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

    /**
     * Bluetooth Objects
     */
    private static MainActivity activity;
    private static BluetoothManager bluetoothManager;
    private static BluetoothAdapter bluetoothAdapter;
    private static BluetoothLeAdvertiser bluetoothLeAdvertiser;
    private static BluetoothGattServer gattServer;
    private static final ArrayList<BluetoothGattCharacteristic> characteristics = new ArrayList<>();

    /**
     * All bluetooth characteristics must have this descriptor for Apple iOS and OSX devices to subscribe to the characteristic
     */
    private static final BluetoothGattDescriptor STUPID_APPLE_DESCRIPTOR = new BluetoothGattDescriptor(
            UUID.fromString("00002902-0000-1000-8000-00805F9B34FB"),
            BluetoothGattDescriptor.PERMISSION_WRITE | BluetoothGattDescriptor.PERMISSION_READ);


    /**
     * Checks if bluetooth is turned on, if not, requests to turn it on
     *
     * @param a Activity
     * @return true if everything is setup already, false if user needs to respond
     */
    static boolean setupBluetooth(Activity a){
        activity = (MainActivity) a;
        bluetoothManager = (BluetoothManager) activity.getApplicationContext().getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();

        //If the device does not support bluetooth
        if (bluetoothAdapter == null){
            activity.sendError("Bluetooth is not supported on this device, this app is useless",true);
            return false;
        } else {
            bluetoothAdapter.setName(Utils.getDeviceName() + " " + BLUETOOTH_ADAPTER_NAME);
        }

        //If bluetooth is not turned on
        if(!bluetoothAdapter.isEnabled()) {
            Log.d(a.getString(R.string.log_tag),"Requesting permission to turn on Bluetooth");
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            activity.startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);

            return false;
        //If bluetooth is turned on
        } else {
            bluetoothLeAdvertiser = bluetoothAdapter.getBluetoothLeAdvertiser();
            return true;
        }


    }

    static void stopAll() {
        if(getAdvertising()) stopAdvertise();
        if(gattServer != null) stopGattServer();

    }

    /*-------------------------------------------------------------------------------*/

    @SuppressWarnings("WeakerAccess")
    protected static boolean getAdvertising() {
        return advertising;
    }

    @SuppressWarnings("SameParameterValue")
    static void setAdvertiseCallback(AdvertiseCallback callback) {
        advertiseCallback = callback;
    }
    @SuppressWarnings("SameParameterValue")
    static void setGattServerCallback(BluetoothGattServerCallback callback) {
        gattServerCallback = callback;
    }

    /**
     * Public method to begin advertising services
     */
    static void startAdvertise() {
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
    static void stopAdvertise() {
        if(!getAdvertising()) return;
        bluetoothLeAdvertiser.stopAdvertising(advertiseCallback);
        advertising = false;
        activity.setAdvertising(false);
        Log.d(TAG,"Stop Advertising");

    }

    public static void setServiceUUID(String serviceUUID) {
        gattService = new BluetoothGattService(UUID.fromString(serviceUUID),BluetoothGattService.SERVICE_TYPE_PRIMARY);
        for (BluetoothGattCharacteristic c: characteristics){
            gattService.addCharacteristic(c);
        }
    }

    private static void stopGattServer(){
        gattServer.clearServices();
        gattServer.close();
        activity.setConnected(false);

    }

    static BluetoothGattServer getGattServer() {
        return gattServer;
    }

    private static void startGattServer() {
        Log.d(activity.getString(R.string.log_tag),"Starting GATT Server");
        gattServer = bluetoothManager.openGattServer(activity, gattServerCallback);

        gattServer.addService(gattService);

    }

    static void enable() {
        boolean isEnabling = bluetoothAdapter.enable();
        //noinspection StatementWithEmptyBody
        if (!isEnabling)
        {
            // an immediate error occurred - perhaps the bluetooth is already on?
        }
        else if (bluetoothAdapter.getState() == BluetoothAdapter.STATE_TURNING_ON)
        {
            //Display a loading dialog while the bluetooth adapter is turning on
            ProgressDialog progress = new ProgressDialog(activity);
            progress.setTitle("Bluetooth");
            progress.setMessage("Turning on Bluetooth...");
            progress.setCancelable(false);
            progress.show();
            while (bluetoothAdapter.getState() ==BluetoothAdapter.STATE_TURNING_ON){
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
            progress.dismiss();
        }
        bluetoothLeAdvertiser = bluetoothAdapter.getBluetoothLeAdvertiser();
    }

    static void sendResponse(BluetoothDevice device, int requestId, int gattSuccess, int offset, byte[] value) {
        gattServer.sendResponse(device, requestId, gattSuccess, offset, value);

    }

    static void sendNotification(BluetoothGattCharacteristic characteristic, BluetoothDevice device, String value){
        characteristic.setValue(value);
        gattServer.notifyCharacteristicChanged(device, characteristic, false);

    }

    public static void addCharacteristic(BluetoothGattCharacteristic characteristic) {
        //Descriptor is required for base to subscribe to services
        characteristic.addDescriptor(STUPID_APPLE_DESCRIPTOR);
        characteristics.add(characteristic);
    }


}