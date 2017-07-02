package es.upv.master.sincronizaciondatos;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Wearable;

public class MainActivity extends Activity implements MessageApi.MessageListener, DataApi.DataListener,
        GoogleApiClient.ConnectionCallbacks {

    private static final String WEAR_MANDAR_TEXTO = "/mandar_texto";
    private GoogleApiClient apiClient;
    private TextView textView;
    private static final String KEY_CONTADOR = "com.example.key.contador";
    private static final String ITEM_CONTADOR = "/contador";
    private int contador = 0;

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
        Wearable.DataApi.removeListener(apiClient, this);
        super.onStop();
    }


    @Override
    public void onConnected(Bundle bundle) {
        Wearable.MessageApi.addListener(apiClient, this);
        Wearable.DataApi.addListener(apiClient, this);
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onDataChanged(DataEventBuffer eventos) {
        for (DataEvent evento : eventos) {
            if (evento.getType() == DataEvent.TYPE_CHANGED) {
                DataItem item = evento.getDataItem();
                if (item.getUri().getPath().equals(ITEM_CONTADOR)) {
                    DataMap dataMap = DataMapItem.fromDataItem(item).getDataMap();
                    contador = dataMap.getInt(KEY_CONTADOR);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ((TextView) findViewById(R.id.textoContador)).setText(Integer.toString(contador));
                        }
                    });
                }
            } else if (evento.getType() == DataEvent.TYPE_DELETED) {
                // Algún ítem ha sido borrado
            }
        }
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
