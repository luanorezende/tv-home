package com.tvhome.launcher;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.Collator;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class MainActivity extends Activity {

    private static final String[][] APPS = Config.APPS;
    /** Marcador do bloco que abre as configurações da TV. */
    private static final String CONFIG = "#config";
    /** Marcador do bloco que libera memória. */
    private static final String MEMORIA = "#memoria";
    private Bloco blocoMemoria;
    private static final String[] ABAS = {"Início", "Jogos", "TV ao vivo"};
    private static final Locale BR = new Locale("pt", "BR");

    private enum Zona { ABAS, APPS, JOGOS, TV, TODOS, AJUSTES }

    private final Handler h = new Handler(Looper.getMainLooper());
    private Papeis papeis;
    private Clima clima;
    private Fundo fundo;

    private FrameLayout raiz, inicio, outraAba, todos, ajustes;
    private LinearLayout linhaAbas, linhaApps;
    private TextView hora, data, temp, outraTitulo;
    private LinearLayout dias;
    private IconeClima iconeClima;
    private final List<View> abas = new ArrayList<>();
    private final List<Bloco> blocos = new ArrayList<>();

    private Zona zona = Zona.APPS;
    private int aba = 0, focoAba = 0, focoApp = 0;
    private int rodizio;
    private long climaEm = 0;

    // jogos
    private FrameLayout jogosTela;
    private ImageView jogosFundo;
    private TextView jogoSistema, jogoNome, jogoAno, jogoJog, jogoFalta;
    private LinearLayout prateleira;
    private final List<List<Bloco>> jLinhas = new ArrayList<>();
    private final List<List<Jogos.Jogo>> jJogos = new ArrayList<>();
    private int jr = 0, jc = 0;

    // tv ao vivo
    private Canais canais;
    private Logos logos;
    private FrameLayout tvTela;
    private TextView tvCategoria, tvNome;
    private LinearLayout tvLista;
    private final List<LinearLayout> tvFilas = new ArrayList<>();
    private final List<List<Bloco>> tvBlocos = new ArrayList<>();
    private final List<List<Canais.Canal>> tvCanais = new ArrayList<>();
    private int tr = 0;
    private int[] tcol;

    // todos os apps
    private final List<Bloco> todosBlocos = new ArrayList<>();
    private LinearLayout todosGrade;
    private int focoTodos = 0;
    private static final int COLUNAS = 5;

    // ajustes
    private final List<List<View>> ajLinhas = new ArrayList<>();
    private int ajLinha = 0, ajColuna = 0;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        DisplayMetrics dm = getResources().getDisplayMetrics();
        Ui.escala = Math.max(dm.widthPixels, dm.heightPixels) / 1920f;
        papeis = new Papeis(this);
        clima = new Clima(this);

        raiz = new FrameLayout(this);
        raiz.setBackgroundColor(0xFF000000);
        fundo = new Fundo(this);
        raiz.addView(fundo, new FrameLayout.LayoutParams(-1, -1));

        montarInicio();
        montarOutraAba();
        montarJogos();
        montarTv();
        montarAbas();
        montarApps();
        setContentView(raiz);

        List<String> r = papeis.rodizio();
        rodizio = papeis.ultimo() % Math.max(1, r.size());
        if (!r.isEmpty()) fundo.mostrar(papeis.carregar(r.get(rodizio), 1), true);
        mostrarClima();
        selecionarAba(0);
        desenharFoco();
    }

    // ---------------- montagem ----------------

    private void montarInicio() {
        inicio = new FrameLayout(this);
        raiz.addView(inicio, new FrameLayout.LayoutParams(-1, -1));

        hora = Ui.texto(this, 200, 200, Ui.BRANCO);
        hora.setLetterSpacing(-0.03f);
        hora.setShadowLayer(Ui.px(40), 0, Ui.px(6), 0x59000000);
        inicio.addView(hora, Ui.em(-2, -2, 112, 96));

        data = Ui.texto(this, 40, 300, Ui.BRANCO_72);
        data.setShadowLayer(Ui.px(20), 0, Ui.px(2), 0x59000000);
        inicio.addView(data, Ui.em(-2, -2, 122, 316));

        LinearLayout clm = new LinearLayout(this);
        clm.setOrientation(LinearLayout.VERTICAL);
        clm.setGravity(Gravity.END);
        LinearLayout agora = new LinearLayout(this);
        agora.setGravity(Gravity.CENTER_VERTICAL);
        iconeClima = new IconeClima(this);
        agora.addView(iconeClima, new LinearLayout.LayoutParams(Ui.px(84), Ui.px(84)));
        temp = Ui.texto(this, 96, 200, Ui.BRANCO);
        LinearLayout.LayoutParams lt = new LinearLayout.LayoutParams(-2, -2);
        lt.leftMargin = Ui.px(18);
        agora.addView(temp, lt);
        clm.addView(agora);
        TextView cidade = Ui.texto(this, 28, 400, Ui.BRANCO_72);
        cidade.setText(Config.CIDADE);
        LinearLayout.LayoutParams lc = new LinearLayout.LayoutParams(-2, -2);
        lc.topMargin = Ui.px(8);
        clm.addView(cidade, lc);
        dias = new LinearLayout(this);
        LinearLayout.LayoutParams ld = new LinearLayout.LayoutParams(-2, -2);
        ld.topMargin = Ui.px(22);
        clm.addView(dias, ld);
        inicio.addView(clm, Ui.noTopoDireita(120, 110));
    }

    private void montarOutraAba() {
        outraAba = new FrameLayout(this);
        outraTitulo = Ui.texto(this, 76, 600, Ui.BRANCO);
        outraAba.addView(outraTitulo, Ui.em(-2, -2, 120, 150));
        TextView sub = Ui.texto(this, 32, 300, Ui.BRANCO_72);
        sub.setText("Em breve");
        outraAba.addView(sub, Ui.em(-2, -2, 122, 260));
        outraAba.setVisibility(View.GONE);
        raiz.addView(outraAba, new FrameLayout.LayoutParams(-1, -1));
    }

    private void montarAbas() {
        linhaAbas = new LinearLayout(this);
        linhaAbas.setGravity(Gravity.CENTER_VERTICAL);
        for (String nome : ABAS) {
            TextView t = Ui.texto(this, 32, 400, Ui.BRANCO_72);
            t.setText(nome);
            t.setPadding(Ui.px(30), Ui.px(12), Ui.px(30), Ui.px(14));
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-2, -2);
            lp.rightMargin = Ui.px(10);
            linhaAbas.addView(t, lp);
            abas.add(t);
        }
        ImageView eng = new ImageView(this);
        eng.setImageDrawable(Desenhos.engrenagem());
        eng.setPadding(Ui.px(18), Ui.px(14), Ui.px(18), Ui.px(14));
        LinearLayout.LayoutParams le = new LinearLayout.LayoutParams(Ui.px(70), Ui.px(62));
        le.leftMargin = Ui.px(16);
        linhaAbas.addView(eng, le);
        abas.add(eng);
        raiz.addView(linhaAbas, Ui.em(-2, -2, 108, 690));
    }

    private void montarApps() {
        linhaApps = new LinearLayout(this);
        linhaApps.setClipChildren(false);
        linhaApps.setClipToPadding(false);
        PackageManager pm = getPackageManager();
        for (String[] a : APPS) {
            if (intencao(a[0]) == null) continue;
            Bloco bl = new Bloco(this, a[0], a[1]);
            Bitmap ban = banner(pm, a[0]);
            if (ban != null) bl.comBanner(ban);
            else bl.comDesenho(icone(pm, a[0]), a[1]);
            adicionarBloco(bl);
        }
        Bloco tudo = new Bloco(this, null, "Apps").comDesenho(Desenhos.grade(), "Apps");
        adicionarBloco(tudo);
        blocoMemoria = new Bloco(this, MEMORIA, "Liberar memória").comDesenho(Desenhos.memoria(), "Liberar memória");
        adicionarBloco(blocoMemoria);
        Bloco cfg = new Bloco(this, CONFIG, "Configurações").comDesenho(Desenhos.engrenagem(), "Configurações");
        adicionarBloco(cfg);
        FrameLayout.LayoutParams lp = Ui.em(-2, -2, 120, 1080 - 100 - 127);
        raiz.addView(linhaApps, lp);
        raiz.setClipChildren(false);
    }

    private void adicionarBloco(Bloco bl) {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(Ui.px(225), Ui.px(127));
        lp.rightMargin = Ui.px(24);
        linhaApps.addView(bl, lp);
        blocos.add(bl);
    }

    // ---------------- jogos ----------------

    private void montarJogos() {
        jogosTela = new FrameLayout(this);
        jogosTela.setClipChildren(false);
        jogosFundo = new ImageView(this);
        jogosFundo.setScaleType(ImageView.ScaleType.CENTER_CROP);
        jogosFundo.setColorFilter(0x8C000000);
        jogosTela.addView(jogosFundo, new FrameLayout.LayoutParams(-1, -1));
        View sombra = new View(this);
        sombra.setBackground(new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,
                new int[]{0x99000000, 0x33000000, 0xB3000000}));
        jogosTela.addView(sombra, new FrameLayout.LayoutParams(-1, -1));

        jogoSistema = Ui.texto(this, 22, 600, Ui.BRANCO_72);
        jogoSistema.setLetterSpacing(0.22f);
        jogosTela.addView(jogoSistema, Ui.em(-2, -2, 122, 150));
        jogoNome = Ui.texto(this, 76, 600, Ui.BRANCO);
        jogosTela.addView(jogoNome, Ui.em(-2, -2, 118, 186));
        LinearLayout meta = new LinearLayout(this);
        jogoAno = chip();
        jogoJog = chip();
        jogoFalta = chip();
        jogoFalta.setText("Sem o arquivo do jogo");
        jogoFalta.setTextColor(0xFFFFC46B);
        meta.addView(jogoAno);
        meta.addView(jogoJog);
        meta.addView(jogoFalta);
        jogosTela.addView(meta, Ui.em(-2, -2, 120, 288));

        FrameLayout janela = new FrameLayout(this);
        janela.setOutlineProvider(android.view.ViewOutlineProvider.BOUNDS);
        janela.setClipToOutline(true);
        prateleira = new LinearLayout(this);
        prateleira.setOrientation(LinearLayout.VERTICAL);
        prateleira.setClipChildren(false);
        prateleira.setPadding(0, Ui.px(20), 0, 0);
        janela.addView(prateleira, Ui.em(Ui.px(1920), Ui.px(3000), 0, 0));
        FrameLayout.LayoutParams lj = new FrameLayout.LayoutParams(-1, Ui.px(1080 - 360));
        lj.topMargin = Ui.px(360);
        jogosTela.addView(janela, lj);

        Jogos.Sistema atual = null;
        List<Bloco> linha = null;
        List<Jogos.Jogo> linhaJ = null;
        LinearLayout fila = null;
        for (Jogos.Jogo j : Jogos.TODOS) {
            boolean novaSecao = j.sistema != atual;
            if (novaSecao) {
                atual = j.sistema;
                TextView t = Ui.texto(this, 24, 600, Ui.BRANCO_72);
                t.setLetterSpacing(0.22f);
                t.setText(j.sistema.nome.toUpperCase(BR));
                LinearLayout.LayoutParams lt = new LinearLayout.LayoutParams(-2, -2);
                lt.leftMargin = Ui.px(122);
                lt.bottomMargin = Ui.px(16);
                prateleira.addView(t, lt);
            }
            if (novaSecao) {
                fila = new LinearLayout(this);
                fila.setClipChildren(false);
                fila.setClipToPadding(false);
                fila.setPadding(Ui.px(120), 0, 0, 0);
                LinearLayout.LayoutParams lf = new LinearLayout.LayoutParams(-2, -2);
                lf.bottomMargin = Ui.px(24);
                prateleira.addView(fila, lf);
                linha = new ArrayList<>();
                linhaJ = new ArrayList<>();
                jLinhas.add(linha);
                jJogos.add(linhaJ);
            }
            Bloco b = new Bloco(this, null, j.nome).comBanner(capa(j));
            // caixa do SNES é larga, arte do fliperama é 16:9, caixa do PlayStation é quadrada
            int larg = j.sistema == Jogos.Sistema.PS1 ? 165 : 260;
            int alt = j.sistema == Jogos.Sistema.SNES ? 182 : j.sistema == Jogos.Sistema.PS1 ? 165 : 146;
            LinearLayout.LayoutParams lb = new LinearLayout.LayoutParams(Ui.px(larg), Ui.px(alt));
            lb.rightMargin = Ui.px(32);
            fila.addView(b, lb);
            linha.add(b);
            linhaJ.add(j);
        }
        jogosTela.setVisibility(View.GONE);
        raiz.addView(jogosTela, new FrameLayout.LayoutParams(-1, -1));
    }

    private TextView chip() {
        TextView t = Ui.texto(this, 24, 400, Ui.BRANCO);
        t.setPadding(Ui.px(18), Ui.px(7), Ui.px(18), Ui.px(9));
        GradientDrawable g = new GradientDrawable();
        g.setCornerRadius(Ui.px(30));
        g.setColor(0x24FFFFFF);
        g.setStroke(Ui.px(1), 0x33FFFFFF);
        t.setBackground(g);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-2, -2);
        lp.rightMargin = Ui.px(14);
        t.setLayoutParams(lp);
        return t;
    }

    private Bitmap capa(Jogos.Jogo j) {
        try (java.io.InputStream in = getAssets().open("capas/" + j.capa + ".jpg")) {
            return BitmapFactory.decodeStream(in);
        } catch (Exception e) {
            return null;
        }
    }

    private void atualizarJogos() {
        for (int l = 0; l < jJogos.size(); l++)
            for (int c = 0; c < jJogos.get(l).size(); c++)
                jLinhas.get(l).get(c).setAlpha(jJogos.get(l).get(c).rom() == null ? 0.4f : 1f);
    }

    private void mostrarJogo() {
        if (jJogos.isEmpty()) return;
        Jogos.Jogo j = jJogos.get(jr).get(jc);
        jogoSistema.setText(j.sistema.nome.toUpperCase(BR));
        jogoNome.setText(j.nome);
        jogoAno.setText(String.valueOf(j.ano));
        jogoJog.setText(j.jogadores);
        Ui.mostrar(jogoFalta, j.rom() == null);
        Bitmap c = capa(j);
        if (c != null) {
            // desfoque barato: reduz muito e amplia de volta
            Bitmap peq = Bitmap.createScaledBitmap(c, 24, Math.max(1, 24 * c.getHeight() / c.getWidth()), true);
            jogosFundo.setImageBitmap(Bitmap.createScaledBitmap(peq, 192, Math.max(1, 192 * peq.getHeight() / peq.getWidth()), true));
        }
        // uma fileira por console: rola para o lado até o jogo focado ficar à vista
        View alvo = jLinhas.get(jr).get(jc);
        View fila = (View) alvo.getParent();
        float tx = fila.getTranslationX();
        int largura = Ui.px(1920);
        if (alvo.getRight() + tx > largura - Ui.px(120)) tx = -(alvo.getRight() - largura + Ui.px(120));
        if (alvo.getLeft() + tx < Ui.px(120)) tx = -(alvo.getLeft() - Ui.px(120));
        if (alvo.getWidth() == 0 || tx > 0) tx = 0;
        fila.animate().translationX(tx).setDuration(220).start();
    }

    // ---------------- tv ao vivo ----------------

    private void montarTv() {
        canais = new Canais(this);
        logos = new Logos(this);
        tvTela = new FrameLayout(this);
        View sombra = new View(this);
        sombra.setBackgroundColor(0x66000000);
        tvTela.addView(sombra, new FrameLayout.LayoutParams(-1, -1));
        tvCategoria = Ui.texto(this, 22, 600, Ui.BRANCO_72);
        tvCategoria.setLetterSpacing(0.22f);
        tvTela.addView(tvCategoria, Ui.em(-2, -2, 122, 150));
        tvNome = Ui.texto(this, 76, 600, Ui.BRANCO);
        tvTela.addView(tvNome, Ui.em(-2, -2, 118, 186));

        FrameLayout janela = new FrameLayout(this);
        janela.setOutlineProvider(android.view.ViewOutlineProvider.BOUNDS);
        janela.setClipToOutline(true);
        tvLista = new LinearLayout(this);
        tvLista.setOrientation(LinearLayout.VERTICAL);
        tvLista.setClipChildren(false);
        tvLista.setPadding(0, Ui.px(24), 0, 0);
        janela.addView(tvLista, Ui.em(Ui.px(8000), Ui.px(4000), 0, 0));
        FrameLayout.LayoutParams lj = new FrameLayout.LayoutParams(-1, Ui.px(1080 - 330));
        lj.topMargin = Ui.px(330);
        tvTela.addView(janela, lj);

        for (java.util.Map.Entry<String, List<Canais.Canal>> e : canais.porCategoria.entrySet()) {
            TextView t = Ui.texto(this, 24, 600, Ui.BRANCO_72);
            t.setLetterSpacing(0.22f);
            t.setText(e.getKey().toUpperCase(BR));
            LinearLayout.LayoutParams lt = new LinearLayout.LayoutParams(-2, -2);
            lt.leftMargin = Ui.px(122);
            lt.bottomMargin = Ui.px(18);
            tvLista.addView(t, lt);
            LinearLayout fila = new LinearLayout(this);
            fila.setClipChildren(false);
            fila.setClipToPadding(false);
            fila.setPadding(Ui.px(120), 0, 0, 0);
            LinearLayout.LayoutParams lf = new LinearLayout.LayoutParams(-2, -2);
            lf.bottomMargin = Ui.px(40);
            tvLista.addView(fila, lf);
            List<Bloco> bl = new ArrayList<>();
            for (Canais.Canal c : e.getValue()) {
                final Bloco b = new Bloco(this, null, c.nome).comLogo(null, c.nome);
                LinearLayout.LayoutParams lb = new LinearLayout.LayoutParams(Ui.px(260), Ui.px(146));
                lb.rightMargin = Ui.px(30);
                fila.addView(b, lb);
                bl.add(b);
                logos.carregar(c.logo, bm -> b.comLogo(bm, c.nome));
            }
            tvFilas.add(fila);
            tvBlocos.add(bl);
            tvCanais.add(e.getValue());
        }
        tcol = new int[tvBlocos.size()];
        tvTela.setVisibility(View.GONE);
        raiz.addView(tvTela, new FrameLayout.LayoutParams(-1, -1));
        new Thread(canais::atualizarSePreciso).start();
    }

    private void abrirCanalApp(Canais.Canal c) {
        Intent i = null;
        if (c.link != null) {
            i = new Intent(Intent.ACTION_VIEW, android.net.Uri.parse(c.link)).setPackage(c.app);
            if (getPackageManager().resolveActivity(i, 0) == null) i = null;
        }
        if (i == null) i = intencao(c.app);
        if (i == null) return;
        try {
            startActivity(i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        } catch (Exception ignored) {}
    }

    private void mostrarCanal() {
        if (tvCanais.isEmpty()) return;
        Canais.Canal c = tvCanais.get(tr).get(tcol[tr]);
        tvCategoria.setText(c.categoria.toUpperCase(BR));
        tvNome.setText(c.nome);
        // vertical: fileira atual sempre na mesma altura
        LinearLayout fila = tvFilas.get(tr);
        View rotulo = tvLista.getChildAt(tvLista.indexOfChild(fila) - 1);
        tvLista.animate().translationY(-Math.max(0, rotulo.getTop() - Ui.px(24))).setDuration(260).start();
        // horizontal: rola a fileira para o canal focado ficar à vista
        View alvo = tvBlocos.get(tr).get(tcol[tr]);
        float tx = fila.getTranslationX();
        int largura = Ui.px(1920);
        if (alvo.getRight() + tx > largura - Ui.px(120)) tx = -(alvo.getRight() - largura + Ui.px(120));
        if (alvo.getLeft() + tx < Ui.px(120)) tx = -(alvo.getLeft() - Ui.px(120));
        if (alvo.getWidth() == 0 || tx > 0) tx = 0; // ainda sem layout, ou não precisa rolar
        fila.animate().translationX(tx).setDuration(220).start();
    }

    // ---------------- abas e foco ----------------

    private void selecionarAba(int i) {
        aba = i;
        boolean ini = i == 0;
        Ui.mostrar(inicio, ini);
        Ui.mostrar(linhaApps, ini);
        Ui.mostrar(outraAba, i >= 3);
        Ui.mostrar(jogosTela, i == 1);
        Ui.mostrar(tvTela, i == 2);
        if (i >= 3) outraTitulo.setText(ABAS[i]);
        if (i == 1) mostrarJogo();
        if (i == 2) mostrarCanal();
        fundo.definirSombra(ini);
        FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) linhaAbas.getLayoutParams();
        lp.topMargin = Ui.px(ini ? 690 : 40);
        linhaAbas.setLayoutParams(lp);
    }

    private void desenharFoco() {
        for (int i = 0; i < abas.size(); i++) {
            View v = abas.get(i);
            boolean f = zona == Zona.ABAS && i == focoAba;
            GradientDrawable pilula = new GradientDrawable();
            pilula.setCornerRadius(Ui.px(40));
            pilula.setColor(f ? Ui.BRANCO : 0x00000000);
            v.setBackground(pilula);
            if (v instanceof TextView) {
                TextView t = (TextView) v;
                boolean sel = i == aba;
                t.setTextColor(f ? 0xFF000000 : (sel ? Ui.BRANCO : Ui.BRANCO_72));
                t.setTypeface(Ui.fonte(this, sel || f ? 600 : 400));
            } else {
                Drawable d = ((ImageView) v).getDrawable();
                d.setColorFilter(new android.graphics.PorterDuffColorFilter(f ? 0xFF000000 : Ui.BRANCO_72, android.graphics.PorterDuff.Mode.SRC_IN));
                v.invalidate();
            }
        }
        for (int i = 0; i < blocos.size(); i++) blocos.get(i).focar(zona == Zona.APPS && i == focoApp);
        for (int l = 0; l < jLinhas.size(); l++)
            for (int c = 0; c < jLinhas.get(l).size(); c++)
                jLinhas.get(l).get(c).focar(zona == Zona.JOGOS && l == jr && c == jc);
        for (int l = 0; l < tvBlocos.size(); l++)
            for (int c = 0; c < tvBlocos.get(l).size(); c++)
                tvBlocos.get(l).get(c).focar(zona == Zona.TV && l == tr && c == tcol[l]);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent e) {
        int k = e.getKeyCode();
        if (k == KeyEvent.KEYCODE_VOLUME_UP || k == KeyEvent.KEYCODE_VOLUME_DOWN || k == KeyEvent.KEYCODE_VOLUME_MUTE)
            return super.dispatchKeyEvent(e);
        if (e.getAction() != KeyEvent.ACTION_DOWN) return true;
        boolean ok = k == KeyEvent.KEYCODE_DPAD_CENTER || k == KeyEvent.KEYCODE_ENTER
                || k == KeyEvent.KEYCODE_NUMPAD_ENTER || k == KeyEvent.KEYCODE_BUTTON_A;
        boolean voltar = k == KeyEvent.KEYCODE_BACK || k == KeyEvent.KEYCODE_BUTTON_B || k == KeyEvent.KEYCODE_ESCAPE;
        int dx = k == KeyEvent.KEYCODE_DPAD_LEFT ? -1 : k == KeyEvent.KEYCODE_DPAD_RIGHT ? 1 : 0;
        int dy = k == KeyEvent.KEYCODE_DPAD_UP ? -1 : k == KeyEvent.KEYCODE_DPAD_DOWN ? 1 : 0;

        switch (zona) {
            case TODOS: teclaTodos(dx, dy, ok, voltar); return true;
            case AJUSTES: teclaAjustes(dx, dy, ok, voltar); return true;
            case ABAS:
                if (dx != 0) {
                    int n = Math.max(0, Math.min(abas.size() - 1, focoAba + dx));
                    focoAba = n;
                    if (n < ABAS.length) selecionarAba(n);
                } else if (dy > 0 && aba == 0 && focoAba < ABAS.length) {
                    zona = Zona.APPS;
                } else if (dy > 0 && aba == 1 && focoAba == 1) {
                    zona = Zona.JOGOS;
                } else if (dy > 0 && aba == 2 && focoAba == 2 && !tvBlocos.isEmpty()) {
                    zona = Zona.TV;
                } else if (ok && focoAba == ABAS.length) {
                    abrirAjustes();
                } else if (voltar && aba != 0) {
                    focoAba = 0;
                    selecionarAba(0);
                }
                break;
            case JOGOS:
                if (voltar) { zona = Zona.ABAS; focoAba = 1; break; }
                if (dx != 0) jc = Math.max(0, Math.min(jLinhas.get(jr).size() - 1, jc + dx));
                if (dy > 0 && jr < jLinhas.size() - 1) { jr++; jc = Math.min(jc, jLinhas.get(jr).size() - 1); }
                if (dy < 0) {
                    if (jr > 0) { jr--; jc = Math.min(jc, jLinhas.get(jr).size() - 1); }
                    else { zona = Zona.ABAS; focoAba = 1; }
                }
                if (ok) {
                    Jogos.Jogo j = jJogos.get(jr).get(jc);
                    if (j.rom() == null) {
                        Ui.mostrar(jogoFalta, true);
                    } else {
                        new Thread(() -> {
                            Memoria.liberar(this);
                            h.post(() -> Jogos.jogar(this, j));
                        }).start();
                    }
                }
                mostrarJogo();
                break;
            case TV:
                if (voltar) { zona = Zona.ABAS; focoAba = 2; break; }
                if (dx != 0) tcol[tr] = Math.max(0, Math.min(tvBlocos.get(tr).size() - 1, tcol[tr] + dx));
                if (dy > 0 && tr < tvBlocos.size() - 1) tr++;
                if (dy < 0) { if (tr > 0) tr--; else { zona = Zona.ABAS; focoAba = 2; } }
                if (ok) {
                    Canais.Canal c = tvCanais.get(tr).get(tcol[tr]);
                    if (c.app != null) {
                        abrirCanalApp(c);
                    } else {
                        // o player só percorre os canais que tocam aqui dentro
                        List<Canais.Canal> tocaveis = new ArrayList<>();
                        for (Canais.Canal x : tvCanais.get(tr)) if (x.app == null) tocaveis.add(x);
                        PlayerActivity.lista = tocaveis;
                        PlayerActivity.atual = tocaveis.indexOf(c);
                        startActivity(new Intent(this, PlayerActivity.class));
                    }
                }
                mostrarCanal();
                break;
            case APPS:
                if (dx != 0) focoApp = Math.max(0, Math.min(blocos.size() - 1, focoApp + dx));
                else if (dy < 0) { zona = Zona.ABAS; focoAba = aba; }
                else if (ok) abrir(blocos.get(focoApp));
                break;
        }
        desenharFoco();
        return true;
    }

    private void abrir(Bloco b) {
        if (b.pacote == null) { abrirTodos(); return; }
        if (MEMORIA.equals(b.pacote)) {
            blocoMemoria.comDesenho(Desenhos.memoria(), "Liberando…");
            new Thread(() -> {
                long[] r = Memoria.liberar(this);
                h.post(() -> {
                    blocoMemoria.comDesenho(Desenhos.memoria(), r[1] + " MB livres");
                    h.postDelayed(() -> blocoMemoria.comDesenho(Desenhos.memoria(), "Liberar memória"), 5000);
                });
            }).start();
            return;
        }
        if (CONFIG.equals(b.pacote)) {
            try {
                startActivity(new Intent(Settings.ACTION_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
            } catch (Exception ignored) {}
            return;
        }
        Intent i = intencao(b.pacote);
        if (i == null) return;
        try {
            startActivity(i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        } catch (Exception ignored) {}
    }

    private Intent intencao(String pacote) {
        PackageManager pm = getPackageManager();
        Intent i = pm.getLeanbackLaunchIntentForPackage(pacote);
        if (i == null) i = pm.getLaunchIntentForPackage(pacote);
        return i;
    }

    private Bitmap banner(PackageManager pm, String pacote) {
        try {
            ApplicationInfo ai = pm.getApplicationInfo(pacote, 0);
            Drawable d = pm.getApplicationBanner(ai);
            if (d == null) {
                Intent i = pm.getLeanbackLaunchIntentForPackage(pacote);
                if (i != null) d = pm.getActivityBanner(i);
            }
            return d == null ? null : bitmap(d, 640, 360);
        } catch (Exception e) {
            return null;
        }
    }

    private Drawable icone(PackageManager pm, String pacote) {
        try {
            return pm.getApplicationIcon(pacote);
        } catch (Exception e) {
            return Desenhos.grade();
        }
    }

    private static Bitmap bitmap(Drawable d, int w, int hh) {
        if (d instanceof BitmapDrawable && ((BitmapDrawable) d).getBitmap() != null)
            return ((BitmapDrawable) d).getBitmap();
        Bitmap b = Bitmap.createBitmap(w, hh, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(b);
        d.setBounds(0, 0, w, hh);
        d.draw(c);
        return b;
    }

    // ---------------- todos os apps ----------------

    private void abrirTodos() {
        if (todos == null) {
            todos = new FrameLayout(this);
            todos.setBackgroundColor(0xE6000000);
            TextView t = Ui.texto(this, 64, 600, Ui.BRANCO);
            t.setText("Apps");
            todos.addView(t, Ui.em(-2, -2, 120, 70));
            todosGrade = new LinearLayout(this);
            todosGrade.setOrientation(LinearLayout.VERTICAL);
            todosGrade.setClipChildren(false);
            todos.addView(todosGrade, Ui.em(-2, -2, 120, 190));
            todos.setClipChildren(false);
            raiz.addView(todos, new FrameLayout.LayoutParams(-1, -1));
        }
        todosGrade.removeAllViews();
        todosBlocos.clear();
        PackageManager pm = getPackageManager();
        Intent q = new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LEANBACK_LAUNCHER);
        List<ResolveInfo> lista = pm.queryIntentActivities(q, 0);
        final Collator col = Collator.getInstance(BR);
        Collections.sort(lista, (a, b) -> col.compare(a.loadLabel(pm).toString(), b.loadLabel(pm).toString()));
        LinearLayout linha = null;
        for (ResolveInfo ri : lista) {
            String pac = ri.activityInfo.packageName;
            if (pac.equals(getPackageName())) continue;
            String nome = ri.loadLabel(pm).toString();
            Bloco bl = new Bloco(this, pac, nome);
            Drawable ban = ri.activityInfo.loadBanner(pm);
            if (ban == null) ban = ri.activityInfo.applicationInfo.loadBanner(pm);
            if (ban != null) bl.comBanner(bitmap(ban, 640, 360));
            else bl.comDesenho(ri.loadIcon(pm), nome);
            if (todosBlocos.size() % COLUNAS == 0) {
                linha = new LinearLayout(this);
                linha.setClipChildren(false);
                LinearLayout.LayoutParams ll = new LinearLayout.LayoutParams(-2, -2);
                ll.bottomMargin = Ui.px(44);
                todosGrade.addView(linha, ll);
            }
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(Ui.px(300), Ui.px(169));
            lp.rightMargin = Ui.px(40);
            linha.addView(bl, lp);
            todosBlocos.add(bl);
        }
        focoTodos = 0;
        todos.setVisibility(View.VISIBLE);
        zona = Zona.TODOS;
        focarTodos();
    }

    private void teclaTodos(int dx, int dy, boolean ok, boolean voltar) {
        int n = todosBlocos.size();
        if (voltar) { fecharTodos(); return; }
        if (n == 0) return;
        if (dx != 0 && (focoTodos % COLUNAS) + dx >= 0 && (focoTodos % COLUNAS) + dx < COLUNAS)
            focoTodos = Math.max(0, Math.min(n - 1, focoTodos + dx));
        if (dy != 0) {
            int novo = focoTodos + dy * COLUNAS;
            if (novo >= 0 && novo < n) focoTodos = novo;
            else if (dy > 0 && focoTodos / COLUNAS < (n - 1) / COLUNAS) focoTodos = n - 1;
        }
        if (ok) {
            Bloco b = todosBlocos.get(focoTodos);
            fecharTodos();
            abrir(b);
            return;
        }
        focarTodos();
    }

    private void focarTodos() {
        for (int i = 0; i < todosBlocos.size(); i++) todosBlocos.get(i).focar(i == focoTodos);
        int linha = focoTodos / COLUNAS;
        todosGrade.animate().translationY(-Math.max(0, linha - 2) * Ui.px(213)).setDuration(220).start();
    }

    private void fecharTodos() {
        todos.setVisibility(View.GONE);
        todosGrade.removeAllViews();
        todosBlocos.clear();
        zona = Zona.APPS;
        desenharFoco();
    }

    // ---------------- ajustes ----------------

    private void abrirAjustes() {
        if (ajustes != null) raiz.removeView(ajustes);
        ajustes = new FrameLayout(this);
        ajustes.setBackgroundColor(0xEB000000);
        ajustes.setClipChildren(false);
        ajLinhas.clear();

        TextView t = Ui.texto(this, 64, 600, Ui.BRANCO);
        t.setText("Ajustes");
        ajustes.addView(t, Ui.em(-2, -2, 120, 70));

        TextView l1 = Ui.texto(this, 30, 400, Ui.BRANCO_72);
        l1.setText("Trocar o papel de parede a cada");
        ajustes.addView(l1, Ui.em(-2, -2, 122, 190));
        LinearLayout tempos = new LinearLayout(this);
        List<View> linhaTempos = new ArrayList<>();
        for (int m : Papeis.MINUTOS) {
            TextView c = Ui.texto(this, 30, 500, Ui.BRANCO);
            c.setText(m >= 60 ? (m / 60) + " h" : m + " min");
            c.setPadding(Ui.px(30), Ui.px(12), Ui.px(30), Ui.px(14));
            c.setTag(m);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-2, -2);
            lp.rightMargin = Ui.px(16);
            tempos.addView(c, lp);
            linhaTempos.add(c);
        }
        ajustes.addView(tempos, Ui.em(-2, -2, 110, 240));
        ajLinhas.add(linhaTempos);

        TextView l2 = Ui.texto(this, 30, 400, Ui.BRANCO_72);
        l2.setText("Papéis de parede no rodízio");
        ajustes.addView(l2, Ui.em(-2, -2, 122, 350));
        LinearLayout grade = new LinearLayout(this);
        grade.setOrientation(LinearLayout.VERTICAL);
        grade.setClipChildren(false);
        LinearLayout linha = null;
        List<View> atual = null;
        for (int i = 0; i < papeis.todos.size(); i++) {
            if (i % 6 == 0) {
                linha = new LinearLayout(this);
                linha.setClipChildren(false);
                LinearLayout.LayoutParams ll = new LinearLayout.LayoutParams(-2, -2);
                ll.bottomMargin = Ui.px(26);
                grade.addView(linha, ll);
                atual = new ArrayList<>();
                ajLinhas.add(atual);
            }
            FrameLayout mini = new FrameLayout(this);
            ImageView img = new ImageView(this);
            img.setScaleType(ImageView.ScaleType.CENTER_CROP);
            img.setImageBitmap(papeis.carregar(papeis.todos.get(i), 8));
            img.setClipToOutline(true);
            GradientDrawable fundoMini = new GradientDrawable();
            fundoMini.setCornerRadius(Ui.px(16));
            img.setBackground(fundoMini);
            mini.addView(img, new FrameLayout.LayoutParams(-1, -1));
            ImageView marca = new ImageView(this);
            marca.setImageDrawable(Desenhos.marca());
            FrameLayout.LayoutParams lm = new FrameLayout.LayoutParams(Ui.px(40), Ui.px(40));
            lm.gravity = Gravity.TOP | Gravity.END;
            lm.topMargin = Ui.px(10);
            lm.rightMargin = Ui.px(10);
            mini.addView(marca, lm);
            mini.setTag(papeis.todos.get(i));
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(Ui.px(246), Ui.px(138));
            lp.rightMargin = Ui.px(26);
            linha.addView(mini, lp);
            atual.add(mini);
        }
        ajustes.addView(grade, Ui.em(-2, -2, 120, 400));

        TextView tv = Ui.texto(this, 30, 500, Ui.BRANCO);
        tv.setText("Configurações da TV");
        tv.setPadding(Ui.px(30), Ui.px(12), Ui.px(30), Ui.px(14));
        tv.setTag("tv");
        TextView mem = Ui.texto(this, 30, 500, Ui.BRANCO);
        mem.setText("Liberar memória");
        mem.setPadding(Ui.px(30), Ui.px(12), Ui.px(30), Ui.px(14));
        mem.setTag("memoria");
        LinearLayout botoes = new LinearLayout(this);
        LinearLayout.LayoutParams lb1 = new LinearLayout.LayoutParams(-2, -2);
        lb1.rightMargin = Ui.px(16);
        botoes.addView(mem, lb1);
        botoes.addView(tv, new LinearLayout.LayoutParams(-2, -2));
        ajustes.addView(botoes, Ui.em(-2, -2, 110, 760));
        memAviso = Ui.texto(this, 28, 400, Ui.BRANCO_72);
        memAviso.setText(Memoria.livreMb(this) + " MB livres");
        ajustes.addView(memAviso, Ui.em(-2, -2, 122, 840));
        List<View> ultima = new ArrayList<>();
        ultima.add(mem);
        ultima.add(tv);
        ajLinhas.add(ultima);

        raiz.addView(ajustes, new FrameLayout.LayoutParams(-1, -1));
        ajLinha = 0;
        ajColuna = 0;
        zona = Zona.AJUSTES;
        desenharAjustes();
    }

    private TextView memAviso;

    private void liberarMemoria() {
        memAviso.setText("Liberando…");
        new Thread(() -> {
            long[] r = Memoria.liberar(this);
            long ganho = Math.max(0, r[1] - r[0]);
            h.post(() -> {
                if (memAviso != null) memAviso.setText("Pronto: +" + ganho + " MB  ·  " + r[1] + " MB livres");
            });
        }).start();
    }

    private void teclaAjustes(int dx, int dy, boolean ok, boolean voltar) {
        if (voltar) {
            raiz.removeView(ajustes);
            ajustes = null;
            zona = Zona.ABAS;
            desenharFoco();
            return;
        }
        if (dy != 0) {
            ajLinha = Math.max(0, Math.min(ajLinhas.size() - 1, ajLinha + dy));
            ajColuna = Math.min(ajColuna, ajLinhas.get(ajLinha).size() - 1);
        }
        if (dx != 0) ajColuna = Math.max(0, Math.min(ajLinhas.get(ajLinha).size() - 1, ajColuna + dx));
        if (ok) {
            Object tag = ajLinhas.get(ajLinha).get(ajColuna).getTag();
            if (tag instanceof Integer) {
                papeis.definirMinutos((Integer) tag);
                agendarTroca();
            } else if ("memoria".equals(tag)) {
                liberarMemoria();
            } else if ("tv".equals(tag)) {
                try {
                    startActivity(new Intent(Settings.ACTION_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                } catch (Exception ignored) {}
            } else if (tag instanceof String) {
                papeis.alternarFavorito((String) tag);
            }
        }
        desenharAjustes();
    }

    private void desenharAjustes() {
        Set<String> fav = papeis.favoritos();
        int minutos = papeis.minutos();
        for (int l = 0; l < ajLinhas.size(); l++) {
            List<View> linha = ajLinhas.get(l);
            for (int c = 0; c < linha.size(); c++) {
                View v = linha.get(c);
                boolean f = l == ajLinha && c == ajColuna;
                Object tag = v.getTag();
                if (v instanceof TextView) {
                    boolean ativo = tag instanceof Integer && (Integer) tag == minutos;
                    GradientDrawable p = new GradientDrawable();
                    p.setCornerRadius(Ui.px(40));
                    p.setColor(f ? Ui.BRANCO : (ativo ? 0x33FFFFFF : 0x14FFFFFF));
                    if (ativo && !f) p.setStroke(Ui.px(2), Ui.BRANCO);
                    v.setBackground(p);
                    ((TextView) v).setTextColor(f ? 0xFF000000 : Ui.BRANCO);
                } else {
                    boolean sim = fav.contains((String) tag);
                    FrameLayout mini = (FrameLayout) v;
                    mini.getChildAt(0).setAlpha(sim ? 1f : 0.35f);
                    mini.getChildAt(1).setVisibility(sim ? View.VISIBLE : View.INVISIBLE);
                    GradientDrawable borda = new GradientDrawable();
                    borda.setCornerRadius(Ui.px(18));
                    borda.setColor(0x00000000);
                    if (f) borda.setStroke(Ui.px(5), Ui.BRANCO);
                    mini.setForeground(borda);
                    float s = f ? 1.1f : 1f;
                    mini.animate().scaleX(s).scaleY(s).setDuration(200).start();
                }
            }
        }
    }

    // ---------------- relógio, clima e papel de parede ----------------

    private final Runnable relogio = new Runnable() {
        @Override
        public void run() {
            Date agora = new Date();
            hora.setText(new SimpleDateFormat("HH:mm", BR).format(agora));
            String d = new SimpleDateFormat("EEEE, d 'de' MMMM", BR).format(agora);
            data.setText(Character.toUpperCase(d.charAt(0)) + d.substring(1));
            h.postDelayed(this, 1000 - (System.currentTimeMillis() % 1000) + 5);
        }
    };

    private final Runnable trocar = new Runnable() {
        @Override
        public void run() {
            List<String> r = papeis.rodizio();
            if (r.size() > 1) {
                rodizio = (rodizio + 1) % r.size();
                papeis.guardarUltimo(rodizio);
                fundo.mostrar(papeis.carregar(r.get(rodizio), 1), false);
            }
            agendarTroca();
        }
    };

    private void agendarTroca() {
        h.removeCallbacks(trocar);
        h.postDelayed(trocar, papeis.minutos() * 60000L);
    }

    private void mostrarClima() {
        if (!clima.temDados()) { temp.setText("--°"); return; }
        temp.setText(clima.temperatura + "°");
        iconeClima.definir(clima.codigo, clima.dia);
        dias.removeAllViews();
        SimpleDateFormat entrada = new SimpleDateFormat("yyyy-MM-dd", BR);
        SimpleDateFormat semana = new SimpleDateFormat("EEE", BR);
        for (Clima.Dia d : clima.proximos) {
            if (d == null) continue;
            LinearLayout col = new LinearLayout(this);
            col.setOrientation(LinearLayout.VERTICAL);
            col.setGravity(Gravity.END);
            TextView nome = Ui.texto(this, 24, 400, Ui.BRANCO_72);
            try {
                nome.setText(semana.format(entrada.parse(d.data)).replace(".", ""));
            } catch (Exception e) {
                nome.setText("");
            }
            TextView t = Ui.texto(this, 28, 400, Ui.BRANCO);
            t.setText(d.max + "°  " + d.min + "°");
            col.addView(nome);
            col.addView(t);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-2, -2);
            lp.leftMargin = Ui.px(34);
            dias.addView(col, lp);
        }
    }

    private void atualizarClima() {
        if (System.currentTimeMillis() - climaEm < 30 * 60000L) return;
        new Thread(() -> {
            if (clima.atualizar()) {
                climaEm = System.currentTimeMillis();
                h.post(this::mostrarClima);
            }
        }).start();
    }

    private final Runnable cicloClima = new Runnable() {
        @Override
        public void run() {
            atualizarClima();
            h.postDelayed(this, 5 * 60000L);
        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        h.post(relogio);
        h.post(cicloClima);
        agendarTroca();
        fundo.retomar();
        atualizarJogos();
        if (aba == 1) mostrarJogo();
    }

    @Override
    protected void onStop() {
        super.onStop();
        h.removeCallbacksAndMessages(null);
        fundo.pausar();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        // botão Home: volta sempre para o Início
        if (todos != null && todos.getVisibility() == View.VISIBLE) fecharTodos();
        if (ajustes != null) { raiz.removeView(ajustes); ajustes = null; }
        selecionarAba(0);
        zona = Zona.APPS;
        desenharFoco();
    }

    @Override
    public void onBackPressed() {
        // o Início nunca fecha
    }
}
