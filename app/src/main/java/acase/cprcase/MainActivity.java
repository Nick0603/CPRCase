package acase.cprcase;

import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import acase.cprcase.bluetooth.BluetoothService;
import acase.cprcase.bluetooth.Constants;

public class MainActivity extends AppCompatActivity {
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
                Toast.makeText(v.getContext(), "目前還沒有自動連線功能", Toast.LENGTH_LONG).show();
            }
        });
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
                                        Toast.makeText(getApplicationContext(), "你選擇了取消", Toast.LENGTH_SHORT).show();
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
                                        Toast.makeText(getApplicationContext(), "你選擇了取消", Toast.LENGTH_SHORT).show();
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
                                    Toast.makeText(getApplicationContext(), "你選擇了取消", Toast.LENGTH_SHORT).show();
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
