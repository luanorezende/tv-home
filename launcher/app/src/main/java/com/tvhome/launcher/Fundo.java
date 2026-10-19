package com.tvhome.launcher;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.GradientDrawable;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;

/** Papel de parede em tela cheia: troca com fade e se move bem devagar. */
final class Fundo extends FrameLayout {
    private final ImageView[] camadas = new ImageView[2];
    private final Bitmap[] bitmaps = new Bitmap[2];
    private final AnimatorSet[] movimento = new AnimatorSet[2];
    private int frente = 0;
    private final View sombra;

    Fundo(Context c) {
        super(c);
        for (int i = 0; i < 2; i++) {
            ImageView v = new ImageView(c);
            v.setScaleType(ImageView.ScaleType.CENTER_CROP);
            v.setAlpha(0f);
            addView(v, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
            camadas[i] = v;
        }
        sombra = new View(c);
        addView(sombra, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        definirSombra(true);
    }

    /** Escurece em cima e embaixo; mais forte fora do Início. */
    void definirSombra(boolean inicio) {
        int[] cores = inicio
                ? new int[]{0x73000000, 0x00000000, 0x00000000, 0xC7000000}
                : new int[]{0xA6000000, 0x8C000000, 0x8C000000, 0xD9000000};
        GradientDrawable g = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, cores);
        sombra.setBackground(g);
    }

    void mostrar(Bitmap b, boolean instantaneo) {
        if (b == null) return;
        int nova = 1 - frente;
        ImageView v = camadas[nova];
        Bitmap antigo = bitmaps[nova];
        v.setImageBitmap(b);
        bitmaps[nova] = b;
        mover(nova);
        v.animate().cancel();
        camadas[frente].animate().cancel();
        if (instantaneo) {
            v.setAlpha(1f);
            camadas[frente].setAlpha(0f);
        } else {
            v.bringToFront();
            sombra.bringToFront();
            v.setAlpha(0f);
            v.animate().alpha(1f).setStartDelay(0).setDuration(1600).start();
            final ImageView velha = camadas[frente];
            velha.animate().alpha(0f).setStartDelay(1600).setDuration(1).start();
        }
        frente = nova;
        if (antigo != null && antigo != b) {
            // libera depois que o fade terminou
            postDelayed(antigo::recycle, 2000);
        }
    }

    void pausar() {
        for (AnimatorSet s : movimento) if (s != null) s.pause();
    }

    void retomar() {
        for (AnimatorSet s : movimento) if (s != null) s.resume();
    }

    private void mover(int i) {
        if (movimento[i] != null) movimento[i].cancel();
        ImageView v = camadas[i];
        v.setScaleX(1.06f);
        v.setScaleY(1.06f);
        v.setTranslationX(0);
        v.setTranslationY(0);
        AnimatorSet s = new AnimatorSet();
        s.playTogether(
                ObjectAnimator.ofFloat(v, "scaleX", 1.06f, 1.12f),
                ObjectAnimator.ofFloat(v, "scaleY", 1.06f, 1.12f),
                ObjectAnimator.ofFloat(v, "translationX", 0, -Ui.px(28)),
                ObjectAnimator.ofFloat(v, "translationY", 0, -Ui.px(12)));
        s.setDuration(60000);
        s.setInterpolator(new LinearInterpolator());
        s.start();
        movimento[i] = s;
    }
}
