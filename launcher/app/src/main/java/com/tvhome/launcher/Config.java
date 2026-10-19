package com.tvhome.launcher;

/** Tudo que muda de casa para casa fica aqui. Edite e recompile. */
final class Config {

    /** Cidade do clima (Open-Meteo, sem chave). */
    static final String CIDADE = "São Paulo";
    static final double LATITUDE = -23.55;
    static final double LONGITUDE = -46.63;

    /** Apps fixos da barra do Início: {pacote, nome}. Só aparecem os que estiverem instalados. */
    static final String[][] APPS = {
            {"com.google.android.youtube.tv", "YouTube"},
            {"com.netflix.ninja", "Netflix"},
            {"com.disney.disneyplus", "Disney+"},
            {"com.amazon.amazonvideo.livingroom", "Prime Video"},
    };

    private Config() {}
}
