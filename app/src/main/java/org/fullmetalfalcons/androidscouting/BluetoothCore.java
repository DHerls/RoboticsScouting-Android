package org.fullmetalfalcons.androidscouting;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattServerCallback;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseSettings;
import android.util.Log;

import java.util.UUID;

/**
 * Created by djher on 2/2/2016.
 */
public class BluetoothCore {
    private static final String DEFAULT_SERVICE_UUID = "333B";
    private static final String DEFAULT_CHARACTERISTIC_UUID = "200B";
    private static final String TAG = "SCOUTING";

    private static BluetoothUtility ble;


    public static void startBLE(Activity a){

        ble = new BluetoothUtility(a);

        ble.setAdvertiseCallback(advertiseCallback);
        ble.setGattServerCallback(gattServerCallback);

        BluetoothGattService firstService = new BluetoothGattService(
                UUID.fromString(DEFAULT_SERVICE_UUID),
                BluetoothGattService.SERVICE_TYPE_PRIMARY);
        // alert level char.
        BluetoothGattCharacteristic firstServiceChar = new BluetoothGattCharacteristic(
                UUID.fromString(DEFAULT_CHARACTERISTIC_UUID),
                BluetoothGattCharacteristic.PROPERTY_NOTIFY,
                BluetoothGattCharacteristic.PERMISSION_READ);
        firstService.addCharacteristic(firstServiceChar);
        ble.addService(firstService);

        firstService.addCharacteristic(firstServiceChar);
        ble.addService(firstService);
    }

    private static BluetoothGattServerCallback gattServerCallback = new BluetoothGattServerCallback() {
        @Override
        public void onConnectionStateChange(BluetoothDevice device, int status, int newState) {
            super.onConnectionStateChange(device, status, newState);
            Log.d(TAG, "onConnectionStateChange status=" + status + "->" + newState);
        }

        @Override
        public void onServiceAdded(int status, BluetoothGattService service) {
            super.onServiceAdded(status, service);
        }

        @Override
        public void onCharacteristicReadRequest(BluetoothDevice device, int requestId, int offset, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicReadRequest(device, requestId, offset, characteristic);
            Log.d(TAG, "onCharacteristicReadRequest requestId=" + requestId + " offset=" + offset);

            if (characteristic.getUuid().equals(UUID.fromString(DEFAULT_CHARACTERISTIC_UUID))) {
                Log.d(TAG, "Changing Characteristic Value");
                characteristic.setValue("Text:This is a test characteristic");
                ble.getGattServer().sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset,
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
            Log.d(TAG,"Notification?");
        }
    };

    private static AdvertiseCallback advertiseCallback = new AdvertiseCallback() {
        @Override
        public void onStartSuccess(AdvertiseSettings settingsInEffect) {
            super.onStartSuccess(settingsInEffect);
            String successMsg = "Advertisement command attempt successful";
            Log.d(TAG, successMsg);
        }

        @Override
        public void onStartFailure(int errorCode) {
            super.onStartFailure(errorCode);
            String failMsg = "Advertisement command attempt failed: " + errorCode;
            Log.e(TAG, failMsg);
        }
    };



}
