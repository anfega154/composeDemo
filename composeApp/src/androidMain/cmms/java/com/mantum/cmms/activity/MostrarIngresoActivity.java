package com.mantum.cmms.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import com.mantum.demo.R;
import com.mantum.component.Mantum;
import android.widget.TextView;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import android.graphics.Color;

public class MostrarIngresoActivity extends Mantum.Activity {

    private TextView identificacion;
    private TextView nombre;
    private TextView empresa;
    private TextView marca;
    private TextView instalacionLocativa;
    private TextView fechaIngreso;
    private TextView codigo;
    private TextView tipo;

    private TextView fechaInicio;
    private TextView fechaFin;
    private TextView estadoIngreso;

    private final String TITLE_BACK = "Información de ingreso";
    private final String ACTIVO = "Autorización activa";
    private final String INACTIVO = "Autorización inactiva";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mostrar_ingreso);

        identificacion = findViewById(R.id.identificacion);
        nombre = findViewById(R.id.nombre);
        empresa = findViewById(R.id.empresa);
        marca = findViewById(R.id.marca);
        instalacionLocativa = findViewById(R.id.instalacion_locativa);
        fechaIngreso = findViewById(R.id.fecha_ingreso);
        codigo = findViewById(R.id.codigo);
        tipo = findViewById(R.id.tipo);
        fechaInicio = findViewById(R.id.fecha_inicio);
        fechaFin = findViewById(R.id.fecha_fin);
        estadoIngreso = findViewById(R.id.estado_ingreso);

        Intent intent = getIntent();
        identificacion.setText(intent.getStringExtra("identificacion"));
        nombre.setText(intent.getStringExtra("nombre"));
        empresa.setText(intent.getStringExtra("empresa"));
        marca.setText(intent.getStringExtra("marca"));
        instalacionLocativa.setText(intent.getStringExtra("instalacionLocativa"));
        fechaIngreso.setText(intent.getStringExtra("fechaIngreso"));
        codigo.setText(intent.getStringExtra("codigo"));
        tipo.setText(intent.getStringExtra("tipo"));
        fechaInicio.setText(intent.getStringExtra("fechaInicio"));
        fechaFin.setText(intent.getStringExtra("fechaFin"));

        String fechaInicioAut = intent.getStringExtra("fechaInicio");
        String fechaFinAut = intent.getStringExtra("fechaFin");

        updateEstadoIngreso(fechaInicioAut, fechaFinAut);


        includeBackButtonAndTitle(TITLE_BACK);
    }

    private void updateEstadoIngreso(String fechaInicio, String fechaFin) {
        String estado = dateNow(fechaInicio, fechaFin);

        estadoIngreso.setText(estado);

        if (ACTIVO.equals(estado)) {
            estadoIngreso.setTextColor(Color.parseColor("#33D349"));
        } else if (INACTIVO.equals(estado)) {
            estadoIngreso.setTextColor(Color.parseColor("#FF0000"));
        }
    }

    private String dateNow(String fechaInicio, String fechaFin){
    Date currentDate = new Date();
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    try {
        Date startDate = sdf.parse(fechaInicio);
        Date endDate = sdf.parse(fechaFin);

        if(currentDate.after(startDate) && currentDate.before(endDate)){
            return ACTIVO;
        }
        return INACTIVO;
    } catch (ParseException e) {
        e.printStackTrace();
        return "Error al validar la fecha de la autorización";
    }
}

    public void includeBackButtonAndTitle(String title) {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(true);
            setTitle(title);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(menuItem);
    }
}