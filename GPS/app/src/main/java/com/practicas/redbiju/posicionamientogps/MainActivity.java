package com.practicas.redbiju.posicionamientogps;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;


import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import org.w3c.dom.Text;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 1;
    TextView ubicacion1, ubicacion2;
    private FusedLocationProviderClient client;
    private LocationCallback locationCallback;
    double x, y;
    double casaX, casaY, trabajoX, trabajoY, facultadX, facultadY;
    boolean casaDeclarado = false;
    boolean trabajoDeclarado = false;
    boolean facultadDeclarado = false;
    ImageButton botonCasa, botonTrabajo, botonFacultad;
    String mtsCasa, mtsTrabajo, mtsFacultad;
    int estadoCasa, estadoTrabajo, estadoFacultad; // 0 = esperando señal GPS; 1 = estas a x metros; 2 = aun no se ha cargado una ubicacion; 3 = actualizando.
    int cantRojoCasa, cantVerdeCasa, cantRojoTrabajo, cantVerdeTrabajo, cantRojoFacultad, cantVerdeFacultad;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ubicacion1 = (TextView) findViewById(R.id.coordenada_X);
        ubicacion2 = (TextView) findViewById(R.id.coordenada_Y);

        botonCasa = (ImageButton) findViewById(R.id.botonCasa);
        botonTrabajo = (ImageButton) findViewById(R.id.botonTrabajo);
        botonFacultad = (ImageButton) findViewById(R.id.botonFacultad);

        botonCasa.setEnabled(false);
        botonTrabajo.setEnabled(false);
        botonFacultad.setEnabled(false);

        SharedPreferences preferences = getSharedPreferences("COORDENADAS", MODE_PRIVATE);


        casaX = preferences.getFloat("CASA_X", -1000);
        casaY = preferences.getFloat("CASA_Y", -1000);
        trabajoX = preferences.getFloat("TRABAJO_X", -1000);
        trabajoY = preferences.getFloat("TRABAJO_Y", -1000);
        facultadX = preferences.getFloat("FACULTAD_X", -1000);
        facultadY = preferences.getFloat("FACULTAD_Y", -1000);
        RelativeLayout layoutCasa = (RelativeLayout) findViewById(R.id.layoutCasa);
        RelativeLayout layoutTrabajo = (RelativeLayout) findViewById(R.id.layoutTrabajo);
        RelativeLayout layoutFacultad = (RelativeLayout) findViewById(R.id.layoutFacultad);

        desaparecerInicioOnCreate();

        if ((casaX != -1000) && (casaY != -1000)) {
            casaDeclarado = true;
            TextView sinUbicacionCasa = (TextView) findViewById(R.id.sinUbicacionCasa);
            sinUbicacionCasa.setText(R.string.esperando);
            estadoCasa = 0;
            layoutCasa.setBackgroundResource(R.color.esperandoUbicacion);

        } else {
            TextView sinUbicacionCasa = (TextView) findViewById(R.id.sinUbicacionCasa);
            sinUbicacionCasa.setText(R.string.sinUbicacion);
            estadoCasa = 2;

        }

        if ((trabajoX != -1000) && (trabajoY != -1000)) {
            trabajoDeclarado = true;
            TextView sinUbicacionTrabajo = (TextView) findViewById(R.id.sinUbicacionTrabajo);
            sinUbicacionTrabajo.setText(R.string.esperando);
            estadoTrabajo = 0;
            layoutTrabajo.setBackgroundResource(R.color.esperandoUbicacion);

        } else {
            TextView sinUbicacionTrabajo = (TextView) findViewById(R.id.sinUbicacionTrabajo);
            sinUbicacionTrabajo.setText(R.string.sinUbicacion);
            estadoTrabajo = 2;

        }

        if ((facultadX != -1000) && (facultadY != -1000)) {
            facultadDeclarado = true;
            TextView sinUbicacionFacultad = (TextView) findViewById(R.id.sinUbicacionFacultad);
            sinUbicacionFacultad.setText(R.string.esperando);
            estadoFacultad = 0;
            layoutFacultad.setBackgroundResource(R.color.esperandoUbicacion);

        } else {
            TextView sinUbicacionFacultad = (TextView) findViewById(R.id.sinUbicacionFacultad);
            sinUbicacionFacultad.setText(R.string.sinUbicacion);
            estadoFacultad = 2;

        }

    }


    @Override
    protected void onStart() {
        super.onStart();
        if (!chequearPermisosLocalizacion()) {
            pedirPermisosLocalizacion();
        } else {
            obtenerLocalizacion();

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // El usuario concedió el permiso, ya podemos usar la ubicación
                obtenerLocalizacion();
            }
        }
    }

    private boolean chequearPermisosLocalizacion() {
        int permissionState = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);
        return permissionState == PackageManager.PERMISSION_GRANTED;
    }

    private void pedirPermisosLocalizacion() {
        ActivityCompat.requestPermissions(MainActivity.this,
                new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                REQUEST_PERMISSIONS_REQUEST_CODE);
    }

    @SuppressWarnings("MissingPermission")
    public void obtenerLocalizacion() {

        client = LocationServices.getFusedLocationProviderClient(this);
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);

                float[] distancia_a_lugar = new float[1];

                x = locationResult.getLastLocation().getLongitude();
                y = locationResult.getLastLocation().getLatitude();


                ubicacion1.setText(Double.toString(x));
                ubicacion2.setText(Double.toString(y));

                botonCasa.setBackgroundResource(R.drawable.icono_mapa);
                botonTrabajo.setBackgroundResource(R.drawable.icono_mapa);
                botonFacultad.setBackgroundResource(R.drawable.icono_mapa);
                botonCasa.setEnabled(true);
                botonTrabajo.setEnabled(true);
                botonFacultad.setEnabled(true);

                if (casaDeclarado) {
                    Location.distanceBetween(x, y, casaX, casaY, distancia_a_lugar);

                    TextView sinUbicacionCasa = (TextView) findViewById(R.id.sinUbicacionCasa);
                    sinUbicacionCasa.setText("");

                    definirColorCasa(distancia_a_lugar[0]);

                    TextView estasA_casa = (TextView) findViewById(R.id.estasA_casa);
                    estasA_casa.setText(R.string.estas_a);
                    TextView distCasa = (TextView) findViewById(R.id.distCasa);
                    distCasa.setText(String.format("%.2f", (double) distancia_a_lugar[0]));
                    TextView metros_casa = (TextView) findViewById(R.id.metros_casa);
                    metros_casa.setText(R.string.metros);

                    mtsCasa = String.format("%.2f", (double) distancia_a_lugar[0]);
                    estadoCasa = 1;

                }
                if (trabajoDeclarado) {
                    Location.distanceBetween(x, y, trabajoX, trabajoY, distancia_a_lugar);

                    TextView sinUbicacionTrabajo = (TextView) findViewById(R.id.sinUbicacionTrabajo);
                    sinUbicacionTrabajo.setText("");

                    definirColorTrabajo(distancia_a_lugar[0]);

                    TextView estasA_trabajo = (TextView) findViewById(R.id.estasA_trabajo);
                    estasA_trabajo.setText(R.string.estas_a);
                    TextView distTrabajo = (TextView) findViewById(R.id.distTrabajo);
                    distTrabajo.setText(String.format("%.2f", (double) distancia_a_lugar[0]));
                    TextView metros_trabajo = (TextView) findViewById(R.id.metros_trabajo);
                    metros_trabajo.setText(R.string.metros);

                    mtsTrabajo = String.format("%.2f", (double) distancia_a_lugar[0]);
                    estadoTrabajo = 1;

                }
                if (facultadDeclarado) {
                    Location.distanceBetween(x, y, facultadX, facultadY, distancia_a_lugar);

                    TextView sinUbicacionFacultad = (TextView) findViewById(R.id.sinUbicacionFacultad);
                    sinUbicacionFacultad.setText("");

                    definirColorFacultad(distancia_a_lugar[0]);

                    TextView estasA_facultad = (TextView) findViewById(R.id.estasA_facultad);
                    estasA_facultad.setText(R.string.estas_a);
                    TextView distFacultad = (TextView) findViewById(R.id.distFacultad);
                    distFacultad.setText(String.format("%.2f", (double) distancia_a_lugar[0]));
                    TextView metros_facultad = (TextView) findViewById(R.id.metros_facultad);
                    metros_facultad.setText(R.string.metros);

                    mtsFacultad = String.format("%.2f", (double) distancia_a_lugar[0]);
                    estadoFacultad = 1;

                }
            }
        };


        LocationRequest request = LocationRequest.create()
                .setInterval(5 * 1000)
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        client.requestLocationUpdates(request, locationCallback, null);

        //Acá usamos el método de geolocalización que hayamos elegido
    }

    protected void definirCasa(View v) {
        SharedPreferences preferences = getSharedPreferences("COORDENADAS", MODE_PRIVATE);
        casaDeclarado = true;
        preferences.edit().putFloat("CASA_X", (float) x).apply();
        casaX = x;
        preferences.edit().putFloat("CASA_Y", (float) y).apply();
        casaY = y;


        estadoCasa = 3;
        RelativeLayout layoutCasa = (RelativeLayout) findViewById(R.id.layoutCasa);
        layoutCasa.setBackgroundColor(Color.rgb(0,255,0));
        TextView estasA_casa = (TextView) findViewById(R.id.estasA_casa);
        estasA_casa.setText(R.string.estas_a);
        TextView distCasa = (TextView) findViewById(R.id.distCasa);

        distCasa.setText("0.0");
        TextView metros_casa = (TextView) findViewById(R.id.metros_casa);
        metros_casa.setText(R.string.metros);

        TextView sinUbicacionCasa = (TextView) findViewById(R.id.sinUbicacionCasa);
        sinUbicacionCasa.setText("");
    }

    protected void definirTrabajo(View v) {
        SharedPreferences preferences = getSharedPreferences("COORDENADAS", MODE_PRIVATE);
        trabajoDeclarado = true;
        preferences.edit().putFloat("TRABAJO_X", (float) x).apply();
        trabajoX = x;
        preferences.edit().putFloat("TRABAJO_Y", (float) y).apply();
        trabajoY = y;


        estadoTrabajo = 3;

        RelativeLayout layoutTrabajo = (RelativeLayout) findViewById(R.id.layoutTrabajo);
        layoutTrabajo.setBackgroundColor(Color.rgb(0,255,0));
        TextView estasA_trabajo = (TextView) findViewById(R.id.estasA_trabajo);
        estasA_trabajo.setText(R.string.estas_a);
        TextView distTrabajo = (TextView) findViewById(R.id.distTrabajo);
        distTrabajo.setText("0.0");
        TextView metros_trabajo = (TextView) findViewById(R.id.metros_trabajo);
        metros_trabajo.setText(R.string.metros);


        TextView sinUbicacionTrabajo = (TextView) findViewById(R.id.sinUbicacionTrabajo);
        sinUbicacionTrabajo.setText("");
    }

    protected void definirFacultad(View v) {
        SharedPreferences preferences = getSharedPreferences("COORDENADAS", MODE_PRIVATE);
        facultadDeclarado = true;
        preferences.edit().putFloat("FACULTAD_X", (float) x).apply();
        facultadX = x;
        preferences.edit().putFloat("FACULTAD_Y", (float) y).apply();
        facultadY = y;


        estadoFacultad = 3;

        RelativeLayout layoutFacultad = (RelativeLayout) findViewById(R.id.layoutFacultad);
        layoutFacultad.setBackgroundColor(Color.rgb(0,255,0));
        TextView estasA_facultad = (TextView) findViewById(R.id.estasA_facultad);
        estasA_facultad.setText(R.string.estas_a);
        TextView distFacultad = (TextView) findViewById(R.id.distFacultad);
        distFacultad.setText("0.0");
        TextView metros_facultad = (TextView) findViewById(R.id.metros_facultad);
        metros_facultad.setText(R.string.metros);


        TextView sinUbicacionFacultad = (TextView) findViewById(R.id.sinUbicacionFacultad);
        sinUbicacionFacultad.setText("");
    }


    protected void definirColorCasa(float dist) {
        RelativeLayout layoutCasa = (RelativeLayout) findViewById(R.id.layoutCasa);
        float porcRojo = dist * 100 / 500;
        float cantRojoF = porcRojo * 255 / 100;
        cantRojoCasa = (int) cantRojoF;
        cantVerdeCasa = 255 - cantRojoCasa;

        if (dist == 0) {
            layoutCasa.setBackgroundColor(Color.rgb(0, 255, 0));
            cantRojoCasa = 0;
            cantVerdeCasa = 255;
        } else if (dist >= 500) {
            layoutCasa.setBackgroundColor(Color.rgb(255, 0, 0));
            cantRojoCasa = 255;
            cantVerdeCasa = 0;
        } else {
            layoutCasa.setBackgroundColor(Color.rgb(cantRojoCasa, cantVerdeCasa, 0));
        }

    }

    protected void definirColorTrabajo(float dist) {
        RelativeLayout layoutTrabajo = (RelativeLayout) findViewById(R.id.layoutTrabajo);
        float porcRojo = dist * 100 / 500;
        float cantRojoF = porcRojo * 255 / 100;
        cantRojoTrabajo = (int) cantRojoF;
        cantVerdeTrabajo = 255 - cantRojoTrabajo;

        if (dist == 0) {
            layoutTrabajo.setBackgroundColor(Color.rgb(0, 255, 0));
            cantRojoTrabajo = 0;
            cantVerdeTrabajo = 255;
        } else if (dist >= 500) {
            layoutTrabajo.setBackgroundColor(Color.rgb(255, 0, 0));
            cantRojoTrabajo = 255;
            cantVerdeTrabajo = 0;
        } else {
            layoutTrabajo.setBackgroundColor(Color.rgb(cantRojoTrabajo, cantVerdeTrabajo, 0));
        }

    }

    protected void definirColorFacultad(float dist) {
        RelativeLayout layoutFacultad = (RelativeLayout) findViewById(R.id.layoutFacultad);
        float porcRojo = dist * 100 / 500;
        float cantRojoF = porcRojo * 255 / 100;
        cantRojoFacultad = (int) cantRojoF;
        cantVerdeFacultad = 255 - cantRojoFacultad;

        if (dist == 0) {
            layoutFacultad.setBackgroundColor(Color.rgb(0, 255, 0));
            cantRojoFacultad = 0;
            cantVerdeFacultad = 255;
        } else if (dist >= 500) {
            layoutFacultad.setBackgroundColor(Color.rgb(255, 0, 0));
            cantRojoFacultad = 255;
            cantVerdeFacultad = 0;
        } else {
            layoutFacultad.setBackgroundColor(Color.rgb(cantRojoFacultad, cantVerdeFacultad, 0));
        }

    }


    protected void desaparecerInicioOnCreate() {
        TextView estasA_casa = (TextView) findViewById(R.id.estasA_casa);
        estasA_casa.setText("");
        TextView distCasa = (TextView) findViewById(R.id.distCasa);
        distCasa.setText("");
        TextView metros_casa = (TextView) findViewById(R.id.metros_casa);
        metros_casa.setText("");

        TextView estasA_trabajo = (TextView) findViewById(R.id.estasA_trabajo);
        estasA_trabajo.setText("");
        TextView distTrabajo = (TextView) findViewById(R.id.distTrabajo);
        distTrabajo.setText("");
        TextView metros_trabajo = (TextView) findViewById(R.id.metros_trabajo);
        metros_trabajo.setText("");

        TextView estasA_facultad = (TextView) findViewById(R.id.estasA_facultad);
        estasA_facultad.setText("");
        TextView distFacultad = (TextView) findViewById(R.id.distFacultad);
        distFacultad.setText("");
        TextView metros_facultad = (TextView) findViewById(R.id.metros_facultad);
        metros_facultad.setText("");
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        ubicacion1 = (TextView) findViewById(R.id.coordenada_X);
        ubicacion2 = (TextView) findViewById(R.id.coordenada_Y);
        String u1string = (String) ubicacion1.getText();
        String u2string = (String) ubicacion2.getText();
        outState.putString("VU1", u1string);
        outState.putString("VU2", u2string);

    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);


        ubicacion1 = (TextView) findViewById(R.id.coordenada_X);
        ubicacion2 = (TextView) findViewById(R.id.coordenada_Y);

        String u1string = savedInstanceState.getString("VU1");
        String u2string = savedInstanceState.getString("VU2");

        ubicacion1.setText(u1string);
        ubicacion2.setText(u2string);

        rotarCasa();
        rotarTrabajo();
        rotarFacultad();

    }

    protected void rotarCasa() {

        RelativeLayout layoutCasa = (RelativeLayout) findViewById(R.id.layoutCasa);
        TextView sinUbicacionCasa = (TextView) findViewById(R.id.sinUbicacionCasa);
        TextView estasA_casa = (TextView) findViewById(R.id.estasA_casa);
        TextView distCasa = (TextView) findViewById(R.id.distCasa);
        TextView metros_casa = (TextView) findViewById(R.id.metros_casa);

        if (estadoCasa == 0) {
            sinUbicacionCasa.setText(R.string.esperando);
            desaparecerInicioOnCreate();
            layoutCasa.setBackgroundResource(R.color.esperandoUbicacion);
        } else if (estadoCasa == 1) {
            sinUbicacionCasa.setText("");
            estasA_casa.setText(R.string.estas_a);
            distCasa.setText(mtsCasa);
            metros_casa.setText(R.string.metros);
            layoutCasa.setBackgroundColor(Color.rgb(cantRojoCasa, cantVerdeCasa, 0));

        } else if (estadoCasa == 2) {
            sinUbicacionCasa.setText(R.string.sinUbicacion);
            estasA_casa.setText("");
            distCasa.setText("");
            metros_casa.setText("");
            layoutCasa.setBackgroundResource(R.color.sinPosicion);
        } else if (estadoCasa == 3) {
            sinUbicacionCasa.setText(R.string.actualizando);
            estasA_casa.setText("");
            distCasa.setText("");
            metros_casa.setText("");
            layoutCasa.setBackgroundResource(R.color.esperandoUbicacion);

        }

    }

    protected void rotarTrabajo() {

        RelativeLayout layoutTrabajo = (RelativeLayout) findViewById(R.id.layoutTrabajo);
        TextView sinUbicacionTrabajo = (TextView) findViewById(R.id.sinUbicacionTrabajo);
        TextView estasA_trabajo = (TextView) findViewById(R.id.estasA_trabajo);
        TextView distTrabajo = (TextView) findViewById(R.id.distTrabajo);
        TextView metros_trabajo = (TextView) findViewById(R.id.metros_trabajo);

        if (estadoTrabajo == 0) {
            sinUbicacionTrabajo.setText(R.string.esperando);
            desaparecerInicioOnCreate();
            layoutTrabajo.setBackgroundResource(R.color.esperandoUbicacion);
        } else if (estadoTrabajo == 1) {
            sinUbicacionTrabajo.setText("");
            estasA_trabajo.setText(R.string.estas_a);
            distTrabajo.setText(mtsTrabajo);
            metros_trabajo.setText(R.string.metros);
            layoutTrabajo.setBackgroundColor(Color.rgb(cantRojoTrabajo, cantVerdeTrabajo, 0));

        } else if (estadoTrabajo == 2) {
            sinUbicacionTrabajo.setText(R.string.sinUbicacion);
            estasA_trabajo.setText("");
            distTrabajo.setText("");
            metros_trabajo.setText("");
            layoutTrabajo.setBackgroundResource(R.color.sinPosicion);
        } else if (estadoTrabajo == 3) {
            sinUbicacionTrabajo.setText(R.string.actualizando);
            estasA_trabajo.setText("");
            distTrabajo.setText("");
            metros_trabajo.setText("");
            layoutTrabajo.setBackgroundResource(R.color.esperandoUbicacion);

        }

    }

    protected void rotarFacultad() {

        RelativeLayout layoutFacultad = (RelativeLayout) findViewById(R.id.layoutFacultad);
        TextView sinUbicacionFacultad = (TextView) findViewById(R.id.sinUbicacionFacultad);
        TextView estasA_facultad = (TextView) findViewById(R.id.estasA_facultad);
        TextView distFacultad = (TextView) findViewById(R.id.distFacultad);
        TextView metros_facultad = (TextView) findViewById(R.id.metros_facultad);

        if (estadoFacultad == 0) {
            sinUbicacionFacultad.setText(R.string.esperando);
            desaparecerInicioOnCreate();
            layoutFacultad.setBackgroundResource(R.color.esperandoUbicacion);
        } else if (estadoFacultad == 1) {
            sinUbicacionFacultad.setText("");
            estasA_facultad.setText(R.string.estas_a);
            distFacultad.setText(mtsFacultad);
            metros_facultad.setText(R.string.metros);
            layoutFacultad.setBackgroundColor(Color.rgb(cantRojoTrabajo, cantVerdeTrabajo, 0));

        } else if (estadoFacultad == 2) {
            sinUbicacionFacultad.setText(R.string.sinUbicacion);
            estasA_facultad.setText("");
            distFacultad.setText("");
            metros_facultad.setText("");
            layoutFacultad.setBackgroundResource(R.color.sinPosicion);
        } else if (estadoFacultad == 3) {
            sinUbicacionFacultad.setText(R.string.actualizando);
            estasA_facultad.setText("");
            distFacultad.setText("");
            metros_facultad.setText("");
            layoutFacultad.setBackgroundResource(R.color.esperandoUbicacion);

        }


    }
}
