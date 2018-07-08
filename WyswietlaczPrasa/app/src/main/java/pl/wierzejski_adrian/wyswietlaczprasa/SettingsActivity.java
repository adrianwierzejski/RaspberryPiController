package pl.wierzejski_adrian.wyswietlaczprasa;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class SettingsActivity extends MyActivity {
    private Intent resultIntent;
    private int [] config;
    private EditText [] editNew;
    private EditText [] editOld;
    private boolean saveCorrect=false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        resultIntent = new Intent();
        Intent intent = getIntent();
        editNew =new EditText[] {findViewById(R.id.editText_rozmiarBelki),findViewById(R.id.editText_czasTasmaRuch),
                findViewById(R.id.editText_czasTasmaObrot),findViewById(R.id.editText_czasSiatkaSznurek),
                findViewById(R.id.editText_czujnikZblizeniowy), findViewById(R.id.editText_licznikSiatkaSznurek),
                findViewById(R.id.editText_czujnikSrodek)};
        editOld =new EditText[] {findViewById(R.id.editText_rozmiarBelkiOld),findViewById(R.id.editText_czasTasmaRuchOld),
                findViewById(R.id.editText_czasTasmaObrotOld),findViewById(R.id.editText_czasSiatkaSznurekOld),
                findViewById(R.id.editText_czujnikZblizeniowyOld),findViewById(R.id.editText_licznikSiatkaSznurekOld),
                findViewById(R.id.editText_czujnikSrodekOld)};
        config = intent.hasExtra(DataConfigReceived) ? intent.getExtras().getIntArray(DataConfigReceived) : new int[editNew.length];
        Button zapisz = findViewById(R.id.button_zapiszUstawienia);
        for(int i=0; i<editOld.length && i<config.length; i++)
           editOld[i].setText(config[i]+"");
        if(intent.hasExtra(DataConfig))
            config = intent.getExtras().getIntArray(DataConfig);
        for(int i=0; i<editNew.length && i<config.length; i++) {
            editNew[i].setText(config[i]+"");
            final EditText editText = editNew[i];
            editText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    editText.setError(getString(R.string.text_DataHint));
                }

                @Override
                public void afterTextChanged(Editable editable) {
                    try{
                        int value = Integer.parseInt(editText.getText().toString());
                        if(value>=1&&value<=1023)
                            editText.setError(null);
                    }catch(NumberFormatException ex){
                        editText.setError(getString(R.string.text_DataError));
                    }
                }
            });
        }
        zapisz.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveCorrect=true;
                try {
                    for(int i=0; i<editNew.length && i<config.length; i++){
                        config[i] = Integer.parseInt(editNew[i].getText().toString());
                        if(config[i]<0||config[i]>1023)
                            throw new NumberFormatException();
                    }
                }catch(NumberFormatException ex){
                    saveCorrect=false;
                }

                if(saveCorrect) {
                    resultIntent.putExtra(DataConfig, config);
                    Snackbar.make(view,getString(R.string.text_ConfigSaveCorrect),Snackbar.LENGTH_LONG).show();
                }else
                    Snackbar.make(view, getString(R.string.text_DataHint), Toast.LENGTH_SHORT).show();
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }

    @Override
    public void onBackPressed() {
        if(saveCorrect)
            setResult(RESULT_OK,resultIntent);
        else
            setResult(RESULT_CANCELED,resultIntent);
        super.onBackPressed();
    }
}
