package com.example.prashant.bluetooth;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Set;


public class MainActivity extends ActionBarActivity implements View.OnClickListener {

    private BluetoothAdapter mBluetoothAdapter;
    private TextView status;
    private Button changeStatus, scanDevice, mPairedBtn, mDiscoverable;
    private ProgressDialog mProgressDlg;
    IntentFilter filter;
    private ArrayList<BluetoothDevice> mDeviceList = new ArrayList<BluetoothDevice>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getWidget();
        dialog();

        filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(mReceiver, filter);
    }


    private void dialog() {

        mProgressDlg = new ProgressDialog(this);

        mProgressDlg.setMessage("Scanning...");
        mProgressDlg.setCancelable(false);
        mProgressDlg.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();

                mBluetoothAdapter.cancelDiscovery();
            }
        });


       /* if (mBluetoothAdapter != null)
         {
            mPairedBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();

                    if (pairedDevices == null || pairedDevices.size() == 0) {
                        showToast("No Paired Devices Found");
                    } else {
                        ArrayList<BluetoothDevice> list = new ArrayList<BluetoothDevice>();

                        list.addAll(pairedDevices);

                        Intent intent = new Intent(MainActivity.this, DeviceListActivity.class);

                        intent.putParcelableArrayListExtra("device.list", list);

                        startActivity(intent);
                    }
                }
            });
          }*/

    }

    private void deviceList() {
        if (mBluetoothAdapter != null) {
            Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
            if (pairedDevices == null || pairedDevices.size() == 0) {
                showToast("No Paired Devices Found");
            } else {
                ArrayList<BluetoothDevice> list = new ArrayList<BluetoothDevice>();

                list.addAll(pairedDevices);

                Intent intent = new Intent(MainActivity.this, DeviceListActivity.class);

                intent.putParcelableArrayListExtra("device.list", list);

                startActivity(intent);
            }
        } else {
            showToast("enable-Bluetooth");
        }
    }

    private Void getWidget() {

        status = (TextView) findViewById(R.id.status);
        changeStatus = (Button) findViewById(R.id.changeStatus);
        scanDevice = (Button) findViewById(R.id.scan);
        mPairedBtn = (Button) findViewById(R.id.btn_view_paired);

        mDiscoverable = (Button) findViewById(R.id.discoverable);
        scanDevice.setOnClickListener(this);
        changeStatus.setOnClickListener(this);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (mBluetoothAdapter == null) {
            status.setText("BlueTooth adapter not found");
            changeStatus.setText("BlueTooth Disabled");
            changeStatus.setEnabled(false);


        }
        //check the status and set the button text accordingly
        else {
            if (mBluetoothAdapter.isEnabled()) {
                status.setText("BlueTooth is currently switched ON");
                changeStatus.setText("Switch OFF Bluetooth");
            } else {
                status.setText("BlueTooth is currently switched OFF");
                changeStatus.setText("Switch ON Bluetooth");
            }
        }


        return null;
    }
/*to Scan bluetooth device*/

    private void scanBluetoothDevices() {
        mBluetoothAdapter.startDiscovery();
    }


    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                //discovery starts, we can show progress dialog or perform other tasks
                mDeviceList = new ArrayList<BluetoothDevice>();
                mProgressDlg.show();

            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                //discovery finishes, dismis progress dialog
                mProgressDlg.dismiss();
                Intent mIntent = new Intent(MainActivity.this, DeviceListActivity.class);
                mIntent.putParcelableArrayListExtra("device.list", mDeviceList);
                startActivity(mIntent);

            } else if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                //bluetooth device found
                BluetoothDevice device = (BluetoothDevice) intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                mDeviceList.add(device);

                showToast("Found device " + device.getName());
            }
        }
    };


    @Override
    protected void onDestroy() {

        unregisterReceiver(mReceiver);
        super.onDestroy();
    }

    private void showToast(String message) {

        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {

            case R.id.changeStatus:

                if (mBluetoothAdapter.isEnabled()) {
                    mBluetoothAdapter.disable();
                    status.setText("Bluetooth OFF");
                    changeStatus.setText("Turn -ON Bluetooth Device");
                } else {
                    mBluetoothAdapter.enable();
                    status.setText("Bluetooth ON");
                    changeStatus.setText("Turn -OFF Bluetooth Device");
                }
                break;
            case R.id.btn_pair:
                deviceList();
                break;
            case R.id.discoverable:
                if (mBluetoothAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {

                    showToast("MAKING YOUR DEVICE DISCOVERABLE");
                    Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                    intent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 30);
                    startActivityForResult(intent, 0);

                } else {
                    showToast("YOUR DEVICE IS DISCOVERABLE");

                }
                break;
            case R.id.scan:
                if (mBluetoothAdapter.isEnabled()) {

                    Toast.makeText(getApplicationContext(), "Bluetooth enabled -starting scan", Toast.LENGTH_SHORT).show();
                    scanBluetoothDevices();

                } else {

                    Toast.makeText(getApplicationContext(), "First_enable Bluetooth", Toast.LENGTH_SHORT).show();
                }
                break;

        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
