package pl.wierzejski_adrian.wyswietlaczprasa;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;


public class MainActivity extends MyActivity {
    private SharedPreferences prefs;
    private String prefsFile ="dane";
    private int [] config;
    private String ipAddress;
    private int port;
    private Client client;// = new Client();
    private static final int reqSettingsActivity = 1;
    private static final int reqConnectActivity = 2;
    private Intent intentSettingsActivity;
    private Intent intentConnectActivity;
    private int reset;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if ( isExternalStorageWritable() ) {

            File appDirectory = new File( Environment.getExternalStorageDirectory().toString()+"/Android/data/"+getPackageName()+"/files");
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd'_T'HH:mm:ss.SSS");
            String date = df.format(Calendar.getInstance().getTime());
            File logDirectory = new File( appDirectory + "/log" );
            File logFile = new File( logDirectory, "logcat" + date+ ".txt" );
            if ( !appDirectory.exists() ) {
                appDirectory.mkdir();
            }
            if ( !logDirectory.exists() ) {
                logDirectory.mkdir();
            }
            try {
                Process process = Runtime.getRuntime().exec("logcat -c");
                process = Runtime.getRuntime().exec("logcat -f " + logFile);
            } catch ( IOException e ) {
                e.printStackTrace();
            }

        }
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        prefs = getSharedPreferences(prefsFile, Activity.MODE_PRIVATE);
        ipAddress = prefs.getString(DataIp,DefaultIpAddress);
        port = prefs.getInt(DataPort,DefaultPort);
        String prefsConfig = prefs.getString(DataConfig,null);
        if(prefsConfig!=null){
            String [] tab =prefsConfig.split(" ");
            config = new int[tab.length];
            try {
                for (int i = 0; i < config.length; i++)
                    config[i] = Integer.parseInt(tab[i]);
            }catch (NumberFormatException e){
                e.printStackTrace();
                config = null;
            }
        }
        intentSettingsActivity = new Intent(MainActivity.this,SettingsActivity.class);
        if(config!=null)
            intentSettingsActivity.putExtra(DataConfig,config);
        intentConnectActivity = new Intent(MainActivity.this,ConnectActivity.class);
        intentConnectActivity.putExtra(DataIp,DefaultIpAddress);
        intentConnectActivity.putExtra(DataPort, DefaultPort);
        final TextView tvLewa= (TextView) findViewById(R.id.textView_lewa);
        final TextView tvPrawa= (TextView) findViewById(R.id.textView_prawa);
        final TextView tvSrodek= (TextView) findViewById(R.id.textView_srodek);
        final ProgressBar pbLewa = (ProgressBar) findViewById(R.id.progressBar_lewa);
        final ProgressBar pbPrawa = (ProgressBar) findViewById(R.id.progressBar_prawa);
        final ProgressBar pbSrodek = (ProgressBar) findViewById(R.id.progressBar_srodek);
        final CheckBox cbKlapa = (CheckBox) findViewById(R.id.checkBox_klapa);
        final CheckBox cbTyl = (CheckBox) findViewById(R.id.checkBox_tyl);
        final CheckBox cbPrzod = (CheckBox) findViewById(R.id.checkBox_przod);
        final TextView tvLiczbaBel =  findViewById(R.id.textView_liczba_bel);
        final TextView tvlog = findViewById(R.id.textView_log);
        final View v = this.findViewById(android.R.id.content);
        Button bReset = findViewById(R.id.button_reset);
        bReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                reset+=Integer.parseInt(tvLiczbaBel.getText().toString());
                tvLiczbaBel.setText("0");
            }
        });
        client = new Client();
        client.setName("Proces_Klienta");
        client.setClientListener(new Client.ClientLoopListener() {
            @Override
            public void onSomeEvent(final int command, final int[] data, final String response) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tvlog.setText(response);
                        String log = "";
                        switch (command) {
                            case Client.sGetData:
                                int czujnikZblizeniowy = config[4];
                                pbLewa.setProgress(data[5]);
                                tvLewa.setText(data[5]+"");
                                pbPrawa.setProgress(data[6]);
                                tvPrawa.setText(data[6]+"");
                                pbSrodek.setProgress(data[7]);
                                tvSrodek.setText(data[7]+"");
                                if (data[4] > czujnikZblizeniowy) cbKlapa.setChecked(true);
                                else cbKlapa.setChecked(false);
                                if (data[0] > czujnikZblizeniowy) cbTyl.setChecked(true);
                                else cbTyl.setChecked(false);
                                if (data[1] > czujnikZblizeniowy) cbPrzod.setChecked(true);
                                else cbPrzod.setChecked(false);
                                tvLiczbaBel.setText((data[8]-reset)+"");
                                break;
                            case Client.sReceiveConfig:
                                intentSettingsActivity.putExtra(DataConfigReceived, data);
                                if (config == null)
                                    config = data;
                                prefs.edit().putString(
                                        DataConfigReceived, Arrays.toString(data)
                                                .replaceAll("\\[|\\]|\\s", "")
                                                .replaceAll(","," ")).commit();
                                Snackbar.make(v, getString(R.string.text_ConfigReceived), Snackbar.LENGTH_LONG);
                                break;
                            case Client.sSendConfig:
                                Snackbar.make(v, getString(R.string.text_ConfigSent), Snackbar.LENGTH_LONG);
                                break;
                            case Client.sClosedConnection:
                                Snackbar.make(v, getString(R.string.text_ClosedConnection), Snackbar.LENGTH_LONG);
                                pbLewa.setProgress(0);
                                tvLewa.setText(0);
                                pbPrawa.setProgress(0);
                                tvPrawa.setText(0);
                                pbSrodek.setProgress(0);
                                tvSrodek.setText(0);
                                cbKlapa.setChecked(false);
                                cbTyl.setChecked(false);
                                cbPrzod.setChecked(false);
                                break;
                            default:
                                tvlog.setText(response);
                                break;
                        }
                    }
                });
            }
        });
        final Button button_polacz = (Button) findViewById(R.id.button_polacz);
        button_polacz.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Łączenie z serwerem", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                Log.v("Main", "Łączenie z serwerem " + ipAddress + ':' + port);
                if (client.getState() == Thread.State.TERMINATED) {
                    client.setClient(ipAddress, port);
                    client.start();
                    button_polacz.setText(R.string.action_disconnect);
                } else if (client.getState() == Thread.State.RUNNABLE || client.getState() == Thread.State.TIMED_WAITING) {
                    client.closeConnection();
                    button_polacz.setText(R.string.action_connect);
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            startActivityForResult(intentSettingsActivity,reqSettingsActivity);
            return true;
        }else if (id == R.id.action_connection) {
            startActivityForResult(intentConnectActivity,reqConnectActivity);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        final SharedPreferences prefs = getSharedPreferences(getString(R.string.app_data), Activity.MODE_PRIVATE);
        switch (requestCode){
            case reqSettingsActivity:
                if(resultCode == RESULT_OK) {
                    if (data.hasExtra(DataConfig)) {
                        config = data.getIntArrayExtra(DataConfig);
                        intentSettingsActivity.putExtra(DataConfig, config);
                        prefs.edit().putString(DataConfig,
                                Arrays.toString(config)
                                        .replaceAll("\\[|\\]|\\s", "")
                                        .replaceAll(","," ")).commit();
                        client.setConfig(config);
                    }
                }
                break;
            case reqConnectActivity:
                if(resultCode==RESULT_OK){
                    if(data.hasExtra(DataIp)) {
                        ipAddress = data.getStringExtra(DataIp);
                        intentConnectActivity.putExtra(DataIp,ipAddress);
                        prefs.edit().putString(DataIp,ipAddress).commit();
                    }
                    if(data.hasExtra(DataPort)) {
                        port = data.getIntExtra(DataPort, DefaultPort);
                        intentConnectActivity.putExtra(DataPort,port);
                        prefs.edit().putInt(DataPort,port).commit();
                    }
                }
                break;
            }
        }
    /* Checks if external storage is available for read and write */
    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if ( Environment.MEDIA_MOUNTED.equals( state ) ) {
            return true;
        }
        return false;
    }

    /* Checks if external storage is available to at least read */
    public boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        if ( Environment.MEDIA_MOUNTED.equals( state ) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals( state ) ) {
            return true;
        }
        return false;
    }
}
