package com.example.conversordivisas_palomo_zambrano_jose_manuel;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

// Imports de librerías externas
import com.bumptech.glide.Glide;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

// Imports de Java
import org.json.JSONException;
import org.json.JSONObject;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Iterator;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    // Variables de interfaz
    EditText etCantidad;
    Spinner spOrigen, spDestino;
    Button btnConvertir;
    TextView tvResultado;
    ImageButton btnSwap;
    ImageView imgGrafico;
    TextView txtBtcPrecio, txtBtcCambio;
    TextView txtEthPrecio, txtEthCambio;
    TextView txtSolPrecio, txtSolCambio;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 1. Vinculación
        etCantidad = findViewById(R.id.etCantidad);
        spOrigen = findViewById(R.id.spOrigen);
        spDestino = findViewById(R.id.spDestino);
        btnConvertir = findViewById(R.id.btnConvertir);
        tvResultado = findViewById(R.id.tvResultado);
        btnSwap = findViewById(R.id.btnSwap);
        imgGrafico = findViewById(R.id.imgGrafico);
// ... tus otros findViewById ...
        txtBtcPrecio = findViewById(R.id.txtBtcPrecio);
        txtBtcCambio = findViewById(R.id.txtBtcCambio);
        txtEthPrecio = findViewById(R.id.txtEthPrecio);
        txtEthCambio = findViewById(R.id.txtEthCambio);
        txtSolPrecio = findViewById(R.id.txtSolPrecio);
        txtSolCambio = findViewById(R.id.txtSolCambio);

        // LLAMADA A LA NUEVA FUNCIÓN
        obtenerCripto();


        // 2. Adaptador de Banderas
        String[] listaMonedas = getResources().getStringArray(R.array.monedas_array);
        MonedaAdapter adapter = new MonedaAdapter(this, listaMonedas);
        spOrigen.setAdapter(adapter);
        spDestino.setAdapter(adapter);

        // Seleccionar moneda distinta por defecto
        spDestino.setSelection(1);

        // 3. Botón Convertir
        btnConvertir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (etCantidad.getText().toString().isEmpty()) {
                    Toast.makeText(MainActivity.this, "Introduce una cantidad", Toast.LENGTH_SHORT).show();
                } else {
                    consultarApi();
                }
            }
        });

        // 4. Botón Swap (Intercambio)
        btnSwap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int posOrigen = spOrigen.getSelectedItemPosition();
                int posDestino = spDestino.getSelectedItemPosition();

                spOrigen.setSelection(posDestino);
                spDestino.setSelection(posOrigen);

                // Si ya hay número, recalcular automáticamente
                if (!etCantidad.getText().toString().isEmpty()) {
                    consultarApi();
                }
            }
        });
    }

    // --- MENÚ SUPERIOR (HISTORIAL) ---
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_historial) {
            startActivity(new Intent(this, HistorialActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // --- LÓGICA PRINCIPAL ---

    private void consultarApi() {
        // Usamos Volley para pedir datos a la API
        String url = "https://api.exchangerate-api.com/v4/latest/USD";
        RequestQueue queue = Volley.newRequestQueue(this);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            // A. Cálculos matemáticos
                            JSONObject rates = response.getJSONObject("rates");
                            String mOrigen = spOrigen.getSelectedItem().toString();
                            String mDestino = spDestino.getSelectedItem().toString();

                            double tOrigen = rates.getDouble(mOrigen);
                            double tDestino = rates.getDouble(mDestino);
                            double cantidad = Double.parseDouble(etCantidad.getText().toString());

                            double resultado = (cantidad / tOrigen) * tDestino;

                            // B. Mostrar Resultado
                            String textoResultado = String.format("%.2f %s", resultado, mDestino);
                            tvResultado.setText(textoResultado);

                            // C. Guardar en Historial
                            String textoHistorial = String.format("%.2f %s ➝ %s", cantidad, mOrigen, textoResultado);
                            guardarEnHistorial(textoHistorial);

                            // D. Cargar el Gráfico (NUEVO)
                            cargarGrafico(mOrigen, mDestino);

                        } catch (JSONException e) {
                            Toast.makeText(MainActivity.this, "Error de datos", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(MainActivity.this, "Error de conexión", Toast.LENGTH_SHORT).show();
                    }
                });

        queue.add(request);
    }
    private void obtenerCripto() {
        String url = "https://api.coingecko.com/api/v3/simple/price?ids=bitcoin,ethereum,solana&vs_currencies=usd&include_24hr_change=true";

        RequestQueue queue = Volley.newRequestQueue(this);
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            // 1. Procesar BITCOIN
                            actualizarDatoCripto(response, "bitcoin", txtBtcPrecio, txtBtcCambio);

                            // 2. Procesar ETHEREUM
                            actualizarDatoCripto(response, "ethereum", txtEthPrecio, txtEthCambio);

                            // 3. Procesar SOLANA
                            actualizarDatoCripto(response, "solana", txtSolPrecio, txtSolCambio);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // Si falla, dejamos los "..."
                    }
                });

        queue.add(request);
    }

    // Método auxiliar para no repetir código 3 veces
    private void actualizarDatoCripto(JSONObject respuesta, String idMoneda, TextView tvPrecio, TextView tvCambio) throws JSONException {
        JSONObject datos = respuesta.getJSONObject(idMoneda);
        double precio = datos.getDouble("usd");
        double cambio = datos.getDouble("usd_24h_change");

        // Formato precio: $96,000.00
        tvPrecio.setText(String.format("$%,.2f", precio));

        // Formato cambio: +2.5% o -1.2%
        tvCambio.setText(String.format("%.2f%%", cambio));

        // Color según suba o baje
        if (cambio >= 0) {
            tvCambio.setTextColor(0xFF00C853); // Verde (Color Hex manual para no liar con resources)
            tvCambio.setText("▲ " + tvCambio.getText());
        } else {
            tvCambio.setTextColor(0xFFD32F2F); // Rojo
            tvCambio.setText("▼ " + tvCambio.getText());
        }
    }
    private void cargarGrafico(String monedaBase, String monedaDestino) {
        // 1. Calcular fechas (Hoy y hace 30 días)
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        Calendar cal = Calendar.getInstance();
        String hoy = sdf.format(cal.getTime());
        cal.add(Calendar.DAY_OF_YEAR, -30);
        String haceUnMes = sdf.format(cal.getTime());

        // 2. URL de la API Histórica (Frankfurter)
        String url = "https://api.frankfurter.app/" + haceUnMes + ".." + hoy +
                "?from=" + monedaBase + "&to=" + monedaDestino;

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONObject rates = response.getJSONObject("rates");

                            // Construimos los datos para el gráfico
                            StringBuilder labels = new StringBuilder();
                            StringBuilder data = new StringBuilder();

                            Iterator<String> keys = rates.keys();
                            while(keys.hasNext()) {
                                String fecha = keys.next();
                                JSONObject dia = rates.getJSONObject(fecha);
                                double valor = dia.getDouble(monedaDestino);

                                labels.append("'").append(fecha.substring(5)).append("',"); // MM-DD
                                data.append(valor).append(",");
                            }

                            // 3. URL de QuickChart
                            String chartUrl = "https://quickchart.io/chart?c={" +
                                    "type:'line'," +
                                    "data:{" +
                                    "labels:[" + labels + "]," +
                                    "datasets:[{" +
                                    "label:'Evolución " + monedaBase + " vs " + monedaDestino + "'," +
                                    "data:[" + data + "]," +
                                    "borderColor:'blue'," +
                                    "fill:false," +
                                    "pointRadius:0" +
                                    "}]" +
                                    "}" +
                                    "}";

                            // 4. Mostrar imagen con GLIDE
                            imgGrafico.setVisibility(View.VISIBLE);
                            Glide.with(MainActivity.this).load(chartUrl).into(imgGrafico);

                        } catch (JSONException e) {
                            // Si la API falla (ej: moneda no soportada), ocultamos el gráfico
                            imgGrafico.setVisibility(View.GONE);
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        imgGrafico.setVisibility(View.GONE);
                    }
                });

        Volley.newRequestQueue(this).add(request);
    }

    private void guardarEnHistorial(String texto) {
        SharedPreferences prefs = getSharedPreferences("MisDatos", MODE_PRIVATE);
        String historial = prefs.getString("historial_string", "");
        // Concatenamos lo nuevo al final con un punto y coma
        prefs.edit().putString("historial_string", historial + texto + ";").apply();
    }
}