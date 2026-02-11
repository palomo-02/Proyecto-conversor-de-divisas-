package com.example.conversordivisas_palomo_zambrano_jose_manuel;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class HistorialActivity extends AppCompatActivity {

    ListView listaHistorial;
    Button btnBorrarHistorial; // Declaramos el botón nuevo
    ArrayAdapter<String> adapter;
    List<String> listaDatos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_historial);

        // Configuración de la barra superior (flecha atrás)
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Historial");
        }

        // 1. Vinculamos
        listaHistorial = findViewById(R.id.listaHistorial);
        btnBorrarHistorial = findViewById(R.id.btnBorrarHistorial); // <--- El botón rojo

        // 2. Cargamos los datos
        cargarHistorial();

        // 3. Damos vida al botón rojo
        btnBorrarHistorial.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Comprobamos si hay algo que borrar para no preguntar a lo tonto
                if (listaDatos.size() == 1 && listaDatos.get(0).equals("No hay conversiones recientes")) {
                    Toast.makeText(HistorialActivity.this, "El historial ya está vacío", Toast.LENGTH_SHORT).show();
                } else {
                    confirmarBorrado();
                }
            }
        });
    }

    // Botón Atrás de la barra superior
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // --- MÉTODOS DE LÓGICA (IGUAL QUE ANTES) ---

    private void cargarHistorial() {
        SharedPreferences prefs = getSharedPreferences("MisDatos", MODE_PRIVATE);
        String historialLargo = prefs.getString("historial_string", "");

        if (historialLargo.isEmpty()) {
            listaDatos = new ArrayList<>();
            listaDatos.add("No hay conversiones recientes");
        } else {
            String[] arrayDatos = historialLargo.split(";");
            listaDatos = new ArrayList<>(Arrays.asList(arrayDatos));
            Collections.reverse(listaDatos);
        }

        adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                listaDatos
        );
        listaHistorial.setAdapter(adapter);
    }

    private void confirmarBorrado() {
        new AlertDialog.Builder(this)
                .setTitle("¿Estás seguro?")
                .setMessage("Vas a eliminar todo el registro de conversiones.")
                .setPositiveButton("BORRAR", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        borrarTodo();
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void borrarTodo() {
        SharedPreferences prefs = getSharedPreferences("MisDatos", MODE_PRIVATE);
        prefs.edit().clear().apply();

        listaDatos.clear();
        listaDatos.add("No hay conversiones recientes");
        adapter.notifyDataSetChanged();

        Toast.makeText(this, "Historial eliminado", Toast.LENGTH_SHORT).show();
    }
}