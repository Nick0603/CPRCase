package acase.cprcase;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import acase.cprcase.bluetooth.BluetoothChatService;

public class MainActivity extends AppCompatActivity {
    private ImageButton Btn_pageCPR,Btn_pageBlueTooth,Btn_pageAutoConnect;

    /*Name of the connected device*/
    public static String mConnectedStatus = "未連線";
    /*Name of the connected device*/
    public static String mConnectedDeviceName = "";

    /*Array adapter for the conversation thread*/

    /*String buffer for outgoing messages */
    public static StringBuffer mOutStringBuffer;

    /* Local Bluetooth adapter*/
    public static BluetoothAdapter mBluetoothAdapter = null;

    /**Member object for the chat services*/
    public static BluetoothChatService mChatService = null;


    public static Handler mHandler = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Btn_pageCPR = (ImageButton) findViewById(R.id.Btn_pageCPR);
        Btn_pageBlueTooth = (ImageButton) findViewById(R.id.Btn_pageBlueTooth);
        Btn_pageAutoConnect = (ImageButton) findViewById(R.id.Btn_pageAutoConnect);

        setToolBar();

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

}
