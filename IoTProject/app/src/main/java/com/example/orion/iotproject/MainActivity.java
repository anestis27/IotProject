package com.example.orion.iotproject;

import android.bluetooth.BluetoothGatt;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.clj.fastble.BleManager;
import com.clj.fastble.callback.BleGattCallback;
import com.clj.fastble.callback.BleNotifyCallback;
import com.clj.fastble.data.BleDevice;
import com.clj.fastble.exception.BleException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.DecimalFormat;


public class MainActivity extends AppCompatActivity {

    private TextView bleTextView,gpsTextView;
    private Button refreshbleButton;
    private static final String TAG = "THEMAIN";
    private static final String servUUID = "0000fff0-0000-1000-8000-00805f9b34fb";
    private static final String readUUID = "0000fff4-0000-1000-8000-00805f9b34fb";
    private final String MAC = "00:80:98:BC:9B:2E";
    private BleDevice myDevice;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();

        BleManager.getInstance().init(getApplication());
        BleManager.getInstance().enableBluetooth();
        BleManager.getInstance()
                .setReConnectCount(1, 5000)
                .setOperateTimeout(10000);
        Connect();
    }


    private void init() {
        bleTextView = (TextView) findViewById(R.id.t3);
        gpsTextView = (TextView) findViewById(R.id.t1);
        refreshbleButton = (Button) findViewById(R.id.blerefreshButton);
        refreshbleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Connect();
            }
        });
    }


    private void Connect() {
        BleManager.getInstance().connect(MAC, new BleGattCallback() {
            @Override
            public void onStartConnect() {
                Log.d(TAG, "onStartConnect");
                bleTextView.setText(R.string.wait);
            }

            @Override
            public void onConnectFail(BleDevice bleDevice, BleException exception) {
                Log.d(TAG, "onConnectFail");
                bleTextView.setText(R.string.problem);
            }

            @Override
            public void onConnectSuccess(BleDevice bleDevice, BluetoothGatt gatt, int status) {
                Log.d(TAG, "onConnectSuccess");
                myDevice = bleDevice;
                bleTextView.setText(R.string.ok);
                Read();
            }

            @Override
            public void onDisConnected(boolean isActiveDisConnected, BleDevice bleDevice, BluetoothGatt gatt, int status) {
                Log.d(TAG, "onDisConnected");
                bleTextView.setText(R.string.problem);
            }
        });
    }

    private void Read() {
        BleManager.getInstance().notify(
                myDevice,
                servUUID,
                readUUID,
                new BleNotifyCallback() {

                    @Override
                    public void onNotifySuccess() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Log.d(TAG, "notify succ");
                                bleTextView.setText(R.string.ok);
                            }
                        });
                    }

                    @Override
                    public void onNotifyFailure(final BleException exception) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Log.d("TAG", "notify fail");
                                bleTextView.setText(R.string.problem);
                            }
                        });
                    }

                    @Override
                    public void onCharacteristicChanged(final byte[] data) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                String str="",d="";
                                try {
                                    str = new String(data, "UTF-8");
                                    if(str.contains("e")){
                                        gpsTextView.setText(R.string.problem);
                                        Log.d(TAG, "GPS problem" );
                                        return;
                                    }
                                    String lat,lon;
                                    int eq = str.indexOf('=');
                                    lat =  str.substring(0,eq);
                                    lon = str.substring(eq+1);//xoris to =

                                    DecimalFormat df = new DecimalFormat("#.######");
                                    lat = Double.valueOf(df.format(Double.parseDouble(lat)))+""; // rounding the numbers (restoring to the original number)
                                    lon = Double.valueOf(df.format(Double.parseDouble(lon)))+""; // rounding the numbers (restoring to the original number)
                                    Log.d(TAG, lat +"   "+ lon );
                                    gpsTextView.setText(R.string.ok);
                                    FireBaseClass.addToDataBase(lat,lon);

                                } catch (Exception e) {
                                    e.printStackTrace();
                                }

                            }
                        });
                    }
                });
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        BleManager.getInstance().disconnectAllDevice();
        BleManager.getInstance().destroy();
    }


}
