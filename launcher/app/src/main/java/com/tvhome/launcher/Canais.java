package com.tvhome.launcher;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Canais gratuitos (lista pública iptv-org, Brasil), escolhidos e testados em assets/canais.json.
 * Uma vez por dia o endereço de cada canal é atualizado pela lista pública, casando pelo tvg-id.
 */
final class Canais {
    private static final String LISTA = "https://iptv-org.github.io/iptv/countries/br.m3u";

    static final class Canal {
        String categoria, nome, logo, id, url;
        String app, link; // canal que abre outro app (Globoplay, YouTube)
        final Map<String, String> cabecalhos = new HashMap<>();
    }

    final List<Canal> todos = new ArrayList<>();
    final LinkedHashMap<String, List<Canal>> porCategoria = new LinkedHashMap<>();
    private final Context ctx;

    Canais(Context c) {
        ctx = c;
        try (InputStream in = c.getAssets().open("canais.json")) {
            JSONArray a = new JSONArray(ler(in));
            Map<String, String> novos = enderecosAtualizados();
            for (int i = 0; i < a.length(); i++) {
                JSONObject o = a.getJSONObject(i);
                Canal k = new Canal();
                k.categoria = o.getString("cat");
                k.nome = o.getString("nome");
                k.logo = o.optString("logo", "");
                k.id = o.optString("id", "");
                k.url = o.optString("url", null);
                k.app = o.optString("app", null);
                k.link = o.optString("link", null);
                if (k.app == null && !k.id.isEmpty() && novos.containsKey(k.id)) k.url = novos.get(k.id);
                JSONObject h = o.optJSONObject("h");
                if (h != null) {
                    Iterator<String> it = h.keys();
                    while (it.hasNext()) {
                        String chave = it.next();
                        k.cabecalhos.put(chave, h.getString(chave));
                    }
                }
                todos.add(k);
                List<Canal> l = porCategoria.get(k.categoria);
                if (l == null) porCategoria.put(k.categoria, l = new ArrayList<>());
                l.add(k);
            }
        } catch (Exception ignored) {}
    }

    private File arquivoAtual() { return new File(ctx.getFilesDir(), "enderecos.json"); }

    private Map<String, String> enderecosAtualizados() {
        Map<String, String> m = new HashMap<>();
        try (InputStream in = new java.io.FileInputStream(arquivoAtual())) {
            JSONObject o = new JSONObject(ler(in));
            Iterator<String> it = o.keys();
            while (it.hasNext()) {
                String k = it.next();
                m.put(k, o.getString(k));
            }
        } catch (Exception ignored) {}
        return m;
    }

    /** Roda fora da thread principal; no máximo uma vez por dia. */
    void atualizarSePreciso() {
        SharedPreferences p = ctx.getSharedPreferences("canais", Context.MODE_PRIVATE);
        if (System.currentTimeMillis() - p.getLong("em", 0) < 24 * 3600_000L) return;
        HttpURLConnection con = null;
        try {
            con = (HttpURLConnection) new URL(LISTA).openConnection();
            con.setConnectTimeout(10000);
            con.setReadTimeout(15000);
            if (con.getResponseCode() != 200) return;
            String m3u = ler(con.getInputStream());
            JSONObject saida = new JSONObject();
            Pattern pid = Pattern.compile("tvg-id=\"([^\"]+)\"");
            String[] linhas = m3u.split("\n");
            for (int i = 0; i < linhas.length; i++) {
                if (!linhas[i].startsWith("#EXTINF")) continue;
                Matcher mt = pid.matcher(linhas[i]);
                if (!mt.find()) continue;
                for (int j = i + 1; j < Math.min(i + 4, linhas.length); j++) {
                    String u = linhas[j].trim();
                    if (!u.isEmpty() && !u.startsWith("#")) { saida.put(mt.group(1), u); break; }
                }
            }
            try (FileOutputStream out = new FileOutputStream(arquivoAtual())) {
                out.write(saida.toString().getBytes("UTF-8"));
            }
            p.edit().putLong("em", System.currentTimeMillis()).apply();
        } catch (Exception ignored) {
        } finally {
            if (con != null) con.disconnect();
        }
    }

    static String ler(InputStream in) throws java.io.IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buf = new byte[16384];
        int n;
        while ((n = in.read(buf)) > 0) out.write(buf, 0, n);
        return out.toString("UTF-8");
    }
}
