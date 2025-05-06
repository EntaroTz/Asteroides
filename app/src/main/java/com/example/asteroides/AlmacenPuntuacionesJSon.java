package com.example.asteroides;

import java.util.List;
import java.util.ArrayList;
import java.util.Vector;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;
import android.content.SharedPreferences;
import android.content.Context;

public class AlmacenPuntuacionesJSon implements AlmacenPuntuaciones {
    private static final String PREFERENCIAS = "puntuaciones";
    private static final String KEY_PUNTUACIONES = "puntuaciones_key";

    private SharedPreferences sharedPreferences;

    public AlmacenPuntuacionesJSon(Context context) {
        sharedPreferences = context.getSharedPreferences(PREFERENCIAS, Context.MODE_PRIVATE);
    }

    @Override
    public void guardarPuntuacion(int puntos, String nombre, long fecha) {
        String jsonString = leerString();
        List<Puntuacion> puntuaciones = leerJSon(jsonString);
        puntuaciones.add(new Puntuacion(puntos, nombre, fecha));
        jsonString = guardarJSon(puntuaciones);
        guardarString(jsonString);
    }

    @Override
    public Vector<String> listaPuntuaciones(int cantidad) {
        String jsonString = leerString();
        List<Puntuacion> puntuaciones = leerJSon(jsonString);
        Vector<String> salida = new Vector<>();
        for (Puntuacion puntuacion: puntuaciones) {
            salida.add(puntuacion.getPuntos()+" "+puntuacion.getNombre());
        }
        return salida;
    }

    private String guardarJSon(List<Puntuacion> puntuaciones) {
        String jsonString = "";
        try {
            JSONArray jsonArray = new JSONArray();
            for (Puntuacion puntuacion : puntuaciones) {
                JSONObject objeto = new JSONObject();
                objeto.put("puntos", puntuacion.getPuntos());
                objeto.put("nombre", puntuacion.getNombre());
                objeto.put("fecha", puntuacion.getFecha());
                jsonArray.put(objeto);
            }
            jsonString = jsonArray.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonString;
    }

    private List<Puntuacion> leerJSon(String jsonString) {
        List<Puntuacion> puntuaciones = new ArrayList<>();
        try {
            JSONObject jsonObject = new JSONObject(jsonString);
            JSONArray json_array = jsonObject.getJSONArray("puntuaciones");
            for (int i = 0; i < json_array.length(); i++) {
                JSONObject objeto = json_array.getJSONObject(i);
                puntuaciones.add(new Puntuacion(objeto.getInt("puntos"),
                        objeto.getString("nombre"), objeto.getLong("fecha")));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return puntuaciones;
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
}
