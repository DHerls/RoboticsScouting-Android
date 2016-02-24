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
import org.fullmetalfalcons.androidscouting.activities.MainActivity;
import org.fullmetalfalcons.androidscouting.activities.RetrieveDataActivity;

import java.util.UUID;

/**
 *
 * Created by djher on 2/2/2016.
 */
public class BluetoothCore {
    private static final String SERVICE_UUID_BASE = "5888-16F1-43F8-AA84-63F1544F2694";
    private static String passphrase = "333B";
    private static final String SEND_CHARACTERISTIC_UUID = "20D0C428-B763-4016-8AC6-4B4B3A6865D9";
    private static final String RECEIVE_CHARACTERISTIC_UUID = "80A37B7F-0563-409B-B320-8C1768CE6A58";

    private static MainActivity activity;
    private static int mtu = 75;
    private static BluetoothDevice BleDevice;
    private static boolean connected = false;
    private static boolean advertising = false;

    private static BluetoothGattCharacteristic sendDataCharacteristic;
    private static BluetoothGattCharacteristic receiveDataCharacteristic;

    private static StringBuilder responseBuilder = new StringBuilder();

    public static void startBLE(Activity a){
        BluetoothCore.activity = (MainActivity) a;
        Log.d(a.getString(R.string.log_tag),"Beginning BLE Setup");

        if (BluetoothUtility.setupBluetooth(a)){
            Log.d(a.getString(R.string.log_tag),"Bluetooth Adapter exists and is turned on");

            addCharacteristics();

            BluetoothUtility.setServiceUUID(getServiceUUID());

            BluetoothUtility.setAdvertiseCallback(advertiseCallback);
            BluetoothUtility.setGattServerCallback(gattServerCallback);

            BluetoothUtility.startAdvertise();
        }

    }

    private static BluetoothGattServerCallback gattServerCallback = new BluetoothGattServerCallback() {
        @Override
        public void onConnectionStateChange(BluetoothDevice device, int status, int newState) {
            super.onConnectionStateChange(device, status, newState);
            Log.d(activity.getString(R.string.log_tag),"Connection changed: "+status + "-->" + newState);
            if (status ==0 && newState==0){
                BluetoothUtility.startAdvertise();
                activity.setConnected(false);
                connected = false;
                activity.setAdvertising(true);
                advertising = true;
            } else if (status == 0 && newState==2){
                BluetoothUtility.stopAdvertise();
                activity.setAdvertising(false);
                advertising = false;
                activity.setConnected(true);
                connected = true;
            }

        }

        @Override
        public void onServiceAdded(int status, BluetoothGattService service) {
            super.onServiceAdded(status, service);
            Log.d(activity.getString(R.string.log_tag), "Service added");
        }

        @Override
        public void onCharacteristicReadRequest(BluetoothDevice device, int requestId, int offset, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicReadRequest(device, requestId, offset, characteristic);
            Log.d(activity.getString(R.string.log_tag), "onCharacteristicReadRequest requestId=" + requestId + " offset=" + offset);

            if (characteristic.getUuid().equals(UUID.fromString(SEND_CHARACTERISTIC_UUID))) {
                Log.d(activity.getString(R.string.log_tag), "Changing Characteristic Value");
                characteristic.setValue("Text:This is a test characteristic");
                BluetoothUtility.getGattServer().sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset,
                        characteristic.getValue());
            }

        }

        @Override
        public void onCharacteristicWriteRequest(BluetoothDevice device, int requestId, BluetoothGattCharacteristic characteristic, boolean preparedWrite, boolean responseNeeded, int offset, byte[] value) {
            super.onCharacteristicWriteRequest(device, requestId, characteristic, preparedWrite, responseNeeded, offset, value);
//            Log.d(activity.getString(R.string.log_tag), "onCharacteristicWriteRequest requestId=" + requestId + " preparedWrite="
//                    + Boolean.toString(preparedWrite) + " responseNeeded="
//                    + Boolean.toString(responseNeeded) + " offset=" + offset);

            if (characteristic.getUuid().equals(receiveDataCharacteristic.getUuid())){
                BluetoothUtility.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, value);
                String write = new String(value);
                if (write.equals("OEM")){
                    RetrieveDataActivity.setResponseString(responseBuilder.toString());
                    responseBuilder = new StringBuilder();
                } else {
                    responseBuilder.append(write);
                }
            } else {
                BluetoothUtility.sendResponse(device, requestId, BluetoothGatt.GATT_REQUEST_NOT_SUPPORTED, offset, value);
            }


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
            Log.d(activity.getString(R.string.log_tag), successMsg);
            activity.setAdvertising(true);
            advertising = true;
        }

        @Override
        public void onStartFailure(int errorCode) {
            super.onStartFailure(errorCode);
            String failMsg = "Advertisement command attempt failed: " + errorCode;
            Log.e(activity.getString(R.string.log_tag), failMsg);
            activity.setAdvertising(false);
            advertising = false;
        }

    };


    public static void enable() {
        Log.d(activity.getString(R.string.log_tag), "Enabling Bluetooth");
        BluetoothUtility.enable();

        addCharacteristics();

        BluetoothUtility.setServiceUUID(getServiceUUID());

        BluetoothUtility.setAdvertiseCallback(advertiseCallback);
        BluetoothUtility.setGattServerCallback(gattServerCallback);

        BluetoothUtility.startAdvertise();
    }

    private static void addCharacteristics() {
        sendDataCharacteristic = new BluetoothGattCharacteristic(
                UUID.fromString(SEND_CHARACTERISTIC_UUID),
                BluetoothGattCharacteristic.PROPERTY_NOTIFY,
                BluetoothGattCharacteristic.PERMISSION_READ);

        BluetoothUtility.addCharacteristic(sendDataCharacteristic);

        receiveDataCharacteristic = new BluetoothGattCharacteristic(
                UUID.fromString(RECEIVE_CHARACTERISTIC_UUID),
                BluetoothGattCharacteristic.PROPERTY_NOTIFY | BluetoothGattCharacteristic.PROPERTY_WRITE,
                BluetoothGattCharacteristic.PERMISSION_READ | BluetoothGattCharacteristic.PERMISSION_WRITE);

        BluetoothUtility.addCharacteristic(receiveDataCharacteristic);

    }

    public static void setPassphrase(String passphrase1){
        if (BluetoothUtility.setupBluetooth(activity) && !passphrase1.equalsIgnoreCase(passphrase)){
            passphrase = passphrase1;
            BluetoothUtility.stopAll();
            BluetoothUtility.setServiceUUID(getServiceUUID());
            BluetoothUtility.startAdvertise();
        }

    }

    public static String getServiceUUID() {
        return passphrase + SERVICE_UUID_BASE;
    }

    public static void stopBLE() {
        Log.d(activity.getString(R.string.log_tag),"Stopping BLE");
        BluetoothUtility.stopAll();
    }

    public static void sendScoutingData(String results) {
        int numPackets = (int) Math.ceil(results.length()/mtu);
        for (int i = 0; i<numPackets;i++){
            String toSend = results.substring(i * mtu, (i + 1) * mtu);
            BluetoothUtility.sendNotification(sendDataCharacteristic, BleDevice, toSend);
        }
        BluetoothUtility.sendNotification(sendDataCharacteristic, BleDevice, results.substring(numPackets * mtu, results.length()));
        BluetoothUtility.sendNotification(sendDataCharacteristic, BleDevice, "EOM");
    }


    public static boolean isConnected() {
        return connected;
    }

    public  static boolean isAdvertising() {
        return advertising;
    }

    public static void requestTeamNum(String s) {
        BluetoothUtility.sendNotification(receiveDataCharacteristic, BleDevice, s);
    }
}
