package com.tvhome.launcher;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.util.LruCache;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Baixa logos de canal uma vez, guarda em disco e em memória. */
final class Logos {
    interface Pronto { void logo(Bitmap b); }

    private final File pasta;
    private final LruCache<String, Bitmap> memoria = new LruCache<String, Bitmap>(12 * 1024 * 1024) {
        @Override
        protected int sizeOf(String k, Bitmap b) { return b.getByteCount(); }
    };
    private final ExecutorService fila = Executors.newFixedThreadPool(3);
    private final Handler principal = new Handler(Looper.getMainLooper());

    private final android.content.res.AssetManager assets;

    Logos(Context c) {
        assets = c.getAssets();
        pasta = new File(c.getCacheDir(), "logos");
        pasta.mkdirs();
    }

    void carregar(final String url, final Pronto pronto) {
        if (url == null || url.isEmpty()) return;
        Bitmap b = memoria.get(url);
        if (b != null) { pronto.logo(b); return; }
        if (url.startsWith("asset:")) {
            try (InputStream in = assets.open("logos/" + url.substring(6))) {
                b = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                return;
            }
            if (b != null) { memoria.put(url, b); pronto.logo(b); }
            return;
        }
        fila.execute(() -> {
            Bitmap bm = baixar(url);
            if (bm != null) {
                memoria.put(url, bm);
                principal.post(() -> pronto.logo(bm));
            }
        });
    }

    private Bitmap baixar(String url) {
        File f = new File(pasta, Integer.toHexString(url.hashCode()) + ".png");
        if (!f.isFile()) {
            HttpURLConnection con = null;
            try {
                con = (HttpURLConnection) new URL(url).openConnection();
                con.setRequestProperty("User-Agent", "Mozilla/5.0");
                con.setInstanceFollowRedirects(true);
                con.setConnectTimeout(8000);
                con.setReadTimeout(10000);
                if (con.getResponseCode() != 200) return null;
                try (InputStream in = con.getInputStream(); FileOutputStream out = new FileOutputStream(f)) {
                    byte[] buf = new byte[16384];
                    int n;
                    while ((n = in.read(buf)) > 0) out.write(buf, 0, n);
                }
            } catch (Exception e) {
                f.delete();
                return null;
            } finally {
                if (con != null) con.disconnect();
            }
        }
        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(f.getPath(), o);
        int amostra = 1;
        while (o.outWidth / amostra > 400) amostra *= 2;
        o = new BitmapFactory.Options();
        o.inSampleSize = amostra;
        return BitmapFactory.decodeFile(f.getPath(), o);
    }
}
