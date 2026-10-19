package com.tvhome.launcher;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;

/** Ícones simples desenhados no código: engrenagem, grade de apps e marca de favorito. */
final class Desenhos {

    static Drawable engrenagem() {
        return new Base() {
            @Override
            public void draw(Canvas c) {
                Rect b = getBounds();
                float cx = b.exactCenterX(), cy = b.exactCenterY(), r = b.width() / 2f;
                p.setStyle(Paint.Style.STROKE);
                p.setStrokeWidth(r * 0.14f);
                c.drawCircle(cx, cy, r * 0.58f, p);
                c.drawCircle(cx, cy, r * 0.22f, p);
                p.setStrokeWidth(r * 0.22f);
                for (int i = 0; i < 8; i++) {
                    double a = Math.PI / 4 * i;
                    c.drawLine(cx + (float) Math.cos(a) * r * 0.66f, cy + (float) Math.sin(a) * r * 0.66f,
                            cx + (float) Math.cos(a) * r * 0.92f, cy + (float) Math.sin(a) * r * 0.92f, p);
                }
            }
        };
    }

    static Drawable grade() {
        return new Base() {
            @Override
            public void draw(Canvas c) {
                Rect b = getBounds();
                float passo = b.width() / 3f, raio = passo * 0.26f;
                p.setStyle(Paint.Style.FILL);
                for (int i = 0; i < 3; i++)
                    for (int j = 0; j < 3; j++)
                        c.drawCircle(b.left + passo * (i + 0.5f), b.top + passo * (j + 0.5f), raio, p);
            }
        };
    }

    /** Pente de memória: placa com chips e contatos embaixo. */
    static Drawable memoria() {
        return new Base() {
            @Override
            public void draw(Canvas c) {
                Rect b = getBounds();
                float w = b.width(), h = b.height();
                float x0 = b.left, y0 = b.top + h * 0.22f, x1 = b.right, y1 = b.top + h * 0.72f;
                p.setStyle(Paint.Style.STROKE);
                p.setStrokeWidth(w * 0.07f);
                c.drawRoundRect(new android.graphics.RectF(x0, y0, x1, y1), w * 0.06f, w * 0.06f, p);
                p.setStyle(Paint.Style.FILL);
                float cw = w * 0.17f, gap = (w - 4 * cw) / 5f;
                for (int i = 0; i < 4; i++) {
                    float cx = x0 + gap + i * (cw + gap);
                    c.drawRect(cx, y0 + h * 0.12f, cx + cw, y1 - h * 0.12f, p);
                }
                p.setStrokeWidth(w * 0.06f);
                for (int i = 0; i < 7; i++) {
                    float px = x0 + w * (0.12f + i * 0.127f);
                    c.drawLine(px, y1 + h * 0.06f, px, y1 + h * 0.2f, p);
                }
            }
        };
    }

    static Drawable marca() {
        return new Base() {
            @Override
            public void draw(Canvas c) {
                Rect b = getBounds();
                float w = b.width();
                p.setStyle(Paint.Style.FILL);
                p.setColor(Ui.BRANCO);
                c.drawCircle(b.exactCenterX(), b.exactCenterY(), w / 2f, p);
                p.setColor(0xFF000000);
                p.setStyle(Paint.Style.STROKE);
                p.setStrokeWidth(w * 0.12f);
                p.setStrokeCap(Paint.Cap.ROUND);
                c.drawLine(b.left + w * 0.28f, b.top + w * 0.52f, b.left + w * 0.44f, b.top + w * 0.68f, p);
                c.drawLine(b.left + w * 0.44f, b.top + w * 0.68f, b.left + w * 0.74f, b.top + w * 0.36f, p);
                p.setColor(Ui.BRANCO);
            }
        };
    }

    private abstract static class Base extends Drawable {
        final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);

        Base() {
            p.setColor(Ui.BRANCO);
            p.setStrokeCap(Paint.Cap.ROUND);
        }

        @Override public void setAlpha(int a) { p.setAlpha(a); }
        @Override public void setColorFilter(ColorFilter f) { p.setColorFilter(f); }
        @Override public int getOpacity() { return PixelFormat.TRANSLUCENT; }
    }

    private Desenhos() {}
}
