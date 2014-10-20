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

import java.util.Set;

public class ListPairedDevicesActivity extends Activity {
    String TAG = "BTLog";

    BluetoothAdapter bluetoothAdapter;
    ListView myListView;
    ArrayAdapter<String> btArrayAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list_devices);

        myListView = (ListView) findViewById(R.id.listView1);
        myListView.getSelectedItem();
        myListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> av, View view, int position, long id) {
                String selectedFromList =(myListView.getItemAtPosition(position).toString());
                Log.v(TAG, "A list item was clicked\n" + selectedFromList);

                Intent intent = new Intent();
                intent.setClass(ListPairedDevicesActivity.this, MainActivity.class);
                intent.putExtra("CLICKED_ITEM",selectedFromList);
                setResult(RESULT_OK, intent);
                startActivity(intent);
            }
        });

        btArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
        myListView.setAdapter(btArrayAdapter);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();

        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                String deviceBTName = device.getName();
                String deviceBTMAC = device.getAddress();
                String deviceBTState = getBTBondState(device.getBondState());
                btArrayAdapter.add(deviceBTName + "\n"
                        + "Address: " + deviceBTMAC + "\n"
                        + "State: " + deviceBTState);
            }
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
