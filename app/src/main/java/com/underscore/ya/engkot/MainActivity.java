package com.underscore.ya.engkot;

import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import static android.R.attr.title;
import static android.R.attr.typeface;
import static android.R.id.progress;

public class MainActivity extends Activity {

    private BluetoothAdapter myBluetooth = null;
    static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    BluetoothSocket btSocket = null;
    private boolean isBtConnected = false;
    private ProgressDialog progress;
    String address;

    protected void setTypeFace() {
        AssetManager am = this.getApplicationContext().getAssets();
        Typeface tf_primetime = Typeface.createFromAsset(am,String.format(Locale.US, "fonts/PRIMETIME.ttf"));
        Typeface tf_sansus = Typeface.createFromAsset(am,String.format(Locale.US, "fonts/Sansus_Webisimo.ttf"));

        TextView titleText = (TextView) findViewById(R.id.title_text);
        titleText.setTypeface(tf_primetime);

        TextView saldoText = (TextView) findViewById(R.id.saldo_text);
        saldoText.setTypeface(tf_sansus);

        TextView saldoValueText = (TextView) findViewById(R.id.saldo_value_text);
        saldoValueText.setTypeface(tf_sansus);

        Button orderButton = (Button) findViewById(R.id.order_button);
        orderButton.setTypeface(tf_primetime);

        Button payButton = (Button) findViewById(R.id.pay_button);
        payButton.setTypeface(tf_primetime);

        Button topupButton = (Button) findViewById(R.id.topup_button);
        topupButton.setTypeface(tf_primetime);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        myBluetooth = BluetoothAdapter.getDefaultAdapter();

        if (myBluetooth == null) {
            Toast.makeText(getApplicationContext(), "Bluetooth Device Not Available", Toast.LENGTH_LONG).show();
            finish();
        } else if (!myBluetooth.isEnabled()) {
            Intent turnBTon = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(turnBTon,1);
        }

        setTypeFace();
    }

    public void orderEngkot(View view) {
        Set<BluetoothDevice> pairedDevices = myBluetooth.getBondedDevices();

        if (pairedDevices.size()>0) {
            for(BluetoothDevice bt : pairedDevices) {
                if (bt.getName().equals("HC-05")) {
                    address = bt.getAddress();

                    new ConnectBT().execute();

                    if (btSocket!=null) {
                        try {
                            btSocket.getOutputStream().write("0".toString().getBytes());
                        } catch (IOException e) {
                            msg("Error");
                        }
                    }
                }
            }
        } else {
            msg("No LED Found");
        }

    }

    private class ConnectBT extends AsyncTask<Void, Void, Void> {
        private boolean ConnectSuccess = true; //if it's here, it's almost connected

        @Override
        protected void onPreExecute() {
            progress = ProgressDialog.show(MainActivity.this, "Connecting...", "Please wait!!!");  //show a progress dialog
        }

        @Override
        protected Void doInBackground(Void... devices) {//while the progress dialog is shown, the connection is done in background
            try {
                if (btSocket == null || !isBtConnected) {
                    myBluetooth = BluetoothAdapter.getDefaultAdapter();//get the mobile bluetooth device
                    msg(address);
                    BluetoothDevice dispositivo = myBluetooth.getRemoteDevice(address);//connects to the device's address and checks if it's available
                    btSocket = dispositivo.createInsecureRfcommSocketToServiceRecord(myUUID);//create a RFCOMM (SPP) connection
                    BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
                    btSocket.connect();//start connection
                }
            } catch (IOException e) {
                ConnectSuccess = false;//if the try failed, you can check the exception here
            }
            return null;
        }
        @Override
        protected void onPostExecute(Void result) {//after the doInBackground, it checks if everything went fine
            super.onPostExecute(result);

            if (!ConnectSuccess) {
                msg("Connection Failed.");
                finish();
            } else {
                msg("Connected.");
                isBtConnected = true;
            }
            progress.dismiss();
        }
    }

    private void msg(String s) {
        Toast.makeText(getApplicationContext(),s,Toast.LENGTH_LONG).show();
    }
}
