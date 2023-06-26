package no.ks.fiks.streaming.klient;


import java.io.InputStream;

public record FilForOpplasting<T>(
        String filnavn,
        T metadata,
        InputStream data
) {
}
