package com.trexel.obdTerminal;

import android.app.Activity;
import android.os.Bundle;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends Activity {
    static String TAG = "BTLog";

    //android view class variables
    EditText userCommandEditText;
    TextView terminalTextView;
    Button sendButton;

    //bluetooth class variables
    BluetoothAdapter bluetoothAdapter = null;
    BluetoothDevice selectedDevice = null;
    BluetoothSocket BTSocket = null;

    //class variables
    String selectedDeviceText = "";
    String deviceAddress = "";
    String connectionStatus = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);


        //link the view class variables to the actual view objects
        terminalTextView = (TextView) findViewById(R.id.terminalTextView);
        userCommandEditText = (EditText) findViewById(R.id.userCommandEditText);
        sendButton = (Button) findViewById(R.id.sendButton);

        //instantiate the class bluetooth variables
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        //grab the appropriate device based on the address from
        //previous activity as passed through the result
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            selectedDeviceText = extras.getString("CLICKED_ITEM");

            //parses the returned string for the MAC address
            Pattern p = Pattern.compile("((([a-f]|[A-F]|[0-9]){2}:){5}([a-f]|[A-F]|[0-9]){2})");
            Matcher m = p.matcher(selectedDeviceText);
            if(m.find()) {
                int start = m.start();
                int end = m.end();
                deviceAddress = selectedDeviceText.substring(start, end);
                Log.v(TAG, "Match: " + deviceAddress);
                if(deviceAddress.length() > 0) {
                    selectedDevice = bluetoothAdapter.getRemoteDevice(deviceAddress);
                }
            }else{
                Log.v(TAG, "Error getting address for device...");
                Toast.makeText(getApplicationContext(), "Error Finding Device.",
                        Toast.LENGTH_SHORT).show();
                finish();
            }
        }

        if(selectedDevice != null) {
            //try to setup a socket connection asynchronously
            new EstablishConnectionTask().execute(selectedDevice);
        }

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!bluetoothAdapter.isEnabled()){
                    finish();
                }else{
                }
            }
        });

    }

    // AsyncTask<PARAMS, PROGRESS, RESULT>
    private class EstablishConnectionTask extends AsyncTask<BluetoothDevice, Integer, Boolean> {
        protected Boolean doInBackground(BluetoothDevice... devices) {
            int count = devices.length;
            for (int i = 0; i < count; i++) {
                BluetoothDevice device = devices[i];

                // Default UUID
                UUID DEFAULT_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

                // Get a BluetoothSocket to connect with the given BluetoothDevice
                try {
                    BluetoothSocket tmpSocket = device.createRfcommSocketToServiceRecord(DEFAULT_UUID);

                    try {
                        tmpSocket.connect();
                        if (tmpSocket.isConnected()) {
                            BTSocket = tmpSocket;
                            return true;
                        } else {
                            return false;
                        }
                    } catch (IOException connectionError) {
                        Log.d(TAG, "ERROR: " + connectionError);
                    }
                }catch (IOException socketError){
                    Log.d(TAG, "ERROR: " + socketError);
                }

                //updates the progress for the onProgressUpdate method
                //publishProgress((int) ((i / (float) count) * 100));

                // Escape early if cancel() is called
                if (isCancelled()) break;
            }
            //default is to return false
            return false;
        }

        protected void onProgressUpdate(Integer... progress) {
            //setProgressPercent(progress[0]);
        }

        protected void onPostExecute(Boolean result) {
            if(BTSocket != null) {
                if (BTSocket.isConnected()) {
                    connectionStatus = "Connected";
                } else {
                    connectionStatus = "Failed Connection";
                }
                terminalTextView.setText("Connection State: " + connectionStatus + "\n\n");
            }else{
                connectionStatus = "Failed Connection";
                terminalTextView.setText("Connection State: " + connectionStatus + "\n\n");
            }
        }
    }

}


