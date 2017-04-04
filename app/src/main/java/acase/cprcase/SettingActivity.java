package acase.cprcase;


import android.accessibilityservice.AccessibilityService;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import acase.cprcase.bluetooth.BluetoothService;
import acase.cprcase.bluetooth.Constants;
import acase.cprcase.bluetooth.DeviceListActivity;


public class SettingActivity extends AppCompatActivity {

    // Intent request codes
    private static final int REQUEST_CONNECT_DEVICE_SECURE = 1;
    private static final int REQUEST_CONNECT_DEVICE_INSECURE = 2;
    private static final int REQUEST_ENABLE_BT = 3;
    private static final String TAG = "Setting";

    // Layout Views
    private EditText mOutEditText;
    private static Button mSendButton,Btn_connectBT;
    private static TextView TV_connectStatus,TV_deviceName,TV_receiveMsg,TV_reciveTime;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        setToolBar();
        initElement();
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
        }
        MainActivity.mBlueToothService.mHandler = mHandler;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

    }

    /**
     * Sends a message.
     * @param message A string of text to send.
     */
    private void sendMessage(String message) {
        // Check that we're actually connected before trying anything
        if (MainActivity.mBlueToothService.getState() != BluetoothService.STATE_CONNECTED) {
            Toast.makeText(this, R.string.not_connected, Toast.LENGTH_SHORT).show();
            return;
        }

        // Check that there's actually something to send
        if (message.length() > 0) {
            // Get the message bytes and tell the BluetoothService to write
            byte[] send = message.getBytes();
            MainActivity.mBlueToothService.write(send);

            // Reset out string buffer to zero and clear the edit text field
            MainActivity.mOutStringBuffer.setLength(0);
            mOutEditText.setText(MainActivity.mOutStringBuffer);
        }
    }

    /**
     * The action listener for the EditText widget, to listen for the return key
     */
    private TextView.OnEditorActionListener mWriteListener
            = new TextView.OnEditorActionListener() {
        public boolean onEditorAction(TextView view, int actionId, KeyEvent event) {
            // If the action is a key-up event on the return key, send the message
            if (actionId == EditorInfo.IME_NULL && event.getAction() == KeyEvent.ACTION_UP) {
                String message = view.getText().toString();
                sendMessage(message);
            }
            return true;
        }
    };

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CONNECT_DEVICE_SECURE:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    connectDevice(data, true);
                }
                break;
            case REQUEST_CONNECT_DEVICE_INSECURE:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    connectDevice(data, false);
                }
                break;
            case REQUEST_ENABLE_BT:
                // User did not enable Bluetooth or an error occurred
                Log.d(TAG, "BT not enabled");
                Toast.makeText(SettingActivity.this, R.string.bt_not_enabled,
                        Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Establish connection with other device
     *
     * @param data   An {@link Intent} with {@link DeviceListActivity#EXTRA_DEVICE_ADDRESS} extra.
     * @param secure Socket Security type - Secure (true) , Insecure (false)
     */
    private void connectDevice(Intent data, boolean secure) {
        // Get the device MAC address
        String address = data.getExtras()
                .getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);

        MainActivity.editor.clear();
        MainActivity.editor.putBoolean(MainActivity.SharePreSecure, secure);
        MainActivity.editor.putString(MainActivity.SharePreAddress, address);
        MainActivity.editor.commit();

        // Get the BluetoothDevice object
        BluetoothDevice device = MainActivity.mBluetoothAdapter.getRemoteDevice(address);
        // Attempt to connect to the device
        MainActivity.mBlueToothService.connect(device, secure);
    }


    void initElement(){
        mOutEditText = (EditText) findViewById(R.id.edit_text_out);
        mSendButton = (Button) findViewById(R.id.button_send);
        Btn_connectBT = (Button) findViewById(R.id.Btn_connectBT);
        TV_receiveMsg = (TextView) findViewById(R.id.TV_receiveMsg);
        TV_reciveTime = (TextView) findViewById(R.id.TV_receiveTime);
        TV_connectStatus = (TextView) findViewById(R.id.TV_connectStatus);
        TV_deviceName = (TextView) findViewById(R.id.TV_deviceName);
        if(MainActivity.mBlueToothService.getState() == BluetoothService.STATE_CONNECTED){
            TV_connectStatus.setText("已連線");
            TV_deviceName.setText( MainActivity.mConnectedDeviceName);
        }else{
            TV_connectStatus.setText("未連線");
            TV_deviceName.setText( "");
        }

        // Initialize the compose field with a listener for the return key
        mOutEditText.setOnEditorActionListener(mWriteListener);

        // Initialize the send button with a listener that for click events
        mSendButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Send a message using content of the edit text widget
                TextView textView = (TextView) findViewById(R.id.edit_text_out);
                String message = textView.getText().toString();
                sendMessage(message);
            }
        });

        Btn_connectBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent serverIntent = new Intent(SettingActivity.this, DeviceListActivity.class);
                startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_SECURE);
            }
        });

    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Constants.MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case BluetoothService.STATE_CONNECTED:
                            TV_connectStatus.setText(R.string.title_connected);
                            TV_deviceName.setText(MainActivity.mConnectedDeviceName);
                            break;
                        case BluetoothService.STATE_CONNECTING:
                            TV_connectStatus.setText(R.string.title_connecting);
                            break;
                        case BluetoothService.STATE_LISTEN:
                        case BluetoothService.STATE_NONE:
                            TV_connectStatus.setText(R.string.title_disConnected);
                            TV_deviceName.setText("");
                            break;
                    }
                    break;
                case Constants.MESSAGE_WRITE:


                    byte[] writeBuf = (byte[]) msg.obj;
                    // construct a string from the buffer
                    String writeMessage = new String(writeBuf);
                    break;

                case Constants.MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;
                    // construct a string from the valid bytes in the buffer

                    String readMessage = new String(readBuf, 0 , msg.arg1);
                    Calendar c = Calendar.getInstance();
                    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    String formattedDate = df.format(c.getTime());

                    TV_receiveMsg.setText(readMessage);
                    TV_reciveTime.setText(formattedDate);

                    if(readMessage.equals("a") && MainActivity.isAlertDialog == false && System.currentTimeMillis() - MainActivity.lastAlertTime > MainActivity.alertDelayTime){
                        MainActivity.myMediaPlaye.start();
                        MainActivity.myMediaPlaye.setLooping(true);
                        MainActivity.isAlertDialog = true;
                        MainActivity.lastAlertTime = System.currentTimeMillis();
                        new AlertDialog.Builder(SettingActivity.this)
                                .setTitle(R.string.alertATitle)
                                .setMessage(R.string.alertAContent)
                                .setPositiveButton(R.string.alertAPositiveBtn, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        MainActivity.isAlertDialog = false;
                                        SettingActivity.this.finish();
                                        startActivity(new Intent(getApplicationContext(),CPRActivity.class));
                                        MainActivity.myMediaPlaye.pause();
                                        MainActivity.myMediaPlaye.setLooping(false);
                                    }
                                })
                                .setNegativeButton(R.string.alertANegativeBtn, new DialogInterface.OnClickListener() {
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
                    }else if(readMessage.equals("b") && MainActivity.isAlertDialog == false && System.currentTimeMillis() - MainActivity.lastAlertTime > MainActivity.alertDelayTime){
                        MainActivity.myMediaPlaye.start();
                        MainActivity.myMediaPlaye.setLooping(true);
                        MainActivity.isAlertDialog = true;
                        new AlertDialog.Builder(SettingActivity.this)
                                .setTitle(R.string.alertBTitle)
                                .setMessage(R.string.alertBContent)
                                .setPositiveButton(R.string.alertBPositiveBtn, new DialogInterface.OnClickListener() {
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
                case Constants.MESSAGE_CONNLOST:
                    MainActivity.myMediaPlaye.start();
                    MainActivity.myMediaPlaye.setLooping(true);
                    MainActivity.isAlertDialog = true;
                    MainActivity.lastAlertTime = System.currentTimeMillis();
                    new AlertDialog.Builder(SettingActivity.this)
                            .setTitle(R.string.alertBTDisConnTitle)
                            .setMessage(R.string.alertBTDisConnContent)
                            .setPositiveButton(R.string.alertBTPositiveBtn, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    MainActivity.isAlertDialog = false;
                                    SettingActivity.this.finish();
                                    startActivity(new Intent(getApplicationContext(),SettingActivity.class));
                                    MainActivity.myMediaPlaye.pause();
                                    MainActivity.myMediaPlaye.setLooping(false);
                                }
                            })
                            .setNegativeButton(R.string.alertBTNegativeBtn, new DialogInterface.OnClickListener() {
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
                case Constants.MESSAGE_TOAST:
                    String strToast = msg.getData().getString(Constants.TOAST);
                    Toast.makeText(SettingActivity.this,strToast, Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };
    void setToolBar(){
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(R.string.SettingPage);
        actionBar.setDisplayHomeAsUpEnabled(true);
    }
}
