package acase.cprcase;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import acase.cprcase.bluetooth.BluetoothService;
import acase.cprcase.bluetooth.Constants;
import acase.cprcase.bluetooth.DeviceListActivity;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private ImageButton Btn_pageCPR,Btn_pageBlueTooth,Btn_pageAutoConnect;

    /*Name of the connected device*/
    public static String mConnectedStatus = "未連線";
    /*Name of the connected device*/
    public static String mConnectedDeviceName = "";

    /*Array adapter for the conversation thread*/

    /*String buffer for outgoing messages */
    public static StringBuffer mOutStringBuffer = null ;

    /* Local Bluetooth adapter*/
    public static BluetoothAdapter mBluetoothAdapter = null;

    /**Member object for the chat services*/
    public static BluetoothService mBlueToothService = null;

    public static boolean isAlertDialog  = false;
    public static MediaPlayer myMediaPlaye;

    // Intent request codes
    private static final int REQUEST_CONNECT_DEVICE_SECURE = 1;
    private static final int REQUEST_CONNECT_DEVICE_INSECURE = 2;
    private static final int REQUEST_ENABLE_BT = 3;

    public static SharedPreferences spref = null;
    public static SharedPreferences.Editor editor = null;
    public static final String SharePreSecure = "lastConnSecure";
    public static final String SharePreAddress = "lastConnAddress";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MainActivity.mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (MainActivity.mBluetoothAdapter == null) {
            Activity activity = this;
            Toast.makeText(activity, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            activity.finish();
        }

        spref = getPreferences(MODE_PRIVATE);
        editor = spref.edit();

        myMediaPlaye = MediaPlayer.create(this, R.raw.alert);
        Btn_pageCPR = (ImageButton) findViewById(R.id.Btn_pageCPR);
        Btn_pageBlueTooth = (ImageButton) findViewById(R.id.Btn_pageBlueTooth);
        Btn_pageAutoConnect = (ImageButton) findViewById(R.id.Btn_pageAutoConnect);

        setToolBar();

        if(mBlueToothService != null){
            MainActivity.mBlueToothService.mHandler = mHandler;
        }
        Btn_pageBlueTooth.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this,SettingActivity.class));
            }
        });

        Btn_pageCPR.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this,CPRActivity.class));
            }
        });

        Btn_pageAutoConnect.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View v) {

                Boolean lastBTSecure = MainActivity.spref.getBoolean(MainActivity.SharePreSecure, false);
                String lastBTAddress = MainActivity.spref.getString(MainActivity.SharePreAddress, null);

                if(lastBTAddress != null) {
                    // Get the BluetoothDevice object
                    BluetoothDevice device = MainActivity.mBluetoothAdapter.getRemoteDevice(lastBTAddress);
                    // Attempt to connect to the device
                    MainActivity.mBlueToothService.connect(device, lastBTSecure);
                }else{
                    Toast.makeText(MainActivity.this, R.string.lastBTNoStored, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        // Performing this check in onResume() covers the case in which BT was
        // not enabled during onStart(), so we were paused to enable it...
        // onResume() will be called when ACTION_REQUEST_ENABLE activity returns.
        if (MainActivity.mBlueToothService != null) {
            // Only if the state is STATE_NONE, do we know that we haven't started already
            if (MainActivity.mBlueToothService.getState() == BluetoothService.STATE_NONE) {
                // Start the Bluetooth services
                MainActivity.mBlueToothService.start();
            }
        }
    }


    @Override
    public void onStart() {
        super.onStart();
        // If BT is not on, request that it be enabled.
        // setup will then be called during onActivityResult
        if (!MainActivity.mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
            // Otherwise, setup the chat session
        } else if (MainActivity.mBlueToothService == null) {
            setupBluetooth();
        }else{
            MainActivity.mBlueToothService.mHandler = mHandler;
        }
    }

    private void setupBluetooth() {
        Log.d(TAG, "setupBluetooth()");

        if(MainActivity.mBlueToothService == null) {
            MainActivity.mBlueToothService = new BluetoothService(this, mHandler);
        }
        // Initialize the BluetoothService to perform bluetooth connections

        if(MainActivity.mOutStringBuffer == null) {
            // Initialize the buffer for outgoing messages
            MainActivity.mOutStringBuffer = new StringBuffer("");
        }
    }

    void setToolBar(){
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Home");
    }


    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Constants.MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case BluetoothService.STATE_CONNECTED:
                            MainActivity.mConnectedStatus = "已連線";
                            Toast.makeText(MainActivity.this, "已連接到" + MainActivity.mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                            break;
                        case BluetoothService.STATE_NONE:
                            MainActivity.mConnectedStatus = "未連線";
                            MainActivity.mConnectedDeviceName = "";
                            break;
                    }
                    break;

                case Constants.MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;
                    // construct a string from the valid bytes in the buffer
                    String readMessage = new String(readBuf, 0 , msg.arg1);

                    if(readMessage.equals("a") && MainActivity.isAlertDialog == false){
                        MainActivity.myMediaPlaye.start();
                        MainActivity.myMediaPlaye.setLooping(true);
                        MainActivity.isAlertDialog = true;
                        new android.app.AlertDialog.Builder(MainActivity.this)
                                .setTitle(R.string.alertATitle)
                                .setMessage(R.string.alertAContent)
                                .setPositiveButton("前往急救教學", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        MainActivity.isAlertDialog = false;
                                        startActivity(new Intent(getApplicationContext(),CPRActivity.class));
                                        MainActivity.myMediaPlaye.pause();
                                        MainActivity.myMediaPlaye.setLooping(false);
                                    }
                                })
                                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        MainActivity.isAlertDialog = false;
                                        MainActivity.myMediaPlaye.pause();
                                        MainActivity.myMediaPlaye.setLooping(false);
                                    }
                                })
                                .setOnCancelListener(new DialogInterface.OnCancelListener(){
                                    @Override
                                    public void onCancel(DialogInterface dialog) {
                                        MainActivity.isAlertDialog = false;
                                        MainActivity.myMediaPlaye.pause();
                                        MainActivity.myMediaPlaye.setLooping(false);
                                    }
                                })
                                .show();
                    }else if(readMessage.equals("b") && MainActivity.isAlertDialog == false){
                        MainActivity.myMediaPlaye.start();
                        MainActivity.myMediaPlaye.setLooping(true);
                        MainActivity.isAlertDialog = true;
                        new android.app.AlertDialog.Builder(MainActivity.this)
                                .setTitle(R.string.alertBTitle)
                                .setMessage(R.string.alertBContent)
                                .setPositiveButton("確認", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        MainActivity.isAlertDialog = false;
                                        MainActivity.myMediaPlaye.pause();
                                        MainActivity.myMediaPlaye.setLooping(false);
                                    }
                                })

                                .setOnCancelListener(new DialogInterface.OnCancelListener(){
                                    @Override
                                    public void onCancel(DialogInterface dialog) {
                                        MainActivity.isAlertDialog = false;
                                        MainActivity.myMediaPlaye.pause();
                                        MainActivity.myMediaPlaye.setLooping(false);
                                    }
                                })
                                .show();
                    }

                    break;
                case Constants.MESSAGE_DEVICE_NAME:
                    // save the connected device's name
                    MainActivity.mConnectedDeviceName = msg.getData().getString(Constants.DEVICE_NAME);
                    break;

                case Constants.MESSAGE_TOAST:
                    MainActivity.myMediaPlaye.start();
                    MainActivity.myMediaPlaye.setLooping(true);
                    MainActivity.isAlertDialog = true;
                    new android.app.AlertDialog.Builder(MainActivity.this)
                            .setTitle(R.string.alertBTDisConnTitle)
                            .setMessage(R.string.alertBTDisConnContent)
                            .setPositiveButton("前往設定頁面", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    MainActivity.isAlertDialog = false;
                                    startActivity(new Intent(getApplicationContext(),SettingActivity.class));
                                    MainActivity.myMediaPlaye.pause();
                                    MainActivity.myMediaPlaye.setLooping(false);
                                }
                            })
                            .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    MainActivity.isAlertDialog = false;
                                    MainActivity.myMediaPlaye.pause();
                                    MainActivity.myMediaPlaye.setLooping(false);
                                }
                            })
                            .setOnCancelListener(new DialogInterface.OnCancelListener(){
                                @Override
                                public void onCancel(DialogInterface dialog) {
                                    MainActivity.isAlertDialog = false;
                                    MainActivity.myMediaPlaye.pause();
                                    MainActivity.myMediaPlaye.setLooping(false);
                                }
                            })
                            .show();
            }
        }
    };
}
