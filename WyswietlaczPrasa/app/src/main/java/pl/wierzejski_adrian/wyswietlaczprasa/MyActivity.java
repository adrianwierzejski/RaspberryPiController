package pl.wierzejski_adrian.wyswietlaczprasa;

import android.support.v7.app.AppCompatActivity;

import java.util.regex.Pattern;


public class MyActivity extends AppCompatActivity {
    public static final String DataIp="Adres_IP";
    public static final String DefaultIpAddress="192.168.0.1";
    public static final String DataPort="Numer_portu";
    public static final int DefaultPort=1234;
    public static final String DataConfig="Konfiguracja";
    public static final String DataConfigReceived="KonfiguracjaOdebrana";
    private static final Pattern patternIP = Pattern.compile(
            "^(([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.){3}([01]?\\d\\d?|2[0-4]\\d|25[0-5])$");

    public static boolean isIpAddressValid(final String ip) {
        return patternIP.matcher(ip).matches();
    }
}
