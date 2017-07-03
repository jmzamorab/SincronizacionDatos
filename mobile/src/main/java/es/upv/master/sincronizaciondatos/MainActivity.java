package es.upv.master.sincronizaciondatos;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

import java.io.ByteArrayOutputStream;
import java.util.Date;

public class MainActivity extends AppCompatActivity {
    private static final String WEAR_MANDAR_TEXTO = "/mandar_texto";
    private GoogleApiClient apiClient;
    private static final String WEAR_ARRANCAR_ACTIVIDAD = "/arrancar_actividad";
    private static final String KEY_CONTADOR = "com.example.key.contador";
    private static final String ITEM_CONTADOR = "/contador";
    private int contador = 0;
    private static final String ITEM_FOTO = "/item_foto";
    private static final String ASSET_FOTO = "/asset_foto";

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

        final TextView textoContador = (TextView) findViewById(R.id.textoContador);
        textoContador.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                contador++;
                textoContador.setText(Integer.toString(contador));
                PutDataMapRequest putDataMapReq = PutDataMapRequest.create(ITEM_CONTADOR);
                putDataMapReq.getDataMap().putInt(KEY_CONTADOR, contador);
                PutDataRequest putDataReq = putDataMapReq.asPutDataRequest();
                PendingResult<DataApi.DataItemResult> resultado = Wearable.DataApi.putDataItem(apiClient, putDataReq);
            }
        });

        Button botonFoto = (Button) findViewById(R.id.botonFoto);
        botonFoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mandarFoto();
            }
        });
    }

    private void mandarFoto() {
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.foto);
        Asset asset = createAssetFromBitmap(bitmap);
        PutDataMapRequest putDataMapReq = PutDataMapRequest.create(ITEM_FOTO);
        putDataMapReq.getDataMap().putAsset(ASSET_FOTO, asset);
        putDataMapReq.getDataMap().putLong("marca_de_tiempo", new Date().getTime());
        PutDataRequest request = putDataMapReq.asPutDataRequest();
        PendingResult<DataApi.DataItemResult> resultdado = Wearable.DataApi.putDataItem(apiClient, request);
    }

    private static Asset createAssetFromBitmap(Bitmap bitmap) {
        final ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteStream);
        return Asset.createFromBytes(byteStream.toByteArray());
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
