package es.upv.master.sincronizaciondatos;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.wearable.view.BoxInsetLayout;
import android.util.Log;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataItemBuffer;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Wearable;

import java.io.InputStream;
import java.util.concurrent.TimeUnit;

public class MainActivity extends Activity implements MessageApi.MessageListener, DataApi.DataListener,
        GoogleApiClient.ConnectionCallbacks {

    private static final String WEAR_MANDAR_TEXTO = "/mandar_texto";
    private GoogleApiClient apiClient;
    private TextView textView;
    private static final String KEY_CONTADOR = "com.example.key.contador";
    private static final String ITEM_CONTADOR = "/contador";
    private int contador = 0;
    private static final String ITEM_FOTO = "/item_foto";
    private static final String ASSET_FOTO = "/asset_foto";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView = (TextView) findViewById(R.id.textView);
        apiClient = new GoogleApiClient.Builder(this).addApi(Wearable.API).addConnectionCallbacks(this).build();

        PendingResult<DataItemBuffer> resultado = Wearable.DataApi.getDataItems(apiClient);
        resultado.setResultCallback(new ResultCallback<DataItemBuffer>() {
            @Override
            public void onResult(DataItemBuffer dataItems) {
                for (DataItem dataItem : dataItems) {
                    if (dataItem.getUri().getPath().equals(ITEM_CONTADOR)) {
                        DataMapItem dataMapItem = DataMapItem.fromDataItem(dataItem);
                        contador = dataMapItem.getDataMap().getInt(KEY_CONTADOR);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                ((TextView) findViewById(R.id.textoContador)).setText(Integer.toString(contador));
                            }
                        });
                    }
                }
                dataItems.release();
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
                } else if (item.getUri().getPath().equals(ITEM_FOTO)) {
                    DataMapItem dataMapItem = DataMapItem.fromDataItem(item);
                    Asset asset = dataMapItem.getDataMap().getAsset(ASSET_FOTO);
                    LoadBitmapFromAsset tarea = new LoadBitmapFromAsset();
                    tarea.execute(asset);
                }
            }
        }
    }

    class LoadBitmapFromAsset extends AsyncTask<Asset, Void, Bitmap> {
        private static final int TIMEOUT_MS = 2000;

        @Override
        protected Bitmap doInBackground(Asset... assets) {
            if (assets.length < 1 || assets[0] == null) {
                throw new IllegalArgumentException("El asset no puede ser null");
            }

            ConnectionResult resultado = apiClient.blockingConnect(TIMEOUT_MS, TimeUnit.MILLISECONDS);
            if (!resultado.isSuccess()) {
                return null;
            } // convertimos el asset en Stream, bloqueando hasta tenerlo
            InputStream assetInputStream = Wearable.DataApi.getFdForAsset(apiClient, assets[0]).await().getInputStream();
            if (assetInputStream == null) {
                Log.w("Sincronización", "Asset desconocido");
                return null;
            } // decodificamos el Stream en un Bitmap
            return BitmapFactory.decodeStream(assetInputStream);
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            ((BoxInsetLayout) findViewById(R.id.boxInsert)).setBackground(new BitmapDrawable(getResources(), bitmap));
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
