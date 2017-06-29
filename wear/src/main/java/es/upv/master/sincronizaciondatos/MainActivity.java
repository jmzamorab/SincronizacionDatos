package es.upv.master.sincronizaciondatos;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Wearable;

public class MainActivity extends Activity implements MessageApi.MessageListener, GoogleApiClient.ConnectionCallbacks {

    private static final String WEAR_MANDAR_TEXTO = "/mandar_texto";
    private GoogleApiClient apiClient;
    private TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView = (TextView) findViewById(R.id.textView);
        apiClient = new GoogleApiClient.Builder(this).addApi(Wearable.API).addConnectionCallbacks(this).build();
    }

    @Override
    protected void onStart() {
        super.onStart();
        apiClient.connect();
    }

    @Override
    protected void onStop() {
        Wearable.MessageApi.removeListener(apiClient, this);
        if (apiClient != null && apiClient.isConnected()) {
            apiClient.disconnect();
        }
        super.onStop();
    }


    @Override
    public void onConnected(Bundle bundle) {
        Wearable.MessageApi.addListener(apiClient, this);
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onMessageReceived(final MessageEvent mensaje) {
        if (mensaje.getPath().equalsIgnoreCase(WEAR_MANDAR_TEXTO)) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    textView.setText(textView.getText() + "\n" + new String(mensaje.getData()));
                }
            });
        }
    }

}
