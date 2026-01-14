package com.example.conversordivisas_palomo_zambrano_jose_manuel; //

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

    EditText etDolares;
    Spinner spMoneda;
    Button btnConvertir;
    TextView tvResultado;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etDolares = findViewById(R.id.etDolares);
        spMoneda = findViewById(R.id.spMoneda);
        btnConvertir = findViewById(R.id.btnConvertir);
        tvResultado = findViewById(R.id.tvResultado);

        btnConvertir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (etDolares.getText().toString().isEmpty()) {
                    Toast.makeText(MainActivity.this, "Por favor escribe una cantidad", Toast.LENGTH_SHORT).show();
                } else {
                    consultarApi();
                }
            }
        });
    }

    private void consultarApi() {
        String url = "https://api.exchangerate-api.com/v4/latest/USD";

        RequestQueue queue = Volley.newRequestQueue(this);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONObject rates = response.getJSONObject("rates");

                            String monedaDestino = spMoneda.getSelectedItem().toString();

                            double valorCambio = rates.getDouble(monedaDestino);

                            double cantidadDolares = Double.parseDouble(etDolares.getText().toString());

                            double resultadoFinal = cantidadDolares * valorCambio;

                            tvResultado.setText(String.format("%.2f %s", resultadoFinal, monedaDestino));

                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(MainActivity.this, "Error al leer los datos", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // AQUÍ LLEGA SI HAY ERROR (No internet, servidor caído, etc.)
                        Toast.makeText(MainActivity.this, "Error de conexión", Toast.LENGTH_SHORT).show();
                    }
                });


        queue.add(request);
    }
}