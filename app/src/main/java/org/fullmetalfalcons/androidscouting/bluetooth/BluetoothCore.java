package org.fullmetalfalcons.androidscouting.bluetooth;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattServerCallback;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseSettings;
import android.util.Log;

import org.fullmetalfalcons.androidscouting.R;
import org.fullmetalfalcons.androidscouting.ScoutingActivity;

import java.util.UUID;

/**
 *
 * Created by djher on 2/2/2016.
 */
public class BluetoothCore {
    private static final String DEFAULT_SERVICE_UUID_BASE = "5888-16F1-43F8-AA84-63F1544F2694";
    private static String passphrase = "333B";
    private static final String DEFAULT_CHARACTERISTIC_UUID = "20D0C428-B763-4016-8AC6-4B4B3A6865D9";
    private static final String TAG = "SCOUTING";
    private static ScoutingActivity a;
    private static int mtu = 75;
    private static BluetoothDevice BleDevice;

    public static void startBLE(Activity a){
        BluetoothCore.a = (ScoutingActivity) a;
        Log.d(a.getString(R.string.log_tag),"Beginning BLE Setup");

        if (BluetoothUtility.setupBluetooth(a)){
            Log.d(a.getString(R.string.log_tag),"Bluetooth Adapter exists and is turned on");
            BluetoothUtility.createNotificationService(passphrase + DEFAULT_SERVICE_UUID_BASE, DEFAULT_CHARACTERISTIC_UUID);

            BluetoothUtility.setAdvertiseCallback(advertiseCallback);
            BluetoothUtility.setGattServerCallback(gattServerCallback);

            BluetoothUtility.startAdvertise();
        }



    }

    private static BluetoothGattServerCallback gattServerCallback = new BluetoothGattServerCallback() {
        @Override
        public void onConnectionStateChange(BluetoothDevice device, int status, int newState) {
            super.onConnectionStateChange(device, status, newState);
            Log.d(a.getString(R.string.log_tag),"Connection changed: "+status + "-->" + newState);
            if (status ==0 && newState==0){
                BluetoothUtility.startAdvertise();
                a.setConnected(false);
            } else if (status == 0 && newState==2){
                BluetoothUtility.stopAdvertise();
                a.setConnected(true);
            }

        }

        @Override
        public void onServiceAdded(int status, BluetoothGattService service) {
            super.onServiceAdded(status, service);
            Log.d(TAG, "Service added");
        }

        @Override
        public void onCharacteristicReadRequest(BluetoothDevice device, int requestId, int offset, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicReadRequest(device, requestId, offset, characteristic);
            Log.d(TAG, "onCharacteristicReadRequest requestId=" + requestId + " offset=" + offset);

            if (characteristic.getUuid().equals(UUID.fromString(DEFAULT_CHARACTERISTIC_UUID))) {
                Log.d(TAG, "Changing Characteristic Value");
                characteristic.setValue("Text:This is a test characteristic");
                BluetoothUtility.getGattServer().sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset,
                        characteristic.getValue());
            }

        }

        @Override
        public void onCharacteristicWriteRequest(BluetoothDevice device, int requestId, BluetoothGattCharacteristic characteristic, boolean preparedWrite, boolean responseNeeded, int offset, byte[] value) {
            super.onCharacteristicWriteRequest(device, requestId, characteristic, preparedWrite, responseNeeded, offset, value);
            Log.d(TAG, "onCharacteristicWriteRequest requestId=" + requestId + " preparedWrite="
                    + Boolean.toString(preparedWrite) + " responseNeeded="
                    + Boolean.toString(responseNeeded) + " offset=" + offset);
        }

        @Override
        public void onNotificationSent(BluetoothDevice device, int status) {
            super.onNotificationSent(device, status);
        }

        @Override
        public void onMtuChanged(BluetoothDevice device, int mtu) {
            super.onMtuChanged(device, mtu);
            //BluetoothCore.mtu = mtu;

        }

        @Override
        public void onDescriptorWriteRequest (BluetoothDevice device, int requestId, BluetoothGattDescriptor descriptor, boolean preparedWrite, boolean responseNeeded, int offset, byte[] value) {

            // now tell the connected device that this was all successful
            BluetoothUtility.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, value);
            BleDevice = device;

        }
    };

    private static AdvertiseCallback advertiseCallback = new AdvertiseCallback() {
        @Override
        public void onStartSuccess(AdvertiseSettings settingsInEffect) {
            super.onStartSuccess(settingsInEffect);
            String successMsg = "Advertisement command attempt successful";
            Log.d(a.getString(R.string.log_tag), successMsg);
        }

        @Override
        public void onStartFailure(int errorCode) {
            super.onStartFailure(errorCode);
            String failMsg = "Advertisement command attempt failed: " + errorCode;
            Log.e(TAG, failMsg);
        }

    };


    public static void enable() {
        Log.d(a.getString(R.string.log_tag), "Enabling Bluetooth");
        BluetoothUtility.enable();

        BluetoothUtility.createNotificationService(getServiceUUID(), DEFAULT_CHARACTERISTIC_UUID);

        BluetoothUtility.setAdvertiseCallback(advertiseCallback);
        BluetoothUtility.setGattServerCallback(gattServerCallback);

        BluetoothUtility.startAdvertise();
    }

    public static void setPassphrase(String passphrase1){
        if (!passphrase1.equalsIgnoreCase(passphrase)){
            passphrase = passphrase1;
            BluetoothUtility.stopAll();
            BluetoothUtility.createNotificationService(getServiceUUID(), DEFAULT_CHARACTERISTIC_UUID);
            BluetoothUtility.startAdvertise();
        }

    }

    public static String getServiceUUID() {
        return passphrase + DEFAULT_SERVICE_UUID_BASE;
    }

    public static void stopBLE() {
        Log.d(a.getString(R.string.log_tag),"Stopping BLE");
        BluetoothUtility.stopAll();
    }

    public static void sendData(String results) {
        int numPackets = (int) Math.ceil(results.length()/mtu);
        System.out.println(results);
        for (int i = 0; i<numPackets;i++){
            String toSend = results.substring(i * mtu, (i + 1) * mtu);
            BluetoothUtility.sendNotification(BleDevice, toSend);
        }
        BluetoothUtility.sendNotification(BleDevice, results.substring(numPackets*mtu,results.length()));
        BluetoothUtility.sendNotification(BleDevice,"EOM");
    }
}
