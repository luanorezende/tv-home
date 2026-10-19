package com.tvhome.launcher;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

import java.io.File;
import java.util.List;

/**
 * Libera memória fechando os apps que ficaram parados em segundo plano.
 * O RetroArch nunca é fechado aqui: um jogo pausado nele perderia o ponto onde parou.
 * (Cache de outros apps só o sistema apaga; o app limpa o próprio cache temporário.)
 */
final class Memoria {

    static long livreMb(Context c) {
        ActivityManager am = (ActivityManager) c.getSystemService(Context.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
        am.getMemoryInfo(mi);
        return mi.availMem / (1024 * 1024);
    }

    /** Roda fora da thread principal. Devolve {livre antes, livre depois} em MB. */
    static long[] liberar(Context c) {
        long antes = livreMb(c);
        ActivityManager am = (ActivityManager) c.getSystemService(Context.ACTIVITY_SERVICE);
        PackageManager pm = c.getPackageManager();
        List<ApplicationInfo> apps = pm.getInstalledApplications(0);
        for (ApplicationInfo ai : apps) {
            String p = ai.packageName;
            if (p.equals(c.getPackageName()) || p.equals(Jogos.RETROARCH)) continue;
            try {
                am.killBackgroundProcesses(p);
            } catch (Exception ignored) {}
        }
        apagar(new File(c.getCacheDir(), "tmp"));
        System.gc();
        try {
            Thread.sleep(1500); // o sistema leva um instante para devolver a memória
        } catch (InterruptedException ignored) {}
        return new long[]{antes, livreMb(c)};
    }

    private static void apagar(File f) {
        File[] filhos = f.listFiles();
        if (filhos != null) for (File x : filhos) apagar(x);
        f.delete();
    }

    private Memoria() {}
}
