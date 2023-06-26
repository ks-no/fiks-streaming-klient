package no.ks.fiks.streaming.klient;

import java.util.Map;
import java.util.Optional;

public record KlientResponse<T>(
        T result,
        int httpStatus,
        Map<String, String> httpHeaders
) {

    public Optional<String> getHeader(String header) {
        return Optional.ofNullable(httpHeaders.get(header));
    }
}
