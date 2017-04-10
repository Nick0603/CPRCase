package acase.cprcase;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import acase.cprcase.bluetooth.BluetoothService;
import acase.cprcase.bluetooth.Constants;
import acase.cprcase.bluetooth.DeviceListActivity;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private Button Btn_pageCPR,Btn_pageBlueTooth,Btn_AutoConnect;

    /*Name of the connected device*/
    public static String mConnectedDeviceName = "";
    public static BluetoothAdapter mBluetoothAdapter = null;
    public static BluetoothService mBlueToothService = null;

    public static long lastAlertTime = 0;
    public static final int alertDelayTime = 3 * 1000;
    public static boolean isAlertDialog  = false;
    public static MediaPlayer myMediaPlaye;
    public static final long[] pattern = {500, 1000, 500,1000};
    public static Vibrator myVibrator;
    // Intent request codes
    private static final int REQUEST_CONNECT_DEVICE_SECURE = 1;
    private static final int REQUEST_CONNECT_DEVICE_INSECURE = 2;
    private static final int REQUEST_ENABLE_BT = 3;

    public static SharedPreferences spref = null;
    public static SharedPreferences.Editor editor = null;
    public static final String SharePreSecure = "lastConnSecure";
    public static final String SharePreAddress = "lastConnAddress";

    public static final String AlertACondition = "a";
    public static final String AlertBCondition = "b";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // local資料庫建立
        spref = getPreferences(MODE_PRIVATE);
        editor = spref.edit();

        // 藍芽建立
        MainActivity.mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (MainActivity.mBluetoothAdapter == null) {
            Activity activity = this;
            Toast.makeText(activity, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            activity.finish();
        }

        // 元件建立
        myVibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        myMediaPlaye = MediaPlayer.create(this, R.raw.alert);
        lastAlertTime = System.currentTimeMillis();
        Btn_pageCPR = (Button) findViewById(R.id.Btn_pageCPR);
        Btn_pageBlueTooth = (Button) findViewById(R.id.Btn_pageBlueTooth);
        Btn_AutoConnect = (Button) findViewById(R.id.Btn_AutoConnect);
        setToolBar();
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

        // 藍芽功能偵測  => 開啟
        if (!MainActivity.mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
            // Otherwise, setup the chat session
        } else if (MainActivity.mBlueToothService == null) {
            // setup mBlueToothService
            MainActivity.mBlueToothService = new BluetoothService(this, mHandler);
        }else{
            // update mHandler
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

        Btn_AutoConnect.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View v) {
                // 待連線狀態 才可使用此按鈕
                if (mBlueToothService.getState() == BluetoothService.STATE_NONE ||
                        mBlueToothService.getState() == BluetoothService.STATE_LISTEN) {

                    Boolean lastBTSecure = MainActivity.spref.getBoolean(MainActivity.SharePreSecure, false);
                    String lastBTAddress = MainActivity.spref.getString(MainActivity.SharePreAddress, null);

                    if (lastBTAddress != null) {
                        // Get the BluetoothDevice object
                        BluetoothDevice lastDevice = MainActivity.mBluetoothAdapter.getRemoteDevice(lastBTAddress);
                        // Attempt to connect to the device
                        MainActivity.mBlueToothService.connect(lastDevice, lastBTSecure);
                    } else {
                        Toast.makeText(MainActivity.this, R.string.lastBTNoStored, Toast.LENGTH_SHORT).show();
                    }
                }else{
                    Toast.makeText(MainActivity.this,R.string.autoConnUnable, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    void setToolBar(){
        Log.d(TAG, "setToolBar()");
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(R.string.homePage);
    }


    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Constants.MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case BluetoothService.STATE_CONNECTED:
                            Toast.makeText(MainActivity.this, "與" + MainActivity.mConnectedDeviceName + "裝置連線成功", Toast.LENGTH_SHORT).show();
                            break;
                        case BluetoothService.STATE_NONE:
                            MainActivity.mConnectedDeviceName = "";
                            break;
                    }
                    break;

                case Constants.MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;
                    // construct a string from the valid bytes in the buffer
                    String readMessage = new String(readBuf, 0 , msg.arg1);

                    if(readMessage.equals(AlertACondition) && MainActivity.isAlertDialog == false && System.currentTimeMillis() - lastAlertTime > alertDelayTime){
                        alertStart();
                        new android.app.AlertDialog.Builder(MainActivity.this)
                                .setTitle(R.string.alertATitle)
                                .setMessage(R.string.alertAContent)
                                .setPositiveButton(R.string.alertAPositiveBtn, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        startActivity(new Intent(getApplicationContext(),CPRActivity.class));
                                        alertStop();
                                    }
                                })
                                .setNegativeButton(R.string.alertANegativeBtn, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        alertStop();
                                    }
                                })
                                .setOnCancelListener(new DialogInterface.OnCancelListener(){
                                    @Override
                                    public void onCancel(DialogInterface dialog) {
                                        alertStop();
                                    }
                                })
                                .show();
                    }else if(readMessage.equals(AlertBCondition) && MainActivity.isAlertDialog == false && System.currentTimeMillis() - lastAlertTime > alertDelayTime){
                        alertStart();
                        new android.app.AlertDialog.Builder(MainActivity.this)
                                .setTitle(R.string.alertBTitle)
                                .setMessage(R.string.alertBContent)
                                .setPositiveButton(R.string.alertBPositiveBtn, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        alertStop();
                                    }
                                })

                                .setOnCancelListener(new DialogInterface.OnCancelListener(){
                                    @Override
                                    public void onCancel(DialogInterface dialog) {
                                        alertStop();
                                    }
                                })
                                .show();
                    }

                    break;
                case Constants.MESSAGE_DEVICE_NAME:
                    // save the connected device's name
                    MainActivity.mConnectedDeviceName = msg.getData().getString(Constants.DEVICE_NAME);
                    break;

                case Constants.MESSAGE_CONNLOST:
                    alertStart();
                    new android.app.AlertDialog.Builder(MainActivity.this)
                            .setTitle(R.string.alertBTDisConnTitle)
                            .setMessage(R.string.alertBTDisConnContent)
                            .setPositiveButton(R.string.alertBTPositiveBtn, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    startActivity(new Intent(getApplicationContext(),SettingActivity.class));
                                    alertStop();
                                }
                            })
                            .setNegativeButton(R.string.alertBTNegativeBtn, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    alertStop();
                                }
                            })
                            .setOnCancelListener(new DialogInterface.OnCancelListener(){
                                @Override
                                public void onCancel(DialogInterface dialog) {
                                    alertStop();
                                }
                            })
                            .show();
                    break;
                case Constants.MESSAGE_CONNFAIL:
                    String deviceName = msg.getData().getString(Constants.DEVICE_NAME);
                    Toast.makeText(MainActivity.this,"與" + deviceName + "裝置連線失敗", Toast.LENGTH_SHORT).show();
                    break;
                case Constants.MESSAGE_TOAST:
                    String strToast = msg.getData().getString(Constants.TOAST);
                    Toast.makeText(MainActivity.this,strToast, Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };
    public static void alertStart(){
        myMediaPlaye.start();
        myMediaPlaye.setLooping(true);
        isAlertDialog = true;
        myVibrator.vibrate(pattern, 0);
        lastAlertTime = System.currentTimeMillis();
    }

    public static void alertStop(){
       isAlertDialog = false;
        myMediaPlaye.pause();
        myMediaPlaye.setLooping(false);
        myVibrator.cancel();
    }
}
