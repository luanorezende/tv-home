package com.tvhome.launcher;

import android.content.Context;
import android.graphics.Typeface;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Map;

/** Tudo é desenhado numa grade de 1920x1080 e escalado para a tela real. */
final class Ui {
    static float escala = 1f;
    private static final Map<Integer, Typeface> fontes = new HashMap<>();

    static int px(float v) { return Math.round(v * escala); }

    static Typeface fonte(Context c, int peso) {
        Typeface t = fontes.get(peso);
        if (t == null) {
            try {
                t = new Typeface.Builder(c.getAssets(), "fonts/Outfit.ttf")
                        .setFontVariationSettings("'wght' " + peso).build();
            } catch (Exception e) {
                t = Typeface.DEFAULT;
            }
            fontes.put(peso, t);
        }
        return t;
    }

    static TextView texto(Context c, float tamanho, int peso, int cor) {
        TextView t = new TextView(c);
        t.setTypeface(fonte(c, peso));
        t.setTextSize(TypedValue.COMPLEX_UNIT_PX, px(tamanho));
        t.setTextColor(cor);
        t.setIncludeFontPadding(false);
        t.setSingleLine(true);
        return t;
    }

    static FrameLayout.LayoutParams em(int largura, int altura, float esquerda, float topo) {
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(largura, altura);
        lp.gravity = Gravity.TOP | Gravity.START;
        lp.leftMargin = px(esquerda);
        lp.topMargin = px(topo);
        return lp;
    }

    static FrameLayout.LayoutParams noTopoDireita(float direita, float topo) {
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
        lp.gravity = Gravity.TOP | Gravity.END;
        lp.rightMargin = px(direita);
        lp.topMargin = px(topo);
        return lp;
    }

    static void mostrar(View v, boolean sim) { v.setVisibility(sim ? View.VISIBLE : View.GONE); }

    static final int BRANCO = 0xFFFFFFFF;
    static final int BRANCO_72 = 0xB8FFFFFF;

    private Ui() {}
}
