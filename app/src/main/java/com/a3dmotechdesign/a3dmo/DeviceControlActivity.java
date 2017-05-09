/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.a3dmotechdesign.a3dmo;

import android.app.ActionBar;
import android.app.Activity;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.SimpleExpandableListAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * For a given BLE device, this Activity provides the user interface to connect, display data,
 * and display GATT services and characteristics supported by the device.  The Activity
 * communicates with {@code BluetoothLeService}, which in turn interacts with the
 * Bluetooth LE API.
 */
public class DeviceControlActivity extends Activity implements View.OnClickListener {

    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";

    private TextView mConnectionState;
    private TextView mDataField;
    private String mDeviceName;
    private String mDeviceAddress;
    private ExpandableListView mGattServicesList;
    private BluetoothLeService mBluetoothLeService;
    private ArrayList<ArrayList<BluetoothGattCharacteristic>> mGattCharacteristics = new ArrayList<ArrayList<BluetoothGattCharacteristic>>();
    private boolean mConnected = false;

    // blechat - characteristics for HM-10 serial
    private BluetoothGattCharacteristic characteristicTX;
    private BluetoothGattCharacteristic characteristicRX;

    private final String LIST_NAME = "NAME";
    private final String LIST_UUID = "UUID";
    TouchEventView mView;
    private int num[] = new int[2];
    private int x[]=new int[2];
    private int y[]=new int[2];
    private int rightX,rightY, leftX,leftY;
    private boolean touchRight = false;
    private boolean touchLeft = false;
    private boolean btnState1=true, btnState2=true, btnState3=true, btnState4=true;
    private ImageButton btnButton1, btnButton2, btnButton3, btnButton4,bluBtn;
    private int mode_count=0;

    private boolean touched[] = new boolean[2];
    // Code to manage Service lifecycle.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName,
                                       IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service)
                    .getService();
            if (!mBluetoothLeService.initialize()) {
                finish();
            }
            // Automatically connects to the device upon successful start-up
            // initialization.
            mBluetoothLeService.connect(mDeviceAddress);

        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };

    // Handles various events fired by the Service.
    // ACTION_GATT_CONNECTED: connected to a GATT server.
    // ACTION_GATT_DISCONNECTED: disconnected from a GATT server.
    // ACTION_GATT_SERVICES_DISCOVERED: discovered GATT services.
    // ACTION_DATA_AVAILABLE: received data from the device. This can be a
    // result of read
    // or notification operations.
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                mConnected = true;
               // updateConnectionState(R.string.connected);
                invalidateOptionsMenu();

            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED
                    .equals(action)) {
                mConnected = false;
                //updateConnectionState(R.string.disconnected);
                invalidateOptionsMenu();
                clearUI();
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED
                    .equals(action)) {
                // Show all the supported services and characteristics on the
                // user interface.

                // blechat
                // set serial chaacteristics
                setupSerial();

                // displayGattServices(mBluetoothLeService.getSupportedGattServices());
            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                //displayData(intent.getStringExtra(BluetoothLeService.EXTRA_DATA));
            }
        }
    };
    private void clearUI() {
        mGattServicesList.setAdapter((SimpleExpandableListAdapter) null);
        mDataField.setText(R.string.no_data);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mView=new TouchEventView(this);
        FrameLayout frame = (FrameLayout)findViewById(R.id.mainLayout);
        frame.addView(mView, 0);

        ActionBar actionBar = getActionBar();
        actionBar.hide();

        btnButton1 = (ImageButton)findViewById(R.id.btn1);
        btnButton1.setOnClickListener(this);
        btnButton2 = (ImageButton)findViewById(R.id.btn2);
        btnButton2.setOnClickListener(this);
        btnButton3 = (ImageButton)findViewById(R.id.btn3);
        btnButton3.setOnClickListener(this);
        btnButton4 = (ImageButton)findViewById(R.id.btn4);
        btnButton4.setOnClickListener(this);

        mView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int pointerIndex = (event.getAction() & MotionEvent.ACTION_POINTER_ID_MASK);
                pointerIndex = pointerIndex >> MotionEvent.ACTION_POINTER_ID_SHIFT;
                int tId = event.getPointerId(pointerIndex);

                switch (event.getAction() & MotionEvent.ACTION_MASK) {

                    case MotionEvent.ACTION_DOWN:
                    case MotionEvent.ACTION_POINTER_DOWN:
                        touched[tId] = true;
                        x[tId] = (int)event.getX(pointerIndex);
                        y[tId] = (int)event.getY(pointerIndex);
                        break;
                    case MotionEvent.ACTION_MOVE:
                        if (mView.getSelectL() || mView.getSelectR()) {
                            int pointer_count = event.getPointerCount();
                            for (int i = 0; i < pointer_count; i++) {
                                pointerIndex = i;

                                tId = event.getPointerId(pointerIndex);
                                x[tId] = (int) event.getX(pointerIndex);
                                y[tId] = (int) event.getY(pointerIndex);

                                if (y[tId]>620) {
                                    if (x[i] % 3 == 0 ) {
                                        rightX = mView.getDrawA();
                                        sendMsg(rightX,2);
                                    }
                                    if (y[tId] % 3 == 0 ) {
                                        rightY = mView.getDrawB();
                                        sendMsg(rightY,3);
                                    }

                                }
                                if (y[tId]<620) {
                                    if (x[tId] % 3 == 0) {
                                        leftX = mView.getDrawX();
                                        sendMsg(leftX,0);
                                    }
                                    if (y[tId] % 3 == 0 ) {
                                        leftY = mView.getDrawY();
                                        sendMsg(leftY,1);
                                    }

                                }
                            }
                        }
                        MatrixTime(50);
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_POINTER_UP:
                    case MotionEvent.ACTION_CANCEL:
                        touched[tId] = false;
                        x[tId] = (int) (event.getX(pointerIndex));
                        y[tId] = (int) (event.getY(pointerIndex));
                        if (y[tId] > 620 ) {
                            //if (mView.getSelectR() && blueconnect) {
                            sendMsg(rightX,2);
                            sendMsg(mView.getrCenY(),3);
                        }
                        //else if(mView.getSelectL()  && blueconnect) {
                        else if(y[tId] < 620 ) {
                            sendMsg(mView.getlCenX(),0);
                            sendMsg(mView.getlCenY(),1);
                        }
                        break;

                }
                return false;
            }
        });

        final Intent intent = getIntent();
        mDeviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME);
        mDeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);



        getActionBar().setTitle(mDeviceName);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);

    }

    // setupSerial
    //
    // set serial characteristics
    //
    private void setupSerial() {

        // blechat - set serial characteristics
        String uuid;
        String unknownServiceString = getResources().getString(
                R.string.unknown_service);

        for (BluetoothGattService gattService : mBluetoothLeService
                .getSupportedGattServices()) {
            // HashMap<String, String> currentServiceData = new HashMap<String,
            // String>();
            uuid = gattService.getUuid().toString();

            // If the service exists for HM 10 Serial, say so.
            if (SampleGattAttributes.lookup(uuid, unknownServiceString) == "HM 10 Serial") {

                // get characteristic when UUID matches RX/TX UUID
                characteristicTX = gattService
                        .getCharacteristic(BluetoothLeService.UUID_HM_RX_TX);
                characteristicRX = gattService
                        .getCharacteristic(BluetoothLeService.UUID_HM_RX_TX);

                mBluetoothLeService.setCharacteristicNotification(
                        characteristicRX, true);

                break;

            } // if

        } // for


    }


    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        if (mBluetoothLeService != null) {
            final boolean result = mBluetoothLeService.connect(mDeviceAddress);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mGattUpdateReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mServiceConnection);
        mBluetoothLeService = null;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.gatt_services, menu);
        if (mConnected) {
            menu.findItem(R.id.menu_connect).setVisible(false);
            menu.findItem(R.id.menu_disconnect).setVisible(true);
        } else {
            menu.findItem(R.id.menu_connect).setVisible(true);
            menu.findItem(R.id.menu_disconnect).setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_connect:
                mBluetoothLeService.connect(mDeviceAddress);
                return true;
            case R.id.menu_disconnect:
                mBluetoothLeService.disconnect();
                return true;
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

   /*private void updateConnectionState(final int resourceId) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mConnectionState.setText(resourceId);
            }
        });
    }

    private void displayData(String data) {
        if (data != null) {
            mDataField.setText(data);
        }
    }*/

    // Demonstrates how to iterate through the supported GATT
    // Services/Characteristics.
    // In this sample, we populate the data structure that is bound to the
    // ExpandableListView
    // on the UI.
    private void displayGattServices(List<BluetoothGattService> gattServices) {
        if (gattServices == null)
            return;
        String uuid = null;
        String unknownServiceString = getResources().getString(
                R.string.unknown_service);
        String unknownCharaString = getResources().getString(
                R.string.unknown_characteristic);
        ArrayList<HashMap<String, String>> gattServiceData = new ArrayList<HashMap<String, String>>();
        ArrayList<ArrayList<HashMap<String, String>>> gattCharacteristicData = new ArrayList<ArrayList<HashMap<String, String>>>();
        mGattCharacteristics = new ArrayList<ArrayList<BluetoothGattCharacteristic>>();

        // Loops through available GATT Services.
        for (BluetoothGattService gattService : gattServices) {
            HashMap<String, String> currentServiceData = new HashMap<String, String>();
            uuid = gattService.getUuid().toString();
            currentServiceData.put(LIST_NAME,
                    SampleGattAttributes.lookup(uuid, unknownServiceString));
            currentServiceData.put(LIST_UUID, uuid);
            gattServiceData.add(currentServiceData);

            ArrayList<HashMap<String, String>> gattCharacteristicGroupData = new ArrayList<HashMap<String, String>>();
            List<BluetoothGattCharacteristic> gattCharacteristics = gattService
                    .getCharacteristics();
            ArrayList<BluetoothGattCharacteristic> charas = new ArrayList<BluetoothGattCharacteristic>();

            // Loops through available Characteristics.
            for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
                charas.add(gattCharacteristic);
                HashMap<String, String> currentCharaData = new HashMap<String, String>();
                uuid = gattCharacteristic.getUuid().toString();
                currentCharaData.put(LIST_NAME,
                        SampleGattAttributes.lookup(uuid, unknownCharaString));
                currentCharaData.put(LIST_UUID, uuid);
                gattCharacteristicGroupData.add(currentCharaData);
            }
            mGattCharacteristics.add(charas);
            gattCharacteristicData.add(gattCharacteristicGroupData);

        }

        SimpleExpandableListAdapter gattServiceAdapter = new SimpleExpandableListAdapter(
                this, gattServiceData,
                android.R.layout.simple_expandable_list_item_2, new String[] {
                LIST_NAME, LIST_UUID }, new int[] { android.R.id.text1,
                android.R.id.text2 }, gattCharacteristicData,
                android.R.layout.simple_expandable_list_item_2, new String[] {
                LIST_NAME, LIST_UUID }, new int[] { android.R.id.text1,
                android.R.id.text2});
        mGattServicesList.setAdapter(gattServiceAdapter);

    }

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }
    public void MatrixTime(int delayTime){
        long saveTime = System.currentTimeMillis();
        long currTime = 0;
        while( currTime - saveTime < delayTime){
            currTime = System.currentTimeMillis();
        }
    }

    @Override
    public void onClick(View v) {

        /*switch (v.getId()) {
            case R.id.btn1:
                if (btnState1){
                    btnButton1.setSelected(true);
                    sendMsg(0,4);
                    btnState1 =false;
                    mode_count++;
                }
                else {
                    btnButton1.setSelected(false);
                    btnState1 = true;
                    mode_count--;
                }
                break;
            case R.id.btn2:
                if (btnState2){
                    btnButton2.setSelected(true);
                    sendMsg(0, 5);
                    btnState2 =false;
                    mode_count++;
                }
                else {
                    btnButton2.setSelected(false);
                    btnState2 = true;
                    mode_count--;
                }
                break;
            case R.id.btn3:
                if (btnState3){
                    btnButton3.setSelected(true);
                    sendMsg(0, 6);
                    btnState3 =false;
                    mode_count++;
                }
                else {
                    btnButton3.setSelected(false);
                    btnState3 = true;
                    mode_count--;
                }
                break;
            default:
                if(mode_count ==0){
                    sendMsg(0,8);
                }
        }*/

    }
    public void sendMsg(int a,int key) {
        String str=null;
        switch(key) {
            case 0:
                str = "p" + Integer.toString(a) + "\n";
                break;
            case 1:
                str = "r" + Integer.toString(a) + "\n";
                break;
            case 2:
                str = "t" + Integer.toString(a) + "\n";
                break;
            case 3:
                str = "y" + Integer.toString(a) + "\n";
                break;
            case 4:
                str = "a" + "\n";
                break;
            case 5:
                str = "b" + "\n";
                break;
            case 6:
                str = "c" + "\n";
                break;
            case 7:
                str = "d" + "\n";
                break;
            case 8:
                str = "e" + "\n";
        }
        byte[] tx = str.getBytes();
        /*try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }*/
        if (mConnected) {
            characteristicTX.setValue(tx);
            mBluetoothLeService.writeCharacteristic(characteristicTX);
        }

    }
}
