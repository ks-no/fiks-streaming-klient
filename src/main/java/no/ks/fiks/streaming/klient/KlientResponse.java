package no.ks.fiks.streaming.klient;

import lombok.Builder;
import lombok.Value;

import java.util.Map;
import java.util.Optional;

@Value
@Builder
public class KlientResponse<T> {
  private T result;
  private int httpStatus;
  private Map<String, String> httpHeaders;

  public Optional<String> getHeader(String header) {
    return Optional.ofNullable(httpHeaders.get(header));
  }
}
