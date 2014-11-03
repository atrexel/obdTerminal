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
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.UUID;
import java.util.logging.Handler;
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
    Handler myHandler;
    BluetoothSocketListener bsl;

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
            terminalTextView.append("\nselected:\n\n"+selectedDeviceText+"\n\nattempting to connect...");

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
                //finish();
            }
        }else{
            terminalTextView.append("Couldn't get previously selected device\n\n");
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
                    if(BTSocket.isConnected()) {
                        String userCommand = userCommandEditText.getText().toString();
                        userCommandEditText.setText("");
                        terminalTextView.append("\n>> " + userCommand + "\n");
                        Log.d("SendBTData", "Sending User Cmd: "+userCommand);
                        new WriteDataTask().execute(userCommand);
                    }else{
                        userCommandEditText.setText("");
                        new EstablishConnectionTask().execute(selectedDevice);
                    }
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


    private class WriteDataTask extends AsyncTask<String, Integer, Boolean> {
        MessagePoster postMsg;
        String returnedData = "";

        protected Boolean doInBackground(String... commands) {
            int count = commands.length;
            for (int i = 0; i < count; i++) {
                String command = commands[i];
                Log.d("WriteDataThread::Background", "Getting user command: " + command);

/*
                try {
                    Log.d("WriteDataThread", "Setting Output Socket");
                    InputStream inputstream = BTSocket.getInputStream();
                    OutputStream outputstream = BTSocket.getOutputStream();

                    new MessagePoster(terminalTextView, ">>"+command+"\n");

                    Log.d("Data", "Input: "+command);
                    Log.d("Data", "Byte Input: "+command.getBytes());
                    Log.d("Data", "Writing to Output Stream");
                    outputstream.write(command.getBytes());
                    Log.d("Data", "Finished writing to OBD");

                    Log.d("Data", "Trying to read from buffer");
                    byte[] buffer = new byte[1024];
                    inputstream.read(buffer);
                    Log.d("Data", "Finished reading from buffer");

                    Log.d("Data", "Byte Output: "+buffer);
                    Log.d("Data", "String Output: "+new String(buffer));
                    new MessagePoster(terminalTextView, new String(buffer)+"\n\n");

                    Log.d("Data", "Flushing out the output stream.");
                    outputstream.flush();

                }catch (IOException e){
                    Log.d(TAG, "ERROR: " + e);
                }
*/

                sendData(BTSocket, new byte[1]);
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
            //have messagePoster post to textview
            postMsg = new MessagePoster(terminalTextView, returnedData);
        }

        public void sendData(BluetoothSocket sock, byte[] bytes){
            String message = "";
            byte[] buffer; // = new byte[1024];
            String result = "";

            try {
                Log.d("sendData", "Setting Input and Output Sockets");
                InputStream inputstream = sock.getInputStream();
                OutputStream outputstream = sock.getOutputStream();


                message = "Atz\r";

                Log.d("sendData", "Input: " + message);
                outputstream.write(message.getBytes());
                Log.d("sendData", "Finished writing to OBD");
                outputstream.flush();
                Log.d("sendData", "Flushed output stream");

                Log.d("sendData", "Sleeping while waiting for OBD");
                try {
                    Thread.currentThread().sleep(2000);
                }catch(Exception e){
                    Log.e("ERROR", "Exception: "+e);
                }
                Log.d("sendData", "Trying to read from buffer");
                buffer = new byte[1024];
                inputstream.read(buffer);
                Log.d("sendData", "Finished reading from buffer");

                result = new String(buffer);
                result = result.split(">")[0];
                Log.d("sendData", "Byte Output: " + buffer);
                Log.d("sendData", "String Output: " + result);



                message = "Atsp0\r";

                Log.d("sendData", "Input: " + message);
                outputstream.write(message.getBytes());
                Log.d("sendData", "Finished writing to OBD");
                outputstream.flush();
                Log.d("sendData", "Flushed output stream");

                Log.d("sendData", "Sleeping while waiting for OBD");
                try {
                    Thread.currentThread().sleep(2000);
                }catch(Exception e){
                    Log.e("ERROR", "Exception: "+e);
                }
                Log.d("sendData", "Trying to read from buffer");
                buffer = new byte[1024];
                inputstream.read(buffer);
                Log.d("sendData", "Finished reading from buffer");

                result = new String(buffer);
                result = result.split(">")[0];
                Log.d("sendData", "Byte Output: " + buffer);
                Log.d("sendData", "String Output: " + result);



                message = "0100\r";

                Log.d("sendData", "Input: " + message);
                outputstream.write(message.getBytes());
                Log.d("sendData", "Finished writing to OBD");
                outputstream.flush();
                Log.d("sendData", "Flushed output stream");

                Log.d("sendData", "Sleeping while waiting for OBD");
                try {
                    Thread.currentThread().sleep(7000);
                }catch(Exception e){
                    Log.e("ERROR", "Exception: "+e);
                }
                Log.d("sendData", "Trying to read from buffer");
                buffer = new byte[1024];
                inputstream.read(buffer);
                Log.d("sendData", "Finished reading from buffer");

                result = new String(buffer);
                result = result.split(">")[0];
                Log.d("sendData", "Byte Output: " + buffer);
                Log.d("sendData", "String Output: " + result);




                message = "0902\r";

                Log.d("sendData", "Input: " + message);
                outputstream.write(message.getBytes());
                Log.d("sendData", "Finished writing to OBD");
                outputstream.flush();
                Log.d("sendData", "Flushed output stream");

                Log.d("sendData", "Sleeping while waiting for OBD");
                try {
                    Thread.currentThread().sleep(3000);
                }catch(Exception e){
                    Log.e("ERROR", "Exception: "+e);
                }
                Log.d("sendData", "Trying to read from buffer");
                buffer = new byte[1024];
                inputstream.read(buffer);
                Log.d("sendData", "Finished reading from buffer");

                result = new String(buffer);
                result = result.split(">")[0];
                Log.d("sendData", "Byte Output: " + buffer);
                Log.d("sendData", "String Output: " + result);


                message = "0131\r";

                Log.d("sendData", "Input: " + message);
                outputstream.write(message.getBytes());
                Log.d("sendData", "Finished writing to OBD");
                outputstream.flush();
                Log.d("sendData", "Flushed output stream");

                Log.d("sendData", "Sleeping while waiting for OBD");
                try {
                    Thread.currentThread().sleep(3000);
                }catch(Exception e){
                    Log.e("ERROR", "Exception: "+e);
                }
                Log.d("sendData", "Trying to read from buffer");
                buffer = new byte[1024];
                inputstream.read(buffer);
                Log.d("sendData", "Finished reading from buffer");

                result = new String(buffer);
                result = result.split(">")[0];
                Log.d("sendData", "Byte Output: " + buffer);
                Log.d("sendData", "String Output: " + result);


            }catch (IOException e){
                Log.d("sendData", "ERROR: " + e);
            }

        }


        public String recieveData(android.bluetooth.BluetoothSocket sock){
            String data = "";

            try {
                InputStream inputStream = sock.getInputStream();

                int rawResult = inputStream.read();

                //convert from ASCII
                Toast.makeText(getApplicationContext(), "Returned Data:\n"+rawResult,
                        Toast.LENGTH_SHORT).show();


                //for each byte (2 hex chars) convert to char
                //for (int i = 0)

                //terminalTextView.append(result);

                //data = result.toString();

            }catch (IOException e){
                Log.d(TAG, "ERROR: " + e);
            }

            return data;
        }

    } //end async task


    //MessagePoster Thread
    private class MessagePoster implements Runnable {
        private TextView textView;
        private String message;
        public MessagePoster(TextView textView, String message){
            this.textView = textView;
            this.message = message;
        }
        public void run(){
            textView.append(message + "\n");
            //textView.setText(message);
        }
    }//end MessagePoster class

    //BluetoothSocketListener Thread
    private class BluetoothSocketListener implements Runnable {
        private BluetoothSocket socket;
        private TextView textView;
        private Handler handler;

        public BluetoothSocketListener(BluetoothSocket socket, Handler handler, TextView textView){
            this.socket = socket;
            this.textView = textView;
            this.handler = handler;
        }

        public void run() {
            int BUFFER_SIZE = 1024;
            byte[] buffer = new byte[BUFFER_SIZE];
            try{
                InputStream instream = socket.getInputStream();
                int bytesRead = -1;
                String message = "";
                while(true){
                    message = "";
                    bytesRead = instream.read(buffer);
                    if(bytesRead != -1){
                        while((bytesRead == BUFFER_SIZE) &&
                                (buffer[BUFFER_SIZE - 1] != 0))
                        {
                            message = message + new String(buffer, 0, bytesRead);
                            bytesRead = instream.read(buffer);
                        }
                        message = message + new String(buffer, 0, bytesRead - 1);
                        //handler.post(new MessagePoster(textView, message));
                        new MessagePoster(textView, message);
                        socket.getInputStream();
                    }
                }
            }catch(IOException bufferError){
                Log.d(TAG, bufferError.getMessage());
            }
        }
    }//end BluetoothSocketListener class

}


