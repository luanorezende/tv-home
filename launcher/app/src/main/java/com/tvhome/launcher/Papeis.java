package com.tvhome.launcher;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Coleção de papéis de parede (assets/wallpapers), favoritos e intervalo de troca. */
final class Papeis {
    static final int[] MINUTOS = {1, 5, 15, 60};

    private final Context ctx;
    private final SharedPreferences prefs;
    final List<String> todos = new ArrayList<>();

    Papeis(Context c) {
        ctx = c;
        prefs = c.getSharedPreferences("papeis", Context.MODE_PRIVATE);
        try {
            String[] arqs = c.getAssets().list("wallpapers");
            if (arqs != null) {
                Arrays.sort(arqs);
                for (String a : arqs) if (a.endsWith(".jpg")) todos.add(a);
            }
        } catch (Exception ignored) {}
    }

    Set<String> favoritos() {
        Set<String> f = prefs.getStringSet("favoritos", null);
        return f == null ? new HashSet<>(todos) : new HashSet<>(f);
    }

    void alternarFavorito(String nome) {
        Set<String> f = favoritos();
        if (f.contains(nome)) {
            if (f.size() > 1) f.remove(nome); // sempre sobra pelo menos um
        } else {
            f.add(nome);
        }
        prefs.edit().putStringSet("favoritos", f).apply();
    }

    /** Favoritos na ordem da coleção. */
    List<String> rodizio() {
        Set<String> f = favoritos();
        List<String> r = new ArrayList<>();
        for (String n : todos) if (f.contains(n)) r.add(n);
        if (r.isEmpty()) r.addAll(todos);
        return r;
    }

    int minutos() { return prefs.getInt("minutos", 5); }

    void definirMinutos(int m) { prefs.edit().putInt("minutos", m).apply(); }

    int ultimo() { return prefs.getInt("ultimo", 0); }

    void guardarUltimo(int i) { prefs.edit().putInt("ultimo", i).apply(); }

    Bitmap carregar(String nome, int reduzir) {
        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inPreferredConfig = Bitmap.Config.RGB_565;
        o.inSampleSize = reduzir;
        try (InputStream in = ctx.getAssets().open("wallpapers/" + nome)) {
            return BitmapFactory.decodeStream(in, null, o);
        } catch (Exception e) {
            return null;
        }
    }
}
