package es.upv.master.sincronizaciondatos;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

public class MainActivity extends AppCompatActivity {
    private static final String WEAR_MANDAR_TEXTO = "/mandar_texto";
    private GoogleApiClient apiClient;
    private static final String WEAR_ARRANCAR_ACTIVIDAD="/arrancar_actividad";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        apiClient = new GoogleApiClient.Builder(this).addApi(Wearable.API).build();
        Button botonMandar = (Button) findViewById(R.id.botonMandar);
        botonMandar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText editText = (EditText) findViewById(R.id.editText);
                mandarMensaje(WEAR_MANDAR_TEXTO, editText.getText().toString());
            }
        });

        Button botonLanzar = (Button) findViewById(R.id.botonLanzar);
        botonLanzar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mandarMensaje(WEAR_ARRANCAR_ACTIVIDAD, "");
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        apiClient.connect();
    }

    @Override
    protected void onStop() {
        if (apiClient != null && apiClient.isConnected()) {
            apiClient.disconnect();
        }
        super.onStop();
    }

    private void mandarMensaje(final String path, final String texto) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                NodeApi.GetConnectedNodesResult nodos = Wearable.NodeApi.getConnectedNodes(apiClient).await();
                for (Node nodo : nodos.getNodes()) {
                    Wearable.MessageApi.sendMessage(apiClient, nodo.getId(), path, texto.getBytes()).setResultCallback(new ResultCallback<MessageApi.SendMessageResult>() {
                        @Override
                        public void onResult(MessageApi.SendMessageResult resultado) {
                            if (!resultado.getStatus().isSuccess()) {
                                Log.e("sincronizacion", "Error al mandar mensaje. Código:" + resultado.getStatus().getStatusCode());
                            }
                        }
                    });
                }
            }
        }).start();
    }
}
