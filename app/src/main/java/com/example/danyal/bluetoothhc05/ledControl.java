package com.example.danyal.bluetoothhc05;

import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;

import java.util.UUID;
public class ledControl extends AppCompatActivity {

    Button btn1, btnDis;
    PathView pathView;
    SeekBar seekBar1;

    TextView text1,text2;
    String address = null;


    private ProgressDialog progress;
    private InputStream input_data_stream;
    BluetoothAdapter myBluetooth = null;
    BluetoothSocket btSocket = null;
    private boolean isBtConnected = false;

    String receive_text_data = "";


    int receive_text_char_count = 0;

    static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");


    private Handler handler = new Handler(new Handler.Callback() {

        @TargetApi(Build.VERSION_CODES.KITKAT)
        @Override
        public boolean handleMessage(Message msg) {
            if (msg.what == 1) {
                receive_text_data += (String)msg.obj;
                receive_text_char_count++;
                if(receive_text_char_count > 256){
                    receive_text_char_count = 0;
                    receive_text_data = "";

                }

            }else if( msg.what == 2){


                if(receive_text_data.startsWith("add 1,0,")){
                    pathView.setData((Integer.parseInt(receive_text_data.substring(8)) - 240/2 )*3);
                }else if(receive_text_data.startsWith("t2.txt=")){
                    text2.setText("  体温：");
                    text2.append( ( receive_text_data.substring(8).split("\"")[0]));
                    text2.append("℃");

                }else{

                    text1.append(receive_text_data);
                }

                receive_text_data = "";
                receive_text_char_count = 0;
            } else {
                throw new IllegalStateException("Unexpected value: " + msg.what);
            }
            return false;
        }
    });



    static int count_0xFF = 0;




    public class bt_receiver_thread extends Thread {
        int receive_char_data;


        @Override
        public void run() {
            super.run();

            while(btSocket.isConnected()){
                try {
                    if(input_data_stream.available() != 0){
                        Message msg = new Message();

                        receive_char_data = input_data_stream.read()&0x0FF ;



                        if(receive_char_data == 0x0FF){
                            count_0xFF++;
                            if(count_0xFF == 3){
                                count_0xFF = 0;
                                msg.what = 2;
                                handler.sendMessage(msg);
                            }
                            continue;
                        }

                        msg.what = 1;
                        msg.obj = String.valueOf((char)receive_char_data);
                        handler.sendMessage(msg);

                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }


        }
    }

    bt_receiver_thread rec_thread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);





        Intent newint = getIntent();
        address = newint.getStringExtra(DeviceList.EXTRA_ADDRESS);

        setContentView(R.layout.activity_led_control);

        btn1 = (Button) findViewById(R.id.button2);

        btnDis = (Button) findViewById(R.id.button4);


        pathView = (PathView) findViewById(R.id.pathView);

        text1 = (TextView) findViewById(R.id.editTextTextPersonName);
        text2 = (TextView) findViewById(R.id.textView2);
        seekBar1 = (SeekBar) findViewById(R.id.seekBar);



        new ConnectBT().execute();

        btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View v) {
                sendSignal("S");

            }
        });

        btnDis.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View v) {
                Disconnect();
            }
        });

        seekBar1.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                pathView.points_snap = i + 1;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

    }



    private void sendSignal ( String number ) {
        if ( btSocket != null ) {
            try {
                btSocket.getOutputStream().write(number.getBytes());
            } catch (IOException e) {
                msg("Error");
            }
        }
    }

    private void Disconnect () {
        if ( btSocket!=null ) {
            try {
                btSocket.close();
            } catch(IOException e) {
                msg("Error");
            }
        }

        finish();
    }

    private void msg (String s) {
        Toast.makeText(getApplicationContext(), s, Toast.LENGTH_LONG).show();
    }

    private class ConnectBT extends AsyncTask<Void, Void, Void> {
        private boolean ConnectSuccess = true;

        @Override
        protected  void onPreExecute () {
            progress = ProgressDialog.show(ledControl.this, "Connecting...", "Please Wait!!!");
        }

        @Override
        protected Void doInBackground (Void... devices) {
            try {
                if ( btSocket==null || !isBtConnected ) {
                   // Log.i("message:", "doInBackground: Try to connect bt.");
                    myBluetooth = BluetoothAdapter.getDefaultAdapter();
                    BluetoothDevice dispositivo = myBluetooth.getRemoteDevice(address);
                    btSocket = dispositivo.createInsecureRfcommSocketToServiceRecord(myUUID);
                    BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
                    btSocket.connect();

                }
            } catch (IOException e) {
                ConnectSuccess = false;
            }




            Log.i("TAG", "doInBackground: connected;");

            return null;
        }

        @Override
        protected void onPostExecute (Void result) {
            super.onPostExecute(result);

            if (!ConnectSuccess) {
                msg("Connection Failed. Is it a SPP Bluetooth? Try again.");
                finish();
            } else {
                msg("Connected");
                isBtConnected = true;

                try {
                    input_data_stream = btSocket.getInputStream();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                rec_thread = new bt_receiver_thread();
                rec_thread.start();
                //ledControl.this.runOnUiThread(rec_thread);






            }

            progress.dismiss();
        }
    }


}
