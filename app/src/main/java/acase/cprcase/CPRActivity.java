package acase.cprcase;


        import android.content.DialogInterface;
        import android.content.Intent;
        import android.os.Bundle;
        import android.os.Handler;
        import android.os.Message;
        import android.support.v7.app.ActionBar;
        import android.support.v7.app.AppCompatActivity;
        import android.support.v7.widget.Toolbar;
        import android.widget.Toast;

        import acase.cprcase.bluetooth.BluetoothService;
        import acase.cprcase.bluetooth.Constants;


public class CPRActivity  extends AppCompatActivity {

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cpr);
        setToolBar();

        if(MainActivity.mBlueToothService != null){
            MainActivity.mBlueToothService.mHandler = mHandler;
        }
    }

    void setToolBar(){
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(R.string.CPRPage);
        actionBar.setDisplayHomeAsUpEnabled(true);
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Constants.MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;
                    // construct a string from the valid bytes in the buffer
                    String readMessage = new String(readBuf, 0 , msg.arg1);

                    if(readMessage.equals("a") && MainActivity.isAlertDialog == false && System.currentTimeMillis() - MainActivity.lastAlertTime > MainActivity.alertDelayTime){
                        MainActivity.myMediaPlaye.start();
                        MainActivity.myMediaPlaye.setLooping(true);
                        MainActivity.isAlertDialog = true;
                        MainActivity.lastAlertTime = System.currentTimeMillis();
                        new android.app.AlertDialog.Builder(CPRActivity.this)
                                .setTitle(R.string.alertATitle)
                                .setMessage(R.string.alertAContent)
                                .setPositiveButton(R.string.alertAPositiveBtn, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        MainActivity.isAlertDialog = false;
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
                        MainActivity.lastAlertTime = System.currentTimeMillis();
                        new android.app.AlertDialog.Builder(CPRActivity.this)
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
                case Constants.MESSAGE_CONNLOST:
                    MainActivity.myMediaPlaye.start();
                    MainActivity.myMediaPlaye.setLooping(true);
                    MainActivity.isAlertDialog = true;
                    new android.app.AlertDialog.Builder(CPRActivity.this)
                            .setTitle(R.string.alertBTDisConnTitle)
                            .setMessage(R.string.alertBTDisConnContent)
                            .setPositiveButton(R.string.alertBTPositiveBtn, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    MainActivity.isAlertDialog = false;
                                    startActivity(new Intent(getApplicationContext(),SettingActivity.class));
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
                case Constants.MESSAGE_TOAST:
                    String strToast = msg.getData().getString(Constants.TOAST);
                    Toast.makeText(CPRActivity.this,strToast, Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };
}
