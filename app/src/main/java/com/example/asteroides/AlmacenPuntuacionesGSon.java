package com.example.asteroides;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class AlmacenPuntuacionesGSon implements AlmacenPuntuaciones {
    private static final String PREF_NAME = "puntuaciones_pref";
    private static final String KEY_PUNTUACIONES = "puntuaciones";
    private Gson gson = new Gson();
    private Type type = new TypeToken<Clase>() {}.getType();
    private SharedPreferences sharedPreferences;

    public AlmacenPuntuacionesGSon(Context context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        guardarPuntuacion(45000, "Mi nombre", System.currentTimeMillis());
        guardarPuntuacion(31000, "Otro nombre", System.currentTimeMillis());
    }

    @Override
    public void guardarPuntuacion(int puntos, String nombre, long fecha) {
        Clase clase = new Clase();
        String jsonString = leerString();
        if (jsonString != null) {
            Clase storedClass = gson.fromJson(jsonString, Clase.class);
            clase.puntuaciones = storedClass.puntuaciones;
        }
        clase.puntuaciones.add(new Puntuacion(puntos, nombre, fecha));
        jsonString = gson.toJson(clase);
        guardarString(jsonString);
    }

    @Override
    public Vector<String> listaPuntuaciones(int cantidad) {
        String jsonString = leerString();
        List<Puntuacion> puntuaciones;
        if (jsonString == null) {
            puntuaciones = new ArrayList<>();
        } else {
            Type type = new TypeToken<List<Puntuacion>>() {}.getType();
            puntuaciones = gson.fromJson(jsonString, type);
        }
        Vector<String> salida = new Vector<>();
        for (Puntuacion puntuacion : puntuaciones) {
            salida.add(puntuacion.getPuntos() + " " + puntuacion.getNombre());
        }
        return salida;
    }

    private void guardarString(String jsonString) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_PUNTUACIONES, jsonString);
        editor.apply();
    }

    private String leerString() {
        String jsonString = sharedPreferences.getString(KEY_PUNTUACIONES, null);
        if (jsonString == null) {
            return null;
        }
        if (jsonString.startsWith("[")) {
            return "{\"puntuaciones\":" + jsonString + "}";
        }
        return jsonString;
    }

    public class Clase {
        private List<Puntuacion> puntuaciones = new ArrayList<>();
        private boolean guardado = false;
    }
}
