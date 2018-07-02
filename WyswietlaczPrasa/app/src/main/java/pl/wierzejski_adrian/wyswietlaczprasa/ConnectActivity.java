package pl.wierzejski_adrian.wyswietlaczprasa;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


public class ConnectActivity extends MyActivity {

    private Intent resultIntent;
    private EditText ETip;
    private EditText ETport;

    private boolean success=false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        resultIntent = new Intent();
        Intent intent = getIntent();
        ETip = (EditText) findViewById(R.id.editText_IP);
        ETip.setText(intent.hasExtra(DataIp) ? intent.getStringExtra(DataIp) : DefaultIpAddress);
        ETport = (EditText) findViewById(R.id.editText_port);
        ETport.setText(intent.hasExtra(DataPort) ? intent.getIntExtra(DataPort, DefaultPort)+"" : DefaultPort+"");
        Button zapisz = (Button) findViewById(R.id.button_zapiszUstPolaczenia);
        zapisz.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isIpAddressValid(ETip.getText().toString())){
                    resultIntent.putExtra(DataIp,ETip.getText().toString());
                    int Iport = Integer.parseInt(ETport.getText().toString());
                    resultIntent.putExtra(getString(R.string.text_port),Iport);
                    success = true;
                    Snackbar.make(view,getString(R.string.text_ConfigSaveCorrect),Toast.LENGTH_LONG).show();
                }else Snackbar.make(view,getString(R.string.text_IPaddressError),Toast.LENGTH_LONG).show();
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }
    @Override
    public void onBackPressed() {
        if(success)
            setResult(RESULT_OK,resultIntent);
        else
            setResult(RESULT_CANCELED,resultIntent);
        super.onBackPressed();
    }

}
