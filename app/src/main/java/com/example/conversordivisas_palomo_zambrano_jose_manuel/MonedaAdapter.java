package com.example.conversordivisas_palomo_zambrano_jose_manuel;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class MonedaAdapter extends ArrayAdapter<String> {

    Context context;
    String[] monedas;

    public MonedaAdapter(Context context, String[] monedas) {
        super(context, R.layout.item_moneda, monedas);
        this.context = context;
        this.monedas = monedas;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return createItemView(position, convertView, parent);
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return createItemView(position, convertView, parent);
    }

    private View createItemView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_moneda, parent, false);
        }

        ImageView imgBandera = convertView.findViewById(R.id.imgBandera);
        TextView txtMoneda = convertView.findViewById(R.id.txtMoneda);

        String monedaActual = monedas[position];

        txtMoneda.setText(monedaActual);

        String nombreImagen = monedaActual.toLowerCase();

        int idImagen = context.getResources().getIdentifier(nombreImagen, "drawable", context.getPackageName());

        if (idImagen != 0) {
            imgBandera.setImageResource(idImagen);
        } else {
            imgBandera.setImageResource(android.R.drawable.ic_menu_help);
        }

        return convertView;
    }
}
