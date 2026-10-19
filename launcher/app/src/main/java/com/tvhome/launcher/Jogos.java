package com.tvhome.launcher;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

import java.io.File;

/**
 * Biblioteca de jogos. Os arquivos ficam na pasta de mídia do RetroArch (a versão da Play Store só lê ali):
 * Android/media/com.retroarch/Jogos/SNES e .../Jogos/Fliperama (junto com o neogeo.zip).
 * No fliperama o nome do .zip tem que ser o nome oficial do jogo (kof97.zip, mslugx.zip...), senão o FinalBurn Neo não reconhece.
 */
final class Jogos {
    static final String PASTA = "/storage/emulated/0/Android/media/com.retroarch/Jogos";
    static final String RETROARCH = "com.retroarch";
    private static final String ATIVIDADE = "com.retroarch.browser.retroactivity.RetroActivityFuture";
    private static final String CONFIG = "/storage/emulated/0/Android/data/com.retroarch/files/retroarch.cfg";

    enum Sistema {
        SNES("Super Nintendo", "SNES", "libsnes9x.so", new String[]{".sfc", ".smc", ".zip"}),
        FLIPERAMA("Fliperama", "Fliperama", "libfbneo.so", new String[]{".zip"}),
        PS1("PlayStation", "PS1", "libpcsx_rearmed.so", new String[]{".chd", ".pbp", ".cue", ".ccd", ".m3u", ".iso", ".bin", ".img"});

        final String nome, pasta, nucleo;
        final String[] extensoes;

        Sistema(String nome, String pasta, String nucleo, String[] extensoes) {
            this.nome = nome;
            this.pasta = pasta;
            this.nucleo = nucleo;
            this.extensoes = extensoes;
        }
    }

    static final class Jogo {
        final Sistema sistema;
        final String arquivo, nome, capa, jogadores;
        final int ano;

        Jogo(Sistema s, String arquivo, String nome, int ano, String jogadores, String capa) {
            sistema = s;
            this.arquivo = arquivo;
            this.nome = nome;
            this.ano = ano;
            this.jogadores = jogadores;
            this.capa = capa;
        }

        File rom() {
            for (String ext : sistema.extensoes) {
                File f = new File(PASTA + "/" + sistema.pasta + "/" + arquivo + ext);
                if (f.isFile()) return f;
            }
            return null;
        }
    }

    static final Jogo[] TODOS = {
            new Jogo(Sistema.SNES, "smw", "Super Mario World", 1991, "1–2 jogadores", "smw"),
            new Jogo(Sistema.SNES, "dkc", "Donkey Kong Country", 1994, "1–2 jogadores", "dkc"),
            new Jogo(Sistema.SNES, "topgear", "Top Gear", 1992, "1–2 juntos", "topgear"),
            new Jogo(Sistema.SNES, "topgear3000", "Top Gear 3000", 1995, "1–2 juntos", "topgear3000"),
            new Jogo(Sistema.SNES, "contra3", "Contra III", 1992, "1–2 juntos", "contra3"),
            new Jogo(Sistema.SNES, "sf2turbo", "Street Fighter II Turbo", 1993, "1–2 juntos", "sf2turbo"),
            new Jogo(Sistema.SNES, "umk3", "Ultimate Mortal Kombat 3", 1996, "1–2 juntos", "umk3"),
            new Jogo(Sistema.SNES, "chrono", "Chrono Trigger", 1995, "1 jogador", "chrono"),
            new Jogo(Sistema.SNES, "bomberman", "Super Bomberman", 1993, "1–2 juntos", "bomberman"),
            new Jogo(Sistema.FLIPERAMA, "kof97", "The King of Fighters '97", 1997, "1–2 jogadores", "kof97"),
            new Jogo(Sistema.FLIPERAMA, "kof98", "The King of Fighters '98", 1998, "1–2 jogadores", "kof98"),
            new Jogo(Sistema.FLIPERAMA, "kof2002", "The King of Fighters 2002", 2002, "1–2 jogadores", "kof02"),
            new Jogo(Sistema.FLIPERAMA, "mslug", "Metal Slug", 1996, "1–2 juntos", "metalslugx"),
            new Jogo(Sistema.FLIPERAMA, "mslug3", "Metal Slug 3", 2000, "1–2 juntos", "metalslug3"),
            new Jogo(Sistema.FLIPERAMA, "dino", "Cadillacs and Dinosaurs", 1993, "1–3 juntos", "cadillacs"),
            new Jogo(Sistema.PS1, "yugioh", "Yu-Gi-Oh! Forbidden Memories", 1999, "1 jogador", "yugioh"),
            new Jogo(Sistema.PS1, "thps3", "Tony Hawk's Pro Skater 3", 2001, "1–2 jogadores", "thps3"),
            new Jogo(Sistema.PS1, "thps2", "Tony Hawk's Pro Skater 2", 2000, "1–2 jogadores", "thps2"),
            new Jogo(Sistema.PS1, "sfa3", "Street Fighter Alpha 3", 1999, "1–2 jogadores", "sfa3"),
    };

    /** Abre o jogo direto no RetroArch, sem passar pelo menu dele. */
    static boolean jogar(Context c, Jogo j) {
        File rom = j.rom();
        if (rom == null) return false;
        android.content.pm.ApplicationInfo ra;
        try {
            ra = c.getPackageManager().getApplicationInfo(RETROARCH, 0);
        } catch (Exception e) {
            return false;
        }
        Intent i = new Intent(Intent.ACTION_MAIN);
        i.setComponent(new ComponentName(RETROARCH, ATIVIDADE));
        i.putExtra("ROM", rom.getAbsolutePath());
        // os núcleos vêm dentro do próprio app da Play Store (pasta de bibliotecas nativas)
        i.putExtra("LIBRETRO", ra.nativeLibraryDir + "/" + j.sistema.nucleo);
        i.putExtra("CONFIGFILE", CONFIG);
        i.putExtra("IME", "com.android.inputmethod.latin/.LatinIME");
        i.putExtra("DATADIR", ra.dataDir);
        i.putExtra("APK", ra.sourceDir);
        i.putExtra("SDCARD", "/storage/emulated/0");
        i.putExtra("EXTERNAL", "/storage/emulated/0/Android/data/com.retroarch/files");
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        try {
            c.startActivity(i);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private Jogos() {}
}
