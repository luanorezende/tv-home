package com.tvhome.launcher;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Outline;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.view.animation.OvershootInterpolator;

/** Bloco de app: banner oficial com moldura de vidro; cresce e ganha contorno branco quando selecionado. */
final class Bloco extends View {
    final String pacote;
    final String nome;
    private Bitmap imagem;
    private Drawable desenho;
    private boolean simbolo; // imagem pequena centralizada sobre vidro
    private boolean logo;    // logo de canal: cabe inteiro na caixa, sem esticar
    private String rotulo;
    private boolean foco;

    private final Paint pImg = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
    private final Paint pVidro = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint pBrilho = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint pBorda = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint pFoco = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint pTexto = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF r = new RectF();
    private final float raio;

    Bloco(Context c, String pacote, String nome) {
        super(c);
        this.pacote = pacote;
        this.nome = nome;
        raio = Ui.px(28);
        pVidro.setColor(0x26FFFFFF);
        pBorda.setStyle(Paint.Style.STROKE);
        pBorda.setStrokeWidth(Ui.px(1.5f));
        pBorda.setColor(0x47FFFFFF);
        pFoco.setStyle(Paint.Style.STROKE);
        pFoco.setStrokeWidth(Ui.px(5));
        pFoco.setColor(Ui.BRANCO);
        pTexto.setColor(Ui.BRANCO);
        pTexto.setTypeface(Ui.fonte(c, 600));
        pTexto.setTextSize(Ui.px(46));
        setOutlineProvider(new ViewOutlineProvider() {
            @Override
            public void getOutline(View v, Outline o) {
                o.setRoundRect(0, 0, v.getWidth(), v.getHeight(), raio);
            }
        });
        setElevation(Ui.px(10));
    }

    Bloco comBanner(Bitmap b) { imagem = b; return this; }

    Bloco comLogo(Bitmap b, String texto) { imagem = b; logo = true; rotulo = texto; invalidate(); return this; }

    Bloco comDesenho(Drawable d, String texto) { desenho = d; rotulo = texto; invalidate(); return this; }

    Bloco comSimbolo(Bitmap b, String texto) { imagem = b; simbolo = true; rotulo = texto; return this; }

    void focar(boolean f) {
        if (foco == f) return;
        foco = f;
        float s = f ? 1.12f : 1f;
        animate().scaleX(s).scaleY(s).setDuration(260).setInterpolator(new OvershootInterpolator(1.6f)).start();
        setElevation(Ui.px(f ? 30 : 10));
        invalidate();
    }

    @Override
    protected void onDraw(Canvas c) {
        float w = getWidth(), h = getHeight();
        r.set(0, 0, w, h);
        if (logo) {
            c.drawRoundRect(r, raio, raio, pVidro);
            if (imagem != null) {
                float bw = w * 0.72f, bh = h * 0.62f;
                float esc = Math.min(bw / imagem.getWidth(), bh / imagem.getHeight());
                float iw = imagem.getWidth() * esc, ih = imagem.getHeight() * esc;
                c.drawBitmap(imagem, null, new RectF((w - iw) / 2f, (h - ih) / 2f, (w + iw) / 2f, (h + ih) / 2f), pImg);
            } else if (rotulo != null) {
                pTexto.setTextSize(Ui.px(30));
                String t = rotulo;
                while (pTexto.measureText(t) > w - Ui.px(30) && t.length() > 4) t = t.substring(0, t.length() - 2);
                if (!t.equals(rotulo)) t = t + "…";
                Paint.FontMetrics fm = pTexto.getFontMetrics();
                c.drawText(t, (w - pTexto.measureText(t)) / 2f, h / 2f - (fm.ascent + fm.descent) / 2f, pTexto);
            }
        } else if (imagem != null && !simbolo) {
            BitmapShader sh = new BitmapShader(imagem, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
            float esc = Math.max(w / imagem.getWidth(), h / imagem.getHeight());
            Matrix m = new Matrix();
            m.setScale(esc, esc);
            m.postTranslate((w - imagem.getWidth() * esc) / 2f, (h - imagem.getHeight() * esc) / 2f);
            sh.setLocalMatrix(m);
            pImg.setShader(sh);
            c.drawRoundRect(r, raio, raio, pImg);
        } else {
            c.drawRoundRect(r, raio, raio, pVidro);
            if (simbolo && imagem != null) {
                float ih = h * 0.56f, iw = ih * imagem.getWidth() / imagem.getHeight();
                float tw = rotulo == null ? 0 : pTexto.measureText(rotulo);
                float total = iw + (rotulo == null ? 0 : Ui.px(18) + tw);
                float x = (w - total) / 2f, y = (h - ih) / 2f;
                c.drawBitmap(imagem, null, new RectF(x, y, x + iw, y + ih), pImg);
                if (rotulo != null) {
                    Paint.FontMetrics fm = pTexto.getFontMetrics();
                    c.drawText(rotulo, x + iw + Ui.px(18), h / 2f - (fm.ascent + fm.descent) / 2f, pTexto);
                }
            } else if (desenho != null) {
                int lado = (int) (h * 0.42f);
                int x = (int) ((w - lado) / 2), y = (int) ((h - lado) / 2) - (rotulo == null ? 0 : Ui.px(16));
                desenho.setBounds(x, y, x + lado, y + lado);
                desenho.draw(c);
                if (rotulo != null) {
                    pTexto.setTextSize(Ui.px(26));
                    float tw = pTexto.measureText(rotulo);
                    if (tw > w - Ui.px(24)) tw = w - Ui.px(24);
                    c.drawText(rotulo, (w - tw) / 2f, y + lado + Ui.px(38), pTexto);
                }
            }
        }
        // brilho de vidro na diagonal + borda fina
        pBrilho.setShader(new LinearGradient(0, 0, w * 0.55f, h * 0.9f, 0x38FFFFFF, 0x00FFFFFF, Shader.TileMode.CLAMP));
        c.drawRoundRect(r, raio, raio, pBrilho);
        float m = pBorda.getStrokeWidth() / 2f;
        c.drawRoundRect(new RectF(m, m, w - m, h - m), raio, raio, pBorda);
        if (foco) {
            float f = pFoco.getStrokeWidth() / 2f;
            c.drawRoundRect(new RectF(f, f, w - f, h - f), raio, raio, pFoco);
        }
    }
}
