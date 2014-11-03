package com.trexel.obdTerminal;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Set;

public class ListPairedDevicesActivity extends Activity {
    String TAG = "BTLog";

    private static final int REQUEST_ENABLE_BT = 1;

    BluetoothAdapter bluetoothAdapter;
    ListView myListView;
    ArrayAdapter<String> btArrayAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list_devices);

        boolean bluetoothEnabled = false;

        myListView = (ListView) findViewById(R.id.listView);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        /*
        if(bluetoothAdapter.isEnabled()){
            bluetoothEnabled = true;
        }else{
            RequestEnableBluetooth();
        }
        */

        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();

        final ArrayList<String> list = new ArrayList<String>();
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                String deviceBTName = device.getName();
                String deviceBTMAC = device.getAddress();
                String deviceBTState = getBTBondState(device.getBondState());
                list.add(deviceBTName + "\n"
                        + "Address: " + deviceBTMAC + "\n"
                        + "State: " + deviceBTState);
                /*
                btArrayAdapter.add(deviceBTName + "\n"
                        + "Address: " + deviceBTMAC + "\n"
                        + "State: " + deviceBTState);
                */
            }
        }

        btArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,
                android.R.id.text1, list);
        myListView.setAdapter(btArrayAdapter);

        myListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> av, View view, int position, long id) {
                String selectedFromList = myListView.getItemAtPosition(position).toString();
                Log.v(TAG, "A list item was clicked\n" + selectedFromList);

                Intent intent = new Intent();
                intent.setClass(ListPairedDevicesActivity.this, MainActivity.class);
                intent.putExtra("CLICKED_ITEM",selectedFromList);
                setResult(RESULT_OK, intent);
                startActivity(intent);
            }
        });

    }

    private void RequestEnableBluetooth(){
        if (bluetoothAdapter == null){
            finish();
        }else{
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
    }


    //checks if the device is paired with the phone
    private String getBTBondState(int bt_bond_state) {
        switch (bt_bond_state) {
            case android.bluetooth.BluetoothDevice.BOND_NONE:
                return "Not Bonded";
            case android.bluetooth.BluetoothDevice.BOND_BONDING:
                return "Pairing Now";
            case android.bluetooth.BluetoothDevice.BOND_BONDED:
                return "Bonded";
            default:
                return "unknown!";
        }
    }
}
