package com.tvhome.launcher;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.view.View;

/** Ícone de clima em traço fino, desenhado no código (sol, lua, nuvem, chuva, tempestade). */
final class IconeClima extends View {
    private final Paint traco = new Paint(Paint.ANTI_ALIAS_FLAG);
    private int codigo;
    private boolean dia = true;

    IconeClima(Context c) {
        super(c);
        traco.setStyle(Paint.Style.STROKE);
        traco.setColor(Ui.BRANCO);
        traco.setStrokeCap(Paint.Cap.ROUND);
        traco.setStrokeJoin(Paint.Join.ROUND);
    }

    void definir(int codigo, boolean dia) {
        this.codigo = codigo;
        this.dia = dia;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas c) {
        float w = getWidth(), h = getHeight(), u = w / 24f;
        traco.setStrokeWidth(Math.max(2f, u * 1.3f));
        boolean nublado = codigo >= 2;
        boolean chuva = (codigo >= 51 && codigo <= 67) || (codigo >= 80 && codigo <= 82);
        boolean raio = codigo >= 95;

        if (!nublado || codigo == 2) {
            float cx = nublado ? 9 * u : 12 * u, cy = nublado ? 9 * u : 12 * u, r = nublado ? 3.6f * u : 4.5f * u;
            if (dia) {
                c.drawCircle(cx, cy, r, traco);
                for (int i = 0; i < 8; i++) {
                    double a = Math.PI / 4 * i;
                    float r1 = r + 2.2f * u, r2 = r + 4.2f * u;
                    c.drawLine(cx + (float) Math.cos(a) * r1, cy + (float) Math.sin(a) * r1,
                            cx + (float) Math.cos(a) * r2, cy + (float) Math.sin(a) * r2, traco);
                }
            } else {
                Path lua = new Path();
                RectF o = new RectF(cx - r - u, cy - r - u, cx + r + u, cy + r + u);
                lua.addArc(o, 60, 260);
                c.drawPath(lua, traco);
                c.drawArc(new RectF(cx - r + u, cy - r - 2 * u, cx + r + 3 * u, cy + r), 140, -170, false, traco);
            }
        }
        if (nublado) {
            Path n = new Path();
            float base = (chuva || raio) ? 15 * u : 18 * u;
            n.moveTo(6 * u, base);
            n.cubicTo(2.5f * u, base, 2.5f * u, base - 6 * u, 7 * u, base - 6 * u);
            n.cubicTo(7.5f * u, base - 11 * u, 15 * u, base - 11 * u, 16 * u, base - 6.5f * u);
            n.cubicTo(21.5f * u, base - 7 * u, 22 * u, base, 18 * u, base);
            n.close();
            c.drawPath(n, traco);
            if (chuva) {
                for (int i = 0; i < 3; i++) {
                    float x = (8 + i * 4) * u;
                    c.drawLine(x, base + 2.5f * u, x - 1.5f * u, base + 6 * u, traco);
                }
            }
            if (raio) {
                Path r = new Path();
                r.moveTo(13 * u, base + 1.5f * u);
                r.lineTo(10.5f * u, base + 5 * u);
                r.lineTo(13.5f * u, base + 5 * u);
                r.lineTo(11 * u, base + 8.5f * u);
                c.drawPath(r, traco);
            }
        }
    }
}
