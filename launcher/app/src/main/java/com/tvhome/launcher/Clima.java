package com.tvhome.launcher;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/** Clima da cidade de Config pelo Open-Meteo (grátis, sem chave). Guarda a última resposta para funcionar sem internet. */
final class Clima {
    private static final String URL_CLIMA = "https://api.open-meteo.com/v1/forecast"
            + "?latitude=" + Config.LATITUDE + "&longitude=" + Config.LONGITUDE
            + "&current=temperature_2m,weather_code,is_day"
            + "&daily=temperature_2m_max,temperature_2m_min,weather_code"
            + "&timezone=America%2FSao_Paulo&forecast_days=4";

    static final class Dia {
        String data;
        int max, min, codigo;
    }

    int temperatura = Integer.MIN_VALUE;
    int codigo;
    boolean dia = true;
    final Dia[] proximos = new Dia[3];

    private final SharedPreferences prefs;

    Clima(Context c) {
        prefs = c.getSharedPreferences("clima", Context.MODE_PRIVATE);
        String guardado = prefs.getString("json", null);
        if (guardado != null) ler(guardado);
    }

    boolean temDados() { return temperatura != Integer.MIN_VALUE; }

    /** Roda fora da thread principal. */
    boolean atualizar() {
        HttpURLConnection con = null;
        try {
            con = (HttpURLConnection) new URL(URL_CLIMA).openConnection();
            con.setConnectTimeout(8000);
            con.setReadTimeout(8000);
            if (con.getResponseCode() != 200) return false;
            InputStream in = con.getInputStream();
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buf = new byte[8192];
            int n;
            while ((n = in.read(buf)) > 0) out.write(buf, 0, n);
            String json = out.toString("UTF-8");
            if (!ler(json)) return false;
            prefs.edit().putString("json", json).apply();
            return true;
        } catch (Exception e) {
            return false;
        } finally {
            if (con != null) con.disconnect();
        }
    }

    private boolean ler(String json) {
        try {
            JSONObject j = new JSONObject(json);
            JSONObject atual = j.getJSONObject("current");
            JSONObject d = j.getJSONObject("daily");
            JSONArray datas = d.getJSONArray("time"), max = d.getJSONArray("temperature_2m_max"),
                    min = d.getJSONArray("temperature_2m_min"), cod = d.getJSONArray("weather_code");
            for (int i = 0; i < 3 && i + 1 < datas.length(); i++) {
                Dia dia = new Dia();
                dia.data = datas.getString(i + 1);
                dia.max = (int) Math.round(max.getDouble(i + 1));
                dia.min = (int) Math.round(min.getDouble(i + 1));
                dia.codigo = cod.getInt(i + 1);
                proximos[i] = dia;
            }
            temperatura = (int) Math.round(atual.getDouble("temperature_2m"));
            codigo = atual.getInt("weather_code");
            dia = atual.optInt("is_day", 1) == 1;
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
